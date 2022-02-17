# Color Quantization and Color Estimation

## Overview
Our challenge is to "guess" at the various colors in a component. This could include:

* Text color
* Color
* Outline color
* Shadow color

![Button example](https://cdn-images-1.medium.com/max/800/1*Dj3YxY5_isjWAvBsNx1Kvw.gif)

The buttons on the left have a blue color, a white text color, and a slight drop shadow. The buttons on the right have a white color and blue text color.

## Color Quantization
Color quantization is the process of reducing the number of colors in an image while staying as close to the original image as possible. The helper class itself provides methods for manipulating and quantizing images.

### ImageHelper Class
The most important method for us is `Color[] quantizeImageAndGetColors(String image, int colors)`. It loads an image file from disk and returns the n most prominent colors as an array of RGB colors.

There isn't a method to tell us where those colors are or exactly how frequent they are... so we might need to modify these methods to be more specific.

The class also provides methods for cropping and "augmenting" (drawing rectangles on) screenshots. These objects operate on on-disk image files.

## Which Color is Which?
While the helper library will let us get the most common colors, it doesn't tell us what color maps to what (background color vs. text color, for example). To solve that we will probably need some heuristics.

For example, text is generally in the center of a button. Since Material design promotes flat colors, the background color will probably be the most frequent color, and the text color will be second.

As we get more advanced, we can add more heuristics and specifications...

----------


> Written with [StackEdit](https://stackedit.io/).