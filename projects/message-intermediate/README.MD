# Clinical Health Message Intermediate Format

This project provides our clinical health message interchange format. Functions are provided to transform [HL7v2 Messaging messages][hl7v2] messages into our intermediate format and then back again.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.cmiles74/message-intermediate.svg)](https://clojars.org/org.clojars.cmiles74/message-intermediate)

## Building

To build the library jar file for this project, navigate to the root directory of the project and invoke the build script like so...

    cd ../..
    clj -T:build jar :project message-intermediate

You will find the library in the target directory. :winking_face:

---
[hl7v2]: https://en.wikipedia.org/wiki/HL7#Version_2
