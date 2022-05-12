import cv2
import json
import glob, os
import numpy as np
from tqdm import tqdm

# Global Declarations.
path="data/output/merge/"
input_path="data/input/"
output_path = 'data/output/ReDrawModel/'
uiautomatorviewer_path = 'data/output/uiautomatorviewer/'
dim = (299, 299)

def crop_resize(image_name,components):

	components=components['compos']

	text_ocr={}
	for text_data in components:
		if 'text_content' in text_data.keys():
			position = text_data['position']
			temp_x1 = position['row_min']
			temp_y1 = position['column_min']
			temp_x2 = position['row_max']
			temp_y2 = position['column_max']
			text_ocr["["+str(temp_x1)+","+str(temp_y1)+"],["+str(temp_x2)+","+str(temp_y2)+"]"]=text_data['text_content']

	# Create a new directory because it does not exist 
	isExist = os.path.exists(uiautomatorviewer_path)
	if not isExist:
		os.makedirs(uiautomatorviewer_path)
		print("Creating directory uiautomatorviewer/")

	# creating a copy from input image.
	processed_image = cv2.imread(path+image_name+'.jpg', cv2.IMREAD_COLOR)
	processed_shape = processed_image.shape[:2]
	new_dim=(processed_shape[1],processed_shape[0])
	
	# find file from input folder:
	full_file_name=""
	for file in os.listdir(input_path):
		if file.startswith(image_name):
			full_file_name=file
			break

	# Writing it to uiautomatorviewer 
	current_image = cv2.imread(input_path+full_file_name, cv2.IMREAD_COLOR)
	current_image = cv2.resize(current_image, new_dim, interpolation = cv2.INTER_AREA)
	cv2.imwrite(uiautomatorviewer_path+image_name+".png", current_image)	

	# Create a new directory because it does not exist 
	isExist = os.path.exists(output_path)
	if not isExist:
		os.makedirs(output_path)
		print("Creating directory ReDrawModel/")

	json_string={}
	print("Loading "+path+image_name)

	# Create a new directory because it does not exist 
	isExist = os.path.exists(output_path+image_name)
	if not isExist:
		os.makedirs(output_path+image_name)
		print("Creating directory "+output_path+image_name)

	json_file=path+image_name+'.json'
	f = open(json_file)
	data = json.load(f)

	count=1

	for i in tqdm(data['compos']):

		# taking positions.
		position=i['position']
		x1 = position['row_min']
		y1 = position['column_min']
		x2 = position['row_max']
		y2 = position['column_max']

		image_file = json_file.replace(".json",'.jpg')

		img = cv2.imread(image_file)

		cropped_image = img[x1:x2, y1:y2]
		#cropped_image = cv2.resize(cropped_image, dim, interpolation = cv2.INTER_AREA)

		# saving croped files.
		cv2.imwrite(output_path+image_name+'/'+str(count)+".jpg", cropped_image)	

		# generating json for metadata.
		current_coordinate="["+str(x1)+","+str(y1)+"],["+str(x2)+","+str(y2)+"]"
		if(current_coordinate in text_ocr):
			json_string[str(count)+".jpg"]=[current_coordinate,text_ocr[current_coordinate]]				
		else:
			json_string[str(count)+".jpg"]=current_coordinate
		#json_string[str(count)+".jpg"]=current_coordinate	

		count+=1

	with open(output_path+image_name+'/'+image_name+'.json', 'w') as outfile:
		json.dump(json_string, outfile)
	 
	f.close()

