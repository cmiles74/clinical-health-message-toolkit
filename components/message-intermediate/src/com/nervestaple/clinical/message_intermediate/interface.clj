(ns com.nervestaple.clinical.message-intermediate.interface
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.defaults :as defaults]
   [com.nervestaple.clinical.message-intermediate.message :as core]
   [com.nervestaple.clinical.message-intermediate.message.dump :as dump]
   [com.nervestaple.clinical.message-intermediate.segment.acc :as acc]
   [com.nervestaple.clinical.message-intermediate.segment.al1 :as al1]
   [com.nervestaple.clinical.message-intermediate.segment.dg1 :as dg1]
   [com.nervestaple.clinical.message-intermediate.segment.drg :as drg]
   [com.nervestaple.clinical.message-intermediate.segment.err :as err]
   [com.nervestaple.clinical.message-intermediate.segment.evn :as evn]
   [com.nervestaple.clinical.message-intermediate.segment.gt1 :as gt1]
   [com.nervestaple.clinical.message-intermediate.segment.in1 :as in1]
   [com.nervestaple.clinical.message-intermediate.segment.mrg :as mrg]
   [com.nervestaple.clinical.message-intermediate.segment.msa :as msa]
   [com.nervestaple.clinical.message-intermediate.segment.msh :as msh]
   [com.nervestaple.clinical.message-intermediate.segment.nk1 :as nk1]
   [com.nervestaple.clinical.message-intermediate.segment.nte :as nte]
   [com.nervestaple.clinical.message-intermediate.segment.obr :as obr]
   [com.nervestaple.clinical.message-intermediate.segment.obx :as obx]
   [com.nervestaple.clinical.message-intermediate.segment.orc :as orc]
   [com.nervestaple.clinical.message-intermediate.segment.pd1 :as pd1]
   [com.nervestaple.clinical.message-intermediate.segment.pid :as pid]
   [com.nervestaple.clinical.message-intermediate.segment.pv1 :as pv1]
   [com.nervestaple.clinical.message-intermediate.segment.pv2 :as pv2]
   [com.nervestaple.clinical.message-intermediate.segment.rol :as rol]
   [com.nervestaple.clinical.message-intermediate.segment.rxa :as rxa]
   [com.nervestaple.clinical.message-intermediate.segment.rxc :as rxc]
   [com.nervestaple.clinical.message-intermediate.segment.rxe :as rxe]
   [com.nervestaple.clinical.message-intermediate.segment.rxr :as rxr]
   [com.nervestaple.clinical.message-intermediate.segment.sft :as sft]))

;; segment specs
(s/def ::message ::core/spec)
(s/def ::acc ::acc/spec)
(s/def ::al1 ::al1/spec)
(s/def ::dg1 ::dg1/spec)
(s/def ::drg ::drg/spec)
(s/def ::err ::err/spec)
(s/def ::evn ::evn/spec)
(s/def ::gt1 ::gt1/spec)
(s/def ::in1 ::in1/spec)
(s/def ::mrg ::mrg/spec)
(s/def ::msa ::msa/spec)
(s/def ::msh ::msh/spec)
(s/def ::nk1 ::nk1/spec)
(s/def ::nte ::nte/spec)
(s/def ::obr ::obr/spec)
(s/def ::obx ::obx/spec)
(s/def ::orc ::orc/spec)
(s/def ::pd1 ::pd1/spec)
(s/def ::pid ::pid/spec)
(s/def ::pv1 ::pv1/spec)
(s/def ::pv2 ::pv2/spec)
(s/def ::rol ::rol/spec)
(s/def ::rxa ::rxa/spec)
(s/def ::rxc ::rxc/spec)
(s/def ::rxe ::rxe/spec)
(s/def ::rxr ::rxr/spec)
(s/def ::sft ::sft/spec)

(def default-delimiters defaults/hl7-message-delimiters)

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

(defn hl7->record
  "Reads HL7 data from the provided source (a Reader, String, etc.), parses that
  message and returns a message record with the same data. Data segments that
  are part of a set are stored in on the `:sets` key (in order), segments that
  cannot be parsed are stored on the `:remainder` key."
  [message]
  (core/hl7->record (parser/parse message)))

(defn record->parsed-message
  "Accepts a message record and returns a parsed HL7 v2 message."
  [record]
  (core/record->hl7 record))

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

(defn dump
  "Prints a human readable version of the message record to standard out."
  [record]
  (dump/message record))
