from hashlib import new
from anytree import Node, RenderTree, PreOrderIter, LevelOrderGroupIter
from anytree.search import findall_by_attr
import xml.etree.ElementTree as ET
import os, math
#from xml_to_dict import XMLtoDict
import xmltodict
from anytree.importer import DictImporter
import json
import numpy
#from anytree import Node, RenderTree

def node_eq(n1,n2):
    """
    Compares Node
    """
    if "bounds" in n1.attrib.keys() and "bounds" in n1.attrib.keys():
        #if both are nodes
        if n1.attrib["bounds"] == n2.attrib["bounds"]:
            return True
        else:
            return False
    elif "bounds" in n1.attrib.keys() or "bounds" in n1.attrib.keys():
        #if either one is node
        return False
    else:
        #if both are not nodes
        if n1.tag==n2.tag:
            return True
        else:
            return False

def xml_to_tree(xmlFile):
    tree = ET.parse(xmlFile)
    return preprocess_attrib(tree)

def print_(tree):
    for i,j in enumerate(tree.iter()):
        print(i,j.tag)
        z=j.attrib
        if "bounds" in z.keys():
            print(z['bounds'])

def preprocess_attrib(tree):
    for i in tree.iter():
        if "bounds" in i.attrib.keys():
            bounds=i.attrib["bounds"]
            bounds=i.attrib["bounds"][1:-1].split('][')  #['2,3', '4,5']
            l=[strs.split(',') for strs in bounds] #[['2', '3'], ['4', '5']]
            bounds=[l[0][0],l[0][1],l[1][0],l[1][1] ]
            xStart = float(bounds[0])
            yStart = float(bounds[1])
            xEnd = float(bounds[2])
            yEnd = float(bounds[3])
            width = xEnd - xStart
            height = yEnd - yStart
            centerX = (xStart + xEnd)/2
            centerY = (yStart + yEnd)/2
            i.attrib["xStart"]=str(xStart)
            i.attrib["yStart"]=str(yStart)
            i.attrib["xEnd"]=str(xEnd)
            i.attrib["yEnd"]=str(yEnd)
            i.attrib["width"]=str(width)
            i.attrib["height"]=str(height)
            i.attrib["centerX"]=str(centerX)
            i.attrib["centerY"]=str(centerY)
            i.attrib["bounds"]=bounds
            i.attrib["resource-id"]=""
    return tree           

def get_leafs(root, list_=[]):
    for child in list(root):
        if list(child)==[]:
            list_.append(child)
        else:
            get_leafs(child,list_)
    return list_

def new_level_order(root):
    list_=[]
    list_.append([root])
    level=0
    while(len(list_)>level):  
        for i in list_[level]:
            if list(i)!=[]:
                if len(list_)!=level-1:
                    list_.append([])
                list_[level+1].extend(list(i))
        level+=1
    return list_
    
def computeIOU(firstNode,secondNode):
    '''
    Takes two nodes and finds iou between them
    copied from newighborhood.m
    '''
    #takes a component and its bounds, finds the iou
    #import pdb; pdb.set_trace()
    first_1=[float(firstNode.attrib["xStart"]),float(firstNode.attrib["yStart"])]
    first_2=[float(firstNode.attrib["xEnd"]),float(firstNode.attrib["yStart"])]
    first_3=[float(firstNode.attrib["xStart"]),float(firstNode.attrib["yEnd"])]
    first_4=[float(firstNode.attrib["xEnd"]),float(firstNode.attrib["yEnd"])]
    first_bb_points=[first_1,first_2,first_3,first_4]
    second_1=[float(secondNode.attrib["xStart"]),float(secondNode.attrib["yStart"])]
    second_2=[float(secondNode.attrib["xEnd"]),float(secondNode.attrib["yStart"])]
    second_3=[float(secondNode.attrib["xStart"]),float(secondNode.attrib["yEnd"])]
    second_4=[float(secondNode.attrib["xEnd"]),float(secondNode.attrib["yEnd"])]
    second_bb_points=[second_1,second_2,second_3,second_4]
    """
    left = max(firstNode.attrib["xStart"], secondNode.attrib["xStart"])
    right = min(firstNode.attrib["xEnd"], secondNode.attrib["xEnd"])
    top = max(firstNode.attrib["yStart"], secondNode.attrib["yStart"])
    bottom = min(firstNode.attrib["yEnd"], secondNode.attrib["yEnd"])
    width = max(0,right - left)
    height = max(0, abs(bottom - top))
    iou = width * height
    """
    
    intersection = numpy.logical_and(first_bb_points, second_bb_points)
    union = numpy.logical_or(first_bb_points, second_bb_points)
    iou_score = numpy.sum(intersection) / numpy.sum(union)
    #print("IoU:",iou_score)
    return iou_score

def getIOUScore(dataset_leaves, target_leaves):
    score = 0
    #NOT sure if this trick is correct or not
    if len(dataset_leaves)==1:
        if "centerX" not in dataset_leaves[0].attrb.keys():
            return 0
    for j in range(len(target_leaves)):
        if len(dataset_leaves)==0:
            return score

        
        #We set a minimum distance with the current target leaf and first dataset leaf
        curMin = math.dist((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(dataset_leaves[0].attrib["centerX"]),float(dataset_leaves[0].attrib["centerY"])))#(distMat, "euclidean")#caluclate distance 
        ndx=0
        for k in range(1,len(dataset_leaves)):
            #we find the node with minimum distance
            if "centerX" in dataset_leaves[k].attrib.keys():
                curDist = math.dist((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(dataset_leaves[k].attrib["centerX"]),float(dataset_leaves[k].attrib["centerY"])))
                if curDist < curMin:
                    curMin = curDist
                    ndx=k
        #for the current leaf, we find the iou score with nearest matching leaf
        score = score + computeIOU(target_leaves[j], dataset_leaves[ndx])
        #remove the leaf already considered
        dataset_leaves.pop(ndx)
    return score

def get_parent(tree,child):
    parent_map = [(p,c) for p in tree.iter() for c in p]
    #print(parent_map)
    for k,v in parent_map:
        if node_eq(v,child):
            return k,v
    return None,None

def compareComponentByComponent2(tree,dataset_leaves, target_leaves):
    '''
    Input are the leaf nodes of target tree
    copied from neighborhood.m
    
    Check what happens when disappearing list is completely empty
    '''
    score = 0
    curExample = [elem for elem in dataset_leaves]
    disappearingList = [elem for elem in dataset_leaves]
    common_nodes,hierarchy_nodes=[],[]

    for j in range(len(target_leaves)):
        #if all the nodes(of particular level) of the received tree is dealt with, stop the execution.
        if len(disappearingList)==0:
            return common_nodes, hierarchy_nodes
        curMin = math.dist((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(disappearingList[0].attrib["centerX"]),float(disappearingList[0].attrib["centerY"])))#(distMat, "euclidean")#caluclate distance 
        ndx=0
        for k in range(1,len(disappearingList)):
            curDist = math.dist((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(disappearingList[k].attrib["centerX"]),float(disappearingList[k].attrib["centerY"])))
            if curDist < curMin:
                curMin = curDist
                ndx=k
            
        #make these two components partners find the index of leaf nodes that are equal
        loc=[i for i in range(len(curExample)) if node_eq(curExample[i],disappearingList[ndx])]
        #some Trees have two copies of the same component, just take one of them
        loc=loc[0]
       
        score = score + computeIOU(target_leaves[j], curExample[loc])
        #remove the one we matched with
        common_nodes.append(target_leaves[j])
        (parent,child) = get_parent(tree,disappearingList[ndx])
        #ideally no need to pass child here
        hierarchy_nodes.append((parent,child))
        disappearingList.pop(ndx)
    return common_nodes, hierarchy_nodes

def remove_nodes_dealt(current_nodes,common_nodes):
    for i in common_nodes:
        if i in current_nodes:
            current_nodes.remove(i)
    return current_nodes

def copy_hierarchy(new_tree,common_nodes,hierarchy_nodes):
    if new_tree.getroot()==None:
        #tree.leafNodes=new_nodes
        root = ET.Element('hierarchy')
        root.attrib.update({"rotation":'0'})
        new_tree._setroot(root)

    root=new_tree.getroot()
    for i,target_node in enumerate(common_nodes):
        if hierarchy_nodes[i]==None:
            list(root)[-1].append(target_node)
        elif hierarchy_nodes[i].tag!="hierarchy":
            #attaching current node as child to parent node
            #if the hierarchy node exists,
            if hierarchy_nodes[i] in list(root):
                hierarchy_nodes[i].append(target_node)
            else:
                parent_temp=hierarchy_nodes[i].attrib
                hierarchy_nodes[i].clear() #removes attribs too
                hierarchy_nodes[i].attrib = parent_temp
                root.append(hierarchy_nodes[i])
                list(root)[-1].append(target_node)     
        else:
            list(root)[-1].append(target_node)
    return new_tree

def save_tree(new_tree):
    for i in tree.iter():
        if "class" in i.attrib.keys():

            i.attrib["package"] = "com.pandora.android" 
            z=i.attrib
            ord_list = 	["bounds","checkable","checked", "class","clickable", "content-desc", "enabled", "focusable", "focused", "index","long-clickable", "package","password", "resource-id","scrollable","selected","text"]

            res = dict()
            for key in ord_list:
                res[key] = z[key]
            i.attrib = res            
            l=i.attrib["bounds"]

            if len(l)==4:
                s=""
                j=0
                while(j<len(l)):
                    s=s+'['+l[j+1] + ','+ l[j]+']'
                    j+=2
                i.attrib["bounds"]=s
    f = open('employees.xml', "wb")
    new_tree.write(f)
    print("file saved!")

def correct_tree_levels(new_tree):
    ####Make as second ss with sir
    p=[]
    root=new_tree.getroot()
    for nodes in list(root):
        if nodes in p:
            #check if they have same child,
            #if they don't, merge them
            if list(nodes)==list(p[p.index(nodes)]):
                root.remove(nodes)
            else:
                p[p.index(nodes)].extend(list[nodes])
                root.remove(nodes)
        else:
            p.append(nodes)
    return new_tree
    '''
   
    #for leaf level, check if the same child is used before?
    save_tree(new_tree)
    leaves=get_leafs(new_tree.getroot(), list_=[])
    print(len(leaves))
    true_nodes=list(set(leaves))
    print(len(list(set(leaves))),"||", len(leaves))
    #print(true_nodes,leaves)
    for i in true_nodes:
        if i in leaves:
            #get all nodes with 
            nodes=[leaves[index] for index, value in enumerate(leaves) if value == i]
            #Find the parent of all these nodes, check if they are same, we take one
            p=[]
            for j in nodes:
                parent,child = get_parent(new_tree,j)
                p.append(parent)
            #check if they are same, we take one
            if len(set(p))==1:
                print("len of p", len(p))
                #nodes[0].clear()
                #p[0].clear()
                if p[0] in list(new_tree.getroot()):
                    new_tree.getroot().remove(p[0])
                    p[0].clear()
                    nodes[0].clear()
                #print(len(leaves))
                #remove the nodes
            #break
    save_tree(new_tree)

    #for second level, check if the parents are same
    

    #save_tree(new_tree)
    print("Corrected tree")
    return new_tree
    '''

def preprocess_hierarchy_nodes(hierarchy_nodes):
    parent_list=[]
    for p,c in hierarchy_nodes:
        parent_list.append(p)
    return parent_list

def main(target_xml,xml_folder):
    '''Feeds xml files in a memory efficient way'''
    target_tree = xml_to_tree(target_xml)
    fileList = os.listdir(xml_folder)
    flag=False
    fileList=[xml_folder+i for i in fileList if i[-4:]==".xml"]
    new_tree=ET.ElementTree()
    current_level=0
    #Compare nodes of target tree with the other files, in a level order
    current_nodes = get_leafs(target_tree.getroot(),[])#leafNodes
    counter=0
    while(flag!=True):
        #print(counter, len(current_nodes))
        counter+=1
        iou_scores = {file: 0 for file in fileList}
        for files in fileList:
            '''compare this tree objects with target tree
            save the iou scores and finally get best tree'''
            #make tree for each file
            tree = xml_to_tree(files)
            
            if current_level!=0:
                levels=new_level_order(tree.getroot())#//changed here
                #levels = level_order([[new_tree.getroot()]],roots=None,root=new_tree.getroot())
                #get all nodes levels of the tree
                dataset_leafs = levels[current_level]
            else:
                #get all nodes levels of the tree
                dataset_leafs=get_leafs(tree.getroot(),[])

            #comparing level nodes of target tree with the new tree
            '''Note :: With common nodes, pass the dataset tree nodes that are similar'''
            score = getIOUScore(dataset_leafs ,current_nodes)
            #ADD A THRESHOLD
            iou_scores[files] = score
            #when you reach root node of files

        print("Completed looking at files:",counter,"times")
        #Issue here---> iou scores are same for many files at a time, iou scores are 0 for all files too.
        best_tree_file = max(iou_scores, key=iou_scores.get)
        #get the common nodes with this tree
        tree = xml_to_tree(best_tree_file)
        #get all levels of the tree

        #comparing level nodes of target tree with the new tree
        #update the target tree with the new trees objects

        if current_level!=0:
            levels = new_level_order(tree.getroot())
            dataset_leafs = levels[current_level]
        else:
            dataset_leafs=get_leafs(tree.getroot(),[])
        common_nodes,hierarchy_nodes = compareComponentByComponent2(tree,dataset_leafs, current_nodes)
        
       
        #break
        #print("common nodes",len(common_nodes))
        #print("hierarchy_nodes",len(hierarchy_nodes))
        hierarchy_nodes=preprocess_hierarchy_nodes(hierarchy_nodes)
        
        #print(hierarchy_nodes)
        #no need of returning current_nodes here.
        #print("current_nodes b4",len(current_nodes))
        #print("current_nodes b4 set",len(set(current_nodes)))
        current_nodes = remove_nodes_dealt(current_nodes,common_nodes)
        #print("current_nodes",len(current_nodes))
        #print("current_nodes after",len(current_nodes))
        #print("current_nodes after set",len(set(current_nodes)))
        #print("common_nodes:",common_nodes)
        '''Note :: With common nodes, attach the node and its child to
        the root. add the attributes of our node to this new node.
        '''
        #print("intersection between common_nodes and current_nodes",set(current_nodes).intersection(set(common_nodes)))
        #introduces more leaf nodes
        
        new_tree = copy_hierarchy(new_tree,common_nodes,hierarchy_nodes)

        #print("leaves after",len(get_leafs(new_tree.getroot(),[])))
        #save_tree(new_tree)
        if current_nodes==[]:
            print("Completed level:",current_level)
            current_level+=1 
            #levels = level_order([[new_tree.getroot()]],roots=None,root=new_tree.getroot())
            #if there are no more levels present:
            #if len(levels)==current_level:
            #    return new_tree
            new_tree = copy_hierarchy(new_tree,common_nodes,hierarchy_nodes)
            current_nodes=list(new_tree.getroot()) 
            #levels[current_level]
            #new_tree = correct_tree_levels(new_tree)
            #save_tree(new_tree)
            #break
            
            #print("current_level",current_level)
        if current_level==1:
            flag=True
    return new_tree

def final_preprocess(tree):
    for i in tree.iter():
        for k,v in i.attrib.items():
            if type(v)=="float":
                i.set(k, str(v))

xml_file_name="../1565.jpg.xml"
xml_folder="../XmlFiles2/"

tree=main(xml_file_name,xml_folder)
#print_(tree)
#final_preprocess(tree)
#levels = level_order([[tree.getroot()]],roots=None,root=tree.getroot())
#print([len(i) for i in levels])
#print(ET.tostring(tree.getroot(), encoding='utf8').decode('utf8'))
#print(ET.dump(tree.getroot()))
save_tree(tree)
print("Out of xml_dict")
