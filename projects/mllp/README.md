# Minimum Low Level Protocol (MLLP) Reader and Writer

This component provides functions for reading and writing messages that are
wrapped in the [Minimum Low Level Protocol (MLLP)][mllp].

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.cmiles74/mllp.svg)](https://clojars.org/org.clojars.cmiles74/mllp)

[![cljdoc badge](https://cljdoc.org/badge/org.clojars.cmiles74/mllp)](https://cljdoc.org/d/org.clojars.cmiles74/mllp)

## Usage

First add the dependency to your project. If you are using Clojure `tools.deps`, you'll add something like the line below to your `deps.clj.` file. Remember to check the badge above or the Clojars website for the newest stable version.

    org.clojars.cmiles74/mllp {:mvn/version "1.1"}

You can now open a REPL for your project and import the `interface` namespace, that's where most of the good stuff lives.

```clojure
user> (require '[com.nervestaple.clinical.mllp.interface :as parser])
nil
```

Now you can write an MLLP wrapped message to your output stream or read one from your input stream.

```clojure
(mllp/write-stream out-stream "1970's tech is the best!")
```

## Building

To build the library jar file for this project, navigate to the root directory of the project and invoke the build script like so...

    cd ../..
    clj -T:build jar :project mllp

You will find the library in the target directory.

----
[mllp]: https://www.hl7.org/documentcenter/public/wg/inm/mllp_transport_specification.PDF
