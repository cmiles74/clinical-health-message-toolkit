(ns com.nervestaple.clinical.message-intermediate.segment.rol
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "ROL")

;; Record representing a ROL segment
(defrecord record
    [role-instance-id
     action-code
     role
     role-person
     role-begin-date
     role-end-date
     role-duration
     role-action-reason
     role-provider-type
     organization-unit-type
     role-address
     role-telephone])

(s/def ::role-instance-id (s/nilable ::segment/spec-ei))
(s/def ::action-code (s/nilable string?))
(s/def ::role (s/nilable ::segment/spec-ce))
(s/def ::role-person (s/nilable ::segment/spec-xcn))
(s/def ::role-begin-date (s/nilable ::segment/spec-ts))
(s/def ::role-end-date (s/nilable ::segment/spec-ts))
(s/def ::role-duration (s/nilable ::segment/spec-ce))
(s/def ::role-action-reason (s/nilable ::segment/spec-ce))
(s/def ::role-provider-type (s/nilable ::segment/spec-ce))
(s/def ::organization-unit-type (s/nilable ::segment/spec-ce))
(s/def ::role-address (s/nilable ::segment/spec-xad))
(s/def ::role-telephone (s/nilable ::segment/spec-xtn))

(s/def ::spec
  (s/keys :opt-un [::role-instance-id ::action-code ::role ::role-person
                   ::role-begin-date ::role-end-date ::role-duration
                   ::role-action-reason ::role-provider-type
                   ::organization-unit-type ::role-address ::role-telephone]))

(defn record->hl7
  "Accepts a record of role data and returns a map of ROL segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (segment/ei->field (:role-instance-id %)))
     (parser/create-field (:action-code %))
     (parser/create-field (segment/ce->field (:role %)))
     (parser/create-field (segment/xcn->field (:role-person %)))
     (parser/create-field (segment/ts->field (:role-begin-date %)))
     (parser/create-field (segment/ts->field (:reol-end-date %)))
     (parser/create-field (segment/ce->field (:role-duration %)))
     (parser/create-field (segment/ce->field (:role-action-reason %)))
     (parser/create-field (segment/ce->field (:role-provider-type %)))
     (parser/create-field (segment/ce->field (:organization-unit-type %)))
     (parser/create-field (segment/xad->field (:role-address %)))
     (parser/create-field (segment/xtn->field (:role-telephone %))))
   record))

(defn hl7->record
  "Accepts a ROL segment of parsed HL7 segment data and returns a ROL record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 13
   #(map->record
     {:role-instance-id (segment/field->ce (message/get-segment-field % 1))
      :action-code (util/read-string (util/unwrap-and-first (message/get-segment-field % 2)))
      :role (segment/field->ce (message/get-segment-field % 3))
      :role-person (segment/field->xcn (message/get-segment-field % 4))
      :role-begin-date (segment/field->ts (message/get-segment-field % 5))
      :role-end-date (segment/field->ts (message/get-segment-field % 6))
      :role-duration (segment/field->ce (message/get-segment-field % 7))
      :role-action-reason (segment/field->ce (message/get-segment-field % 8))
      :role-provider-type (segment/field->ce (message/get-segment-field % 9))
      :organization-unit-type (segment/field->ce (message/get-segment-field % 10))
      :role-address (segment/field->xad (message/get-segment-field % 11))
      :role-telephone (segment/field->xtn (message/get-segment-field % 12))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))

