(ns com.nervestaple.clinical.message-intermediate.segment.drg
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; segment identifier
(def SEGMENT-ID "DRG")

(defrecord record
    [diagnosis-related-group
     drg-assigned-date
     drg-approval-indicator
     drg-grouper-review-code
     outlier-type
     outlier-days
     outlier-cost
     drg-payor
     outlier-reimbursement
     confidential-indicator
     drg-transfer-type
     coder-name
     grouper-status
     pccl-value-code
     effective-weight
     monetary-amount
     status-patient
     grouper-software-name
     grouper-software-version
     status-financial-calculation
     relative-discount
     basic-charge
     total-charge
     discount
     calculated-days
     status-gender
     status-age
     status-length-of-stay
     status-same-day-flag
     status-separation-mode
     status-weight-birth
     status-respiration-minutes
     status-admission])

(s/def ::diagnosis-related-group (s/nilable ::segment/spec-cne))
(s/def ::drg-assigned-date (s/nilable ::segment/spec-ts))
(s/def ::drg-approval-indicator (s/nilable string?))
(s/def ::drg-grouper-review-code (s/nilable ::segment/spec-cwe))
(s/def ::outlier-type (s/nilable ::segment/spec-cwe))
(s/def ::outlier-days (s/nilable number?))
(s/def ::outlier-cost (s/nilable ::segment/spec-cp))
(s/def ::drg-payor (s/nilable ::segment/spec-cwe))
(s/def ::outlier-reimbursement (s/nilable ::segment/spec-cp))
(s/def ::confidential-indicator (s/nilable string?))
(s/def ::drg-transfer-type (s/nilable ::segment/spec-cwe))
(s/def ::coder-name (s/nilable ::segment/spec-xpn))
(s/def ::grouper-status (s/nilable ::segment/spec-cwe))
(s/def ::pccl-value-code (s/nilable ::segment/spec-cwe))
(s/def ::effective-weight (s/nilable number?))
(s/def ::monetary-amount (s/nilable ::segment/spec-mo))
(s/def ::status-patient (s/nilable ::segment/spec-cwe))
(s/def ::grouper-software-name (s/nilable string?))
(s/def ::grouper-software-version (s/nilable string?))
(s/def ::status-financial-calculation (s/nilable ::segment/spec-cwe))
(s/def ::relative-discount (s/nilable ::segment/spec-mo))
(s/def ::basic-charge (s/nilable ::segment/spec-mo))
(s/def ::total-charge (s/nilable ::segment/spec-mo))
(s/def ::discount (s/nilable ::segment/spec-mo))
(s/def ::calculated-days (s/nilable number?))
(s/def ::status-gender (s/nilable ::segment/spec-cwe))
(s/def ::status-age (s/nilable ::segment/spec-cwe))
(s/def ::status-length-of-stay (s/nilable ::segment/spec-cwe))
(s/def ::status-same-day-flag (s/nilable ::segment/spec-cwe))
(s/def ::status-separation-mode (s/nilable ::segment/spec-cwe))
(s/def ::status-weight-birth (s/nilable ::segment/spec-cwe))
(s/def ::status-respiration-minutes (s/nilable ::segment/spec-cwe))
(s/def ::status-admission (s/nilable ::segment/spec-cwe))

(s/def ::spec
  (s/keys :req-un []
          :opt-un [::diagnosis-related-group
                   ::drg-assigned-date
                   ::drg-approval-indicator
                   ::drg-grouper-review-code
                   ::outlier-type
                   ::outlier-days
                   ::outlier-cost
                   ::drg-payor
                   ::outlier-reimbursement
                   ::confidential-indicator
                   ::drg-transfer-type
                   ::coder-name
                   ::grouper-status
                   ::pccl-value-code
                   ::effective-weight
                   ::monetary-amount
                   ::status-patient
                   ::grouper-software-name
                   ::grouper-software-version
                   ::status-financial-calculation
                   ::relative-discount
                   ::basic-charge
                   ::total-charge
                   ::discount
                   ::calculated-days
                   ::status-gender
                   ::status-age
                   ::status-length-of-stay
                   ::status-same-day-flag
                   ::status-separation-mode
                   ::status-weight-birth
                   ::status-respiration-minutes
                   ::status-admission]))

(defn record->hl7
  "Accepts a record of diagnosis related group data and returns a map of DRG
  segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils (segment/cne->field (:diagnosis-related-group %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:drg-assigned-date %))))
     (parser/create-field (:drg-approval-indicator %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-grouper-review-code %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:outlier-type %))))
     (parser/create-field (:outlier-days %))
     (parser/create-field (util/trim-nils (segment/cp->field (:outlier-cost %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-payor %))))
     (parser/create-field (util/trim-nils (segment/cp->field (:outlier-reimbursement %))))
     (parser/create-field (:confidential-indicator %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:drg-transfer-type %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:coder-name %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:grouper-status %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:pccl-value-code %))))
     (parser/create-field (:effective-weight %))
     (parser/create-field (util/trim-nils (segment/mo->field (:monetary-amount %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-patient %))))
     (parser/create-field (:grouper-software-name %))
     (parser/create-field (:grouper-software-version %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-financial-calculation %))))
     (parser/create-field (util/trim-nils (segment/mo->field (:relative-discount %))))
     (parser/create-field (util/trim-nils (segment/mo->field (:basic-charge %))))
     (parser/create-field (util/trim-nils (segment/mo->field (:total-charge %))))
     (parser/create-field (util/trim-nils (segment/mo->field (:discount %))))
     (parser/create-field (:calculated-days %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-gender %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-age %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-length-of-stay %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-same-day-flag %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-separation-mode %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-weight-birth %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-respiration-minutes %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:status-admission %)))))
   record))

(defn hl7->record
  "Accepts a DRG segment of parsed HL7 segment data and returns a DRG record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 34
   #(map->record
     {:diagnosis-related-group (segment/field->cne (message/get-segment-field % 1))
      :drg-assigned-date (segment/field->ts (message/get-segment-field % 2))
      :drg-approval-indicator (util/unwrap-and-first (message/get-segment-field % 3))
      :drg-grouper-review-code (segment/field->cwe (message/get-segment-field % 4))
      :outlier-type (segment/field->cwe (message/get-segment-field % 5))
      :outlier-days (util/read-string (util/unwrap-and-first (message/get-segment-field % 6)))
      :outlier-cost (segment/field->cp (message/get-segment-field % 7))
      :drg-payor (segment/field->cwe (message/get-segment-field % 8))
      :outlier-reimbursement (segment/field->cp (message/get-segment-field % 9))
      :confidential-indicator (util/unwrap-and-first (message/get-segment-field % 10))
      :drg-transfer-type (segment/field->cwe (message/get-segment-field % 11))
      :coder-name (segment/field->xpn (message/get-segment-field % 12))
      :grouper-status (segment/field->cwe (message/get-segment-field % 13))
      :pccl-value-code (segment/field->cwe (message/get-segment-field % 14))
      :effective-weight (util/read-string (util/unwrap-and-first (message/get-segment-field % 15)))
      :monetary-amount (segment/field->mo (message/get-segment-field % 16))
      :status-patient (segment/field->cwe (message/get-segment-field % 17))
      :grouper-software-name (util/unwrap-and-first (message/get-segment-field % 18))
      :grouper-software-version (util/unwrap-and-first (message/get-segment-field % 19))
      :status-financial-calculation (segment/field->cwe (message/get-segment-field % 20))
      :relative-discount (segment/field->mo (message/get-segment-field % 21))
      :basic-charge (segment/field->mo (message/get-segment-field % 22))
      :total-charge (segment/field->mo (message/get-segment-field % 23))
      :discount (segment/field->mo (message/get-segment-field % 24))
      :calculated-days (util/read-string (util/unwrap-and-first (message/get-segment-field % 25)))
      :status-gender (segment/field->cwe (message/get-segment-field % 26))
      :status-age (segment/field->cwe (message/get-segment-field % 27))
      :status-length-of-stay (segment/field->cwe (message/get-segment-field % 28))
      :status-same-day-flag (segment/field->cwe (message/get-segment-field % 29))
      :status-separation-mode (segment/field->cwe (message/get-segment-field % 30))
      :status-weight-birth (segment/field->cwe (message/get-segment-field % 31))
      :status-respiration-minutes (segment/field->cwe (message/get-segment-field % 32))
      :status-admission (segment/field->cwe (message/get-segment-field % 33))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
