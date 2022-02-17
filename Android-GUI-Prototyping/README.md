# Android GUI Prototyping

## Overview

This application takes an UIAutomatorViewer file and a screenshot
and converts it into a working, if skeletal, Android project. You
can scale the app by dpi and screen dimensions, write the output to
a SQL database, and even link together multiple screens into one
project!

The aim is to recreate the project with pixel- and color-perfect
accuracy through the careful implementation of RelativeLayouts,
LinearLayouts, etc.

This project was created to facilitate rapid prototyping of Android 
applications, particularly by people without programming skills. 
However, this is also part of an attempt to generate Android 
applications from nothing but a screenshot of an app. This is done 
through the TAs' machine learning black magic; I know nothing about it, 
and frankly, I'd rather not learn out of fear for my sanity.

Carefully go through each part of this installation process, and you
will (hopefully) have nothing to complain about! If you do, then please
update this documentation with accurate information (or common woes
that people come across).

## Prerequisites

To run this program, you will need to install:
 * MySQL Database
 * The Android SDK (supplied by downloading Android Studio)

Any version should work fine. Emphasis on *should*.

To build generated applications, you will need to install the
following packages through the SDK manager:

 * Android platform (API 25)
 * Build tools level 25.0.3
 * Platform tools level 25

To make life easier, you will want to familiarize yourself with 
`avdmanager`, `sdkmanager`, `adb`, and `emulator`, all of which are 
within the SDK. You should probably add them to your `PATH`. However, 
most of these tools can be replaced with Android Studio if you're 
unusually masochistic.

The website dpi.lv provides the DPIs of several common mobile devices.

## Invoking the Program

The main function of this program simply creates the source code
(layouts, build code, Java files, etc.) for a project.

This code can be imported as an Eclipse project. If you want to
compile and invoke it through the command line, you can use:

```
# Bash
javac -cp 'dependencies/*:src' -d bin src/cs435/guiproto/Main.java
java -ea -cp 'dependencies/*:bin' cs435/guiproto/Main [args]
# cmd.exe
javac -cp 'dependencies/*;src' -d bin src/cs435/guiproto/Main.java
java -ea -cp 'dependencies/*;bin' cs435/guiproto/Main [args]
```

Invoking the project with no arguments will open up an, er, *minimal*
GUI. If you want things to go faster, the command line arguments are:

```
Main in-xml out-dir sdk-dir in-screen dpi in-width in-height out-width out-height
```

argument   | description
-----------|------------
in-xml     | Path to UIAutomatorViewer file
out-dir    | Directory that the project will be created in. Must exist, but be empty.
sdk-dir    | Directory of your Android SDK.
in-screen  | Directory to the screenshot.
dpi        | DPI of the input device.
in-width   | Width of the input device's screen.
in-height  | Height of the input device's screen.
out-width  | Width of the output device's screen.
out-height | Height of the output device's screen.

## Building

Your generated project comes complete with a Gradle wrapper, which 
automatically installs everything you need to build the project! Just 
invoke `./gradlew --info` (on UNIX) or `gradlew.bat --info` (on 
Windows) in the root directory of the generated project!

Building the project can take awhile - sometimes up to a minute. 
Android Studio can build a project in seconds, so we're clearly doing 
something wrong. It's your job to figure out what!

## Installing

If building the project succeeds, you'll need to install it to a
running emulator.

1. Start your Android emulator. You can use Android Studio's
   AVD Manager (or `emulator`).
2. Navigate to `{project root}/build/outputs/apk/`.
3. Use `adb install debug-unsigned.apk`.

You can't install an app twice; you need to uninstall the app first. 
You can do this from the emulator or use `adb uninstall 
{project.package.name}`.

## Known Issues/Hints

* On Macs, this project cannot be run or debugged from Eclipse. Instead, use the command line.
* On Ubuntu, your Android SDK should be in ~/Android/Sdk. On Macs, this is in user/Library/Android. On Windows, it's somewhere in Appdata/local.

## Acceptance Tests

`AcceptanceTests.java` lets you generate, build, and install many 
projects to a running emulator at once. I'm not going to go into
too much detail here, but suffice to say it will let you see what your
changes are doing to a variety of apps.

## SQL Database

If you have mySQL installed, this project will write the structure of the 
generated app to a database named 'android_db'.
The project uses root as the user. If no password has been configured for
root, the project will automatically create and build the database. If a 
password has been configured, there is a text field available 
to insert it in the GUI, at the bottome of the middle column (under the
width and height entries).

The database android_db contains the table 'views', and is built as follows:

| id  | parent_id | view | x | y | width | height | text |

id = number assigned to a view. Each number is unique to its view.
parent_id = id of the view which this view is assigned under.
view = the type of view (i.e. Absolute Layout, Relative Layout, Button, etc.)
x = the absolute x coordinate of the view.
y = the absolute y coordinate of the view.
width = the width of the view.
height = the heigth of the view.
text = the words contained within the view. May be empty if the view contains no text.

## Project structure

You can get a good idea of the general code flow by looking at 
`ProjectBuilder` (natch). The most important class is probably `View`, 
since much of that project-building capacity is delegated to `View`. 
For layout generation, look at `View.getLayoutElement()` and 
`View.pack()`; for style generation, look at `StyleFragment`, 
`StyleBuilder`, and `View.initStyleFragment()`.

On the other hand, that's just the stuff I worked on, so the others 
probably have different opinions entrely. You're probably going to have 
to look at every class at some point or another, so this is by no means 
comprehensive.

## Original Project Proposal

Application prototyping is a powerful way to present an initial idea to 
clients, in order to illustrate a proof of concept or give a general 
idea of the design of an application. Graphic design tools like Sketch,  
Pencil and Photoshop among others greatly facilitate this process by 
allowing designers to create near pixel perfect representations of 
mobile application GUIs. While these tools are powerful, the 
prototyping process could be further extended to speed up initial steps 
of the mobile app development process. The purpose of this project is 
to work towards extending this prototyping process so that it benefits 
both designers and developers by leveraging a sketch that is created by 
a designer to generate a skeleton of Android application. This project 
should take as input a set of application mock-ups containing 
GUI-widgets labeled with their Android View Classes and generate a 
compilable Android project including navigation between screens. This 
will involve parsing information from mockup files, and generating 
Android GUIs and the logic required to enable navigation between 
screens and the compilation of the app. This project could be developed 
either as a web application or standalone application, the decision is 
up to the members of the team.
