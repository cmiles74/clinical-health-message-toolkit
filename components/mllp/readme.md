# Minimum Low Level Protocol (MLLP) Reader and Writer

This component provides functions for reading and writing messages that are
wrapped in the [Minimum Low Level Protocol (MLLP)][mllp]. It works with input
and output streams because it's rare to find MLLP used outside of network
messages. Note that this doesn't handle HLLP messages which are significantly
different.

Using these functions is easy! To write a message, simply provide your output
stream and your message.

```clojure
(ns awesome.project
  (:require [com.nervestaple.clinical.mllp.interface :as mllp]))

(mllp/write-stream out-stream "1970's tech is the best!")
```

The library will add the envelope and write the message to the output stream.

Your typical MLLP message producer will be sending more than one message (that's
the whole point of the MLLP wrapper after all). We provide the input stream and
a handler function to the `read-stream` function, as messages are read from the
stream they are passed to your handler function. If the sender writes any
garbled messages then an exception will be thrown.

```clojure
(mllp/read-stream in-stream
  #(println "New message: " %))
```

Because `read-stream` blocks, you probably want to start it in another thread.
:wink:

If you really need to read just one message (perhaps you're sending messages and
need to read your acknowledgement), we do provide a blocking read,
`blocking-read-stream`. It works as you would expect, it blocks until it
receives one entire message.

```clojure
(let [message (mllp/read-stream in-stream)]
  (println message))
```

----
[mllp]: https://www.hl7.org/documentcenter/public/wg/inm/mllp_transport_specification.PDF
