# Nervestaple Clinical Health Message Toolkit

This project contains all of the bits and pieces that make up the Nervestaple Clinical Health Message Toolkit. These tools are designed to make clinical health messages in the [HL7 2.x Messaging][wiki-hl7] format easier to work with.

## Development

This project follows the Polylith architecture for organizing source code and the various files needed to generate useful artifacts.

### About Polylith

The Polylith documentation can be found here:

- The [high-level documentation](https://polylith.gitbook.io/polylith)
- The [Polylith Tool documentation](https://github.com/polyfy/polylith)
- The [RealWorld example app documentation](https://github.com/furkan3ayraktar/clojure-polylith-realworld-example-app)

You can also get in touch with the Polylith Team via our
[forum](https://polylith.freeflarum.com) or on
[Slack](https://clojurians.slack.com/archives/C013B7MQHJQ).

## Building and Running

All of the documentation for the various projects are in their respective
directories in the "project" folder. The build script for all of these projects,
however, lives in the root directory of this project (`./project.clj`).

To build the "uberjar" for any project, invoke the script for the root directory like so:

    clj -T:build uberjar :project message-intermediate

The invocation above calls the "uberjar" function in the "build.clj" file for
the project "message-intermediate", the output will be in the "target" directory
of the project. Documentation about these projects can be found in their
respective folders.

Once the build is complete you can invoke the uberjar like so:

     java -jar projects/minichat/target/message-intermediate.jar

Even the projects that build library code typically have some demonstration utility that you can call directly from the JAR file.

----
[wiki-hl7]: https://en.wikipedia.org/wiki/HL7
