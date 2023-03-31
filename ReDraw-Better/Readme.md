# ReDraw-Better
## _Enhancement on : Machine Learning-Based Prototyping of Graphical User Interfaces for Mobile Apps (ReDraw)_

<img src="https://sagelab.io/images/sage-logo.png" style="height:15px;width:15px;" /> SAGE Research Lab

Our project is focused on automating the process of creating a prototype of a graphical user interface (GUI) for Android mobile applications. Typically, developers manually transform a GUI mock-up into code, which can be a time-consuming and error-prone process. Our approach, which we have implemented in a system called REDRAW-Better, uses automated dynamic analysis, deep convolutional neural networks, and computer vision to accurately classify the components of the GUI mock-up.

Once the components have been classified, a data-driven, K-nearest-neighbors algorithm is used to generate a suitable hierarchical structure from which a prototype application can be automatically assembled. This hierarchical structure allows for the automatic assembly of the GUI prototype, saving time and effort for developers.

Our approach is based on the paper "Machine Learning-Based Prototyping of Graphical User Interfaces for Mobile Apps," and extends the work presented in that paper. With our automated approach, we aim to provide developers with an efficient and accurate way to prototype GUIs, allowing them to focus on other aspects of application development.

## Approach Overview

- Taking input as image file.
- Selecting 16 most commonly used sub-components of android.  
- Using UIED model for detecting sub-components from input image.
- Passing detected sub-components to VGG-16 model for Classificaiton.
- Passing this information to generate XML tree.
- Generate image from XML tree using ReDraw code.


## Installation
```
# Create python virtual environment
python3 -m venv venv

# Activate the python virtual environment
source venv/bin/activate

# Install the requirements for the project into the virtual environment
pip install -r requirements.txt
``` 

## Part 0: Download Dataset and trained model.
Create a folder name "Dataset" in the same folder
``` 
#Download zip file: 
https://drive.google.com/file/d/1x0Xcom9nzAZjOV_PCWWsZI9HRJZhrlWV/view?usp=share_link
unzip Dataset.zip
``` 
Create a folder named "Trained Model" in the same folder
``` 
#Download zip file: 
https://drive.google.com/file/d/1HjQ1hsX_vVZk4PEqtPUXHOVyyJyWg-_C/view?usp=share_link
unzip Trained Model.zip
``` 

## Part 1: Detect Subcomponent From Images.
The first step in the process for ReDraw-Better is to identify sub-components taking an image/ images as input. For Identifying sub-components we have several approached form the tabel below we can see that UIED performs the best.

<img src="https://github.com/SageSELab/ReDraw-Tool/blob/main/ReDraw-Better/images/model_F1.JPG" />

We have performed several changes in the UIED repository to make it compactable with our code. We apply UIED to the input image and detect all the sub-componets in the image, then we crop all the sub-components and save them in a seperate folder along with a json file storing it's coordinates. This folder is been later used to generate XML file.

For single image
```
python3 UIED/run_single.py --input_image input_image1.png
```
For Batch images
```
python3 UIED/run_single.py --batch_images True
```

## Part 2 (Optional): Classify the Detected Subcomponents.
##### We have provided trained model weights for you to use. You do not need to retrain the model.
Now before generating XML file, we need to train a CNN model for classficiaiton of detected sub-components. 
### Dataset
We are using ReDraw Dataset to train our model which can be directly be available from [Link](https://zenodo.org/record/2530277#.YnF9StrMK01). [https://zenodo.org/record/2530277#.YnF9StrMK01]
```
mkdir Dataset
!wget https://zenodo.org/record/2530277/files/CNN-Data-Final.tar.gz?download=1
```

### Selected Most Common Sub-Component:
1. Switch
2. ToggleButton 
3. ImageButton
4. ProgressBarHorizontal
5. SeekBar
6. RadioButton
7. CheckedTextView
8. Button
9. NumberPicker
10. EditText
11. ImageView
12. CheckBox
13. ProgressBarVertical
14. TextView
15. RatingBar
16. Spinner

### Pre-Processing 
We have provided a pre-processing file where all the images from the dataset will be arranged according to the 16 classes and are resized. This is used futher in the Image Data Generator method.

```
python preprocessing.py
```

### Model Training
We tested several CNN models and comapred their accuracy and the time taken by them to predict the output. 

<img src="https://github.com/SageSELab/ReDraw-Tool/blob/main/ReDraw-Better/images/Comparing%20Model%20Accuracy%20for%205000%20test%20images%20(1).png" />
<img src="https://github.com/SageSELab/ReDraw-Tool/blob/main/ReDraw-Better/images/Comparing%20Model%20Runtime%20in%20seconds%20for%205000%20test%20images%20(1).png" />

We are using VGG16 model which is pre-trained on ImageNet model and finetuning it with ReDraw Dataset.

Hyper-Parameters while model training:
- image_size = (224,224)
- batch_size = 32
- epoch=5
- early stop (monitor='val_accuracy', patience=3)

```
python train_model.py
```

## Part 3: Using Trained Model for predicting output on images of sub-components.

Once the model is been trained we use the generate_xml.py file.

To run for single image
```
python3 generate_xml.py --input_image input_image1.png --run_batch False
```
To run for batch images
```
python3 generate_xml.py --run_batch True
```

## Part 4: Using Generated XML to construct hierarchy of sub-components.

Finally we generate the UIX file with the hierarchy using KNN.

```
python generate_hierarchy.py
```
## Project Steps (For reference only)
- Upload input image and use UIED to get output.
- Upload output of UIED Redraw folder to Drive. 
- Use generate xml from UEID code from Google Colab to generate xml file.
- Change the 1st and the last line of the xml file.
- Use xml formator online
- Save updated file and send for Juhi code. 

## Project Structure (For reference only)
```bash
ReDraw-Better/
│
├── Code
│ ├── checkpoints/
│ ├── checkpoints.index
│ │ └── checkpoints.data-00000-of-00001
│ │ └──checkpoint
│ │── preprocessing.py
│ │── train_model.py
| |── train_model.py
│ │── Generate_XML.ipynb
├── Dataset/
├── UIED/
