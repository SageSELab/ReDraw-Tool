# ReDraw

## Project Summary

As mobile applications begin to dominate the industry the need to rapidly develop applications becomes more necessary. An early part of this process is the development of GUI prototypes. Facilitating the rapid prototyping of GUIs becomes a priority. Due to the complexity of the underlying GUI layouts on mobile systems, this is not as simple as it is on other machines.

In order to assist the prototyping of android applications ReDraw was developed. Redraw is able to take in images and potentially metadata to classify the components based on the visual representation of them. Then it is able to group together the components in hierarchies and build a basic android application using the information.

This can facilitate the development of GUI based android apps by allowing basic drawings to be used to develop the front end interface of the mobile app.

## Use Cases

Redraw is able to create an android application based on simple mock-up data that has been fed into it. It's primary application is to allow for the fast development of GUIs by individuals who may not have experience in traditional GUI development software. It could also be used to create a mock-up of an already existent GUI.

## Getting Started

### Prerequisites

In order to Use ReDraw the user must have Python 3 installed on their machine. In addition they should have at least tensorflow 1.12 installed on their machine as well. ReDraw was developed with Java version 8.
In order to create an android application using ReDraw the user should have a version of the Android SDK on their machine. In order to do OCR text recognition for the textboxes and buttons, the user will have to have Tesseract on their machine.
ReDraw also utilizes other SEMERU projects such as REMAUI, Android-Gui-Prototyping, Android-Core and SEMERU-Core. In order to continue developing ReDraw, these projects will also need to be downloaded.

ReDraw has been developed to run on Windows and Unix based machines. Most of the later development was done on a Windows environment using Eclipse.

### Installing and Running the GCat Binaries

The steps to install and run redraw are as follows:

* Download the latest version of ReDraw from the repository (https://gitlab.com/SEMERU-Code/ReDraw/tree/reDrawSprague)
* Unzip the directory
* Find The zip folder MATLAB-R-CNN\Android-Workspace\tiers.zip and unzip it to a location that will be remembered
* Create a folder for preliminary output, Remember this folder name.
* Modify the config.json file with parameters that will be listed later in this readme

In order to continue development for ReDraw:

* Download the Semeru projects Android-Core, Android-GUI-Prototyping, REMAUI, and SEMERU-Core in addition to ReDraw
* Add the projects into the same workspace using File --> Import into the "Existing Java Projects into Workspace" option and follow the prompts.
* The primary class is GenerateWrapper, A test class called TestPipeline also exists to be modified for testing purposes
* The arguments for the GenerateWrapper class is the location of the config.json file



### Capturing Input Files from a Device or Emulator

ReDraw can be run using metadata that is extracted from remaui using the image files or metadata that has been extracted from projects.
In order to get metadata from an existing android application users can use the adb.exe tool in the android SDK

Using REMAUI the xml data can be gathered using a screenshot from the application.

## Usage

In order to Run redraw:

* locate the redraw.jar file from the install folder
* open the command line
* call: java -jar ReDraw.jar <config.json path>

### Parameters

The config.json file contains the following parameters that will need to be modified for the application to work:

| Parameter | Description | Default |
|---|---| --- |
|```--FinalOutputDirectory```| The directory to output the finial android application code to | "C:\\Redraw\\FinalOutput" |
|```--PreliminaryOutputDirectory```| The directory that preliminary files are put into for processing such as for tesseract or the CNN to operate on | "C:\\Redraw\\Output" | 
|```--PathToNet```| The path to the frozen model to be used for classification. The current checkpoint that is included( and recommended) with the project is in the modelDir and called frozen_inception_v3.pb | "C:\\Redraw\\frozen_inception_v3.pb" | 
|```--PathToKnnData```| The path to the directory that contains the KNN data. This is included with the project as the "tiers.zip" file. When extracted this parameter should point the the parent folder that contains "tier1","tier2","tier3" and "tier4"| "C:\\Redraw\\tiers" | 
|```--PathToAndroidSDK```| The path to the directory that contains the Android SDK, this can come installed with android studio or be installed on it's own. It is not included in this repository| "C:\\AndroidSDK" | 
|```--PathToTesseract```| The path to the directory that contains the Tesseract tool. This is required for OCR classification| "C:\\Redraw\\tesseract" | 
|```--labelFile```| The Location of the labels.txt file that is included with the repository. This files is used to associate the labels with the CNN output| "C:\\Redraw\\labels.txt" | 
|```--scriptPath```| The path to the script that loads the CNN and classifies the data into the component types. The file is included in the project repository| "C:\\Redraw\\redrawClassify.py" | 
|```--inputLayer```| The name of the layer where input is fed into the CNN. This should not be changed unless a new CNN is provided that requires a different input layer| "input:0" | 
|```--outputLayer```| The name of the layer where output is received from the CNN. This should not be changed unless a new CNN is provided that has a different output layer| "InceptionV3/Predictions/Reshape_1:0" | 
|```--useREMAUI```| Specifies whether or not to automatically use REMAUI or previous metadata. Currently the automatic use of REMAUI is not fully implemented| "false" | 
|```--useAbsolutPositioning```| Specifies whether or not to use absolute positioning when creating the android code| "true" | 
|```--inputFolder```| The folder where the input is expected. The pattern of the inputfolder will be specified later| "C:\\ReDraw-REMAUI-Pix2Code-Oracle-Images-and-XML\\oracle" | 
|```--pythonCommand```| The command that the user uses to call their installation of python 3 with tensorflow| "python" | 



### Input Folder structure

Redraw expects a parent folder that contains a number of sub folders. Each subfolder needs to have one or more png images, and an xml file for each png image (either extracted, or created from REMAUI). The xml file needs to correlate with the png, having the same name, with a different extension. Each image will be processed as a different screen and each subfolder will be considered a different app.

Example:

* MainInputFolder/
	* app1Folder/
		* 1.png
		* 1.xml
		* 2.png
		* 2.xml
	* app2Folder/
		* 1.png
		* 1.xml


## Output

The output overwrites the previous output of the same name in the Final output folder.

The output will be parsed into folders first by the application name, then into folders by screen number, which will contain the android code.

For the previous example given the expected output would be:

* MainOutputFolder/
	* app1Folder/
		* 1
			* <Android Code for app1 screen 1)
		* 2
			* <Android Code for app1 screen 2)
	* app2Folder/
		* 1
			* <Android Code for app2 screen 1)


## Our Team

### Advisors

* [Kevin Moran](http://www.kpmoran.com)
* [Denys Poshyvanyk](http://www.cs.wm.edu/~denys/index.html)

### Graduate Student Contributors for this branch

* Andrew Sprague

