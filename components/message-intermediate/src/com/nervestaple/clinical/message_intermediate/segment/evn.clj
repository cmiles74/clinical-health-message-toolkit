(ns com.nervestaple.clinical.message-intermediate.segment.evn
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; segment identifier
(def SEGMENT-ID "EVN")

(def event-reason-data
  "Sequence of event reason code values (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Patient Request" ::event-reason-patient] :code "01"}
   {:keys ["Physician Order" ::event-reason-physician] :code "02"}
   {:keys ["Census Management" ::event-reason-census] :code "03"}])

(defn string->event-reason
  "Returns the matching code value for the provided event reason textual string or
  nil if the provided value has no matches."
  [key-string]
  (lookups/value-for-key event-reason-data nil key-string))

(defn event-reason->string
  "Returns the matching textual string for the provided event reason code or nil
  if the provided value has no matches."
  [key-vector]
  (lookups/key-by-value event-reason-data nil key-vector))

(defrecord record
    [event-type-code
     recorded-date
     planned-event-date
     reason-code
     operator-id
     event-occurred-date
     event-facility])

(s/def ::event-type-code (s/nilable string?))
(s/def ::recorded-date (s/nilable ::segment/spec-ts))
(s/def ::planned-event-date (s/nilable ::segment/spec-ts))
(s/def ::reason-code (s/nilable string?))
(s/def ::operator-id (s/nilable ::segment/spec-xcn))
(s/def ::event-occurred-date (s/nilable ::segment/spec-ts))
(s/def ::event-facility (s/nilable ::segment/spec-hd))

(s/def ::spec
  (s/keys :req-un [::recorded-date]
          :opt-un [::event-type-code ::planned-event-date ::reason-code
                   ::operator-id ::event-occurred-date ::event-facility]))

(defn record->hl7
  "Accepts a record of event type (EVN) data and returns a map of EVN segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:event-type-code %))
     (parser/create-field (util/trim-nils (segment/ts->field (:recorded-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:planned-event-date %))))
     (parser/create-field (:reason-code %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:operator-id %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:event-occurred-date %))))
     (parser/create-field (util/trim-nils (segment/hd->field (:event-facility %)))))
   record))

(defn hl7->record
  "Accepts a segment of parsed HL7 segment data and returns an EVN record."
  [segment]
  (message/hl7-to-record
    SEGMENT-ID 8
    #(map->record
       {:event-type-code (util/read-string
                           (util/unwrap-and-first (message/get-segment-field % 1)))
        :recorded-date (segment/field->ts (message/get-segment-field % 2))
        :planned-event-date (segment/field->ts (message/get-segment-field % 3))
        :reason-code (util/read-string
                       (util/unwrap-and-first (message/get-segment-field % 4)))
        :operator-id (segment/field->xcn (message/get-segment-field % 5))
        :event-occurred-date (segment/field->ts (message/get-segment-field % 6))
        :event-facility (segment/field->hd (message/get-segment-field % 7))})
    segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
    (merge {:id SEGMENT-ID}
           data-map)))
