from re import I
from urllib.parse import ParseResult
import xml.etree.ElementTree as ET
import os
from scipy.spatial import distance
import numpy
import copy
import shutil

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

def order_attribs(tree):
    """Orders the attributes, to make it same as all nodes

    Args:
        new_tree (ET.Tree): Tree to iterate over for ordering attributes
    """
    #root.attrib["bounds"]='[0,0][1200,1920]'
    for i in tree.iter():
        if "rotation" not in i.attrib.keys():
            i.attrib["package"] = "com.android.example" 
            z=i.attrib
            ord_list = ["bounds","checkable","checked", "class","clickable", "content-desc", "enabled", "focusable", "focused", "index","long-clickable", "package","password", "resource-id","scrollable","selected","text"]

            res = dict()
            flag=0
            for key in ord_list:
                if key in z.keys():
                    res[key] = z[key]
                else:
                    flag=1
                    break
            i.attrib = res
            if "bounds" in i.attrib:
                try:
                    l=i.attrib["bounds"]
                    l2=[int(j) for j in l] #condition holds try-except
                    i.attrib["bounds"] = '['+l[0]+','+l[1]+']['+l[2]+','+l[3]+']'
                except:
                    pass

                if flag==1:
                    d={"bounds":i.attrib["bounds"],"checkable":"false", "checked":"false", "class":"android.widget.LinearLayout", "clickable":"false" ,"content-desc":"", "enabled":"true", "focusable":"false", "focused":"false", "index":"0" ,"long-clickable":"false", "package":"com.android.example", "password":"false" ,"resource-id":"" ,"scrollable":"false" ,"selected":"false", "text":""}
                    i.attrib=d
    #setting a root node for the entire screenshot
    root = ET.Element("node")
    bounds = '[0,0][1200,1920]'
    d={"bounds":bounds,"checkable":"false", "checked":"false", "class":"android.widget.LinearLayout", "clickable":"false" ,"content-desc":"", "enabled":"true", "focusable":"false", "focused":"false", "index":"0" ,"long-clickable":"false", "package":"com.android.example", "password":"false" ,"resource-id":"" ,"scrollable":"false" ,"selected":"false", "text":""}
    root.attrib=d
    new_child = tree.getroot()
    for j in new_child:
        root.append(j)
    new_child.clear()
    new_child.attrib["rotation"]='0'
    new_child.append(root)
    return new_child

def save_tree(new_tree,new_file_name):
    """Saves the tree in given xml

    Args:
        new_tree (ET.tree): _description_
        new_file_name (String): Path of XML file to save on.
    """
    #print_(new_tree)
    root = order_attribs(new_tree) 
    #remove_duplicate_nodes2(new_tree)
    #print_(new_tree)
    f = open(new_file_name, "wb")
    f.write(ET.tostring(root, encoding='utf8'))
    #new_tree.write(f, encoding='utf-8', xml_declaration=True)
    print("file saved!")

def preprocess_attrib(tree,target=True):
    """Adds and Preprocesses attributes

    Args:
        tree (Element Tree object): Uses Element tree obtained from xml_to_tree()

    Returns:
        Element Tree object: with attributes
    """
    for i in tree.iter():         
        if "bounds" in i.attrib.keys():
            
            bounds=i.attrib["bounds"][1:-1].split('][')  #['2,3', '4,5']
            if len(bounds) == 1:
                bounds=i.attrib["bounds"][1:-1].split('],[')
            l=[strs.split(',') for strs in bounds] #[['2', '3'], ['4', '5']]
            
            bounds=[l[0][0],l[0][1],l[1][0],l[1][1] ] #['2', '3','4', '5']
            
            xStart = int(bounds[0])#2.0
            yStart = int(bounds[1])#3.0
            #width = int(bounds[2])#4.0
            #height = int(bounds[3])#5.0
            xEnd = int(bounds[2])
            yEnd = int(bounds[3])
            width = xEnd-xStart#4.0
            height = yEnd-yStart#5.0
            #xEnd = width + xStart #4+2=6.0
            #yEnd = height + yStart#5+3 = 8.0
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
            
            if target==False:
                i.attrib["bounds"]=[i.attrib["xStart"],i.attrib["yStart"],i.attrib["xEnd"],i.attrib["yEnd"]]
            else:
                i.attrib["bounds"]=bounds
            i.attrib["resource-id"]=""

    return tree  

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

def get_leafs(root, list_=[]):
    """Provide Leaf elements of the tree passed

    Args:
        root (_type_): root element of tree.
        list_ (list, optional): List to facilitate recursion. Defaults to [].

    Returns:
        list: List of leaf nodes of the tree
    """
    for child in list(root):
        if list(child)==[]:
            list_.append(child)
        else:
            get_leafs(child,list_)
    return list_

def xml_to_tree(xmlFile,target=False):
    """Converts Xml file to Element Tree object

    Args:
        xmlFile (string): File path of xml File

    Returns:
        Element Tree object
    """
    tree = ET.parse(xmlFile)
    return preprocess_attrib(tree,target=target)

def get_iou(a, b, epsilon=1e-5):
    """ Given two boxes `a` and `b` defined as a list of four numbers:
            [x1,y1,x2,y2]
        where:
            x1,y1 represent the upper left corner
            x2,y2 represent the lower right corner
        It returns the Intersect of Union score for these two boxes.

    Args:
        a:          (list of 4 numbers) [x1,y1,x2,y2]
        b:          (list of 4 numbers) [x1,y1,x2,y2]
        epsilon:    (float) Small value to prevent division by zero

    Returns:
        (float) The Intersect of Union score.
    """
    # COORDINATES OF THE INTERSECTION BOX
    x1 = max(a[0], b[0])
    y1 = max(a[1], b[1])
    x2 = min(a[2], b[2])
    y2 = min(a[3], b[3])



    # AREA OF OVERLAP - Area where the boxes intersect
    area_overlap = max((x2 - x1, 0)) * max((y2 - y1), 0)
    # handle case where there is NO overlap
    if area_overlap == 0:
        return 0.0
    
    # COMBINED AREA
    area_a = (a[2] - a[0]) * (a[3] - a[1])
    area_b = (b[2] - b[0]) * (b[3] - b[1])
    area_combined = area_a + area_b - area_overlap
    
    # RATIO OF AREA OF OVERLAP OVER COMBINED AREA
    iou = area_overlap / (area_combined+epsilon)
    return iou
    
def computeIOU(firstNode,secondNode):
    """Conputes Intersection over Union of given Nodes

    Args:
        firstNode (ET.Element object)
        secondNode (ET.Element object)

    Returns:
        float: IOU score of the nodes
    """
    ##Corrected iou score mechanism
    #takes a component and its bounds, finds the iou
    if( "xStart" in firstNode.attrib and "xStart" in secondNode.attrib ): 
        first_1=[float(firstNode.attrib["xStart"]),float(firstNode.attrib["yStart"]),float(firstNode.attrib["xEnd"]),float(firstNode.attrib["yEnd"])]
        second_1=[float(secondNode.attrib["xStart"]),float(secondNode.attrib["yStart"]),float(secondNode.attrib["xEnd"]),float(secondNode.attrib["yEnd"])]
        return get_iou(first_1,second_1)
    else:
        return 0

def getIOUScore(dataset_leaves, target_leaves):
    score=0
    count=0
    for j in range(len(dataset_leaves)):    
            for k in range(len(target_leaves)):
                if( "xStart" in dataset_leaves[j].attrib): 
                    s=computeIOU(target_leaves[k], dataset_leaves[j])
                    score = score + s
                else:
                    count += 1
    #normalized_score = score/(len(dataset_leaves)*len(target_leaves))
    
    if((len(dataset_leaves)-count)!=0):
        normalized_score = score/ (len(dataset_leaves)-count)
    else:
        normalized_score=0
    #print(score,count,normalized_score)
    return normalized_score

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

def compareComponentByComponent2(dataset_leaves, target_leaves):
    """compares all the nodes in givrn lists

    Args:
        tree (ET.Tree): _description_
        dataset_leaves (list): list of nodes of tree of dataset
        target_leaves (list): list of nodes of tree of target xml file

    Returns:
        list: common_nodes are the nodes of target leaves that were common with the dataset leaves
                hierarchy_nodes (list of set of parent-child nodes) from the dataaset leaves.
    """
    
    n=len(dataset_leaves)
    threshold = 0.001
    #list of nodes with iou>=threshold for each dataset leaf
    common_nodes_1= {int(j): [] for j in range(n)}
    #store scores of all dataset leaves
    all_scores = [0]*n 
    for j in range(n):
        #if node has bounds
        if("xStart" in dataset_leaves[j].attrib):
            for k in range(len(target_leaves)):
                score = computeIOU(target_leaves[k], dataset_leaves[j])
                if score>=threshold:
                    common_nodes_1[j].append(target_leaves[k])
                all_scores[j] += score
            
            to_remove_leaves  = common_nodes_1[j]
            for i in to_remove_leaves:
                target_leaves.remove(i)
    #print(common_nodes_1)
    max=0    
    max_parent = None
    for parent,children in common_nodes_1.items():
        for child in children:
            score+= computeIOU(dataset_leaves[parent],child)
        if score>max:
            max=score
            max_parent = parent
    #print("-"*10)
    #print(target_leaves2)
    #print("-"*10)
    children = common_nodes_1[max_parent]
    
    
    return common_nodes_1[max_parent],dataset_leaves[max_parent]
    #Getting all the parents with 1 or more matched nodes
    parents = [dataset_leaves[p] for p,c in common_nodes_1.items() if len(c)!=0]
    child_nodes = [common_nodes_1[p] for p,c in common_nodes_1.items() if len(c)!=0]
    return child_nodes, parents, target_leaves

#TODO: Remmove all_matched_nodes
def correct_parent_bounds(parent,current_nodes,common_nodes):
    #import pdb; pdb.set_trace();
    #for i,parent in enumerate(parent):
    attrib = parent.attrib
    parent.clear()
    children= common_nodes
    parent.attrib = attrib
    parent_bound_changed=0
    #print("Original1",parent.attrib['bounds'])
    for child in children:
        #when child is overlapping with parent
        if computeIOU(child, parent)>0:
            if int(child.attrib["xStart"]) < int(parent.attrib["xStart"]):
                parent.attrib["xStart"] = child.attrib["xStart"]
                parent_bound_changed=1
            if int(child.attrib["yStart"]) < int(parent.attrib["yStart"]):
                parent.attrib["yStart"] = child.attrib["yStart"] 
                parent_bound_changed=1
            if int(child.attrib["xEnd"]) > int(parent.attrib["xEnd"]):
                parent.attrib["xEnd"] = child.attrib["xEnd"]
                parent_bound_changed=1
            if int(child.attrib["yEnd"]) > int(parent.attrib["yEnd"]):
                parent.attrib["yEnd"] = child.attrib["yEnd"]
                parent_bound_changed=1
            parent.append(child)
            if parent_bound_changed==1:
                parent.attrib["bounds"]=[parent.attrib["xStart"],parent.attrib["yStart"],parent.attrib["xEnd"],parent.attrib["yEnd"]]                
            #print(child.attrib['bounds'])

    #print("New Parent",parent.attrib['bounds'])
    #iterating over all nodes that are still in the pool
    #keep it running until the #child of parent does not increase
    
    matched_nodes=[]
    flag=0
    #import pdb; pdb.set_trace();
    #print("Original2:",parent.attrib["bounds"])
    while(flag==0 and parent_bound_changed==1):
        flag=1            
        for leaves in current_nodes: 
            new_parent_bound_changed=0
            if computeIOU(leaves, parent)>0:
                #print(leaves.attrib["bounds"])
                if int(leaves.attrib["xStart"]) < int(parent.attrib["xStart"]):
                    parent.attrib["xStart"] = leaves.attrib["xStart"]
                    new_parent_bound_changed=1
                if int(leaves.attrib["yStart"]) < int(parent.attrib["yStart"]):
                    parent.attrib["yStart"] = leaves.attrib["yStart"] 
                    new_parent_bound_changed=1
                if int(leaves.attrib["xEnd"]) > int(parent.attrib["xEnd"]):
                    parent.attrib["xEnd"] = leaves.attrib["xEnd"]
                    new_parent_bound_changed=1
                if int(leaves.attrib["yEnd"]) > int(parent.attrib["yEnd"]):
                    parent.attrib["yEnd"] = leaves.attrib["yEnd"]
                    new_parent_bound_changed=1
                parent.append(leaves)
                if new_parent_bound_changed==1:
                    parent.attrib["bounds"]=[parent.attrib["xStart"],parent.attrib["yStart"],parent.attrib["xEnd"],parent.attrib["yEnd"]]
                    #print("New",parent.attrib["bounds"])
                common_nodes.append(leaves)
                matched_nodes.append(leaves)
                #print(len(all_matched_nodes))
                flag=0
        if len(matched_nodes)>0:
            for matched_node in matched_nodes:
                current_nodes.remove(matched_node)
            matched_nodes=[]
    
    return common_nodes,current_nodes,parent

#depricated
def check_node_presence(root,node):
    levels = new_level_order(root)
    all_nodes = [j for i in levels for j in i]
    for i in all_nodes:
        equal = node_eq(i,node)
        if equal==True:
            #if node already present, add this node's child to existing one
            for child in node:
                i.append(child)
            return True
    return False

def add_to_hierarchy(target_tree,parent):
    '''
     2. Adds parent to hierarchy
    '''
    root =target_tree.getroot()
    children = list(root)
    



    # all_childs=[]
    # for parent in parents:
    #     print("Parent:",parent.attrib['bounds'])
    #     for child in parent:
    #         print(child.attrib['bounds'])
    #         all_childs.append(child.attrib['bounds'])

    # print(len(all_childs),len(all_matched_nodes))

    # print(all_matched_nodes)
    # print("--")
    # print(children)
    children.append(parent)
    # Emptying the root
    root.clear()
    for child in children:
        root.append(child)
    target_tree._setroot(root)
    #print(list(root))
    return root


def add_to_hierarchy2(target_tree,new_current_nodes):
    '''
     2. Adds parent to hierarchy
    '''
    root =target_tree.getroot()
    # Emptying the root
    root.clear()
    for child in new_current_nodes:
        root.append(child)
    target_tree._setroot(root)
    #print(list(root))
    return root

#depricated
def adjust_current_nodes(current_nodes,common_nodes,parents):    
    for i,parent in enumerate(parents):
        #current_nodes.append(parent)
        for com_node in common_nodes[i]:
            current_nodes.remove(com_node)
    return current_nodes


def main(target_xml,xml_folder):
    """Arranges target xml files in a hierarchy using xml files in xml_folder

    Args:
        target_xml (String): Path to xml with child nodes only
        xml_folder (String): Path to folder containing xml files of dataset

    Returns:
        ET.tree: Tree with the hierarchy assigned
    """
    target_tree = xml_to_tree(target_xml,target=True)
    fileList = os.listdir(xml_folder)
    fileList=[xml_folder+i for i in fileList if i[-4:]==".xml"]
    current_nodes = get_leafs(target_tree.getroot(),[])
    print(len(current_nodes))
    for i in range(3):
        # A new loop which makes sure all of the components are matched at each level
        new_current_nodes=[]
        x_level = -2-i #-2,-3,-4,..
        count_current_nodes=len(current_nodes)
        while count_current_nodes!=0:
            #initializing dictionary to store scores
            iou_scores = {file: 0 for file in fileList}
            
            for files in fileList:
                #making tree off the xml file and getting nodes for a particular level
                #print(files)
                tree = xml_to_tree(files,target=False)
                levels=new_level_order(tree.getroot())
                # remove empty lists of the levels
                new_level=[i for i in levels if len(i)!=0]
                try:
                    dataset_leafs=new_level[x_level] #-2,-3,-4,..
                except:
                    iou_scores[files]=0
                    continue
                
                #getting IOU scores for the file
                score = getIOUScore(dataset_leafs ,current_nodes)
                iou_scores[files]=score            
            
            #Working with the best file
            best_tree_file = max(iou_scores, key=iou_scores.get)
            print(best_tree_file, "score:",iou_scores[best_tree_file])
            tree = xml_to_tree(best_tree_file,target=False)
            levels=new_level_order(tree.getroot())
            new_level=[ i for i in levels if len(i)!=0]
            dataset_leafs=new_level[x_level]
            print("---------------> AT BEST FILE")
            
            #iterating over the components to get a parent
            #common_nodes,parents, current_nodes = compareComponentByComponent2(dataset_leafs, current_nodes)
            #for j in current_nodes:
            #    print(j.attrib["bounds"])
            #print("-==-=-=-=--=-=-=-=-=-=-=-=")
            #print(len(current_nodes))
            my_current_nodes= copy.deepcopy(current_nodes)
            common_nodes,parent = compareComponentByComponent2(dataset_leafs, my_current_nodes)
            #print(len(current_nodes), len(common_nodes))
            #removing newly obtained children from current nodes
            for i in common_nodes:
                for j in current_nodes:
                    if node_eq(i,j):
                       current_nodes.remove(j)
            #print(len(current_nodes))
            common_nodes,current_nodes,parent = correct_parent_bounds(parent,current_nodes,common_nodes)
            #print(len(current_nodes))
            #ADDING TO hierarchy, getting ready for next round
            #root = add_to_hierarchy(target_tree,parent)
            #target_tree._setroot(root)
            
            #make it new_current_nodes
            new_current_nodes.append(parent)
            count_current_nodes = len(current_nodes)
            #print(count_current_nodes)
            #break
        #break
        root = add_to_hierarchy2(target_tree,new_current_nodes)
        target_tree._setroot(root)
        print("----NEW LEVEL -----")
        current_nodes = new_current_nodes 
        #current_nodes = list(target_tree.getroot())
    return target_tree

#input xml file
#xml_file_name="screenshot_5 copy.xml"
#xml_file_name="XmlFiles/Weather.xml"
#xml_folder="200_xml_dataset/" #dataset folder 
xml_folder="Dataset/" #dataset folder 

l=os.listdir("xml/")

for xml_file_name in l:
    xml_file_name="xml/"+xml_file_name
    print("Processing: "+xml_file_name) 
    tree=main(xml_file_name,xml_folder)
    new_file_name=xml_file_name.split("/")[-1][:-4]+"_"+xml_folder[:-1]+".uix"
    save_tree(tree,new_file_name)
    print("Out of xml_dict")

UIX_output = "UIX_output"
isExist = os.path.exists(UIX_output)
if not isExist: 
    os.makedirs("UIX_output")
    print("Created UIX_output folder") 

files=os.listdir("./")
for file in files:
    if '.uix' in file:
        des=UIX_output+"/"+file
        shutil.move(file, des)