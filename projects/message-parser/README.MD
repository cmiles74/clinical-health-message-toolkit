# Clinical Health Message Parser

This  project provides out clinical health message ([HL7 Messaging v2][hl7v2]) parser. Functions are provided to parser these messages into a Clojure structure, alter them as needed and to serialize them back to messages.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.cmiles74/message-parser.svg)](https://clojars.org/org.clojars.cmiles74/message-parser)

[![cljdoc badge](https://cljdoc.org/d/org.clojars.cmiles74/message-parser/1.0/api/com.nervestaple.clinical.message-parser.interface)](https://cljdoc.org/badge/org.clojars.cmiles74/message-parser)

## Usage

First add the dependency to your project. If you are using Clojure `tools.deps`, you'll add something like the line below to your `deps.clj.` file. Remember to check the badge above or the Clojars website for the newest stable version.

    org.clojars.cmiles74/message-parser {:mvn/version "1.0"}

You can now open a REPL for your project and import the `core` namespace, that's where most of the good stuff lives.

```clojure
user> (require '[com.nervestaple.clinical.message-parser.interface.core :as core])
nil
```

Now you can parse and dump an HL7 Messaging v2 message...

```
user> (def parsed-message (message/parse message))
{:delimiters {:field 124, :component 94, :subcomponent 38,
              :repeating 126, :escape 92},
 :segments [{:id "MSH", :fields [{:content "^~\\&"}
            {:content ["AcmeHIS"]} {:content ["StJohn"]}
            {:content ["CATH"]} {:content["StJohn"]}...]}
  ...
```

There's other handy stuff we can do, check out [the function documentation][cljdocs] for more details.

## Building

To build the library jar file for this project, navigate to the root directory of the project and invoke the build script like so...

    cd ../..
    clj -T:build jar :project message-parser

You will find the library in the target directory.

---
[hl7v2]: https://en.wikipedia.org/wiki/HL7#Version_2
[cljdocs]: https://cljdoc.org/d/org.clojars.cmiles74/message-parser/1.0/api/com.nervestaple.clinical.message-parser.interface
