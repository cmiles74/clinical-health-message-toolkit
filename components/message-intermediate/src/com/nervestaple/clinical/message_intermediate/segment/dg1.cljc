(ns com.nervestaple.clinical.message-intermediate.segment.dg1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.common-fields :as common]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; segment identifier
(def SEGMENT-ID "DG1")

;; record representing DG1 segment
(defrecord record
    [set-id
     diagnosis-coding-method
     diagnosis
     diagnosis-description
     diagnosis-date
     diagnosis-type
     major-diagnostic-category
     diagnostic-related-group
     drg-approval
     drg-grouper-review
     outlier-type
     outlier-days
     outlier-cost
     grouper-version-and-type
     diagnosis-priority
     diagnosing-clinician
     diagnosis-classification
     confidential
     attestation-date
     diagnosis-identifier
     diagnosis-action
     parent-diagnosis
     drg-ccl-value
     drg-grouping-usage
     drg-diagnosis-determination-status
     present-on-admission])

(s/def ::set-id ::common/set-id)
(s/def ::diagnosis-coding-method (s/nilable string?))
(s/def ::diagnosis (s/nilable ::segment/spec-cwe))
(s/def ::diagnosis-description (s/nilable string?))
(s/def ::diagnosis-date (s/nilable ::segment/spec-ts))
(s/def ::diagnosis-type (s/nilable ::segment/spec-cwe))
(s/def ::major-diagnostic-category (s/nilable ::segment/spec-cne))
(s/def ::diagnostic-related-group (s/nilable ::segment/spec-cne))
(s/def ::drg-approval (s/nilable string?))
(s/def ::drg-grouper-review (s/nilable ::segment/spec-cwe))
(s/def ::outlier-type (s/nilable ::segment/spec-cwe))
(s/def ::outlier-days (s/nilable number?))
(s/def ::outlier-cost (s/nilable ::segment/spec-cp))
(s/def ::grouper-version-and-type (s/nilable string?))
(s/def ::diagnosis-priority (s/nilable number?))
(s/def ::diagnosing-clinician (s/nilable ::segment/spec-xcn))
(s/def ::diagnosis-classification (s/nilable ::segment/spec-cwe))
(s/def ::confidential (s/nilable string?))
(s/def ::attestation-date (s/nilable ::segment/spec-ts))
(s/def ::diagnosis-identifier (s/nilable ::segment/spec-ei))
(s/def ::diagnosis-action (s/nilable string?))
(s/def ::parent-diagnosis (s/nilable ::segment/spec-ei))
(s/def ::drg-ccl-value (s/nilable ::segment/spec-cwe))
(s/def ::drg-grouping-usage (s/nilable string?))
(s/def ::drg-diagnosis-determination-status (s/nilable ::segment/spec-cwe))
(s/def ::present-on-admission (s/nilable ::segment/spec-cwe))

(s/def ::spec
  (s/keys :req-un [::set-id ::diagnosis ::diagnosis-type]
          :opt-un [::diagnosis-coding-method
                   ::diagnosis-description
                   ::diagnosis-date
                   ::major-diagnostic-category
                   ::diagnostic-related-group
                   ::drg-approval
                   ::drg-grouper-review
                   ::outlier-type
                   ::outlier-days
                   ::outlier-cost
                   ::grouper-version-and-type
                   ::diagnosis-priority
                   ::diagnosing-clinician
                   ::diagnosis-classification
                   ::confidential
                   ::attestation-date
                   ::diagnosis-identifier
                   ::diagnosis-action
                   ::parent-diagnosis
                   ::drg-ccl-value
                   ::drg-grouping-usage
                   ::drg-diagnosis-determination-status
                   ::present-on-admission]))

(defn record->hl7
  "Accepts a record of diagnosis data and returns a map of DG1 segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (:diagnosis-coding-method %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:diagnosis %))))
     (parser/create-field (:diagnosis-description %))
     (parser/create-field (util/trim-nils (segment/ts->field (:diagnosis-date %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:diagnosis-type %))))
     (parser/create-field (util/trim-nils (segment/cne->field (:major-diagnostic-category %))))
     (parser/create-field (util/trim-nils (segment/cne->field (:diagnostic-related-group %))))
     (parser/create-field (:drg-approval %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-grouper-review %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:outlier-type %))))
     (parser/create-field (:outlier-days %))
     (parser/create-field (util/trim-nils (segment/cp->field (:outlier-cost %))))
     (parser/create-field (:grouper-version-and-type %))
     (parser/create-field (:diagnosis-priority %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:diagnosing-clinician %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:diagnosis-classification %))))
     (parser/create-field (:confidential %))
     (parser/create-field (util/trim-nils (segment/ts->field (:attestation-date %))))
     (parser/create-field (util/trim-nils (segment/ei->field (:diagnosis-identifier %))))
     (parser/create-field (:diagnosis-action %))
     (parser/create-field (util/trim-nils (segment/ei->field (:parent-diagnosis %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-ccl-value %))))
     (parser/create-field (:drg-grouping-usage %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-diagnosis-determination-status %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:present-on-admission %)))))
   record))

(defn hl7->record
  "Accepts a DG1 segment of parsed HL7 segment data and returns a DG1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 27
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :diagnosis-coding-method (util/unwrap-and-first (message/get-segment-field % 2))
      :diagnosis (segment/field->cwe (message/get-segment-field % 3))
      :diagnosis-description (util/unwrap-and-first (message/get-segment-field % 4))
      :diagnosis-date (segment/field->ts (message/get-segment-field % 5))
      :diagnosis-type (segment/field->cwe (message/get-segment-field % 6))
      :major-diagnostic-category (segment/field->cne (message/get-segment-field % 7))
      :diagnostic-related-group (segment/field->cne (message/get-segment-field % 8))
      :drg-approval (util/unwrap-and-first (message/get-segment-field % 9))
      :drg-grouper-review (segment/field->cwe (message/get-segment-field % 10))
      :outlier-type (segment/field->cwe (message/get-segment-field % 11))
      :outlier-days (util/read-string (util/unwrap-and-first (message/get-segment-field % 12)))
      :outlier-cost (segment/field->cp (message/get-segment-field % 13))
      :grouper-version-and-type (util/unwrap-and-first (message/get-segment-field % 14))
      :diagnosis-priority (util/read-string (util/unwrap-and-first (message/get-segment-field % 15)))
      :diagnosing-clinician (segment/field->xcn (message/get-segment-field % 16))
      :diagnosis-classification (segment/field->cwe (message/get-segment-field % 17))
      :confidential (util/unwrap-and-first (message/get-segment-field % 18))
      :attestation-date (segment/field->ts (message/get-segment-field % 19))
      :diagnosis-identifier (segment/field->ei (message/get-segment-field % 20))
      :diagnosis-action (util/unwrap-and-first (message/get-segment-field % 21))
      :parent-diagnosis (segment/field->ei (message/get-segment-field % 22))
      :drg-ccl-value (segment/field->cwe (message/get-segment-field % 23))
      :drg-grouping-usage (util/unwrap-and-first (message/get-segment-field % 24))
      :drg-diagnosis-determination-status (segment/field->cwe (message/get-segment-field % 25))
      :present-on-admission (segment/field->cwe (message/get-segment-field % 26))})

   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
    (merge {:id SEGMENT-ID}
           data-map)))
