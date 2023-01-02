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
                l=i.attrib["bounds"]
                l2=[int(i) for i in l]
                i.attrib["bounds"] = '['+l[0]+','+l[1]+']['+l[2]+','+l[3]+']'
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
            #bounds=i.attrib["bounds"]
            #print(i.attrib["bounds"])
            bounds=i.attrib["bounds"][1:-1].split('][')  #['2,3', '4,5']
            if len(bounds) == 1:
                bounds=i.attrib["bounds"][1:-1].split('],[')
            l=[strs.split(',') for strs in bounds] #[['2', '3'], ['4', '5']]
            ###NOT CHANGED AS MEET
            bounds=[l[0][0],l[0][1],l[1][0],l[1][1] ] #['2', '3','4', '5']
            #print("bounds",bounds)
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
            #print('i.attrib["bounds"]',i.attrib["bounds"])
            i.attrib["resource-id"]=""
            #if "NAF" in attrib:
            #    i.attrib["NAF"]=
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
    width = (x2 - x1)
    height = (y2 - y1)
    # handle case where there is NO overlap
    if (width<0) or (height <0):
        return 0.0
    area_overlap = width * height

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
    first_1=[float(firstNode.attrib["xStart"]),float(firstNode.attrib["yStart"]),float(firstNode.attrib["xEnd"]),float(firstNode.attrib["yEnd"])]
    second_1=[float(secondNode.attrib["xStart"]),float(secondNode.attrib["yStart"]),float(secondNode.attrib["xEnd"]),float(secondNode.attrib["yEnd"])]
    return get_iou(first_1,second_1)

def getIOUScore(dataset_leaves, target_leaves):
    score=0
    for j in range(len(target_leaves)):        
        count=0
        for k in range(len(dataset_leaves)):
            if( dataset_leaves[k].attrib!={} ):
                score = score + computeIOU(target_leaves[j], dataset_leaves[k])
            else:
                count += 1
    #normalized_score = score/(len(dataset_leaves)*len(target_leaves))
    if((len(dataset_leaves)-count)!=0):
        normalized_score = score/ (len(dataset_leaves)-count)
    else:
        normalized_score=0
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
    disappearingList = [elem for elem in dataset_leaves]
    common_nodes,hierarchy_nodes=[],[]
    for j in range(len(target_leaves)):
        my_matching_node=None
        max_score=-1
        for k in range(len(disappearingList)):
            score = computeIOU(target_leaves[j], disappearingList[k])
            threshold=0
            if score>=threshold:
                if score > max_score:
                    my_matching_node=disappearingList[k]
                    max_score=score
        if max_score!=-1:
            common_nodes.append(target_leaves[j])
            hierarchy_nodes.append(get_parent(tree,my_matching_node))
            disappearingList.remove(my_matching_node)
    return common_nodes, hierarchy_nodes

def correct_parent_bounds(parent):
    if len(parent)==1:
        #bounds of child
        child=parent[0]
        parent.attrib["xStart"] = str(int(child.attrib["xStart"])-1)
        parent.attrib["yStart"] = str(int(child.attrib["yStart"])-1)
        parent.attrib["xEnd"] = str(int(child.attrib["xEnd"])+1)
        parent.attrib["yEnd"] = str(int(child.attrib["yEnd"])+1)
    else:
        xstart = []
        ystart = []
        xEnd = []
        yEnd = []
        for child in parent:
            xstart.append(int(child.attrib["xStart"]))
            ystart.append(int(child.attrib["yStart"]))
            xEnd.append(int(child.attrib["xEnd"]))
            yEnd.append(int(child.attrib["yEnd"]))
        parent.attrib["xStart"] = str(min(xstart)-1)
        parent.attrib["yStart"] = str(min(ystart)-1)
        parent.attrib["xEnd"] = str(max(xEnd)+1)
        parent.attrib["yEnd"] = str(max(yEnd)+1)
    parent.attrib["bounds"]=[parent.attrib["xStart"],parent.attrib["yStart"],parent.attrib["xEnd"],parent.attrib["yEnd"]]
    return parent

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

def add_to_hierarchy(target_tree,common_nodes,hierarchy_nodes):
    root= target_tree.getroot()
    new_hierarchy_nodes=[]
    i=0
    for p,c in hierarchy_nodes:
        if p not in new_hierarchy_nodes:
            equal=None
            for j in new_hierarchy_nodes:
                equal = node_eq(p,j)
                if equal==True:
                    break
            if equal==True:
                j.append(common_nodes[i])
            else:
                new_hierarchy_nodes.append(p)
                parent = p
                bounds = parent.attrib["bounds"]
                class_=parent.attrib["class"]
                parent.clear()
                parent.append(common_nodes[i])
                parent.attrib["class"] = class_
                parent.attrib["bounds"] = bounds
                parent.attrib["xStart"]=bounds[0]
                parent.attrib["yStart"]=bounds[1]
                parent.attrib["xEnd"]=bounds[2]
                parent.attrib["yEnd"]=bounds[3]
        else:
            p.append(common_nodes[i])
        i+=1

    # Removing matched nodes from root
    for i in range(len(common_nodes)):
        root.remove(common_nodes[i])
    # Restricting bounds of parent
    for parent in new_hierarchy_nodes:
        #check if parent already present in root
        x = check_node_presence(root,parent)
        if x==False:
            parent = correct_parent_bounds(parent)
            root.append(parent)
    #root.extend(new_hierarchy_nodes) 
    return new_hierarchy_nodes

def add_remove_current_nodes(current_nodes,common_nodes,new_parents_added):
    for i in common_nodes:
        current_nodes.remove(i)
    for i in new_parents_added:
        current_nodes.append(i)
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
    for i in range(3):
        iou_scores = {file: 0 for file in fileList}
        for files in fileList:
            tree = xml_to_tree(files,target=False)
            levels=new_level_order(tree.getroot())
            dataset_leafs=levels[-(1+i)]
            if len(dataset_leafs)!=0:
                score = getIOUScore(dataset_leafs ,current_nodes)
                iou_scores[files]=score
            else:
                iou_scores[files]=0
        best_tree_file = max(iou_scores, key=iou_scores.get)
        print(best_tree_file, "score:",iou_scores[best_tree_file])
        tree = xml_to_tree(best_tree_file,target=False)
        levels=new_level_order(tree.getroot())
        dataset_leafs=levels[-(1+i)]
        #print("dataset_leafs",len(dataset_leafs))
        common_nodes,hierarchy_nodes = compareComponentByComponent2(tree,dataset_leafs, current_nodes)
        #print("common_nodes",len(common_nodes))
        #1. remove originl chidren from hierarchy and our children instead
        #4. add the new parent to our root
        new_parents_added=add_to_hierarchy(target_tree,common_nodes,hierarchy_nodes)
        #2. current nodes common nodes kadh
        #3. parent in current node
        current_nodes = add_remove_current_nodes(current_nodes,common_nodes,new_parents_added)
        print("----")
    return target_tree

#input xml file
xml_file_name="../without_header/Weather.xml"
xml_folder="../200_xml_dataset/" #dataset folder 

tree=main(xml_file_name,xml_folder)
new_file_name=xml_file_name.split("/")[-1][:-4]+"_200_files.uix"
save_tree(tree,new_file_name)
print("Out of xml_dict")
