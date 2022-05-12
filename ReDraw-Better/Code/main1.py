import xml.etree.ElementTree as ET
import os
from scipy.spatial import distance
import numpy

def node_eq(n1,n2):
    """Compares Node n1 and n2.

    Args:
        n1,n2 (Element Tree Element Object): 

    Returns:
        Boolean: True, if the have the same bounds

    """
    
    if "bounds" in n1.attrib.keys() and "bounds" in n2.attrib.keys():
        #if both are nodes
        if n1.attrib["bounds"] == n2.attrib["bounds"]:
            return True
        else:
            return False
    elif "bounds" in n1.attrib.keys() or "bounds" in n2.attrib.keys():
        #if either one is node
        return False
    else:
        #if both are not nodes
        if n1.tag==n2.tag:
            return True
        else:
            return False

def xml_to_tree(xmlFile):
    """Converts Xml file to Element Tree object

    Args:
        xmlFile (string): File path of xml File

    Returns:
        Element Tree object
    """
    tree = ET.parse(xmlFile)
    return preprocess_attrib(tree)

def print_(tree):
    """Prints Tree Nodes 

    Args:
        tree (ELement Tree object)
    """
    for i,j in enumerate(tree.iter()):
        print(i,j.tag)
        z=j.attrib
        if "bounds" in z.keys():
            print(z['bounds'])

def preprocess_attrib(tree):
    """Adds and Preprocesses attributes

    Args:
        tree (Element Tree object): Uses Element tree obtained from xml_to_tree()

    Returns:
        Element Tree object: with attributes
    """
    for i in tree.iter():
        if "bounds" in i.attrib.keys():
            bounds=i.attrib["bounds"]
            bounds=i.attrib["bounds"][1:-1].split('][')  #['2,3', '4,5']
            l=[strs.split(',') for strs in bounds] #[['2', '3'], ['4', '5']]
            bounds=[l[0][0],l[0][1],l[1][0],l[1][1] ] #['2', '3','4', '5']
            xStart = float(bounds[0])#2.0
            yStart = float(bounds[1])#3.0
            xEnd = float(bounds[2])#4.0
            yEnd = float(bounds[3])#5.0
            width = xEnd - xStart #4-2=2.0
            height = yEnd - yStart#5-3 = 2.0
            centerX = (xStart + xEnd)/2 #(4+2)/2 = 3.0
            centerY = (yStart + yEnd)/2 #(5+3)/2 = 4.0
            #Adding all the calculated stuff as attributes
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
    """Provide Leaf elements of the tree passed

    Args:
        root (_type_): root element of tree.
        list_ (list, optional): List to facilitate recursion. Defaults to [].

    Returns:
        liat: List of leaf nodes of the tree
    """
    for child in list(root):
        if list(child)==[]:
            list_.append(child)
        else:
            get_leafs(child,list_)
    return list_

def new_level_order(root):
    """Level order traversal of the tree

    Args:
        root (ET.Element object ): root of tree to traverse

    Returns:
        list: List of List containing nodes of each level at respective indices. 
                Level 0 is root node
    """
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
    """Conputes Intersection over Union of given Nodes

    Args:
        firstNode (ET.Element object)
        secondNode (ET.Element object)

    Returns:
        float: IOU score of the nodes
    """
    #takes a component and its bounds, finds the iou
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
    
    intersection = numpy.logical_and(first_bb_points, second_bb_points)
    union = numpy.logical_or(first_bb_points, second_bb_points)
    iou_score = numpy.sum(intersection) / numpy.sum(union)
    return iou_score

def getIOUScore(dataset_leaves, target_leaves):
    """Gets IOU scores of all the matching target nodes with given dataset nodes

    Args:
        dataset_leaves (list): list of nodes of tree of dataset
        target_leaves (list): list of nodes of tree of target xml file

    Returns:
        float: Returns IOU score of the file(summation of IOUs of individual file)
    """
    score = 0
    #NOT sure if this trick is correct or not
    if len(dataset_leaves)==1:
        #if dataset leaves contains root at 0th index
        if "centerX" not in dataset_leaves[0].attrib.keys():
            return 0
    for j in range(len(target_leaves)):
        if len(dataset_leaves)==0:
            return score
        if "centerX" not in target_leaves[j].attrib.keys() or "centerX" not in dataset_leaves[0].attrib.keys():
            return score
        #We set a minimum distance with the current target leaf and first dataset leaf
        curMin = distance.euclidean((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(dataset_leaves[0].attrib["centerX"]),float(dataset_leaves[0].attrib["centerY"])))#(distMat, "euclidean")#caluclate distance 
        ndx=0
        for k in range(1,len(dataset_leaves)):
            #we find the node with minimum distance
            if "centerX" in dataset_leaves[k].attrib.keys():
                curDist = distance.euclidean((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(dataset_leaves[k].attrib["centerX"]),float(dataset_leaves[k].attrib["centerY"])))
                if curDist < curMin:
                    curMin = curDist
                    ndx=k
        #for the current leaf, we find the iou score with nearest matching leaf
        score = score + computeIOU(target_leaves[j], dataset_leaves[ndx])
        #remove the leaf already considered
        dataset_leaves.pop(ndx)
    return score

def get_parent(tree,child):
    """Given a tree and a child finds its parent

    Args:
        tree (ET.Tree object)
        child (ET.Element object)

    Returns:
        set: Set of parent and child if present. (None,None) if not present
    """

    parent_map = [(p,c) for p in tree.iter() for c in p]
    for k,v in parent_map:
        if node_eq(v,child):
            return k,v
    return None,None

def compareComponentByComponent2(tree,dataset_leaves, target_leaves):
    """compares all the nodes in givrn lists

    Args:
        tree (ET.Tree): _description_
        dataset_leaves (list): list of nodes of tree of dataset
        target_leaves (list): list of nodes of tree of target xml file

    Returns:
        list: common_nodes are the nodes of target leaves that were common with the dataset leaves
                hierarchy_nodes (list of set of parent-child nodes) from the dataaset leaves.
    """
    score = 0
    curExample = [elem for elem in dataset_leaves]
    disappearingList = [elem for elem in dataset_leaves]
    common_nodes,hierarchy_nodes=[],[]

    for j in range(len(target_leaves)):
        #if all the nodes(of particular level) of the received tree is dealt with, stop the execution.
        if len(disappearingList)==0:
            return common_nodes, hierarchy_nodes
        if "centerX" not in target_leaves[j].attrib.keys() or "centerX" not in disappearingList[0].attrib.keys():
            #if any one is a root node
            return common_nodes, hierarchy_nodes
        
        curMin = distance.euclidean((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(disappearingList[0].attrib["centerX"]),float(disappearingList[0].attrib["centerY"])))#(distMat, "euclidean")#caluclate distance 
        ndx=0
        for k in range(1,len(disappearingList)):
            curDist = distance.euclidean((float(target_leaves[j].attrib["centerX"]),float(target_leaves[j].attrib["centerY"])),(float(disappearingList[k].attrib["centerX"]),float(disappearingList[k].attrib["centerY"])))
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
    """Removes nodes that have been matched, and no need to be used further.

    Args:
        current_nodes (list of ET.Element): Nodes that were initially present in the level
        common_nodes (list of ET.Element): Nodes that are matched

    Returns:
        list : current_nodes
    """
    for i in common_nodes:
        if i in current_nodes:
            current_nodes.remove(i)
    return current_nodes

def copy_hierarchy(new_tree,common_nodes,hierarchy_nodes):
    """Copies hierarchy of the dataset tree on 

    Args:
        new_tree (ET.tree): New tree whose hierarchy we will be buiding.
        common_nodes (list of ET.Element): Target tree Nodes that matched
        hierarchy_nodes (list of set of ET.Elements): (parent,child) of the matched child of dataset tree

    Returns:
        ET.tree: new_tree With updated hierarchy
    """
    #if root is not present yet. Set root.
    if new_tree.getroot()==None:
        root = ET.Element('hierarchy')
        root.attrib.update({"rotation":'0'})
        new_tree._setroot(root)
    
    root=new_tree.getroot()
    for i,target_node in enumerate(common_nodes):
        new_parent = ET.Element('node')
        new_parent.attrib.update(hierarchy_nodes[i].attrib) #giving it attributes
        new_parent.append(target_node) #adding child to it
        flag=0
        for node in list(root):
            if node_eq(new_parent,node):
                #if parent already exists, add to it
                node.append(target_node)
                flag=1
                break
        if flag!=1:
            #if parent does not exist, add it
            root.append(new_parent)
    return new_tree

def remove_duplicate_nodes(new_tree):
    """Removes duplicate Nodes present in the tree

    Args:
        new_tree (ET.Tree): Tree to iterate over for duplicate nodes
    """
    root=new_tree.getroot()
    prev = ET.Element("None")
    for page in root:                     # iterate over pages
        elems_to_remove = []
        for elem in page:
            if node_eq(elem, prev):
                print("found duplicate: %s" % elem.text)   # equal function works well
                elems_to_remove.append(elem)
                continue
            prev = elem
        for elem_to_remove in elems_to_remove:
            page.remove(elem_to_remove)

def order_attribs(new_tree):
    """Orders the attributes, to make it same as all nodes

    Args:
        new_tree (ET.Tree): Tree to iterate over for ordering attributes
    """
    for i in new_tree.iter():
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

def save_tree(new_tree,new_file_name):
    """Saves the tree in given xml

    Args:
        new_tree (ET.tree): _description_
        new_file_name (String): Path of XML file to save on.
    """
    order_attribs(new_tree)
    remove_duplicate_nodes(new_tree)
    
    f = open(new_file_name, "wb")
    new_tree.write(f, encoding='utf-8', xml_declaration=True)
    print("file saved!")

def correct_tree_levels(new_tree):
    """Combines bounds of the latest level added.

    Args:
        new_tree (ET.Tree): Tree whose levels are to be corrected

    Returns:
        ET.Tree: Corrected tree
    """
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

def preprocess_hierarchy_nodes(hierarchy_nodes):
    """Returns only the parent to be attached, to copy hierarchy

    Args:
        hierarchy_nodes (list): list of set of parent-child nodes

    Returns:
        list: List of parents only
    """
    parent_list=[]
    for p,c in hierarchy_nodes:
        parent_list.append(p)
    return parent_list

def main(target_xml,xml_folder):
    """Arranges target xml files in a hierarchy using xml files in xml_folder

    Args:
        target_xml (String): Path to xml with child nodes only
        xml_folder (String): Path to folder containing xml files of dataset

    Returns:
        ET.tree: Tree with the hierarchy assigned
    """

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

        counter+=1
        iou_scores = {file: 0 for file in fileList}
        for files in fileList:
            '''compare this tree objects with target tree
            save the iou scores and finally get best tree'''
            #make tree for each file
            tree = xml_to_tree(files)
            
            if current_level!=0:
                levels=new_level_order(tree.getroot())#//changed here
                #get all nodes levels of the tree
                dataset_leafs = levels[current_level]
            else:
                #get all nodes levels of the tree
                dataset_leafs=get_leafs(tree.getroot(),[])

            #comparing level nodes of target tree with the new tree
            #Note :: With common nodes, pass the dataset tree nodes that are similar
            score = getIOUScore(dataset_leafs ,current_nodes)
            #ADD A THRESHOLD
            iou_scores[files] = score
            #when you reach root node of files
        
        print("Completed looking at files:",counter,"times")
        #Issue here---> iou scores are same for many files at a time
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

        hierarchy_nodes=preprocess_hierarchy_nodes(hierarchy_nodes)
        current_nodes = remove_nodes_dealt(current_nodes,common_nodes)
        #Note :: With common nodes, attach the node and its child to
        #the root. add the attributes of our node to this new node.
    
        new_tree = copy_hierarchy(new_tree,common_nodes,hierarchy_nodes)
        remove_duplicate_nodes(new_tree)
        if current_nodes==[]:
            print("Completed level:",current_level)
            current_level+=1 
            levels=new_level_order(tree.getroot())#//changed here
            current_nodes = levels[current_level]
        if current_level==3:
            flag=True
    return new_tree

xml_file_name=".././raw_xml/1565 (2).xml"
xml_folder="../XmlFiles2/"

tree=main(xml_file_name,xml_folder)
new_file_name=xml_file_name.split("/")[-1][:-4]+"_final.xml"
save_tree(tree,new_file_name)
print("Out of xml_dict")
