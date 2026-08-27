(ns com.nervestaple.clinical.message-intermediate.message.admit-visit
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as hl7-parser]
   [com.nervestaple.hl7-parser.message :as message]
   [luminare.hl7.parser.message.defaults :as defaults]
   [luminare.hl7.parser.segment.msh :as msh]
   [luminare.hl7.parser.segment.evn :as evn]
   [luminare.hl7.parser.segment.pid :as pid]
   [luminare.hl7.parser.segment.pd1 :as pd1]
   [luminare.hl7.parser.segment.rol :as rol]
   [luminare.hl7.parser.segment.nk1 :as nk1]
   [luminare.hl7.parser.segment.pv1 :as pv1]
   [luminare.hl7.parser.segment.in1 :as in1]
   [luminare.hl7.parser.segment.obx :as obx]))

;; insurance information
(defrecord insurance
    [in1])

(s/def ::in1 ::in1/spec)

(s/def ::spec-insurance
  (s/keys :req-un [::in1]))

;; patient administration, admit/visit notification
(defrecord record
    [msh evn pid pd1 nk1 pv1 obx insurance])

(s/def ::msh ::msh/spec)
(s/def ::evn ::evn/spec)
(s/def ::pid ::pid/spec)
(s/def ::pd1 ::pd1/spec)
(s/def ::rol ::rol/spec)
(s/def ::nk1 (s/coll-of ::nk1/spec))
(s/def ::pv1 ::pv1/spec)
(s/def ::obx (s/coll-of ::obx/spec))
(s/def ::insurance (s/coll-of ::spec-insurance))

(s/def ::spec
  (s/keys :req-un [::msh ::evn ::pid ::pv1 ::in1]
          :opt-un [::pd1 ::rol ::nk1 ::obx ::insurance]))

(defn insurance->hl7
  "Accepts an admit visit record and returns a vector with all of the insurance
  segments as parsed HL7 data."
  [record]

  ;; accumulate the insurance segments
  (loop [segments []
         item-this (first (:insurance record))
         items (rest (:insurance record))]

    ;; create segments from the child insurance records
    (if item-this
      (recur (-> (conj segments
                       (in1/record->hl7 (:in1 item-this))))
             (first items)
             (rest items))

      segments)))

(defn record->hl7
  "Accepts an admit visit record and returns a parsed HL7 message."
  ([record]
   (record->hl7 defaults/hl7-message-delimiters record))
  ([delimiters record]
   (apply (partial hl7-parser/create-message delimiters)
          (concat [(msh/record->hl7 (:msh record))
                   (evn/record->hl7 (:evn record))
                   (pid/record->hl7 (:pid record))
                   (pd1/record->hl7 (:pd1 record))]
                  (mapv nk1/record->hl7 (:nk1 record))
                  [(pv1/record->hl7 (:pv1 record))]
                  (mapv obx/record->hl7 (:obx record))
                  (insurance->hl7 record)))))

(defn hl7->insurance-records
  [parsed-message]
  (let [ins-segments (filter #(or (= in1/SEGMENT-ID (:id %))
                              (= in1/SEGMENT-ID (:id %)))
                         (:segments parsed-message))]
    (loop [groups {}
           segment-this (first ins-segments)
           segments (rest ins-segments)]
      (if segment-this
        (let [set-id (first (message/get-segment-field segment-this 1))
              segment-id (:id segment-this)
              result (if (groups set-id) (groups set-id) {})]
          (recur (assoc groups
                        set-id
                        (cond (= in1/SEGMENT-ID segment-id)
                              (assoc result :in1 (in1/hl7->record segment-this))))
                 (first segments)
                 (rest segments)))
        (mapv map->insurance groups)))))

(defn hl7->record
  "Accepts a parsed HL7 message and returns an admit visit record."
  [parsed-message]

  ;; pull out our repeating segments
  (let [insurance-recs (hl7->insurance-records parsed-message)
        nk1-segments (filter #(= nk1/SEGMENT-ID (:id %))
                             (:segments parsed-message))
        obx-segments (filter #(= obx/SEGMENT-ID (:id %))
                             (:segments parsed-message))
        role-segments (filter #(= rol/SEGMENT-ID (:id %))
                             (:segments parsed-message))]
    (map->record
     {:msh (msh/hl7->record
            (first (message/get-segments parsed-message msh/SEGMENT-ID)))
      :evn (evn/hl7->record
            (first (message/get-segments parsed-message evn/SEGMENT-ID)))
      :pid (pid/hl7->record
            (first (message/get-segments parsed-message pid/SEGMENT-ID)))
      :pd1 (pd1/hl7->record
            (first (message/get-segments parsed-message pd1/SEGMENT-ID)))
      :rol role-segments
      :nk1 nk1-segments
      :pv1 (pv1/hl7->record
            (first (message/get-segments parsed-message pv1/SEGMENT-ID)))
      :obx obx-segments
      :insurance insurance-recs})))

