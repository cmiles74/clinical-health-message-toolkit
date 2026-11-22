(ns com.nervestaple.clinical.mllp.core
  (:require
   [com.nervestaple.clinical.log.interface :as log])
  (:import
   [java.nio.charset StandardCharsets]))

;; ASCII codes of characters used to delimit and wrap messages
(def ASCII_VT (int 11))    ;; start block
(def ASCII_FS (int 28))    ;; end block
(def ASCII_CR (int 13))    ;; carriage return required after each end block

(defn read-stream
  ([input-stream] (read-stream input-stream nil))
  ([input-stream handler-fn]

   ;; loop to read in each character from the client, accumulating the message
   (loop [int-in (.read input-stream)                                                   ;; read the next character
          parsing :start-block                                                          ;; parsing starts with the start block
          message (StringBuilder.)]                                                     ;; accumulate characters in a buffer
     (cond
       (= parsing :start-block)                                                         ;; parse out MLLP start block
       (cond (= ASCII_VT int-in)                                                        ;; we have a start block
             (recur (.read input-stream) :message message)                              ;; start parsing the message content

             (= -1 int-in)                                                              ;; stream closed, bail out
             (log/debug "Input stream was closed")

             :else                                                                      ;; not a start block, bail out
             (throw (Exception. (str "Start block was expected but wasn't present, "
                                     "read: " int-in))))

       (= parsing :message)                                                             ;; parse out MLLP message content
       (cond (= -1 int-in)
             (throw (Exception. "Expected end block but found end of stream"))          ;; stream closed!

             (not= ASCII_FS int-in)                                                     ;; check for the end block
             (recur (.read input-stream) :message (.append message (char int-in)))      ;; not the end block, accumulate content

             :else
             (recur (.read input-stream) :carriage-return message))                     ;; end block, start parsing for closing char

       (and (= parsing :carriage-return) handler-fn)                                    ;; parse out closing carriage return w/handler
       (if (= ASCII_CR int-in)                                                          ;; we have the CR, end of the message
         (let [message-out (str message)]
           (handler-fn message-out)                                                     ;; call the handler function with message
           (recur (.read input-stream) :start-block (StringBuilder.)))                  ;; start parsing out the start block
         (throw (Exception. (str "Expected carriage return after end block but "        ;; no carriage return, bail out
                                 "read: " int-in))))

       (and (= parsing :carriage-return) (not handler-fn))                              ;; parse out closing carriage return, no handler
       (if (= ASCII_CR int-in)                                                          ;; we have the CR, end of the message
         (str message)                                                                  ;; return the read message and exit
         (throw (Exception. (str "Expected carriage return after end block but "        ;; no carriage return, bail out
                                 "read: " int-in))))

       (= -1 int-in)                                                                    ;; stream closed, bail out
       (cond (= parsing :start-block)
             (throw (Exception. "Expected start block but found end of stream"))

             (= parsing :carriage-return)
             (throw (Exception. (str "Expected carriage return after end block "
                                     "but found end of stream"))))))))

(defn write-stream
  [output-stream message]
  (let [bytes-out (.getBytes message StandardCharsets/ISO_8859_1)]
    (.write output-stream ASCII_VT)
    (.write output-stream bytes-out)
    (.write output-stream ASCII_FS)
    (.write output-stream ASCII_CR)
    (.flush output-stream)))
