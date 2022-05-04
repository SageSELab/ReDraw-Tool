from os.path import join as pjoin
import cv2
import os
import numpy as np


def resize_height_by_longest_edge(img_path, resize_length=800):
    org = cv2.imread(img_path)
    height, width = org.shape[:2]
    if height > width:
        return resize_length
    else:
        return int(resize_length * (height / width))


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

    '''
        ele:min-grad: gradient threshold to produce binary map         
        ele:ffl-block: fill-flood threshold
        ele:min-ele-area: minimum area for selected elements 
        ele:merge-contained-ele: if True, merge elements contained in others
        text:max-word-inline-gap: words with smaller distance than the gap are counted as a line
        text:max-line-gap: lines with smaller distance than the gap are counted as a paragraph

        Tips:
        1. Larger *min-grad* produces fine-grained binary-map while prone to over-segment element to small pieces
        2. Smaller *min-ele-area* leaves tiny elements while prone to produce noises
        3. If not *merge-contained-ele*, the elements inside others will be recognized, while prone to produce noises
        4. The *max-word-inline-gap* and *max-line-gap* should be dependent on the input image size and resolution

        mobile: {'min-grad':4, 'ffl-block':5, 'min-ele-area':50, 'max-word-inline-gap':6, 'max-line-gap':1}
        web   : {'min-grad':3, 'ffl-block':5, 'min-ele-area':25, 'max-word-inline-gap':4, 'max-line-gap':4}
    '''

    key_params = {'min-grad':10, 'ffl-block':5, 'min-ele-area':50,
                  'merge-contained-ele':True, 'merge-line-to-paragraph':False, 'remove-bar':True}

    # set input image path
    input_folder = 'data/input'
    input_path_img_folder=[]
    for r,d,f in os.walk(input_folder):
        for files in f:
            input_path_img_folder.append(input_folder+'/'+files)

    #input_path_img = 'data/input/Full_Image5.png'
    #input_path_img_folder = ['data/input/Full_Image5.png','data/input/Full_Image4.png','data/input/Full_Image3.png','data/input/Full_Image2.png','data/input/Full_Image1.png']
    for input_path_img in input_path_img_folder:
        output_root = 'data/output'
        resized_height = resize_height_by_longest_edge(input_path_img, resize_length=800)
        color_tips()

        is_ip = True
        is_clf = False
        is_ocr = True
        is_merge = True
        is_redraw = True

        if is_ocr:
            import detect_text.text_detection as text
            os.makedirs(pjoin(output_root, 'ocr'), exist_ok=True)
            text.text_detection(input_path_img, output_root, show=True)

        if is_ip:
            import detect_compo.ip_region_proposal as ip
            os.makedirs(pjoin(output_root, 'ip'), exist_ok=True)
            classifier = None
            if is_clf:
                classifier = {}
                from cnn.CNN import CNN
                classifier['Elements'] = CNN('Elements')
            ip.compo_detection(input_path_img, output_root, key_params,
                               classifier=classifier, resize_by_height=resized_height, show=True)

        if is_merge:
            import detect_merge.merge as merge
            os.makedirs(pjoin(output_root, 'merge'), exist_ok=True)
            name = input_path_img.split('/')[-1][:-4]
            compo_path = pjoin(output_root, 'ip', str(name) + '.json')
            ocr_path = pjoin(output_root, 'ocr', str(name) + '.json')
            merge.merge(input_path_img, compo_path, ocr_path, pjoin(output_root, 'merge'),
                        is_remove_bar=key_params['remove-bar'], is_paragraph=key_params['merge-line-to-paragraph'], show=True)

        if is_redraw:
            import crop
            image_name = input_path_img.split('/')[-1]
            image_name=image_name.split('.')[0]
            crop.crop_resize(image_name)