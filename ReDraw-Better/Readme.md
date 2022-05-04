# ReDraw-Better
## _Enhancement on : Machine Learning-Based Prototyping of Graphical User Interfaces for Mobile Apps (ReDraw)_

<img src="https://sagelab.io/images/sage-logo.png" style="height:15px;width:15px;" /> SAGE Research Lab

> To be changed <===> From the three main steps of Design UI, Implementing UI, and Testing UI,  ReDraw majorly focuses on automatically Implementing UI for android applications. 

## Approach Over-View

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
## Part 1: Setting up UIED model.
The first step in the process for ReDraw-Better is to identify sub-components taking an image/ images as input. For Identifying sub-components we have several approached form the tabel below we can see that UIED performs the best.

<img src="https://github.com/SageSELab/ReDraw-Tool/blob/main/ReDraw-Better/images/model_F1.JPG" />

We have performed several changes in the UIED repository to make it compactable with our code. We apply UIED to the input image and detect all the sub-componets in the image, then we crop all the sub-components and save them in a seperate folder along with a json file storing it's coordinates. This folder is been later used to generate XML file.

You can add all the input images in the UIED/data/input folder. The output of the UIED will be saved in UIED/data/output/ReDrawModel/

```
python run_single.py
```

## Part 2: Model training for Sub-Component Classification.
Part 2 focuses on the following: Now before generating XML file, we need to train a CNN model for classficiaiton of detected sub-components. 
### Dataset
We are using ReDraw Dataset which can be directly be available from [Link](https://zenodo.org/record/2530277#.YnF9StrMK01). [https://zenodo.org/record/2530277#.YnF9StrMK01]
```
mkdir Dataset
!wget https://zenodo.org/record/2530277/files/CNN-Data-Final.tar.gz?download=1
```

### Selected Sub-Component:
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
We have provided a pre-processing file where all the images from the dataset will be arranged according to the 15 classes and are resized. This is used futher in the Image Data Generator method.

```
python preprocessing.py
```

### Model Training
We are using VGG16 model which is pre-trained on ImageNet model and finetuning it with ReDraw Dataset.

Hyper-Parameters while model training:
- image_size = (224,224)
- batch_size = 32
- epoch=5
- early stop (monitor='val_accuracy', patience=3)

```
python train_model.py
```

## Part 3: Using Trained Model for predicting output on images of sub-components obtained from UIED Model.

Once the model is been trained we have created a google colab notebook which can be used to generate XML file.

[Link](https://colab.research.google.com/drive/1ntCboWsQAouVxhzHDlDCuW-0Z7UASjjz?usp=sharing)
https://colab.research.google.com/drive/1ntCboWsQAouVxhzHDlDCuW-0Z7UASjjz?usp=sharing

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
│ │── Generate_XML.ipynb
├── Dataset/
├── UIED/
