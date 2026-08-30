# Clinical Health Message Interchange Format

This project provides our clinical health message interchange format. In addition to the library code we also include a simple utility script that can be invoked directly from the JAR file.

## Building

To build the "uberjar" for this project, navigate to the root directory of the project and invoke the build script like so...

    cd ../..
    clj -T:build uberjar :project message-intermediate

You will find the library in the target directory and it may be invoked directly.

