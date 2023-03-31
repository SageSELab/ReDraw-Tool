import os
import cv2
import shutil
import numpy as np
import tensorflow as tf
from keras.models import Model
from keras.layers import Dense
from keras.layers import Flatten

from keras.applications.vgg16 import VGG16
from keras.preprocessing.image import load_img
from keras.preprocessing.image import img_to_array
from keras.preprocessing.image import ImageDataGenerator
from keras.applications.vgg16 import preprocess_input
from keras.applications.vgg16 import decode_predictions

def get_model():
  # load model
  model = VGG16(weights="imagenet", include_top=False, input_shape=(224, 224, 3))
  # set trainable params as false.
  model.trainable = False

  # add new classifier layers
  flat1 = Flatten()(model.layers[-1].output)
  class1 = Dense(1024, activation='relu')(flat1)
  class2 = Dense(512, activation='relu')(class1)
  class3 = Dense(256, activation='relu')(class2)
  class4 = Dense(64, activation='relu')(class3)
  output = Dense(16, activation='softmax')(class4)

  # define new model
  model = Model(inputs=model.inputs, outputs=output)

  return model

image_size = (224,224)
batch_size = 32
epoch=50
class_subset=['Switch','ToggleButton','ImageButton','ProgressBarHorizontal','SeekBar','RadioButton', 'CheckedTextView', 'Button', 'NumberPicker',
              'EditText','ImageView', 'CheckBox', 'ProgressBarVertical', 'TextView', 'RatingBar','Spinner']

# creating Image Data Generators.
train_datagen = ImageDataGenerator(preprocessing_function=preprocess_input) 
test_datagen = ImageDataGenerator(preprocessing_function=preprocess_input) 
validate_datagen = ImageDataGenerator(preprocessing_function=preprocess_input) 

# Loading images in generator.
train_generator = train_datagen.flow_from_directory(
                  "../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Training",
                  target_size=(224, 224),
                  batch_size=batch_size,
                  classes=class_subset,
                  class_mode='categorical')

test_generator = test_datagen.flow_from_directory(
                      "../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Test",
                      target_size=(224,224),
                      batch_size=batch_size,
                      classes=class_subset,
                      class_mode='categorical')

validation_generator = validate_datagen.flow_from_directory(
                      "../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Validation",
                      target_size=(224,224),
                      batch_size=batch_size,
                      classes=class_subset,
                      class_mode='categorical')              

# Create Model.
model=get_model()
# Adding Early Stopping.
callback = tf.keras.callbacks.EarlyStopping(monitor='val_accuracy', patience=3)
# compile the model.
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
# save records in history.
history = model.fit(train_generator,epochs=epoch,validation_data=validation_generator,batch_size=32,workers=2, callbacks=[callback],use_multiprocessing=True,verbose=1)  
# Create path for saving checkpoints.
checkpoint_path = "./checkpoints"

print_checkpoint = "Saving weights to: "+checkpoint_path
print(print_checkpoint)
# Saving wrights of trained model.
model.save_weights(checkpoint_path)

# checking testing accuracy.
testing_path = '../Dataset/CNN-Evaluation/Partitioned-Organic-Data-Split/Test/'

"""
# Only use the below code in case of directly loading the saved weights to the model. 

# Create model.
model = get_model()

# Load trained weights.
model.load_weights("./checkpoints")

# create classes.
class_subset=['Switch','ToggleButton','ImageButton','ProgressBarHorizontal','SeekBar','RadioButton', 'CheckedTextView', 'Button', 'NumberPicker',
              'EditText','ImageView', 'CheckBox', 'ProgressBarVertical', 'TextView', 'RatingBar','Spinner']
"""

# reading all the files and saving it in list.
inputFileList=[]
for r,d,f in os.walk(testing_path):
  for i in f:
    if(i[0]!='.'):
      img_class = i.split('.')[2]
      inputFileList.append(testing_path+str(img_class)+'/'+i)  

print("Calculating Testing Accuracy...")

# checking for all images in testing folder.
count=0
total=len(inputFileList)

for fil1 in range(total):
  if(fil1%1000==0):
    print("Processing...")
  fil=inputFileList[fil1]
  true_ans = fil.split('.')[-2]  
  # load an image from file
  image = load_img(fil, target_size=(224, 224))
  # convert the image pixels to a numpy array
  image = img_to_array(image)
  # reshape data for the model
  image = image.reshape((1, image.shape[0], image.shape[1], image.shape[2]))
  # prepare the image for the VGG model
  image = preprocess_input(image)
  # predict the probability across all output classes
  yhat = model.predict(image)
  yhat=yhat.reshape(16)
  label = np.argmax(yhat)
  if(true_ans==class_subset[label]):
    count+=1

# printing results.
print_testingAccuracy = "Image Numbers: "+str(fil1)+ " Testing Accuracy: "+str(count/fil1)+" Model: VGG16"
print(print_testingAccuracy)