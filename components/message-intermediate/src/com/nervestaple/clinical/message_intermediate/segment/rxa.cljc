(ns com.nervestaple.clinical.message-intermediate.segment.rxa
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "RXA")

(def action-code-data
  [{:keys ["Add" "Insert" ::add] :code "A"}
   {:keys ["Delete" "Cancel" ::delete] :code "D"}
   {:keys ["Update" ::update] :code "U"}
   {:keys ["No change" "Nothing" ::none] :code "X"}])

(defn string->action-code
  "Returns the RXA action code that matches the provided keyword or string or nil
  if no match was found."
  [code-key]
  (lookups/value-for-key action-code-data nil code-key))

(defn action-code->string
  "Returns the textual string for the provided RXA action code, or nil if no match
  was found."
  [code-key]
  (lookups/value-for-key action-code-data nil code-key))

;; LA2: location with address, variation 2
(defrecord la2
    [point-of-care room bed facility location-status location-type building floor
     street-address other-designation city state postal-code country address-type
     other-geographic-designation])

(s/def ::point-of-care (s/nilable string?))
(s/def ::room (s/nilable string?))
(s/def ::bed (s/nilable string?))
(s/def ::facility (s/nilable ::segment/spec-hd))
(s/def ::location-status (s/nilable string?))
(s/def ::building (s/nilable string?))
(s/def ::floor (s/nilable string?))
(s/def ::street-address (s/nilable string?))
(s/def ::other-designation (s/nilable string?))
(s/def ::city (s/nilable string?))
(s/def ::state (s/nilable string?))
(s/def ::postal-code (s/nilable string?))
(s/def ::country (s/nilable string?))
(s/def ::address-type (s/nilable string?))
(s/def ::other-geo-designation (s/nilable string?))

(s/def ::spec-la2
  (s/keys :opt-un [::point-of-care ::room ::bed ::facility ::location-status
                   ::building ::floor ::street-address ::other-designation
                   ::city ::state ::postal-code ::country ::address-type
                   ::other-geo-designation]))

(s/fdef la2->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-la2))
              :coll (s/cat :coll (s/coll-of ::spec-la2)))
  :ret (s/nilable vector?))

(defn la2->field
  "Accepts one or a sequence of LA2 records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:point-of-care %)
            (:room %)
            (:bed %)
            (util/trim-nils (segment/hd->field (:facility %)))
            (:location-status %)
            (:building %)
            (:floor %)
            (:street-address %)
            (:other-designation %)
            (:city %)
            (:state %)
            (:postal-code %)
            (:country %)
            (:address-type %)
            (:other-geo-designation %))))

(s/fdef field->la2
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-la2))
             :record (s/nilable ::spec-la2)))

(defn field->la2
  "Accepts an HL7 v2 field of data and returns a single record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->la2
   #(array-map :point-of-care (util/get-or-nil % 0)
               :room (util/get-or-nil % 1)
               :bed (util/get-or-nil % 2)
               :facility (segment/field->hd (util/get-or-nil % 3))
               :location-status (util/get-or-nil % 4)
               :building (util/get-or-nil % 5)
               :floor (util/get-or-nil % 6)
               :street-address (util/get-or-nil % 7)
               :other-designation (util/get-or-nil % 8)
               :city (util/get-or-nil % 9)
               :state (util/get-or-nil % 10)
               :postal-code (util/get-or-nil % 11)
               :country (util/get-or-nil % 12)
               :address-type (util/get-or-nil % 13)
               :other-geo-designation (util/get-or-nil % 14))))

;; Record representing an RXA segment
(defrecord record
    [rxg-id-counter
     admin-id-counter
     admin-start
     admin-end
     administered-code
     administered-amount
     administered-units
     administered-dosage-form
     admin-notes
     admin-provider
     admin-location
     administered-time-unit
     administered-strength
     administered-strength-unit
     lot-number
     lot-expiration
     lot-manufacturer
     treatment-refusal-reason
     indication
     completion-status
     action-code
     entry-date
     administered-strength-volume
     administered-strength-volume-unit
     administered-barcode-id
     pharmacy-order-type])

(s/def ::rxg-id-counter number?)
(s/def ::admin-id-counter number?)
(s/def ::admin-start ::segment/spec-ts)
(s/def ::admin-end ::segment/spec-ts)
(s/def ::administered-code ::segment/spec-ce)
(s/def ::administered-amount number?)
(s/def ::administered-units (s/nilable ::segment/spec-ce))
(s/def ::administered-dosage-form (s/nilable ::segment/spec-ce))
(s/def ::admin-notes (s/nilable ::segment/spec-ce))
(s/def ::admin-provider (s/nilable ::segment/spec-xcn))
(s/def ::admin-location (s/nilable ::spec-la2))
(s/def ::administered-time-unit (s/nilable string?))
(s/def ::administered-strength (s/nilable number?))
(s/def ::administered-strength-unit (s/nilable ::segment/spec-ce))
(s/def ::lot-number (s/nilable string?))
(s/def ::lot-expiration (s/nilable ::segment/spec-ts))
(s/def ::lot-manufacturer (s/nilable ::segment/spec-ce))
(s/def ::treatment-refusal-reason (s/nilable ::segment/spec-ce))
(s/def ::indication (s/nilable ::segment/spec-ce))
(s/def ::completion-status (s/nilable string?))
(s/def ::action-code (s/nilable string?))
(s/def ::entry-date (s/nilable ::segment/spec-ts))
(s/def ::administered-strength-volume (s/nilable number?))
(s/def ::administered-strength-volume-unit (s/nilable ::segment/spec-cwe))
(s/def ::administered-barcode-id (s/nilable ::segment/spec-cwe))
(s/def ::pharmacy-order-type (s/nilable string?))

(s/def ::spec
  (s/keys :req-un [::rxg-id-counter ::admin-id-counter ::admin-start ::admin-end
                   ::administered-code ::administered-amount]
          :opt-un [::administered-units ::administered-dosage-form ::admin-notes
                   ::admin-provider ::admin-location ::administered-time-unit
                   ::administered-strength ::administered-strength-unit ::lot-number
                   ::lot-expiration ::lot-manufacturer ::treatment-refusal-reason
                   ::indication ::completion-status ::action-code ::entry-date
                   ::administered-strength-volume ::administered-strength-volume-unit
                   ::administered-barcode-id ::pharmacy-order-type]))

(defn record->hl7
  "Accepts a record of treatment administration data and returns a record of RXA
  segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:rxg-id-counter %))
     (parser/create-field (:admin-id-counter %))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:admin-start %))))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:admin-end %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:administered-code %))))
     (parser/create-field (:administered-amount %))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:administered-units %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:administered-dosage-form %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:admin-notes %))))
     (parser/create-field (util/trim-nils
                           (segment/xcn->field (:admin-provider %))))
     (parser/create-field (util/trim-nils
                           (la2->field (:admin-location %))))
     (parser/create-field (:administered-time-unit %))
     (parser/create-field (:administered-strength %))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:administered-strength-unit %))))
     (parser/create-field (:lot-number %))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:lot-expiration %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:lot-manufacturer %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:treatment-refusal-reason %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:indication %))))
     (parser/create-field (:completion-status %))
     (parser/create-field (:action-code %))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:entry-date %))))
     (parser/create-field (:administered-strength-volume %))
     (parser/create-field (util/trim-nils
                           (segment/cwe->field (:administered-strength-volume-unit %))))
     (parser/create-field (util/trim-nils
                           (segment/cwe->field (:administered-barcode-id %))))
     (parser/create-field (:pharmacy-order-type %)))
   record))

(defn hl7->record
  "Accepts an RXA segment of parsed HL7 v2 data and returns an RXA record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 27
   #(map->record
     {:rxg-id-counter (util/read-string
                       (util/unwrap-and-first (message/get-segment-field % 1)))
      :admin-id-counter (util/read-string
                         (util/unwrap-and-first (message/get-segment-field % 2)))
      :admin-start (segment/field->ts (message/get-segment-field % 3))
      :admin-end (segment/field->ts (message/get-segment-field % 4))
      :administered-code (segment/field->ce (message/get-segment-field % 5))
      :administered-amount (util/read-string
                            (util/unwrap-and-first (message/get-segment-field % 6)))
      :administered-units (segment/field->ce (message/get-segment-field % 7))
      :administered-dosage-form (segment/field->ce (message/get-segment-field % 8))
      :admin-notes (segment/field->ce (message/get-segment-field % 9))
      :admin-provider (segment/field->xcn (message/get-segment-field % 10))
      :admin-location (field->la2 (message/get-segment-field % 11))
      :administered-time-unit (util/unwrap-and-first (message/get-segment-field % 12))
      :administered-strength (util/read-string
                              (util/unwrap-and-first (message/get-segment-field % 13)))
      :administered-strength-unit (segment/field->ce (message/get-segment-field % 14))
      :lot-number (message/get-segment-field % 15)
      :lot-expiration (segment/field->ts (message/get-segment-field % 16))
      :lot-manufacturer (segment/field->ce (message/get-segment-field % 17))
      :treatment-refusal-reason (segment/field->ce (message/get-segment-field % 18))
      :indication (segment/field->ce (message/get-segment-field % 19))
      :completion-status (util/unwrap-and-first (message/get-segment-field % 20))
      :action-code (util/unwrap-and-first (message/get-segment-field % 21))
      :entry-date (segment/field->ts (message/get-segment-field % 22))
      :administered-strength-volume (util/unwrap-and-first (message/get-segment-field % 23))
      :administered-strength-volume-unit (segment/field->cwe (message/get-segment-field % 24))
      :administered-barcode-id (segment/field->cwe (message/get-segment-field % 25))
      :pharmacy-order-type (util/unwrap-and-first (message/get-segment-field % 26))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
