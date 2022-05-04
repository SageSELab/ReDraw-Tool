# Prerequisites
All necessary dependencies are included in the repository; you should simply
need to configure the build path to look for the OpenCV and JDOM jars.  Note
that, since we use bindings for OpenCV rather than a native implementation, you
will need to configure eclipse for use with opencv. See [this link](https://docs.opencv.org/2.4/doc/tutorials/introduction/java_eclipse/java_eclipse.html) for more information.

## Setting the Tesseract Environment (Optional)
We use the command line `tesseract` utility rather than the library.  The Reach
source will take care of creating an appropriate environment for it, but if for
whatever reason you find that you need to do something different, we note the
necessary configuration here.  First, `tesseract` requires a `tessdata/`
directory with pre-trained language data; this directory also has configuration
files for various forms of output.  You'll need to point it to this directory
using the `TESSDATA_PREFIX` environment variable, for example:

```bash
export TESSDATA_PREFIX=<project root>/lib/tesseract/
```

Note that the trailing forward slash is required.

You'll also need to set `LD_LIBRARY_PATH` to include the location of the
`tesseract` and `leptonica` (which `tesseract` uses for image I/O) dynamic
libraries.

```bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:<project root>/lib/tesseract/
```

Finally, the heuristics borrowed from REMAUI make use of the font family and
font size to remove false positives from the OCR output.  To make use of this,
you will need to add the line `hocr_text_info 1` to the hOCR configuration file
located in `<project root>/lib/tesseract/tessdata/configs/`.
