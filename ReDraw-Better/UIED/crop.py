import cv2
import json
import glob, os
import numpy as np
from tqdm import tqdm

# Global Declarations.
path="UIED/data/output/merge/"
output_path = 'UIED/data/output/ReDrawModel/'
dim = (299, 299)

def crop_resize(image_name):
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
		#Improvement one
		#json_string[str(count)+".jpg"]="["+str(y1-10)+","+str(x1-10)+"],["+str(y2+10)+","+str(x2+10)+"]"	
		json_string[str(count)+".jpg"]="["+str(y1)+","+str(x1)+"],["+str(y2)+","+str(x2)+"]"	

		count+=1

	with open(output_path+image_name+'/'+image_name+'.json', 'w') as outfile:
		json.dump(json_string, outfile)
	 
	f.close()

