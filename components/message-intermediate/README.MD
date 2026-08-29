# Clinical Health Message Intermediate Format

This project provides a library that will parse an HL7 v2 message (parsed with
the [Clojure HL7 v2 message parser][hl7-parser]) into our intermediate format: a
record containing the data objects representing the various segments and sets of
segments in the message. Not every possible segment is currently represented,
anything this library isn't prepared to parse is stored in the output record
and, if we convert that record into an HL7 message, that data _will be
retained._

We would like to support a larger set of segments and message components and are
open to pull requests that will fill in missing pieces. We're also investigating
ways people might define their own segments to map out proprietary data (like
data stored in "Z" segments).

The records returned by the message parser can be serialized directly to HL7.
When you take an HL7 message, parse it into a record and then serialize that
back to HL7, the two HL7 messages will be _semantically_ identical. You may find
that some systems may omit some segment fields from the end of messages when
they are empty and others may not, the same with repeating fields. Some systems
may omit trailing components if they are empty, again some others may not. In
all cases this library will include empty fields in segments and fields and will
omit them from components.

Lastly, should you find the need, converting a record to JSON is trivial. `;-)`

## Usage

To use this library, require it in your project.

```clojure
(ns com.example.project
  (:require
   [com.nervestaple.clinical.message-intermediate.interface :as message]))

```

This will also pull in our HL7 v2 parser, we expose the `parse` method from that
library under `parse`.

### Reading Messages

Next we take an incoming HL7 v2 message, parse that and then transform _that_
result into a set of segments representing the message. First we generate a test message...

```clojure
(require '[com.nervestaple.hl7-parser.test :as parser-test])
(def test-message (parser-test/test-message))
```

Now we can transform that into our intermediate format, this includes named
attributes. It will be a bit easier to make out the data. `;-)`

```clojure
(def test-record (message/hl7->record test-message))
```

The message will be parsed into Clojure map that includes the delimiters and a
list of segments; this is the "intermediate format". Any segments that we don't
recognize will be placed under the `:remainder` key, any segment that has any
trailing data we don't recognize will also have this data stored in its
`:remainder` segment, in this way no data is lost. If we have a set of segments
grouped together by a "set ID", they will be available as a sequence under the
`:sets` key.

```clojure
user> (keys msg)
(:delimiters :segments :sets :remainder)
```

Our test message, like many messages you'll find out in the wild, has some
issue. It has a junk date set as the observation start date in its `OBR`
segment. You may have noticed the warning message at parsing time...

```
2026-08-23T23:18:55.392Z maui WARN [com.nervestaple...utility:194] - Couldn't parse HL7 date/time "12349876": Conversion failed
```

But no worries! We've stored this value as-is in case you are expecting it our can make some sense out of it on your own.

``` clojure
(-> (message/get-segments test-record "OBR") (first) :observation-start)
;; {:time "12349876", :precision nil}
```

We can also do other nifty stuff, like fetch all of the segments we might be interested in.

```clojure
(message/get-segments test-record "PID")
```

We get a lot of output, as you can see we have a `nil` in fields that lack a value.

```clojure
({:set-id nil,
  :patient-id nil,
  :patient-id-list
  {:identifier "20301",
   :check-digit nil,
   :check-scheme nil,
   :assigning-authority nil,
   :identifier-type-code nil,
   :assigning-facility nil,
   :effective-date nil,
   :expiration-date nil,
   :assigning-jurisdiction nil,
   :assigning-agency nil},
  :alternate-id nil,
  :patient-name
  {:family-name
   {:surname "Durden",
    :own-surname-prefix nil,
    :own-surname nil,
    :spouse-surname-prefix nil,
    :spouse-surname nil},
   :given-name "Tyler",
   :further-given-name nil,
   :suffix nil,
   :prefix "Mr.",
   :degree nil,
   :name-type-code nil,
   :name-representation-code nil,
   :name-context nil,
   :name-validity-range nil,
   :name-assembly-order nil},
  :mother-maiden-name nil,
  :date-of-birth
  {:time #object[java.time.LocalDate 0x5e33a438 "1970-03-12"], :precision nil},
  :admin-gender "Male"
  ...
})]})
```

We provide human-friendly output with the `dump` function, here we elide the `nil` values for more compact output.

```clojure
(message/dump test-record)
```

Will generate the following output:

```
Segments:
  MSH
     :delimiters <PersistentArrayMap>
      :field <Integer> 124
      :component <Integer> 94
      :repeating <Integer> 126
      :escape <Integer> 92
      :subcomponent <Integer> 38
     :sending-application <hd>
      :namespace-id <String> AcmeHIS
     :sending-facility <hd>
      :namespace-id <String> StJohn
     :receiving-application <hd>
      :namespace-id <String> CATH
     :receiving-facility <hd>
      :namespace-id <String> StJohn
     :message-created <ts>
      :time <LocalDateTime> 2006-10-19T17:27:19
     :message-type <msg>
      :msg-code <String> ORM
      :msg-trigger <String> O01
     :message-control-id <String> 1787527057264
     :message-processing-id <pt>
      :processing-id <String> Production
     :message-version-id <vid>
      :version-id <String> 2.3
     :id <String> MSH
     :index <Long> 0
   PID
     :patient-id-list <cx>
      :identifier <String> 20301
     :patient-name <xpn>
      :family-name <fn-name>
        :surname <String> Durden
      :given-name <String> Tyler
      :prefix <String> Mr.
     :date-of-birth <ts>
      :time <LocalDate> 1970-03-12
     :admin-gender <String> Male
     :address <xad>
      :street-1 <sad>
        :street-address <String> 88 Punchward Dr.
      :city <String> Los Angeles
      :state <String> CA
      :postal-code <String> 11221
      :country <String> USA
      :address-type <String> Current
     :index <Long> 1
     :id <String> PID
  ...
```

So much easier to read than the raw HL7 message!

### Writing Messages

All of the HL7 data types, segments and even messages have been specified as
much as possible to ensure that we create syntactically valid messages. While
this doesn't prevent gibberish messages it does give us some ability to catch
messages that have data of the wrong type in certain fields, as you saw above we emit a warning if we can't parse a date out of a field that is expected to contain a date.

You may use the [spec](https://clojure.org/about/spec) livrary to generate segments of
test data...

```clojure
(ns com.example.project
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.nervestaple.clinical.message-intermediate.interface :as message]))

(gen/generate (s/gen ::message/msh))
```

This will return a record filled with mostly gibberish data but which meets the
technical contract for the MSH segment. Messages may be created in the same
manner.

```clojure
(gen/generate (s/gen ::message/spec))
```

Of course, with a test message in hand you can output a test HL7 message.

```clojure
(message/record->hl7 (gen/generate (s/gen ::message/spec)))
```

Your output will be similar to the following.

```
MSH|^~\\&|I7EMC87H7Sa01KiZk1MG^871b3dv9TLNk^aEq2hR9VgqiCyM3c9V8ka6mvyWbIG0|7cjNU5e^C5cA69Qg47zOOecvL7^AQkXpuLqLfP6j38o2||||R1Prc4S|^^|KPiPguheNM88p|^iztD7Oyu7hLNnIt401a6Uc1mOP32||256|||||rHb6||||||OQefzkV7xKL3M68I|
PID||5vptGBJdf1bj984llvoHamyT^^Li^XOyi71340Yx2o^Jd015^^20020728143509-0400|1wF9IUmGn0913HK5i8YdY^1^^M4qKe&0^^^&S||^^X8GMArwRl3tcdZ7n8heH9FE8nIWT82^3Lj2bu8FvDZ1NiyDjeI3XBOdwg9^Q^5^^^q57vq863e1do01RZ88p619Z&9Br51qiep57cHw9dokbNpow7o&JkZHEQoYp8MQloZhsbHp&4cs7&8c5VwLZ4SFN0vJKCk4ht754&Xzgu4^20010923194643&20010816235831&S^QlXhu13RpnPqW||20140909123740^S|||||nNw|Zx5A937KP6zc8a51IJyB^^d80XYyEfv^S8VMEC3gseQq63WV^^10067^^1426^xjr2ezP4BGc017y||HHG5oEgXj5nZzhm|||lDd3uCRntFyn57Vk1Vj|||6M8BtJcs94Hq270ynbdVE1Mn^^^^^ig1WhA&OAdZl&LQzVysumNYOEtk12Fj6dL084^20071120185501+0000^19771129033201+0000&S|9Ll9ZrzE9NYK59fh89w2qX67tOd^8F6q^36piaq^bAF5ozy0Ys3GnEZbtPGD||||m9Md0S0fvXRFR5vVvOAvmE4M^^CBEDsUba4msnW^^^z0||||XT4urE|||||||hKWsQ7X27M|^5QJ7Db7GzmhSgLztbb69835^^N4Jcl54v2Fb8rP2DRN8voo07504DI
...
```

There are a lot of odd little data types, convenience methods are available
where appropriate to make this easier. For instance, instead of creating a
time-stamp (TS) record by hand you can create one from a zoned or local data and
time.


```clojure
(message/time->ts (time/zoned-date-time))
```

You can also create one with the current date and time.

```clojure
(message/now->ts)
```

This was originally a Leiningen project, we're working on converting it to a Polylith component so that we can manage the code in the same place as our other clinical health messaging tools. As we figure out nice ways to structure to message writing code we'll update this file.

## Development

This is a [Clojure][clojure] project that uses [Leiningen][lein] for project
management. Once you have those tools installed, please feel free to submit pull
requests to this project.

### Branching

At this time, all development should be done on individual feature branches and
then merged into the `main` branch when the feature is complete.

### Releasing

When we are ready for release, merge the `main` branch into the `release`
branch. Before pushing the `release` branch, change the version number in the
`project.clj` file. For instance, if the version number is `0.0.1-SNAPSHOT` you
will want to change it to `0.0.1` to indicate that we are publishing the final
version. With that done, push to `release`: the release version of the library
will be compiled and published.

Lastly, checkout the `main` branch and update the version number for the next
development cycle. Following along with the example above, we would change the
version number to `0.1.2-SNAPSHOT`.

----

[hl7-parser]: https://github.com/cmiles74/clojure-hl7-messaging-2-parser
[clojure]: https://clojure.org/
[lein]: https://github.com/technomancy/leiningen
[snapshot]: https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md#snapshot-versions
[gh-pkgs]: https://luminaremed.atlassian.net/wiki/spaces/DEV/pages/2006646785/Using+GitHub+Packages+with+Leiningen
