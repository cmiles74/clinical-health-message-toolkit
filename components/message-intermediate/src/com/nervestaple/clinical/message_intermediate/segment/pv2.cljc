(ns com.nervestaple.clinical.message-intermediate.segment.pv2
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.pv1 :as pv1]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment Identifier
(def SEGMENT-ID "PV2")

;; record representing PV2 segment
(defrecord record
    [prior-pending-location
     accomodation
     admit-reason
     transfer-reason
     patient-valuables
     patient-valuables-location
     visit-user
     expected-admit-date
     expected-discharge-date
     estimated-length-inpatient-stay
     estimated-length-outpatient-stay
     visit-description
     referral-source
     previous-service-date
     employment-illness-related
     purge-status
     purge-status-date
     special-program
     retention
     expected-number-insurance-plans
     visit-publicity
     clinic-organization
     patient-status
     visit-priority
     previous-treatment-date
     expected-discharge-disposition
     signature-on-file-date
     first-similar-illness-date
     patient-charge-adjustment
     recurring-service
     billing-media
     expected-surgery-date
     military-partnership
     military-non-availability
     newborn-baby
     mode-of-arrival
     recreational-drug-use
     admission-level-of-care
     precaution
     patient-condition
     living-will
     organ-donor
     advance-directive
     patient-status-effective-date
     expected-loa-return-date
     expected-pre-admission-testing-date
     notify-clergy
     advance-directive-last-verified-date])

(s/def ::prior-pending-location (s/nilable ::pv1/spec-pl))
(s/def ::accomodation (s/nilable ::segment/spec-cwe))
(s/def ::admit-reason (s/nilable ::segment/spec-cwe))
(s/def ::transfer-reason (s/nilable ::segment/spec-cwe))
(s/def ::patient-valuables (s/nilable string?))
(s/def ::patient-valuables-location (s/nilable string?))
(s/def ::visit-user (s/nilable string?))
(s/def ::expected-admit-date (s/nilable ::segment/spec-ts))
(s/def ::expected-discharge-date (s/nilable ::segment/spec-ts))
(s/def ::estimated-length-inpatient-stay (s/nilable number?))
(s/def ::estimated-length-outpatient-stay (s/nilable number?))
(s/def ::visit-description (s/nilable string?))
(s/def ::referral-source (s/nilable ::segment/spec-xcn))
(s/def ::previous-service-date (s/nilable ::segment/spec-ts))
(s/def ::employment-illness-related (s/nilable string?))
(s/def ::purge-status (s/nilable string?))
(s/def ::purge-status-date (s/nilable ::segment/spec-ts))
(s/def ::special-program (s/nilable string?))
(s/def ::retention (s/nilable string?))
(s/def ::expected-number-insurance-plans (s/nilable number?))
(s/def ::visit-publicity (s/nilable string?))
(s/def ::visit-protection (s/nilable string?))
(s/def ::clinic-organization (s/nilable ::segment/spec-xon))
(s/def ::patient-status (s/nilable string?))
(s/def ::visit-priority (s/nilable string?))
(s/def ::previous-treatment-date (s/nilable ::segment/spec-ts))
(s/def ::expected-discharge-disposition (s/nilable string?))
(s/def ::signature-on-file-date (s/nilable ::segment/spec-ts))
(s/def ::first-similar-illness-date (s/nilable ::segment/spec-ts))
(s/def ::patient-charge-adjustment (s/nilable ::segment/spec-cwe))
(s/def ::recurring-service (s/nilable string?))
(s/def ::billing-media (s/nilable string?))
(s/def ::expected-surgery-date (s/nilable ::segment/spec-ts))
(s/def ::military-partnership (s/nilable string?))
(s/def ::military-non-availability (s/nilable string?))
(s/def ::newborn-baby (s/nilable string?))
(s/def ::baby-detained (s/nilable string?))
(s/def ::mode-of-arrival (s/nilable ::segment/spec-cwe))
(s/def ::recreational-drug-use (s/nilable ::segment/spec-cwe))
(s/def ::admission-level-of-care (s/nilable ::segment/spec-cwe))
(s/def ::precaution (s/nilable ::segment/spec-cwe))
(s/def ::patient-condition (s/nilable ::segment/spec-cwe))
(s/def ::living-will (s/nilable string?))
(s/def ::organ-donor (s/nilable string?))
(s/def ::advance-directive (s/nilable ::segment/spec-cwe))
(s/def ::patient-status-effective-date (s/nilable ::segment/spec-ts))
(s/def ::expected-loa-return-date (s/nilable ::segment/spec-ts))
(s/def ::expected-pre-admission-testing-date (s/nilable ::segment/spec-ts))
(s/def ::notify-clergy (s/nilable string?))
(s/def ::advance-directive-last-verified-date (s/nilable ::segment/spec-ts))

(s/def ::spec
  (s/keys :opt-un [::prior-pending-location ::accomodation ::admit-reason
                   ::transfer-reason ::patient-valuables ::patient-valuables-location
                   ::visit-user ::expected-admit-date ::expected-discharge-date
                   ::estimated-length-inpatient-stay ::estimated-length-outpatient-stay
                   ::visit-description ::referral-source ::previous-service-date
                   ::employment-illness-related ::purge-status ::purge-status-date
                   ::special-program ::retention ::expected-number-insurance-plans
                   ::visit-publicity ::visit-protection ::clinic-organization ::patient-status
                   ::visit-priority ::previous-treatment-date ::expected-discharge-disposition
                   ::signature-on-file-date ::first-similar-illness-date ::patient-charge-adjustment
                   ::recurring-service ::billing-media ::expected-surgery-date
                   ::military-partnership ::military-non-availability ::newborn-baby
                   ::baby-detained ::mode-of-arrival ::recreational-drug-use ::admission-level-of-care
                   ::precaution ::patient-condition ::living-will ::organ-donor
                   ::advance-directive ::patient-status-effective-date
                   ::expected-loa-return-date ::expected-pre-admission-testing-date
                   ::notify-clergy ::advance-directive-last-verified-date]))

(defn record->hl7
  "Accepts a record of patient visit 2 (PV2) data and returns a map of PV@ segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils (pv1/pl->field (:prior-pending-location %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:accomodation %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:admit-reason %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:transfer-reason %))))
     (parser/create-field (:patient-valuables %))
     (parser/create-field (:patient-valuables-location %))
     (parser/create-field (::visit-user %))
     (parser/create-field (util/trim-nils (segment/ts->field (:expected-admit-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:expected-discharge-date %))))
     (parser/create-field (:estimated-length-inpatient-stay %))
     (parser/create-field (:estimated-length-outpatient-stay %))
     (parser/create-field (:visit-description %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:referral-source %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:previous-service-date %))))
     (parser/create-field (:employment-illness-related %))
     (parser/create-field (:purge-status %))
     (parser/create-field (util/trim-nils (segment/ts->field (:purge-status-date %))))
     (parser/create-field (:special-program %))
     (parser/create-field (:retention %))
     (parser/create-field (:expected-number-insurance-plans %))
     (parser/create-field (:visit-publicity %))
     (parser/create-field (util/trim-nils (segment/xon->field (:clinic-organization %))))
     (parser/create-field (:patient-status %))
     (parser/create-field (:visit-priority %))
     (parser/create-field (:visit-protection %))
     (parser/create-field (util/trim-nils (segment/ts->field (:previous-treatment-date %))))
     (parser/create-field (:expected-discharge-disposition %))
     (parser/create-field (util/trim-nils (segment/ts->field (:signature-on-file-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:first-similar-illness-date %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:patient-charge-adjustment %))))
     (parser/create-field (:recurring-service %))
     (parser/create-field (:billing-media %))
     (parser/create-field (util/trim-nils (segment/ts->field (:expected-surgery-date %))))
     (parser/create-field (:military-partnership %))
     (parser/create-field (:military-non-availability %))
     (parser/create-field (:newborn-baby %))
     (parser/create-field (:baby-detained %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:mode-of-arrival %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:recreational-drug-use %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:admission-level-of-care %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:precaution %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:patient-condition %))))
     (parser/create-field (:living-will %))
     (parser/create-field (:organ-donor %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:advance-directive %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:patient-status-effective-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:expected-loa-return-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:expected-pre-admission-testing-date %))))
     (parser/create-field (:notify-clergy %))
     (parser/create-field (util/trim-nils (segment/ts->field (:advance-directive-last-verified-date %)))))
   record))

(defn hl7->record
  "Accepts a segment of parsed HL7 segment data and returns a PV2 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 51
   #(map->record
     {:prior-pending-location (pv1/field->pl (message/get-segment-field % 1))
      :accomodation (segment/field->cwe (message/get-segment-field % 2))
      :admit-reason (segment/field->cwe (message/get-segment-field % 3))
      :transfer-reason (segment/field->cwe (message/get-segment-field % 4))
      :patient-valuables (util/read-string (util/unwrap-and-first (message/get-segment-field % 5)))
      :patient-valuables-location (util/read-string (util/unwrap-and-first (message/get-segment-field % 6)))
      :visit-user (util/read-string (util/unwrap-and-first (message/get-segment-field % 7)))
      :expected-admit-date (segment/field->ts (message/get-segment-field % 8))
      :expected-discharge-date (segment/field->ts (message/get-segment-field % 9))
      :estimated-length-inpatient-stay (util/read-string (util/unwrap-and-first (message/get-segment-field % 10)))
      :estimated-length-outpatient-stay (util/read-string (util/unwrap-and-first (message/get-segment-field % 11)))
      :visit-description (util/read-string (util/unwrap-and-first (message/get-segment-field % 12)))
      :referral-source (segment/field->xcn (message/get-segment-field % 13))
      :previous-service-date (segment/field->ts (message/get-segment-field % 14))
      :employment-illness-related (util/read-string (util/unwrap-and-first (message/get-segment-field % 15)))
      :purge-status (util/read-string (util/unwrap-and-first (message/get-segment-field % 16)))
      :purge-status-date (segment/field->ts (message/get-segment-field % 17))
      :special-program (util/read-string (util/unwrap-and-first (message/get-segment-field % 18)))
      :retention (util/read-string (util/unwrap-and-first (message/get-segment-field % 19)))
      :expected-number-insurance-plans (util/read-string (util/unwrap-and-first (message/get-segment-field % 20)))
      :visit-publicity (util/read-string (util/unwrap-and-first (message/get-segment-field % 21)))
      :visit-protection (util/read-string (util/unwrap-and-first (message/get-segment-field % 22)))
      :clinic-organization (segment/field->xon (message/get-segment-field % 23))
      :patient-status (util/read-string (util/unwrap-and-first (message/get-segment-field % 24)))
      :visit-priority (util/read-string (util/unwrap-and-first (message/get-segment-field % 25)))
      :previous-treatment-date (segment/field->ts (message/get-segment-field % 26))
      :expected-discharge-disposition (util/read-string (util/unwrap-and-first (message/get-segment-field % 27)))
      :signature-on-file-date (segment/field->ts (util/unwrap-and-first (message/get-segment-field % 28)))
      :first-similar-illness-date (segment/field->ts (message/get-segment-field % 29))
      :patient-charge-adjustment (segment/field->cwe (message/get-segment-field % 30))
      :recurring-service (util/read-string (util/unwrap-and-first (message/get-segment-field % 31)))
      :billing-media (util/read-string (util/unwrap-and-first (message/get-segment-field % 32)))
      :expected-surgery-date (segment/field->ts (message/get-segment-field % 33))
      :military-partnership (util/read-string (util/unwrap-and-first (message/get-segment-field % 34)))
      :military-non-availability (util/read-string (util/unwrap-and-first (message/get-segment-field % 35)))
      :newborn-baby (util/read-string (util/unwrap-and-first (message/get-segment-field % 36)))
      :baby-detained (util/read-string (util/unwrap-and-first (message/get-segment-field % 37)))
      :mode-of-arrival (segment/field->cwe (message/get-segment-field % 38))
      :recreational-drug-use (segment/field->cwe (message/get-segment-field % 39))
      :admission-level-of-care (segment/field->cwe (message/get-segment-field % 40))
      :precaution (segment/field->cwe (message/get-segment-field % 41))
      :patient-condition (segment/field->cwe (message/get-segment-field % 42))
      :living-will (util/read-string (util/unwrap-and-first (message/get-segment-field % 43)))
      :organ-donor (util/read-string (util/unwrap-and-first (message/get-segment-field % 44)))
      :advance-directive (segment/field->cwe (message/get-segment-field % 45))
      :patient-status-effective-date (segment/field->ts (message/get-segment-field % 46))
      :expected-loa-return-date (segment/field->ts (message/get-segment-field % 47))
      :expected-pre-admission-testing-date (segment/field->ts (message/get-segment-field % 48))
      :notify-clergy (util/read-string (util/unwrap-and-first (message/get-segment-field % 49)))
      :advance-directive-last-verified-date (segment/field->ts (message/get-segment-field % 50))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
    (merge {:id SEGMENT-ID}
            data-map)))
