(ns com.nervestaple.clinical.message-intermediate.segment.nte
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "NTE")

;; Record representing an NTE segment
(defrecord record
    [set-id
     comment-source
     comment
     comment-type])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::comment-source (s/nilable string?))
(s/def ::comment (s/nilable string?))
(s/def ::comment-type ::segment/spec-ce)

(s/def ::spec
  (s/keys :opt-un [::set-id ::comment-source ::comment ::comment-type]))

(defn record->hl7
  "Accepts a record of note (NTE) segment data and returns a parsed HL7 segment."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (:comment-source %))
     (parser/create-field (:comment %))
     (parser/create-field (segment/ce->field (:comment-type %))))
   record))

(defn hl7->record
  "Accepts an NTE segment of parsed HL7 data and returns an NTE record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 5
   #(map->record
     {:set-id (util/read-string
               (util/unwrap-and-first (message/get-segment-field % 1)))
      :comment-source (first (message/get-segment-field % 2))
      :comment (first (message/get-segment-field % 3))
      :comment-type (segment/field->ce (message/get-segment-field % 4))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
