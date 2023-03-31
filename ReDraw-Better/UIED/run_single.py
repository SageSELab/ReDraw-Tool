import detect_compo.ip_region_proposal as ip
import detect_text.text_detection as text
import detect_merge.merge as merge
from os.path import join as pjoin
#from cnn.CNN import CNN
import numpy as np
import argparse
import shutil
import cv2
import os
import crop

def resize_height_by_longest_edge(img_path, resize_length=800):
    org = cv2.imread(img_path)
    height, width = org.shape[:2]
    if height > width:
        return resize_length
    else:
        return int(resize_length * (height / width))

def resize_to_ReDraw_Dimensions(img_path):
    org = cv2.imread(img_path)
    dim=(1200,1920)
    resized = cv2.resize(org, dim, interpolation = cv2.INTER_AREA)  
    height, width, channels = resized.shape
    for x in range(0,71):
        for y in range(0,width):
            resized[x][y]=(255,255,255)

    for x in range(1775,height):
        for y in range(0,width):
            resized[x][y]=(255,255,255)
    return resized      


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description="1. Input Image Path in PNG 2. batch images True or False")
    parser.add_argument("--input_image", type=str)
    parser.add_argument("--batch_images", type=str)
    args = parser.parse_args()
    
    key_params = {'min-grad':10, 'ffl-block':5, 'min-ele-area':50,
                  'merge-contained-ele':True, 'merge-line-to-paragraph':False, 'remove-bar':True}

    # get if batch input is True or False
    batch_input=""
    if args.batch_images:
        if args.batch_images.lower()=='true':
            batch_input="True"
        else:
            batch_input="False"
    else:
        batch_input="False"

    if batch_input=='False':

        # get input image path
        input_image=args.input_image.lower()    

        # make a copy to the data/input folder.
        input_image_name = input_image.split('.png')[:-1]
        dst = "UIED/data/input/"+str(input_image_name[0])+".png"
    
        shutil.copyfile(input_image, dst)

        # set input image path
        input_folder = 'UIED/data/input/'+input_image

        input_path_img_folder = [input_folder]
        print("Input Image:"+str(input_path_img_folder))

    else:
        # get batch images
        image_list = os.listdir("Batch_Input_Images/")
        input_path_img_folder=[]

        for images in image_list:
            # get image name
            input_image_name = images.split('.')[:-1]

            input_image = "Batch_Input_Images/"+images
            dst = "UIED/data/input/"+str(input_image_name[0])+".png"        
            shutil.copyfile(input_image, dst)

            input_path_img_folder.append(dst)


    for input_path_img in input_path_img_folder:
        print("Processing image: "+str(input_path_img))
        output_root = 'UIED/data/output'
        resized_height=1200
        
        print("--Resize To ReDraw Dimensions--")
        resized_image = resize_to_ReDraw_Dimensions(input_path_img)

        print("--Saving Resize ReDraw Image--")
        cv2.imwrite(input_path_img, resized_image)

        is_ip = True
        is_clf = False
        is_ocr = True
        is_merge = True
        is_redraw = True

        print("--Procesing OCR--")
        if is_ocr:
            os.makedirs(pjoin(output_root, 'ocr'), exist_ok=True)
            text.text_detection(input_path_img, output_root, show=True)

        print("--Procesing IP--")
        if is_ip:
            os.makedirs(pjoin(output_root, 'ip'), exist_ok=True)
            classifier = None
    
            print("--Procesing CLF--")
            if is_clf:
                classifier = {}
                classifier['Elements'] = CNN('Elements')
            ip.compo_detection(input_path_img, output_root, key_params,
                               classifier=classifier, resize_by_height=resized_height, show=True)

        print("--Procesing merge--")
        if is_merge:
            os.makedirs(pjoin(output_root, 'merge'), exist_ok=True)
            name = input_path_img.split('/')[-1][:-4]
            compo_path = pjoin(output_root, 'ip', str(name) + '.json')
            ocr_path = pjoin(output_root, 'ocr', str(name) + '.json')
            merge.merge(input_path_img, compo_path, ocr_path, pjoin(output_root, 'merge'),
                        is_remove_bar=key_params['remove-bar'], is_paragraph=key_params['merge-line-to-paragraph'], show=True)

        print("--Procesing ReDraw--")
        if is_redraw:
            image_name = input_path_img.split('/')[-1]
            image_name=image_name.split('.')[0]
            crop.crop_resize(image_name)