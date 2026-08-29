(ns com.nervestaple.clinical.message-intermediate.interface.segment
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
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

(defn time->ts
  "Accepts a zonded date time or a local data time and returns a TS record."
  [date-time]
  (segment/time->ts date-time))

(defn now->ts
  "Returns a TS record with the current date and time."
  []
  (segment/now->ts))

(defn ts->field
  "Accepts one or a sequence of TS records and returns a value, collection or map
  of HL7 v2 data."
  [ts-record]
  (segment/ts->field ts-record))

(defn field->ts
  "Accepts an HL7 v2 field of TS data and returns a single TS record or a sequence
  of records or nil."
  [field]
  (segment/field->ts field))

