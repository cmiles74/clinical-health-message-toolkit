(ns com.nervestaple.clinical.message-intermediate.segment.in1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]))

;; Segment Identifier
(def SEGMENT-ID "IN1")

;; AUI: authorization information
(defrecord aui
    [authorization-number date source])

(s/def ::authorization-number (s/nilable string?))
(s/def ::date (s/nilable ::segment/spec-ts))
(s/def ::source (s/nilable string?))

(s/def ::spec-aui
  (s/keys :opt-un [::authorization-number ::date ::source]))

(s/fdef aui->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-aui))
              :coll (s/cat :coll (s/coll-of ::spec-aui)))
  :ret (s/nilable vector?))

(defn aui->field
  "Accepts one or a sequence of AUI records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (segment/type-to-field record
                         #(vector (:authorization-number %)
                                  (util/trim-nils (::segment/ts->field (:date %)))
                                  (:source %))))

(s/fdef field->aui
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-aui))
             :record (s/nilable ::spec-aui)))

(defn field->aui
  "Accepts an HL7 v2 field of AUI data and returns a single record, a sequence
  of records or nil."
  [field]
  (segment/field-to-type field
                         map->aui
                         #(array-map :authorization-number (util/get-or-nil % 0)
                                     :date (segment/field->ts (util/get-or-nil % 1))
                                     :source (util/get-or-nil % 2))))

;; Record representing an IN1 segment
(defrecord record
    [set-id
     plan-id
     company-id
     company-name
     company-address
     company-contact
     company-telephone
     group-number
     group-name
     group-employee-id
     group-employee-name
     plan-effective-date
     plan-expiration-date
     authorization-info
     plan-type
     insured-name
     insured-relationship
     insured-birth-date
     insured-address
     assignment-of-benefits
     coordination-of-benefits
     coordination-of-benefits-priority
     notice-admission-flag
     notice-admission-date
     report-eligibility-flag
     report-eligibility-date
     release-info-code
     pre-admit-cert
     verification-date
     verification-person
     agreement-type-code
     billing-status
     lifetime-reserve-days
     delay-before-lr-day
     company-plan-code
     policy-number
     policy-deductible
     policy-limit-amount
     policy-limit-days
     room-rate-semi-private
     room-rate-private
     insured-employment-status
     insured-administrative-gender
     insured-employer-address
     verification-status
     prior-insurance-plan-id
     coverage-type
     handicap
     insured-id-number])

(s/def ::set-id pos-int?)
(s/def ::plan-id ::segment/spec-ce)
(s/def ::company-id ::segment/spec-cx)
(s/def ::company-name (s/nilable ::segment/spec-xon))
(s/def ::company-address (s/nilable ::segment/spec-xad))
(s/def ::company-contact (s/nilable ::segment/spec-xpn))
(s/def ::company-telephone (s/nilable ::segment/spec-xtn))
(s/def ::group-number (s/nilable string?))
(s/def ::group-name (s/nilable ::segment/spec-xon))
(s/def ::group-employee-id (s/nilable ::segment/spec-cx))
(s/def ::group-employee-name (s/nilable ::segment/spec-xon))
(s/def ::plan-effective-date (s/nilable ::segment/spec-ts))
(s/def ::plan-expiration-date (s/nilable ::segment/spec-ts))
(s/def ::authorization-info (s/nilable ::spec-aui))
(s/def ::plan-type (s/nilable string?))
(s/def ::insured-name (s/nilable ::segment/spec-xpn))
(s/def ::insured-relationship (s/nilable ::segment/spec-ce))
(s/def ::insured-birth-date (s/nilable ::segment/spec-ts))
(s/def ::insured-address (s/nilable ::segment/spec-xad))
(s/def ::assignment-of-benefits (s/nilable string?))
(s/def ::coordination-of-benefits (s/nilable string?))
(s/def ::coordination-of-benefits-priority (s/nilable string?))
(s/def ::notice-adminssion-flag (s/nilable string?))
(s/def ::notice-admission-date (s/nilable ::segment/spec-ts))
(s/def ::report-eligibility-flag (s/nilable string?))
(s/def ::report-eligibility-date (s/nilable ::segment/spec-ts))
(s/def ::release-info-code (s/nilable string?))
(s/def ::pre-admit-cert (s/nilable string?))
(s/def ::verification-date (s/nilable ::segment/spec-ts))
(s/def ::verification-person (s/nilable ::segment/spec-xcn))
(s/def ::agreement-type-code (s/nilable string?))
(s/def ::billing-status (s/nilable string?))
(s/def ::lifetime-reserve-days (s/nilable number?))
(s/def ::delay-before-lr-day (s/nilable number?))
(s/def ::company-plan-code (s/nilable string?))
(s/def ::policy-number (s/nilable string?))
(s/def ::policy-deductible (s/nilable ::segment/spec-cp))
(s/def ::policy-limit-amount (s/nilable ::segment/spec-cp))
(s/def ::policy-limit-days (s/nilable number?))
(s/def ::room-rate-semi-private (s/nilable ::segment/spec-cp))
(s/def ::room-rate-private (s/nilable ::segment/spec-cp))
(s/def ::insured-employment-status (s/nilable ::segment/spec-ce))
(s/def ::insured-administrative-gender (s/nilable string?))
(s/def ::insured-employer-address (s/nilable ::segment/spec-xad))
(s/def ::verification-status (s/nilable string?))
(s/def ::prior-insurance-plan-id (s/nilable string?))
(s/def ::coverage-type (s/nilable string?))
(s/def ::handicap (s/nilable string?))
(s/def ::insured-id-number (s/nilable ::segment/spec-cx))

(s/def ::spec
  (s/keys :req-un [::set-id ::plan-id ::company-id]
          :opt-un [::company-name ::company-address ::company-contact
                   ::company-telephone ::group-number ::group-name
                   ::group-employee-id ::group-employee-name ::plan-effective-date
                   ::plan-expiration-date ::authorization-info ::plan-type
                   ::insured-name ::insured-relationship ::insured-birth-date
                   ::insured-address ::assignment-of-benefits
                   ::coordination-of-benefits ::coordination-of-benefits-priority
                   ::notice-adminssion-flag
                   ::notice-admission-date ::report-eligibility-flag
                   ::report-eligibility-date ::release-info-code ::pre-admit-cert
                   ::verification-date ::verification-person ::agreement-type-code
                   ::billing-status ::lifetime-reserve-days ::delay-before-lr-day
                   ::company-plan-code ::policy-number ::policy-deductible
                   ::policy-limit-amount ::policy-limit-days
                   ::room-rate-semi-private ::room-rate-private
                   ::insured-employment-status ::insured-administrative-gender
                   ::insured-employer-address ::verification-status
                   ::prior-insurance-plan-id ::coverage-type ::handicap
                   ::insured-id-number]))

(defn record->hl7
  "Accepts a record of insurance data and returns a map of IN1 segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils
                            (segment/ce->field (:plan-id %))))
     (parser/create-field (util/trim-nils
                            (segment/cx->field (:company-id %))))
     (parser/create-field (util/trim-nils
                            (segment/xon->field (:company-name %))))
     (parser/create-field (util/trim-nils
                            (segment/xad->field (:company-address %))))
     (parser/create-field (util/trim-nils
                            (segment/xpn->field (:company-contact %))))
     (parser/create-field (util/trim-nils
                            (segment/xtn->field (:company-telephone %))))
     (parser/create-field (:group-number %))
     (parser/create-field (util/trim-nils
                            (segment/xon->field (:group-name %))))
     (parser/create-field (util/trim-nils
                            (segment/cx->field (:group-employee-id %))))
     (parser/create-field (util/trim-nils
                            (segment/xon->field (:group-employee-name %))))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:plan-effective-date %))))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:plan-expiration-date %))))
     (parser/create-field (util/trim-nils
                            (aui->field (:authorization-info %))))
     (parser/create-field (:plan-type %))
     (parser/create-field (util/trim-nils
                            (segment/xpn->field (:insured-name %))))
     (parser/create-field (util/trim-nils
                            (segment/ce->field (:insured-relationship %))))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:insured-birth-date %))))
     (parser/create-field (util/trim-nils
                            (segment/xad->field (:insured-address %))))
     (parser/create-field (:coordination-of-benefits %))
     (parser/create-field (:coordination-of-benefits-priority %))
     (parser/create-field (:notice-admission-flag %))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:notice-admission-date %))))
     (parser/create-field (:report-eligibility-flag %))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:report-eligibility-date %))))
     (parser/create-field (:release-info-code %))
     (parser/create-field (:pre-admit-cert %))
     (parser/create-field (util/trim-nils
                            (segment/ts->field (:verification-date %))))
     (parser/create-field (util/trim-nils
                            (segment/xcn->field (:verification-person %))))
     (parser/create-field (:agreement-type-code %))
     (parser/create-field (:billing-status %))
     (parser/create-field (:lifetime-reserve-days %))
     (parser/create-field (:delay-before-lr-day %))
     (parser/create-field (:company-plan-code %))
     (parser/create-field (:policy-number %))
     (parser/create-field (util/trim-nils
                            (segment/cp->field (:policy-deductible %))))
     (parser/create-field (util/trim-nils
                            (segment/cp->field (:policy-limit-amount %))))
     (parser/create-field (:policy-limit-days %))
     (parser/create-field (util/trim-nils
                            (segment/cp->field (:room-rate-semi-private %))))
     (parser/create-field (util/trim-nils
                            (segment/cp->field (:room-rate-private %))))
     (parser/create-field (util/trim-nils
                            (segment/ce->field (:insured-employment-status %))))
     (parser/create-field (lookups/gender-by-key (:insured-administrative-gender %)))
     (parser/create-field (util/trim-nils
                            (segment/xad->field (:insured-employer-address %))))
     (parser/create-field (:verification-status %))
     (parser/create-field (:prior-insurance-plan-id %))
     (parser/create-field (:coverage-type %))
     (parser/create-field (:handicap %))
     (parser/create-field (util/trim-nils
                            (segment/cx->field (:insured-id-number %)))))
   record))

(defn hl7->record
  "Accepts a segment of parsed HL7 data and returns an IN1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 50
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first
                                (message/get-segment-field % 1)))
      :plan-id (segment/field->ce (message/get-segment-field % 2))
      :company-id (segment/field->cx (message/get-segment-field % 3))
      :company-name (segment/field->xon (message/get-segment-field % 4))
      :company-address (segment/field->xad (message/get-segment-field % 5))
      :company-contact (segment/field->xpn (message/get-segment-field % 6))
      :company-telephone (segment/field->xtn (message/get-segment-field % 7))
      :group-number (util/unwrap-and-first (message/get-segment-field % 8))
      :group-name (segment/field->xon (message/get-segment-field % 9))
      :group-employee-id (segment/field->cx (message/get-segment-field % 10))
      :group-employee-name (segment/field->xon (message/get-segment-field % 11))
      :plan-effective-date (segment/field->ts (message/get-segment-field % 12))
      :plan-expiration-date (segment/field->ts (message/get-segment-field % 13))
      :authorization-info (field->aui (message/get-segment-field % 14))
      :plan-type (util/unwrap-and-first (message/get-segment-field % 15))
      :insured-name (segment/field->xpn (message/get-segment-field % 16))
      :insured-relationship (segment/field->ce (message/get-segment-field % 17))
      :insured-birth-date (segment/field->ts (message/get-segment-field % 18))
      :insured-address (segment/field->xad (message/get-segment-field % 19))
      :assignment-of-benefits (util/unwrap-and-first (message/get-segment-field % 20))
      :coordination-of-benefits (util/unwrap-and-first (message/get-segment-field % 21))
      :coordination-of-benefits-priority (util/unwrap-and-first (message/get-segment-field % 22))
      :notice-admission-flag (util/unwrap-and-first (message/get-segment-field % 23))
      :notice-admission-date (segment/field->ts (message/get-segment-field % 24))
      :report-eligibility-flag (util/unwrap-and-first (message/get-segment-field % 25))
      :report-eligibility-date (segment/field->ts (message/get-segment-field % 26))
      :release-info-code (util/unwrap-and-first (message/get-segment-field % 27))
      :pre-admit-cert (util/unwrap-and-first (message/get-segment-field % 28))
      :verification-date (segment/field->ts (message/get-segment-field % 29))
      :verification-person (segment/field->xcn (message/get-segment-field % 30))
      :agreement-type-code (util/unwrap-and-first (message/get-segment-field % 31))
      :billing-status (util/unwrap-and-first (message/get-segment-field % 32))
      :lifetime-reserve-days (util/read-string (util/unwrap-and-first
                                               (message/get-segment-field % 33)))
      :delay-before-lr-day (util/read-string (util/unwrap-and-first
                                             (message/get-segment-field % 34)))
      :company-plan-code (util/unwrap-and-first (message/get-segment-field % 35))
      :policy-number (util/unwrap-and-first (message/get-segment-field % 36))
      :policy-deductible (segment/field->cp (message/get-segment-field % 37))
      :policy-limit-amount (segment/field->cp (message/get-segment-field % 38))
      :policy-limit-days (util/read-string (util/unwrap-and-first
                                           (message/get-segment-field % 39)))
      :room-rate-semi-private (segment/field->cp (message/get-segment-field % 40))
      :room-rate-private (segment/field->cp (message/get-segment-field % 41))
      :insured-employment-status (segment/field->ce (message/get-segment-field % 42))
      :insured-administrative-gender (lookups/gender-by-value
                                      (util/unwrap-and-first (message/get-segment-field % 43)))
      :insured-employer-address (segment/field->xad (message/get-segment-field % 44))
      :verification-status (util/unwrap-and-first (message/get-segment-field % 45))
      :prior-insurance-plan-id (util/unwrap-and-first (message/get-segment-field % 46))
      :coverage-type (util/unwrap-and-first (message/get-segment-field % 47))
      :handicap (util/unwrap-and-first (message/get-segment-field % 48))
      :insured-id-number (segment/field->cx (message/get-segment-field % 49))
      })
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
