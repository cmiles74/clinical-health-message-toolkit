(ns com.nervestaple.clinical.mllp.interface
  (:require
   [com.nervestaple.clinical.mllp.core :as mllp]))

(defn read-stream
  "Accepts an input stream and, optionally, a handler function that takes one
  argument, the message content. This function will block while it reads
  messages wrapped in Minimum Low Level Protocol (MLLP) from the input stream,
  unwraps them and provides the message to the provided handler function (if
  provided) and then waits for the next message. If no handler function is
  provided them this function blocks until a message is read and then returns
  that message. An exception is thrown on incomplete messages."
  ([input-stream]
   (mllp/read-stream input-stream))
  ([input-stream handler-fn]
   (mllp/read-stream input-stream handler-fn)))

(defn write-stream
  "Accepts an output stream and a string with the message to be written. The
  message will be wrapped in a Minimum Low Level Protocol (MLLP) envelope and
  then written to the output stream."
  [output-stream message]
  (mllp/write-stream output-stream message))
