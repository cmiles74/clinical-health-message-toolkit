(ns com.nervestaple.clinical.message-intermediate.segment.obx
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]))

;; Segment identifier
(def SEGMENT-ID "OBX")

;; Record representing an OBX segment
(defrecord record
    [set-id
     value-type
     observation-identifier
     observation-sub-identifier
     observation-value
     units
     references-range
     abnormal-flags
     probability
     nature-of-abnormal-test
     observation-result-status
     reference-range-effective-date
     user-defined-access-checks
     observation-date
     producer-identifier
     responsible-observer
     observation-method
     equipment-instance-identifier
     analysis-date])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::value-type (s/nilable string?))
(s/def ::observation-identifier ::segment/spec-ce)
(s/def ::observation-sub-identifier (s/nilable string?))
(s/def ::observation-value ::segment/spec-ce)
(s/def ::units (s/nilable ::segment/spec-ce))
(s/def ::references-range (s/nilable string?))
(s/def ::abnormal-flags (s/nilable string?))
(s/def ::probability (s/nilable number?))
(s/def ::nature-of-abnormal-test (s/nilable string?))
(s/def ::observation-result-status string?)
(s/def ::reference-range-effective-date (s/nilable ::segment/spec-ts))
(s/def ::user-defined-access-checks (s/nilable string?))
(s/def ::observation-date (s/nilable ::segment/spec-ts))
(s/def ::producer-identifier (s/nilable ::segment/spec-ce))
(s/def ::responsible-observer (s/nilable ::segment/spec-xcn))
(s/def ::observation-method (s/nilable ::segment/spec-ce))
(s/def ::equipment-instance-identifier (s/nilable ::segment/spec-ei))
(s/def ::analysis-date (s/nilable ::segment/spec-ts))

(s/def ::spec
  (s/keys :req-un [::observation-identifier ::observation-result-status]
          :opt-un [::set-id ::value-type ::observation-sub-identifier
                   ::observation-value ::units ::references-range
                   ::abnormal-flags ::probability ::nature-of-abnormal-test
                   ::reference-range-effective-date ::user-defined-access-checks
                   ::observation-date ::producer-identifier
                   ::responsible-observer ::observation-method
                   ::equipment-instance-identifier ::analysis-date]))

(defn record->hl7
  "Accepts a record of observation/result data and returns map of OBR segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (:value-type %))
     (parser/create-field (util/trim-nils (segment/ce->field (:observation-identifier %))))
     (parser/create-field (:observation-sub-identifier %))
     (parser/create-field (util/trim-nils (segment/ce->field (:observation-value %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:units %))))
     (parser/create-field (:references-range %))
     (parser/create-field (:abnormal-flags %))
     (parser/create-field (:probability %))
     (parser/create-field (:nature-of-abnormal-test %))
     (parser/create-field (:observation-result-status %))
     (parser/create-field (util/trim-nils (segment/ts->field (:reference-range-effective-date %))))
     (parser/create-field (:user-defined-access-checks %))
     (parser/create-field (util/trim-nils (segment/ts->field (:observation-date %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:producer-identifier %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:responsible-observer %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:observation-method %))))
     (parser/create-field (util/trim-nils (segment/ei->field (:equipment-instance-identifier %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:analysis-date %)))))
   record))

(defn hl7->record
  "Accepts an OBX segment of parsed HL7 segment data and returns an OBX record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 20
   #(map->record
     {:set-id (util/read-string
               (util/unwrap-and-first (message/get-segment-field % 1)))
      :value-type (util/unwrap-and-first (message/get-segment-field % 2))
      :observation-identifier (segment/field->ce (message/get-segment-field % 3))
      :observation-sub-identifier (util/unwrap-and-first (message/get-segment-field % 4))
      :observation-value (segment/field->ce (message/get-segment-field % 5))
      :units (segment/field->ce (message/get-segment-field % 6))
      :references-range (util/unwrap-and-first (message/get-segment-field % 7))
      :abnormal-flags (util/unwrap-field (message/get-segment-field % 8))
      :probability (util/read-string
                    (util/unwrap-and-first (message/get-segment-field % 9)))
      :nature-of-abnormal-test (util/unwrap-field (message/get-segment-field % 10))
      :observation-result-status (util/unwrap-and-first (message/get-segment-field % 11))
      :reference-range-effective-date (segment/field->ts (message/get-segment-field % 12))
      :user-defined-access-checks (util/unwrap-and-first (message/get-segment-field % 13))
      :observation-date (segment/field->ts (message/get-segment-field % 14))
      :producer-identifier (segment/field->ce (message/get-segment-field % 15))
      :responsible-observer (segment/field->xcn (message/get-segment-field % 16))
      :observation-method (segment/field->ce (message/get-segment-field % 17))
      :equipment-instance-identifier (segment/field->ei (message/get-segment-field % 18))
      :analysis-date (segment/field->ts (message/get-segment-field % 19))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
