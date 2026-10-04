(ns com.nervestaple.clinical.message-intermediate.interface.core
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.defaults :as defaults]
   [com.nervestaple.clinical.message-intermediate.message.message :as core]
   [com.nervestaple.clinical.message-intermediate.message.dump :as dump]))

;; segment specs
(s/def ::message ::core/spec)

(def default-delimiters
  "Default set of HL7 v2 message delimiters"
  defaults/hl7-message-delimiters)

(defn hl7->record
  "Reads HL7 data from the provided source (a Reader, String, etc.), parses that
  message and returns a message record with the same data. Data segments that
  are part of a set are stored in on the `:sets` key (in order), segments that
  cannot be parsed are stored on the `:remainder` key."
  [message]
  (core/hl7->record (parser/parse message)))

(defn record->hl7
  "Accepts a message record and returns a String with a HL7 v2 message."
  [record]
  (parser/str-message (core/record->hl7 record)))

(defn get-segments
  "Returns all of the segments (those that are not part of a set) with the
   matching segment ID from the provided message record."
  [record segment-id]
  (core/get-segments record segment-id))

(defn get-set
  "Returns the set of segment data that is at the provided index in the given
   message record."
  [record set-index]
  (core/get-set record set-index))

(defn get-set-segment
  "Returns all of the segments from the set at the provided index that match
   the given segment id in the message record."
  [record set-index segment-id]
  (core/get-set-segment record set-index segment-id))

(defn hl7->parsed-message
  "Reads data from the provided source (a Reader, String, etc.) and parses that
  data into a map that represents the content of the message."
  [message-source]
  (parser/parse message-source))

(defn parsed-message->record
  "Accepts a map of parsed message data and returns a message record with the same
  data. Data segments that are part of a set are stored in on the `:sets`
  key (in order), segments that cannot be parsed are stored on the `:remainder`
  key."
  [parsed-message]
  (core/hl7->record parsed-message))

(defn record->parsed-message
  "Accepts a message record and returns a parsed HL7 v2 message."
  [record]
  (core/record->hl7 record))

(defn dump
  "Prints a human readable version of the message record to standard out."
  [record]
  (dump/message record))
