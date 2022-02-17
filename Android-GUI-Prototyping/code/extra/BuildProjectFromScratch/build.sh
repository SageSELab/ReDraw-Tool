#!/bin/bash

./clean.sh

# Raw materials for building
echo "Copying files..."
mkdir -p src/main/java/proto/autogen
mkdir -p src/main/res/layout
cp inputs/AndroidManifest.xml src/main/
cp inputs/main_activity.xml   src/main/res/layout
cp inputs/MainActivity.java   src/main/java/proto/autogen

echo "Copying local.properties..."
cp inputs/local.properties .
cp inputs/build.gradle .

# gradle wrapper script - the user doesn't need to install gradle
# to build projects
echo "Copying gradlew..."
mkdir -p gradle/wrapper
cp inputs/gradlew/gradlew .
cp inputs/gradlew/gradlew.bat .
cp inputs/gradlew/gradle-wrapper.properties gradle/wrapper
cp inputs/gradlew/gradle-wrapper.jar gradle/wrapper

# if you get errors with debug keyfile generation, I'm not sure how to
# really get rid of them...

echo "Executing gradlew..."
./gradlew build
