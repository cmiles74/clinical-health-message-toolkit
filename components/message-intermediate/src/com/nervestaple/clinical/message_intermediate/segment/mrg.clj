(ns com.nervestaple.clinical.message-intermediate.segment.mrg
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment Identifier
(def SEGMENT-ID "MRG")

;; Record representing the MRG segment
(defrecord record
    [prior-patient-id-list
     prior-alternate-id
     prior-patient-account-number
     prior-patient-id
     prior-visit-number
     prior-alternate-visit-id
     prior-patient-name])

(s/def ::prior-patient-id-list ::segment/spec-cx)
(s/def ::prior-alternate-id (s/nilable ::segment/spec-cx))
(s/def ::prior-patient-account-number (s/nilable ::segment/spec-cx))
(s/def ::prior-patient-id (s/nilable ::segment/spec-cx))
(s/def ::prior-visit-number (s/nilable ::segment/spec-cx))
(s/def ::prior-alternate-visit-id (s/nilable ::segment/spec-cx))
(s/def ::prior-patient-name (s/nilable ::segment/spec-xpn))

(s/def ::spec
  (s/keys :req-un [::prior-patient-id-list]
          :opt-un [::prior-alternate-id
                   ::prior-patient-account-number
                   ::prior-patient-id
                   ::prior-visit-number
                   ::prior-alternate-visit-id
                   ::prior-patient-name]))

(defn record->hl7
  "Accepts a record of merge data and returns a map of MRG segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-patient-id-list %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-alternate-id %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-patient-account-number %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-patient-id %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-visit-number %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:prior-alternate-visit-id %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:prior-patient-name %)))))
   record))

(defn hl7->record
  "Accepts an MRG segment of parsed HL7 segment data and returns an MRG record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 8
   #(map->record
     {:prior-patient-id-list (segment/field->cx (message/get-segment-field % 1))
      :prior-alternate-id (segment/field->cx (message/get-segment-field % 2))
      :prior-patient-account-number (segment/field->cx (message/get-segment-field % 3))
      :prior-patient-id (segment/field->cx (message/get-segment-field % 4))
      :prior-visit-number (segment/field->cx (message/get-segment-field % 5))
      :prior-alternate-visit-id (segment/field->cx (message/get-segment-field % 6))
      :prior-patient-name (segment/field->xpn (message/get-segment-field % 7))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
