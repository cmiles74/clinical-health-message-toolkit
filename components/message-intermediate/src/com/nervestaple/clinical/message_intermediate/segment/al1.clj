(ns com.nervestaple.clinical.message-intermediate.segment.al1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; segment identifier
(def SEGMENT-ID "AL1")

(defrecord record
    [set-id
     allergen-type
     allergen-description
     allergy-severity
     allergy-reaction
     identification-date])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::allergen-type (s/nilable ::segment/spec-cwe))
(s/def ::allergen-description (s/nilable ::segment/spec-cwe))
(s/def ::allergy-severity (s/nilable ::segment/spec-cwe))
(s/def ::allergy-reaction (s/nilable string?))
(s/def ::identification-date (s/nilable ::segment/spec-ts))

(s/def ::spec
  (s/keys :opt-un [::set-id ::allergen-type ::allergen-description
                   ::allergy-severity ::allergy-reaction
                   ::identification-date]))

(defn record->hl7
  "Accepts a record of patient allergy data and returns a map of AL1 segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:allergen-type %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:allergen-description %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:allergy-severity %))))
     (parser/create-field (:allregy-reaction %))
     (parser/create-field (segment/ts->field (:identification-date %))))
   record))

(defn hl7->record
  "Accepts an AL1 segment of parsed HL7 data and returns an AL1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 7
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :allergen-type (segment/field->cwe (message/get-segment-field % 2))
      :allergen-description (segment/field->cwe (message/get-segment-field % 3))
      :allergy-severity (segment/field->cwe (message/get-segment-field % 4))
      :allergy-reaction (util/unwrap-and-first (message/get-segment-field % 5))
      :identification-date (segment/field->ts (message/get-segment-field % 6))})
   segment))
