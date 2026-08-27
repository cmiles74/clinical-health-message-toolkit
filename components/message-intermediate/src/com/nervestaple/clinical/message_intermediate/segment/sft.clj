(ns com.nervestaple.clinical.message-intermediate.segment.sft
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment Identifier
(def SEGMENT-ID "SFT")

;; Record representing SFT segment
(defrecord record
    [vendor
     release-number
     product
     identifier
     product-information
     install-date])

(s/def ::vendor ::segment/spec-xon)
(s/def ::release-number string?)
(s/def ::product string?)
(s/def ::identifier string?)
(s/def ::product-information (s/nilable string?))
(s/def ::install-date (s/nilable ::segment/spec-ts))

(s/def ::spec
  (s/keys :req-un [::vendor ::release-number ::product ::identifier]
          :opt-un [::product-information ::install-date]))

(defn record->hl7
  "Accepts a record of software data and returns a map of SFT segment data."
  [record]
  (message/record-to-hl7
    SEGMENT-ID
    #(vector
       (parser/create-field (util/trim-nils (segment/xon->field (:vendor %))))
       (parser/create-field (:release-number %))
       (parser/create-field (:product %))
       (parser/create-field (:identifier %))
       (parser/create-field (:product-information %))
       (parser/create-field (util/trim-nils (segment/ts->field (:install-date %)))))
    record))

(defn hl7->record
  "Accepts a segment of parsed SFT data and returns an SFT record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 7
   #(map->record
     {:vendor (segment/field->xon (message/get-segment-field % 1))
      :release-number (util/read-string (util/unwrap-and-first
                                        (message/get-segment-field % 2)))
      :product (util/read-string (util/unwrap-and-first
                                 (message/get-segment-field % 3)))
      :identifier (util/read-string (util/unwrap-and-first
                                    (message/get-segment-field % 4)))
      :product-information (util/read-string (util/unwrap-and-first
                                             (message/get-segment-field % 5)))
      :install-date (segment/field->ts (message/get-segment-field % 6))})
   segment))
