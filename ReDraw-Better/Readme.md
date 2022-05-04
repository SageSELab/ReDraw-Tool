# ReDraw-Better
## _Enhancement on : Machine Learning-Based Prototyping of Graphical User Interfaces for Mobile Apps (ReDraw)_

<img src="https://sagelab.io/images/sage-logo.png" style="height:15px;width:15px;" /> SAGE Research Lab

> From the three main steps of Design UI, Implementing UI, and Testing UI,  ReDraw majorly focuses on automatically Implementing UI for android applications. 

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
## Part 1: Model training for Sub-Component Classification.

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

## Part 2: Using Trained Model for predicting output on images of sub-components obtained from UIED Model.

The first step when we get an image of an android UI is to detect sub-component. For sub-component detection we compare a couple of models and used UIED as it performed the best. 

Want to contribute? Great!

Dillinger uses Gulp + Webpack for fast developing.
Make a change in your file and instantaneously see your updates!

Open your favorite Terminal and run these commands.

First Tab:

```sh
node app
```

Second Tab:

```sh
gulp watch
```

(optional) Third:

```sh
karma test
```

#### Building for source

For production release:

```sh
gulp build --prod
```

Generating pre-built zip archives for distribution:

```sh
gulp build dist --prod
```

## Docker

Dillinger is very easy to install and deploy in a Docker container.

By default, the Docker will expose port 8080, so change this within the
Dockerfile if necessary. When ready, simply use the Dockerfile to
build the image.

```sh
cd dillinger
docker build -t <youruser>/dillinger:${package.json.version} .
```

This will create the dillinger image and pull in the necessary dependencies.
Be sure to swap out `${package.json.version}` with the actual
version of Dillinger.

Once done, run the Docker image and map the port to whatever you wish on
your host. In this example, we simply map port 8000 of the host to
port 8080 of the Docker (or whatever port was exposed in the Dockerfile):

```sh
docker run -d -p 8000:8080 --restart=always --cap-add=SYS_ADMIN --name=dillinger <youruser>/dillinger:${package.json.version}
```

> Note: `--capt-add=SYS-ADMIN` is required for PDF rendering.

Verify the deployment by navigating to your server address in
your preferred browser.

```sh
127.0.0.1:8000
```


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
├── Dataset/
