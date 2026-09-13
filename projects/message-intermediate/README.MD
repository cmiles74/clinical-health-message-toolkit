# Clinical Health Message Intermediate Format

This project provides our clinical health message interchange format. Functions are provided to transform [HL7v2 Messaging messages][hl7v2] messages into our intermediate format and then back again.

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.cmiles74/message-intermediate.svg)](https://clojars.org/org.clojars.cmiles74/message-intermediate)

[![cljdoc badge](https://cljdoc.org/badge/org.clojars.cmiles74/message-intermediate)](https://cljdoc.org/d/org.clojars.cmiles74/message-intermediate)

## Usage

First add the dependency to your project. If you are using Clojure `tools.deps`, you'll add something like the line below to your `deps.clj.` file. Remember to check the badge above or the Clojars website for the newest stable version. :wink:

    org.clojars.cmiles74/message-intermediate {:mvn/version "0.10"}

You can now open a REPL for your project and import the `core` namespace, that's where most of the good stuff lives.

```clojure
user> (require '[com.nervestaple.clinical.message-intermediate.interface.core :as core])
nil
```

Now we can parse an HL7 v2 message into our snazzy intermediate format. First let's get a test message together...

```clojure
user> (def message "MSH|^~\\&|AcmeHIS|StJohn|CATH|StJohn|20061019172719||ORM^O01|1789321394264|P|2.3\rPID|||20301||Durden^Tyler^^^Mr.||19700312|M|||88 Punchward Dr.^^Los Angeles^CA^11221^USA|||||||\rPV1||O|OP^^||||4652^Paulson^Robert|||OP|||||||||9|||||||||||||||||||||||||20061019172717|20061019172718\rORC|NW|20061019172719\rOBR|1|20061019172719||76770^Ultrasound: retroperitoneal^C4|||12349876\r")
```

Now we can parse that message into our intermediate format.

```clojure
user> (def r (core/hl7->record message))
2026-09-13T17:53:25.721Z maui WARN [com.nervestaple.clinical.message-intermediate.segment.utility:?] - Couldn't parse HL7 date/time "12349876": Conversion failed
#'user/r
```

The warning message indicates that there was a date that couldn't be parsed (I think you can see why). The warning let's us know that when we fetch that value it will not be a parsed date, the data will be intact. Now we can get an idea of what our data looks like.

```clojure
user> (keys r)
(:delimiters :segments :sets :remainder)
user> (count (:segments r))
5
user> (map #(:id %) (:segments r))
("MSH" "PID" "PV1" "ORC" "OBR")
```

Not bad! Let's take a look at that `PID` segment.

```clojure
user> (first (filter #(= "MSH" (:id %)) (:segments r)))
{:delimiters
 {:field 124, :component 94, :repeating 126, :escape 92, :subcomponent 38},
 :sending-application
 {:namespace-id "AcmeHIS", :universal-id nil, :universal-id-type nil},
 :sending-facility
 {:namespace-id "StJohn", :universal-id nil, :universal-id-type nil},
 :receiving-application
 {:namespace-id "CATH", :universal-id nil, :universal-id-type nil},
 :receiving-facility
 {:namespace-id "StJohn", :universal-id nil, :universal-id-type nil},
 :message-created
 {:time #object[java.time.LocalDateTime 0x1b3a7536 "2006-10-19T17:27:19"],
  :precision nil},
 :security nil,
 :message-type {:msg-code "ORM", :msg-trigger "O01", :msg-structure nil},
 :message-control-id "1789321394264",
 :message-processing-id {:processing-id "Production", :processing-mode nil},
 :message-version-id
 {:version-id "2.3", :international-code nil, :international-version-id nil},
 :sequence-number nil,
 :continuation-pointer nil,
 :accept-acknowledgement-type nil,
 :application-acknowledgement-type nil,
 :country-code nil,
 :character-set nil,
 :principal-language nil,
 :alternate-character-set nil,
 :message-profile-indicator nil,
 :sending-responsible-organization nil,
 :receiving-responsible-organization nil,
 :sending-network-address nil,
 :receiving-network-address nil,
 :id "MSH",
 :index 0}
```

As you can see, we have all of the data in the segment mapped out such that we can see what the data actually means. Lastly we can turn this record back into an HL7v2 Messaging message.

```clojure
user> (core/record->hl7 r)
"MSH|^~\\&|AcmeHIS|StJohn|CATH|StJohn|20061019172719||ORM^O01^|1789321394264|P|2.3|||||||||||||\rPID|||20301||Durden^Tyler^^^Mr.||19700312|M|||88 Punchward Dr.^^Los Angeles^CA^11221^USA^C|||||||||||||||||||||||||||\rPV1||O|OP||||4652^Paulson^Robert|||OP|||||||||9|||||||||||||||||||||||||20061019172717|20061019172718|||||||\rORC|NW|20061019172719|||||||||||||||||||||||||||||\rOBR|1|20061019172719||76770^Ultrasound: retroperitoneal^C4|||12349876||||||||||||||||||||||||||||||||||||||||\r"
```

While the output here is not _exactly_ the same as the message we started with, it is the same semantically. When we transform a record into an HL7v2 message we retain all of the empty values, you can see this in the example above as the trailing empty fields in the segments.

There's other handy stuff we can do, check out [the function documentation][cljdocs] for more details. Probably the niftiest thing is that now that we have the data in a Clojure `map`, we can use familiar tools to make changes to the record.

## Building

To build the library jar file for this project, navigate to the root directory of the project and invoke the build script like so...

    cd ../..
    clj -T:build jar :project message-intermediate

You will find the library in the target directory. :winking_face:

---
[hl7v2]: https://en.wikipedia.org/wiki/HL7#Version_2
[cljdocs]: https://cljdoc.org/d/org.clojars.cmiles74/message-intermediate
