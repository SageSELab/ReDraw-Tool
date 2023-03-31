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

def color_tips():
    color_map = {'Text': (0, 0, 255), 'Compo': (0, 255, 0), 'Block': (0, 255, 255), 'Text Content': (255, 0, 255)}
    board = np.zeros((200, 200, 3), dtype=np.uint8)
    board[:50, :, :] = (0, 0, 255)
    board[50:100, :, :] = (0, 255, 0)
    board[100:150, :, :] = (255, 0, 255)
    board[150:200, :, :] = (0, 255, 255)
    cv2.putText(board, 'Text', (10, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)
    cv2.putText(board, 'Non-text Compo', (10, 70), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)
    cv2.putText(board, "Compo's Text Content", (10, 120), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)
    cv2.putText(board, "Block", (10, 170), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 0), 2)
    cv2.imshow('colors', board)


if __name__ == '__main__':

    parser = argparse.ArgumentParser(description="1. Input Image Path in PNG")
    parser.add_argument("--input_image", type=str)
    args = parser.parse_args()
    
    # get input image path
    input_image=args.input_image.lower()    

    # make a copy to the data/input folder.
    input_image_name = input_image.split('.png')[:-1]
    dst = "UIED/data/input/"+str(input_image_name[0])+".png"
 
    shutil.copyfile(input_image, dst)

    key_params = {'min-grad':10, 'ffl-block':5, 'min-ele-area':50,
                  'merge-contained-ele':True, 'merge-line-to-paragraph':False, 'remove-bar':True}

    # set input image path
    input_folder = 'UIED/data/input/'+input_image

    input_path_img_folder = [input_folder]

    for input_path_img in input_path_img_folder:
        output_root = 'UIED/data/output'
        resized_height=1200
        resized_image = resize_to_ReDraw_Dimensions(input_path_img)
        cv2.imwrite(input_path_img, resized_image)
        color_tips()

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