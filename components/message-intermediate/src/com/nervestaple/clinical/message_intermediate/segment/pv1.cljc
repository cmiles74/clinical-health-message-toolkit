(ns com.nervestaple.clinical.message-intermediate.segment.pv1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "PV1")

;; PLN: person location
(defrecord pl
    [point-of-care room bed facility location-status location-type building
     floor description identifier assigning-authority])

(s/def ::point-of-care (s/nilable string?))
(s/def ::room (s/nilable string?))
(s/def ::bed (s/nilable string?))
(s/def ::facility (s/nilable ::segment/spec-hd))
(s/def ::location-status (s/nilable string?))
(s/def ::location-type (s/nilable string?))
(s/def ::building (s/nilable string?))
(s/def ::floor (s/nilable string?))
(s/def ::description (s/nilable string?))
(s/def ::identifier (s/nilable ::segment/spec-ei))
(s/def ::assigning-authority (s/nilable ::segment/spec-hd))

(s/def ::spec-pl
  (s/keys :opt-un [::point-of-care ::room ::bed ::facility ::location-status
                   ::location-type ::building ::floor ::description ::identifier
                   ::assigning-authority]))

(s/fdef pl->field
  :args (s/or :record (s/cat :pl-record (s/nilable ::spec-pl))
              :coll (s/cat :coll (s/coll-of ::spec-pl)))
  :ret (s/nilable vector?))

(defn pl->field
  "Accepts one or a sequence of PL records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:point-of-care %)
            (:room %)
            (:bed %)
            (util/trim-nils (segment/hd->field (:facility %)))
            (:location-status %)
            (:location-type %)
            (:building %)
            (:floor %)
            (:description %)
            (util/trim-nils (segment/ei->field (:identifier %)))
            (util/trim-nils (segment/hd->field (:assigining-authority %))))))

(s/fdef field->pl
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-pl))
             :record (s/nilable ::spec-pl)))

(defn field->pl
  "Accepts an HL7 v2 field of PLN data and returns a single PLN record, a sequence
  of records or nil."
  [field]
  (segment/field-to-type
   field
   map->pl
   #(array-map :point-of-care (util/get-or-nil % 0)
               :room (util/get-or-nil % 1)
               :bed (util/get-or-nil % 2)
               :facility (segment/field->hd (util/get-or-nil % 3))
               :location-status (util/get-or-nil % 4)
               :location-type (util/get-or-nil % 5)
               :building (util/get-or-nil % 6)
               :floor (util/get-or-nil % 7)
               :description (util/get-or-nil % 8)
               :identifier (segment/field->ei (util/get-or-nil % 9))
               :assiging-authority (segment/field->hd (util/get-or-nil % 10)))))

;; FN: financial class
(defrecord fc
    [class-code effective-date])

(s/def ::class-code string?)
(s/def ::effective-date (s/nilable ::segment/spec-ts))

(s/def ::spec-fc
  (s/keys :req-un [::class-code]
          :opt-un [::effective-date]))

(s/fdef fc->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-fc))
              :coll (s/cat :coll (s/coll-of ::spec-fc)))
  :ret (s/nilable vector?))

(defn fc->field
  "Accepts on or a sequence of FC records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:class-code %)
            (util/trim-nils (segment/ts->field (:effective-date %))))))

(s/fdef field->fc
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-fc))
             :record (s/nilable ::spec-fc)))

(defn field->fc
  "Accepts an HL7 v2 field of FC data and returns a single FC record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->fc
   #(array-map :class-code (util/get-or-nil % 0)
               :effective-date (segment/field->ts (util/get-or-nil % 1)))))

;; DLD: discharge location
(defrecord dld
    [location effective-date])

(s/def ::location string?)

(s/def ::spec-dld
  (s/keys :req-un [::location]
          :opt-un [::effective-date]))

(s/fdef dld->field
  :args (s/or :record (s/cat :xpn-record (s/nilable ::spec-dld))
              :coll (s/cat :coll (s/coll-of ::spec-dld)))
  :ret (s/nilable vector?))

(defn dld->field
  "Accepts on or a sequence of DLD records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:location %)
            (util/trim-nils (segment/ts->field (:effective-date %))))))

(s/fdef field->dld
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-dld))
             :record (s/nilable ::spec-dld)))

(defn field->dld
  "Accepts an HL7 v2 field of DLD data and returns a singl FC record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->dld
   #(array-map :location (util/get-or-nil % 0)
               :effective-date (segment/field->ts (util/get-or-nil % 1)))))

;; Record representing a PV1 segment
(defrecord record
    [set-id
     patient-class
     patient-location
     admission-type
     preadmit-number
     prior-patient-location
     attending-doctor
     referring-doctor
     consulting-doctor
     hospital-service
     temporary-location
     preadmit-test-indicator
     readmission-indicator
     admit-source
     ambulatory-status
     vip-indicator
     admitting-doctor
     patient-type
     visit-number
     financial-class
     charge-price-indicator
     courtesy-code
     credit-rating
     contract-code
     contract-effective-date
     contract-amount
     contract-period
     interest-code
     transfer-to-bad-debt-code
     transfer-to-bad-debt-date
     bad-debt-agency-code
     bad-debt-transfer-amount
     bad-debt-recovery-amount
     delete-account-indicator
     delete-account-date
     discharge-disposition
     discharge-location
     diet-type
     servicing-facility
     bed-status
     account-status
     pending-location
     prior-temporary-location
     admit-date-time
     discharge-date-time
     current-patient-balance
     total-charges
     total-adjustments
     total-payments
     alternate-visit-id
     visit-indicator
     other-healthcare-provider])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::patient-class (s/nilable string?))
(s/def ::patient-location (s/nilable ::spec-pl))
(s/def ::admission-type (s/nilable string?))
(s/def ::preadmit-number (s/nilable ::segment/spec-cx))
(s/def ::prior-patient-location (s/nilable ::spec-pl))
(s/def ::attending-doctor (s/nilable ::segment/spec-xcn))
(s/def ::referring-doctor (s/nilable ::segment/spec-xcn))
(s/def ::consulting-doctor (s/nilable ::segment/spec-xcn))
(s/def ::hospital-service (s/nilable string?))
(s/def ::temporary-location (s/nilable ::spec-pl))
(s/def ::preadmit-test-indicator (s/nilable string?))
(s/def ::readmission-indicator (s/nilable string?))
(s/def ::admit-source (s/nilable string?))
(s/def ::ambulatory-status (s/nilable string?))
(s/def ::vip-indicator (s/nilable string?))
(s/def ::admitting-doctor (s/nilable ::segment/spec-xcn))
(s/def ::patient-type (s/nilable string?))
(s/def ::visit-number (s/nilable ::segment/spec-cx))
(s/def ::financial-class (s/nilable ::spec-fc))
(s/def ::charge-price-indicator (s/nilable string?))
(s/def ::courtesy-code (s/nilable string?))
(s/def ::credit-rating (s/nilable string?))
(s/def ::contract-code (s/nilable string?))
(s/def ::contract-effective-date (s/nilable ::segment/spec-ts))
(s/def ::contract-amount (s/nilable number?))
(s/def ::contract-period (s/nilable number?))
(s/def ::interest-code (s/nilable string?))
(s/def ::transfer-to-bad-debt-code (s/nilable string?))
(s/def ::transfer-to-bad-debt-date (s/nilable ::segment/spec-ts))
(s/def ::bad-debt-agency-code (s/nilable string?))
(s/def ::bad-debt-transfer-amount (s/nilable number?))
(s/def ::bad-debt-recovery-amount (s/nilable number?))
(s/def ::delete-account-indicator (s/nilable string?))
(s/def ::delete-account-date (s/nilable ::segment/spec-ts))
(s/def ::discharge-disposition (s/nilable string?))
(s/def ::discharge-location (s/nilable ::spec-dld))
(s/def ::diet-type (s/nilable ::segment/spec-ce))
(s/def ::servicing-facility (s/nilable string?))
(s/def ::bed-status (s/nilable string?))
(s/def ::account-status (s/nilable string?))
(s/def ::pending-location (s/nilable ::spec-pl))
(s/def ::prior-temporary-location (s/nilable ::spec-pl))
(s/def ::admit-date-time (s/nilable ::segment/spec-ts))
(s/def ::discharge-date-time (s/nilable ::segment/spec-ts))
(s/def ::current-patient-balance (s/nilable number?))
(s/def ::total-charges (s/nilable number?))
(s/def ::total-adjustments (s/nilable number?))
(s/def ::total-payments (s/nilable number?))
(s/def ::alternate-visit-id (s/nilable ::segment/spec-cx))
(s/def ::visit-indicator (s/nilable string?))
(s/def ::other-healthcare-provider (s/nilable ::segment/spec-xcn))

(s/def ::spec
  (s/keys :req-un [::patient-class]
          :opt-un [::set-id ::patient-location ::admission-type ::preadmit-number
                   ::prior-patient-location ::attending-doctor ::referring-doctor
                   ::consulting-doctor ::hospital-service ::temporary-location
                   ::preadmit-test-indicator ::readmission-indicator
                   ::admit-source ::ambulatory-status ::vip-indicator
                   ::admitting-doctor ::patient-type ::visit-number
                   ::financial-class ::charge-price-indicator ::courtesy-code
                   ::credit-rating ::contract-code ::contract-effective-date
                   ::contract-amount ::contract-period ::interest-code
                   ::transfer-to-bad-debt-code ::transfer-to-bad-debt-date
                   ::bad-debt-agency-code ::bad-debt-transfer-amount
                   ::bad-debt-recovery-amount ::delete-account-indicator
                   ::delete-account-date ::discharge-disposition
                   ::discharge-location ::diet-type ::servicing-facility
                   ::bed-status ::account-status ::pending-location
                   ::prior-temporary-location ::admit-date-time
                   ::discharge-date-time ::current-patient-balance ::total-charges
                   ::total-adjustments ::total-payments ::alternate-visit-id
                   ::visit-indicator ::other-healthcare-provider]))

(defn record->hl7
  "Accepts a record of patient visit data and returns a map of PV1 segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (lookups/patient-class-by-key (:patient-class %)))
     (parser/create-field (util/trim-nils (pl->field (:patient-location %))))
     (parser/create-field (:admission-type %))
     (parser/create-field (util/trim-nils (segment/cx->field (:preadmit-number %))))
     (parser/create-field (util/trim-nils (pl->field (:prior-patient-location %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:attending-doctor %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:referring-doctor %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:consulting-doctor %))))
     (parser/create-field (:hospital-service %))
     (parser/create-field (util/trim-nils (pl->field (:temporary-location %))))
     (parser/create-field (:preadmit-test-indicator %))
     (parser/create-field (:readmission-indicator %))
     (parser/create-field (:admit-source %))
     (parser/create-field (:ambulatory-status %))
     (parser/create-field (:vip-indicator %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:admitting-doctor %))))
     (parser/create-field (:patient-type %))
     (parser/create-field (util/trim-nils (segment/cx->field (:visit-number %))))
     (parser/create-field (util/trim-nils (fc->field (:financial-class %))))
     (parser/create-field (:charge-price-indicator %))
     (parser/create-field (:courtesy-code %))
     (parser/create-field (:credit-rating %))
     (parser/create-field (:contract-code %))
     (parser/create-field (util/trim-nils (segment/ts->field (:contract-effective-date %))))
     (parser/create-field (:contract-amount %))
     (parser/create-field (:contract-period %))
     (parser/create-field (:interest-code %))
     (parser/create-field (:transfer-to-bad-debt-code %))
     (parser/create-field (util/trim-nils (segment/ts->field (:transfer-to-bad-debt-date %))))
     (parser/create-field (:bad-debt-agency-code %))
     (parser/create-field (:bad-debt-transfer-amount %))
     (parser/create-field (:bad-debt-recovery-amount %))
     (parser/create-field (:delete-account-indicator %))
     (parser/create-field (util/trim-nils (segment/ts->field (:delete-account-date %))))
     (parser/create-field (:discharge-disposition %))
     (parser/create-field (util/trim-nils (dld->field (:discharge-location %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:diet-type %))))
     (parser/create-field (:servicing-facility %))
     (parser/create-field (:bed-status %))
     (parser/create-field (:account-status %))
     (parser/create-field (util/trim-nils (pl->field (:pending-location %))))
     (parser/create-field (util/trim-nils (pl->field (:prior-temporary-location %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:admit-date-time %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:discharge-date-time %))))
     (parser/create-field (:current-patient-balance %))
     (parser/create-field (:total-charges %))
     (parser/create-field (:total-adjustments %))
     (parser/create-field (:total-payments %))
     (parser/create-field (util/trim-nils (segment/cx->field (:alternate-visit-id %))))
     (parser/create-field (:visit-indicator %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:other-healthcare-provider %)))))
   record))

(defn hl7->record
  "Accepts a PV1 segment of parsed HL7 segment data and returns a PV1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 53
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :patient-class (lookups/patient-class-by-value (util/unwrap-and-first
                                                      (message/get-segment-field % 2)))
      :patient-location (field->pl (message/get-segment-field % 3))
      :admission-type (util/unwrap-and-first (message/get-segment-field % 4))
      :preadmit-number (segment/field->cx (message/get-segment-field % 5))
      :prior-patient-location (field->pl (message/get-segment-field % 6))
      :attending-doctor (segment/field->xcn (message/get-segment-field % 7))
      :referring-doctor (segment/field->xcn (message/get-segment-field % 8))
      :consulting-doctor (segment/field->xcn (message/get-segment-field % 9))
      :hospital-service (util/unwrap-and-first (message/get-segment-field % 10))
      :temporary-location (field->pl (message/get-segment-field % 11))
      :preadmit-test-indicator (util/unwrap-and-first (message/get-segment-field % 12))
      :readmission-indicator (util/unwrap-and-first (message/get-segment-field % 13))
      :admit-source (util/unwrap-and-first (message/get-segment-field % 14))
      :ambulatory-status (util/unwrap-and-first (message/get-segment-field % 15))
      :vip-indicator (util/unwrap-and-first (message/get-segment-field % 16))
      :admitting-doctor (segment/field->xcn (message/get-segment-field % 17))
      :patient-type (util/unwrap-and-first (message/get-segment-field % 18))
      :visit-number (segment/field->cx (message/get-segment-field % 19))
      :financial-class (field->fc (message/get-segment-field % 20))
      :charge-price-indicator (util/unwrap-and-first (message/get-segment-field % 21))
      :courtesy-code (util/unwrap-and-first (message/get-segment-field % 22))
      :credit-rating (util/unwrap-and-first (message/get-segment-field % 23))
      :contract-code (util/unwrap-and-first (message/get-segment-field % 24))
      :contract-effective-date (segment/field->ts (message/get-segment-field % 25))
      :contract-amount (util/read-string
                        (util/unwrap-and-first (message/get-segment-field % 26)))
      :contract-period (util/read-string
                        (util/unwrap-and-first (message/get-segment-field % 27)))
      :interest-code (util/unwrap-and-first (message/get-segment-field % 28))
      :transfer-to-bad-debt-code (util/unwrap-and-first (message/get-segment-field % 29))
      :transfer-to-bad-debt-date (segment/field->ts (message/get-segment-field % 30))
      :bad-debt-agency-code (util/unwrap-and-first (message/get-segment-field % 31))
      :bad-debt-transfer-amount (util/read-string
                                 (util/unwrap-and-first (message/get-segment-field % 32)))
      :bad-debt-recovery-amount (util/read-string
                                 (util/unwrap-and-first (message/get-segment-field % 33)))
      :delete-account-indicator (util/unwrap-and-first (message/get-segment-field % 34))
      :delete-account-date (segment/field->ts (message/get-segment-field % 35))
      :discharge-disposition (util/unwrap-and-first (message/get-segment-field % 36))
      :discharge-location (field->dld (message/get-segment-field % 37))
      :diet-type (segment/field->ce (message/get-segment-field % 38))
      :servicing-facility (util/unwrap-and-first (message/get-segment-field % 39))
      :bed-status (util/unwrap-and-first (message/get-segment-field % 40))
      :account-status (util/unwrap-and-first (message/get-segment-field % 41))
      :pending-location (field->pl (message/get-segment-field % 42))
      :prior-temporary-location (field->pl (message/get-segment-field % 43))
      :admit-date-time (segment/field->ts (message/get-segment-field % 44))
      :discharge-date-time(segment/field->ts (message/get-segment-field % 45))
      :current-patient-balance (util/read-string
                                (util/unwrap-and-first (message/get-segment-field % 46)))
      :total-charges (util/read-string
                      (util/unwrap-and-first (message/get-segment-field % 47)))
      :total-adjustments (util/read-string
                          (util/unwrap-and-first (message/get-segment-field % 48)))
      :total-payments (util/read-string
                       (util/unwrap-and-first (message/get-segment-field % 49)))
      :alternate-visit-id (segment/field->cx (message/get-segment-field % 50))
      :visit-indicator (util/unwrap-and-first (message/get-segment-field % 51))
      :other-healthcare-provider (segment/field->xcn (message/get-segment-field % 52))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
