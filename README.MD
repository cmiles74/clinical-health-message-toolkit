# Nervestaple Clinical Health Message Toolkit

This project contains all of the bits and pieces that make up the Nervestaple Clinical Health Message Toolkit. These tools are designed to make clinical health messages in the [HL7 2.x Messaging][wiki-hl7] format easier to work with.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.cmiles74/message-intermediate.svg)](https://clojars.org/org.clojars.cmiles74/message-intermediate)

## Projects

The following projects make up this larger Polylith project.

- [Intermediate Format](projects/message-intermediate/README.MD) - Transforms [HL7v2 Messaging messages][hl7v2] into our intermediate format and back again

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

Some projects build libraries, others build executable JAR files (uberjar) and some product both. To build a library project, invoke the script for the root directory like so:

    clj -T:build jar :project message-intermediate

To build the "uberjar" for a project, invoke the script for the root directory like so:

    clj -T:build uberjar :project interface-engine

Once the build is complete, you'll find the output in the project's `target` directory. If you've built an uberjar, you can invoke the uberjar like so:

     java -jar projects/minichat/target/interface-engine.jar

Projects may be also be cleaned, this will remove the `target` directory and it's contents from the target project.

    clj -T:build clean :project message-intermediate

Each of these invocations calls the appropriate" function in the `build.clj` file here at the root of this parent project. Documentation about these projects can be found in their respective folders.

## Deployment

We also provide a build target that will deploy a particular project out to Clojars. You will need to have the environment variables `CLOJARS_USERNAME` and `CLOJARS_PASSSWORD` set in your shell's environment.

    clj -T:build deploy :project message-intermediate

The version number used is set in the `deps.edn` file of the target project (under the `:library`, `:version`) keys. The Maven "group name" for every project is in the root `deps.clj` under the `:deploy`, `:group` key.

Typically we use a "snapshot" version number during development (i.e. `0.11-SNAPSHOT`), when we have completed development we'll change that to `0.11`, deploy, and then move to the next snapshot version (`0.12-SNAPSHOT`).

----
[wiki-hl7]: https://en.wikipedia.org/wiki/HL7
[hl7v2]: https://en.wikipedia.org/wiki/HL7#Version_2
