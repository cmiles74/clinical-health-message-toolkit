(ns com.nervestaple.clinical.message-intermediate.segment.msa
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "MSA")

;; Record representing an MSA segment
(defrecord record
    [acknowledgement-code
     message-control-id
     message
     expected-sequence-number
     delayed-acknowledgement-type
     error-condition
     message-waiting-number
     message-waiting-priority])

(s/def ::acknowledgement-code (s/nilable string?))
(s/def ::message-control-id (s/and (s/nilable string?) #(>= 199 (count %))))
(s/def ::message (s/and (s/nilable string?) #(>= 80 (count %))))
(s/def ::expected-sequence-number (s/nilable number?))
(s/def ::delayed-acknowledgement-type (s/nilable string?))
(s/def ::error-condition (s/nilable ::segment/spec-ce))
(s/def ::message-waiting-number (s/nilable number?))
(s/def ::message-waiting-priority (s/nilable string?))

(s/def ::spec
  (s/keys :req-un [::acknowledgement-code ::message-control-id]
          :opt-un [::message ::expected-sequence-number
                   ::delayed-acknowledgement-type ::error-condition
                   ::message-waiting-number ::message-waiting-priority]))

(defn record->hl7
  "Accepts a record of message acknowledgement (MSA) segment data and returns an
  MSA record."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (lookups/acknowledgement-code-by-key (:acknowledgement-code %)))
     (parser/create-field (:message-control-id %))
     (parser/create-field (:message %))
     (parser/create-field (:expected-sequence-number %))
     (parser/create-field (:delayed-acknowledgement-type %))
     (parser/create-field (util/trim-nils (segment/ce->field (:error-condition %))))
     (parser/create-field (:message-waiting-number %))
     (parser/create-field (:message-waiting-priority %)))
   record))

(defn hl7->record
  "Accpets an MSA segment of parsed HL7 data and returns a message acknowledgement
  record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 9
   #(map->record
     {:acknowledgement-code (lookups/acknowledgement-code-by-value
                             (first (message/get-segment-field % 1)))
      :message-control-id (first (message/get-segment-field % 2))
      :message (first (message/get-segment-field % 3))
      :expected-sequence-number (util/read-string (first (message/get-segment-field % 4)))
      :delayed-acknowledgement-type (first (message/get-segment-field % 5))
      :error-condition (segment/field->ce (message/get-segment-field % 6))
      :message-waiting-number (util/read-string (first (message/get-segment-field % 7)))
      :message-waiting-priority (first (message/get-segment-field % 8))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
