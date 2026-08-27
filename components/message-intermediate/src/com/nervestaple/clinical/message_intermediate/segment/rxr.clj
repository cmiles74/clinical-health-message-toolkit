(ns com.nervestaple.clinical.message-intermediate.segment.rxr
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "RXR")

;; Record representing an RXR segment
(defrecord record
    [route
     admin-site
     admin-device
     admin-method
     routing-instruction])

(s/def ::route ::segment/spec-ce)
(s/def ::admin-site (s/nilable ::segment/spec-ce))
(s/def ::admin-device (s/nilable ::segment/spec-ce))
(s/def ::admin-method (s/nilable ::segment/spec-ce))
(s/def ::routing-instruction (s/nilable ::segment/spec-ce))

(s/def ::spec
  (s/keys :req-un [::route]
          :opt-un [::admin-site ::admin-device ::admin-method
                   ::routing-instruction]))

(defn record->hl7
  "Accepts a record of treatment route data and returns a record of RXR segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:route %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:admin-site %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:admin-device %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:admin-method %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:routing-instruction %)))))
   record))

(defn hl7->record
  "Accepts an RXR segment of parsed HL7 v2 data and returns an RCR record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 6
   #(map->record
     {:route (segment/field->ce (message/get-segment-field % 1))
      :admin-site (segment/field->ce (message/get-segment-field % 2))
      :admin-device (segment/field->ce (message/get-segment-field % 3))
      :admin-method (segment/field->ce (message/get-segment-field % 4))
      :routing-instruction (segment/field->ce (message/get-segment-field % 5))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
