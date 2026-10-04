(ns com.nervestaple.clinical.message-intermediate.segment.rxc
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "RXC")

;; Record representing an RXC segment
(defrecord record
  [rx-component-type
   component-code
   component-amount
   component-units
   component-strength
   component-strength-units
   supplementary-code
   component-drug-strength-volume
   component-drug-strength-volume-units])

(s/def ::rx-component-type string?) ;; A for Additive or B for Base
(s/def ::component-code ::segment/spec-cwe)
(s/def ::component-amount number?)
(s/def ::component-units ::segment/spec-cwe)
(s/def ::component-strength (s/nilable number?))
(s/def ::component-strength-units (s/nilable ::segment/spec-cwe))
(s/def ::supplementary-code (s/nilable ::segment/spec-cwe))
(s/def ::component-drug-strength-volume (s/nilable number?))
(s/def ::component-drug-strength-volume-units (s/nilable ::segment/spec-cwe))

(s/def ::spec
  (s/keys :req-un [::rx-component-type ::component-code
                   ::component-amount ::component-units]
          :opt-un [::component-strength ::component-strength-units
                   ::supplementary-code ::component-drug-strength-volume
                   ::component-drug-strength-volume-units]))

(defn record->hl7
  "Accepts a record of RXC data and returns a map of RXC segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:rx-component-type %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:component-code %))))
     (parser/create-field (:component-amount %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:component-units %))))
     (parser/create-field (:component-strength %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:component-strength-units %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:supplementary-code %))))
     (parser/create-field (:component-drug-strength-volume %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:component-drug-strength-volume-units %)))))
   record))

(defn hl7->record
  "Accepts a RXC segment of parsed HL7 segment data and returns a RXC record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID
   10
   #(map->record
     {:rx-component-type                    (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :component-code                       (segment/field->cwe (message/get-segment-field % 2))
      :component-amount                     (util/read-string (util/unwrap-and-first (message/get-segment-field % 3)))
      :component-units                      (segment/field->cwe (message/get-segment-field % 4))
      :component-strength                   (util/read-string (util/unwrap-and-first (message/get-segment-field % 5)))
      :component-strength-units             (segment/field->cwe (message/get-segment-field % 6))
      :supplementary-code                   (segment/field->cwe (message/get-segment-field % 7))
      :component-drug-strength-volume       (util/read-string (util/unwrap-and-first (message/get-segment-field % 8)))
      :component-drug-strength-volume-units (segment/field->cwe (message/get-segment-field % 9))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
