import os
import cv2

# image folder.
all_path=["../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Test/",
          "../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Training/",
          "../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Validation/"]

def pre_process(path):
  """
  input: path of dictionary for train test and validate.
  output: generate folder for each class and resize the image for image data generator.
  """
  # printing the current directory.
  print("Processing :"+path)
  # for all files in the directory.
  files = os.listdir(path)
  # getting labels form the image name.
  for f in files:
    if(f.find('._')==0):
      f=f[2:]
    if '.png' in f:
      folder=(f.split('.')[-2])
      # Check whether the specified path exists or not
      isExist = os.path.exists(path+folder)
      # creating folder for saving images of same classes.
      if not isExist:    
        os.makedirs(path+folder)
        print("The new directory is created!")
      # resize all images in same share for VGG 16.
      try:
        full_path = path+f
        i = cv2.imread(full_path)
        i = cv2.resize(i,(224, 224),interpolation = cv2.INTER_AREA)
        cv2.imwrite(path+folder+'/'+f, i)
        # removing the old image.
        os.remove(full_path)
      except:
        print("Error: "+f)

# Applying pre processing for all images for train test and validate.
for path in all_path:
  pre_process(path)        

# for all folders count number of images in each folder. (Just a senatory check).
for p in all_path:
  l=[]
  for it in os.scandir(p):
      if it.is_dir():
          l.append(it.path)

  count = 0

  for path in l:
    files = os.listdir(path)
    count+=len(files)

  print(p,count)  