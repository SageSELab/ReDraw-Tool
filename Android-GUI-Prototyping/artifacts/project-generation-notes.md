# Automated Project Generation

Android apps aren't really meant to be easily created or built without the help of an IDE, to the extent that there really are no centralized tools for doing it. This document will describe how to build an .apk from nothing with the help of a few tools from the Android SDK.

## Build Plan

In order to build an Android project, we need to do the following (as far as we know). We can add more detail as we go along.

> [Structure of an Android project](http://www.compiletimeerror.com/2013/01/directorystructure-of-android-project.html)
> [Building .apks by hand](https://spin.atomicobject.com/2011/08/22/building-android-application-bundles-apks-by-hand/)

### Create Some Folders

While Android Studio has a complicated system of folders, we might not need to use all of them, ultimately. Regardless, here are all the folders I could find:

* `src/` - Java source code
* `gen/` - Auto-generated code (`R.java`)
* `res/` - Resources (layouts, etc.)
* `res/values` - Can leave empty
* `res/animator` - Can leave empty
* `res/layout` - Layout .xml files
* `res/menu` - Can leave empty for now
* `res/raw` - Can leave empty?
* `res/drawable-mdpi` - Can put an image right here
* `/libs` - External libraries go here; we might not need these
* `/assets` - Raw files
* `/bin` - Generated code

### Generate our resources
This includes .xml layouts and icons.

### Generate Java Source Code

### Make some config files
AndroidManifest.xml
Project.Properties (?)

### Create `R.java`
`R.java` is an auto-generated class used for accessing resources.

```bash
aapt package -m -J gen/ -M ./AndroidManifest.xml -S res1/ -S res2 ... -I android.jar
```

### Compile everything into .jars
Unfortunately, we will probably need to make build code for this.

### Compile and Link Everything with `dx`
```bash
cd "the place where your compiled classes are" ; dx --dex --output=classes.dex SomeJar1.jar SomeJar2.jar ... 
```

### Package everything into an .apk

```bash
#Make your project
aapt package -f -M ./AndroidManifest.xml -S res1/ -S res2/ ... -I android.jar -F MyProject.apk.unaligned
#Add your classes
aapt add -f MyProject.apk.unaligned classes.dex
```

### Sign and align the .apk

Hell if I know what this is, all I know is that we need to do it.

## Tools

### aapt
`aapt` is a twofold tool: on one hand, it lets you create and extract from .apk archives, and on the other, it lets you "package" resources.
