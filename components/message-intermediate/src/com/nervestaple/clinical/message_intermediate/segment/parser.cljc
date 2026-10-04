(ns com.nervestaple.clinical.message-intermediate.segment.parser
  (:require
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
   [com.nervestaple.clinical.message-intermediate.segment.pid :as pid]
   [com.nervestaple.clinical.message-intermediate.segment.pd1 :as pd1]
   [com.nervestaple.clinical.message-intermediate.segment.pv1 :as pv1]
   [com.nervestaple.clinical.message-intermediate.segment.pv2 :as pv2]
   [com.nervestaple.clinical.message-intermediate.segment.rol :as rol]
   [com.nervestaple.clinical.message-intermediate.segment.rxa :as rxa]
   [com.nervestaple.clinical.message-intermediate.segment.rxc :as rxc]
   [com.nervestaple.clinical.message-intermediate.segment.rxe :as rxe]
   [com.nervestaple.clinical.message-intermediate.segment.rxr :as rxr]
   [com.nervestaple.clinical.message-intermediate.segment.sft :as sft]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]))

;;
;; Functions for turning parsed segments into records and records into parsed
;; segments of data
;;

(def segment-to-parser-map
  "Map of segment identifiers to functions that will parse HL7 v2 segment data."
  {acc/SEGMENT-ID acc/hl7->record
   al1/SEGMENT-ID al1/hl7->record
   dg1/SEGMENT-ID dg1/hl7->record
   drg/SEGMENT-ID drg/hl7->record
   err/SEGMENT-ID err/hl7->record
   evn/SEGMENT-ID evn/hl7->record
   gt1/SEGMENT-ID gt1/hl7->record
   in1/SEGMENT-ID in1/hl7->record
   mrg/SEGMENT-ID mrg/hl7->record
   msa/SEGMENT-ID msa/hl7->record
   msh/SEGMENT-ID msh/hl7->record
   nk1/SEGMENT-ID nk1/hl7->record
   nte/SEGMENT-ID nte/hl7->record
   obr/SEGMENT-ID obr/hl7->record
   orc/SEGMENT-ID orc/hl7->record
   obx/SEGMENT-ID obx/hl7->record
   pid/SEGMENT-ID pid/hl7->record
   pd1/SEGMENT-ID pd1/hl7->record
   pv1/SEGMENT-ID pv1/hl7->record
   pv2/SEGMENT-ID pv2/hl7->record
   rol/SEGMENT-ID rol/hl7->record
   rxa/SEGMENT-ID rxa/hl7->record
   rxc/SEGMENT-ID rxc/hl7->record
   rxe/SEGMENT-ID rxe/hl7->record
   rxr/SEGMENT-ID rxr/hl7->record
   sft/SEGMENT-ID sft/hl7->record})

(def record-to-hl7-map
  "Map of segment record classes to functions that will convert them to HL7 v2
  segment data."
  {acc/SEGMENT-ID acc/record->hl7
   al1/SEGMENT-ID al1/record->hl7
   dg1/SEGMENT-ID dg1/record->hl7
   drg/SEGMENT-ID drg/record->hl7
   err/SEGMENT-ID err/record->hl7
   evn/SEGMENT-ID evn/record->hl7
   gt1/SEGMENT-ID gt1/record->hl7
   in1/SEGMENT-ID in1/record->hl7
   mrg/SEGMENT-ID mrg/record->hl7
   msa/SEGMENT-ID msa/record->hl7
   msh/SEGMENT-ID msh/record->hl7
   nk1/SEGMENT-ID nk1/record->hl7
   nte/SEGMENT-ID nte/record->hl7
   obr/SEGMENT-ID obr/record->hl7
   orc/SEGMENT-ID orc/record->hl7
   obx/SEGMENT-ID obx/record->hl7
   pid/SEGMENT-ID pid/record->hl7
   pd1/SEGMENT-ID pd1/record->hl7
   pv1/SEGMENT-ID pv1/record->hl7
   pv2/SEGMENT-ID pv2/record->hl7
   rol/SEGMENT-ID rol/record->hl7
   rxa/SEGMENT-ID rxa/record->hl7
   rxc/SEGMENT-ID rxc/record->hl7
   rxe/SEGMENT-ID rxe/record->hl7
   rxr/SEGMENT-ID rxr/record->hl7
   sft/SEGMENT-ID sft/record->hl7})

(def map-to-record-map
  {acc/SEGMENT-ID acc/map->record
   al1/SEGMENT-ID al1/map->record
   dg1/SEGMENT-ID dg1/map->record
   drg/SEGMENT-ID drg/map->record
   err/SEGMENT-ID err/map->record
   evn/SEGMENT-ID evn/map->record
   gt1/SEGMENT-ID gt1/map->record
   in1/SEGMENT-ID in1/map->record
   mrg/SEGMENT-ID mrg/map->record
   msa/SEGMENT-ID msa/map->record
   msh/SEGMENT-ID msh/map->record
   nk1/SEGMENT-ID nk1/map->record
   nte/SEGMENT-ID nte/map->record
   obr/SEGMENT-ID obr/map->record
   orc/SEGMENT-ID orc/map->record
   obx/SEGMENT-ID obx/map->record
   pid/SEGMENT-ID pid/map->record
   pd1/SEGMENT-ID pd1/map->record
   pv1/SEGMENT-ID pv1/map->record
   pv2/SEGMENT-ID pv2/map->record
   rol/SEGMENT-ID rol/map->record
   rxa/SEGMENT-ID rxa/map->record
   rxc/SEGMENT-ID rxc/map->record
   rxe/SEGMENT-ID rxe/map->record
   rxr/SEGMENT-ID rxr/map->record
   sft/SEGMENT-ID sft/map->record})

(defn hl7->record
  "Accepts a segment of HL7 v2 data and an optional set of HL7 v2 delimeters (if
  none are provide the default set is used). Returns a segment record
  representing the provided segment data."
  [segment & [delimiters]]
  (let [parser-fn (segment-to-parser-map (:id segment))]
    (when parser-fn
      (if (= msh/SEGMENT-ID (:id segment))
        (parser-fn segment delimiters)
        (parser-fn segment)))))

(defn record->hl7
  "Accepts a record of segment data and returns an HL7 v2 data segment."
  [record]
  (let [hl7-fn (record-to-hl7-map (:id record))]
    (when hl7-fn (hl7-fn record))))

(defn map->record
  "Accepts an HL7 segment identifier and a map of segment data, returns the appropriate segment record
  with that data."
  [segment-id segment-map]
  (let [map-to-rec-fn (map-to-record-map segment-id)]
    (when map-to-rec-fn (map-to-rec-fn (merge {:id segment-id} segment-map)))))

(s/def ::acc
  (s/with-gen ::acc/spec
    #(gen/fmap (partial map->record "ACC") (s/gen ::acc/spec))))

(s/def ::al1
  (s/with-gen ::al1/spec
    #(gen/fmap (partial map->record "AL1") (s/gen ::al1/spec))))

(s/def ::dg1
  (s/with-gen ::dg1/spec
    #(gen/fmap (partial map->record "DG1") (s/gen ::dg1/spec))))

(s/def ::err
  (s/with-gen ::err/spec
    #(gen/fmap (partial map->record "ERR") (s/gen ::err/spec))))

(s/def ::evn
  (s/with-gen ::evn/spec
    #(gen/fmap (partial map->record "EVN") (s/gen ::evn/spec))))

(s/def ::gt1
  (s/with-gen ::gt1/spec
    #(gen/fmap (partial map->record "GT1") (s/gen ::gt1/spec))))

(s/def ::in1
  (s/with-gen ::in1/spec
    #(gen/fmap (partial map->record "IN1") (s/gen ::in1/spec))))

(s/def ::mrg
  (s/with-gen ::mrg/spec
    #(gen/fmap (partial map->record "MRG") (s/gen ::mrg/spec))))

(s/def ::msh
  (s/with-gen ::msh/spec
    #(gen/fmap (partial map->record "MSH") (s/gen ::msh/spec))))

(s/def ::msa
  (s/with-gen ::msa/spec
    #(gen/fmap (partial map->record "MSA") (s/gen ::msa/spec))))

(s/def ::nk1
  (s/with-gen ::nk1/spec
    #(gen/fmap (partial map->record "NK1") (s/gen ::nk1/spec))))

(s/def ::nte
  (s/with-gen ::nte/spec
    #(gen/fmap (partial map->record "NTE") (s/gen ::nte/spec))))

(s/def ::obr
  (s/with-gen ::obr/spec
    #(gen/fmap (partial map->record "OBR") (s/gen ::obr/spec))))

(s/def ::obx
  (s/with-gen ::obx/spec
    #(gen/fmap (partial map->record "OBX") (s/gen ::obx/spec))))

(s/def ::orc
  (s/with-gen ::orc/spec
    #(gen/fmap (partial map->record "ORC") (s/gen ::orc/spec))))

(s/def ::pid
  (s/with-gen ::pid/spec
    #(gen/fmap (partial map->record "PID") (s/gen ::pid/spec))))

(s/def ::pd1
  (s/with-gen ::pd1/spec
    #(gen/fmap (partial map->record "PD1") (s/gen ::pd1/spec))))

(s/def ::pv1
  (s/with-gen ::pv1/spec
    #(gen/fmap (partial map->record "PV1") (s/gen ::pv1/spec))))

(s/def ::pv2
  (s/with-gen ::pv2/spec
    #(gen/fmap (partial map->record "PV2") (s/gen ::pv2/spec))))

(s/def ::rol
  (s/with-gen ::rol/spec
    #(gen/fmap (partial map->record "ROL") (s/gen ::rol/spec))))

(s/def ::rxa
  (s/with-gen ::rxa/spec
    #(gen/fmap (partial map->record "RXA") (s/gen ::rxa/spec))))

(s/def ::rxc
  (s/with-gen ::rxc/spec
              #(gen/fmap (partial map->record "RXC") (s/gen ::rxc/spec))))

(s/def ::rxe
  (s/with-gen ::rxe/spec
              #(gen/fmap (partial map->record "RXE") (s/gen ::rxe/spec))))

(s/def ::rxr
  (s/with-gen ::rxr/spec
    #(gen/fmap (partial map->record "RXR") (s/gen ::rxr/spec))))

(s/def ::sft
  (s/with-gen ::sft/spec
    #(gen/fmap (partial map->record "SFT") (s/gen ::sft/spec))))

(s/def ::drg
  (s/with-gen ::drg/spec
    #(gen/fmap (partial map->record "DRG") (s/gen ::drg/spec))))

(s/def ::other-segment
  (s/or :acc ::acc :al1 ::al1 :dg1 ::dg1 :drg ::drg :err ::err :evn ::evn
        :gt1 ::gt1 :in1 ::in1 :mrg ::mrg :msa ::msa :obr ::obr :obx ::obx
        :orc ::orc :pid ::pid :pd1 ::pd1 :pv1 ::pv1 :pv2 ::pv2 :rol ::rol
        :rxa ::rxa :rxc ::rxc :rxe ::rxe :rxr ::rxr :sft ::sft))

(s/def ::set-segment
  (s/or :al1 ::al1 :dg1 ::dg1 :err ::err :gt1 ::gt1 :in1 ::in1 :msa ::msa
        :nk1 ::nk1 :nte ::nte :obr ::obr :obx ::obx :pid ::pid :pv1 ::pv1))

(s/def ::segments
  (s/cat :msh ::msh :other (s/+ ::other-segment)))

(s/def ::segment-set
  (s/with-gen (s/coll-of (s/and #(:set-id %) ::set-segment))
    #(gen/fmap (fn [segments]
                 (let [index (rand-int 10)]
                   (map (fn [segment]
                          (assoc segment :set-id index))
                        segments)))
               (s/gen (s/coll-of ::set-segment)))))

