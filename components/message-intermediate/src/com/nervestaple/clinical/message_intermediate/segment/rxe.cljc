(ns com.nervestaple.clinical.message-intermediate.segment.rxe
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.pv1 :as pv1]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "RXE")

;; Record representing a RXE segment. See https://hl7-definition.caristix.com/v2/HL7v2.8/Segments/RXE
(defrecord record
  [quantity-timing
   give-code
   give-amount-minimum
   give-amount-maximum
   give-units
   give-dosage-form
   providers-administration-instructions
   deliver-to-location
   substitution-status
   dispense-amount
   dispense-units
   number-of-refills
   ordering-providers-dea-number
   pharmacist-treatment-suppliers-verifier-id
   prescription-number
   number-of-refills-remaining
   number-of-refills-doses-dispensed
   dt-of-most-recent-refill-or-dose-dispensed
   total-daily-dose
   needs-human-review
   pharmacy-treatment-suppliers-special-dispensing-instructions
   give-per-time-unit
   give-rate-amount
   give-rate-units
   give-strength
   give-strength-units
   give-indication
   dispense-package-size
   dispense-package-size-unit
   dispense-package-method
   supplementary-code
   original-order-date-time
   give-drug-strength-volume
   give-drug-strength-volume-units
   controlled-substance-schedule
   formulary-status
   pharmaceutical-substance-alternative
   pharmacy-of-most-recent-fill
   initial-dispense-amount
   dispensing-pharmacy
   dispensing-pharmacy-address
   deliver-to-patient-location
   deliver-to-address
   pharmacy-order-type
   pharmacy-phone-number])

(s/def ::quantity-timing string?)
(s/def ::give-code ::segment/spec-cwe) ;; TODO different value table listed: https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0292
(s/def ::give-amount-minimum number?)
(s/def ::give-amount-maximum (s/nilable number?))
(s/def ::give-units ::segment/spec-cwe)
(s/def ::give-dosage-form (s/nilable ::segment/spec-cwe))
(s/def ::providers-administration-instructions (s/nilable ::segment/spec-cwe))
(s/def ::deliver-to-location string?)
(s/def ::substitution-status (s/nilable string?)) ;; TODO unique table ? https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0167
(s/def ::dispense-amount (s/nilable number?))
(s/def ::dispense-units  (s/nilable ::segment/spec-cwe))
(s/def ::number-of-refills (s/nilable number?))
(s/def ::ordering-providers-dea-number (s/nilable ::segment/spec-xcn))
(s/def ::pharmacist-treatment-suppliers-verifier-id (s/nilable ::segment/spec-xcn))
(s/def ::prescription-number (s/nilable string?))
(s/def ::number-of-refills-remaining (s/nilable number?))
(s/def ::number-of-refills-doses-dispensed (s/nilable number?))
(s/def ::dt-of-most-recent-refill-or-dose-dispensed (s/nilable ::segment/spec-ts))
(s/def ::total-daily-dose (s/nilable ::segment/spec-cq))
(s/def ::needs-human-review (s/nilable string?)) ;; TODO yes/no table https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0136
(s/def ::pharmacy-treatment-suppliers-special-dispensing-instructions (s/nilable ::segment/spec-cwe))
(s/def ::give-per-time-unit (s/nilable string?))
(s/def ::give-rate-amount (s/nilable string?))
(s/def ::give-rate-units (s/nilable ::segment/spec-cwe))
(s/def ::give-strength (s/nilable number?))
(s/def ::give-strength-units (s/nilable ::segment/spec-cwe))
(s/def ::give-indication (s/nilable ::segment/spec-cwe))
(s/def ::dispense-package-size (s/nilable number?))
(s/def ::dispense-package-size-unit (s/nilable ::segment/spec-cwe))
(s/def ::dispense-package-method (s/nilable string?)) ;; TODO unique table? https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0321
(s/def ::supplementary-code (s/nilable ::segment/spec-cwe))
(s/def ::original-order-date-time (s/nilable ::segment/spec-ts))
(s/def ::give-drug-strength-volume (s/nilable number?))
(s/def ::give-drug-strength-volume-units (s/nilable ::segment/spec-cwe))
(s/def ::controlled-substance-schedule (s/nilable ::segment/spec-cwe)) ;; TODO unique table? https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0477
(s/def ::formulary-status (s/nilable string?)) ;; TODO unique table? https://hl7-definition.caristix.com/v2/HL7v2.8/Tables/0478
(s/def ::pharmaceutical-substance-alternative (s/nilable ::segment/spec-cwe))
(s/def ::pharmacy-of-most-recent-fill (s/nilable ::segment/spec-cwe))
(s/def ::initial-dispense-amount (s/nilable number?))
(s/def ::dispensing-pharmacy (s/nilable ::segment/spec-cwe))
(s/def ::dispensing-pharmacy-address (s/nilable ::segment/spec-xad))
(s/def ::deliver-to-patient-location (s/nilable :com.nervestaple.clinical.message-intermediate.segment.pv1/spec-pl))
(s/def ::deliver-to-address (s/nilable ::segment/spec-xad))
(s/def ::pharmacy-order-type (s/nilable string?))
(s/def ::pharmacy-phone-number (s/nilable ::segment/spec-xtn))

(s/def ::spec
  (s/keys :req-un [::give-code ::give-amount-minimum ::give-units]
          :opt-un [::quantity-timing
                   ::give-amount-maximum
                   ::give-dosage-form
                   ::providers-administration-instructions
                   ::deliver-to-location
                   ::substitution-status
                   ::dispense-amount
                   ::dispense-units
                   ::number-of-refills
                   ::ordering-providers-dea-number
                   ::pharmacist-treatment-suppliers-verifier-id
                   ::prescription-number
                   ::number-of-refills-remaining
                   ::number-of-refills-doses-dispensed
                   ::dt-of-most-recent-refill-or-dose-dispensed
                   ::total-daily-dose
                   ::needs-human-review
                   ::pharmacy-treatment-suppliers-special-dispensing-instructions
                   ::give-per-time-unit
                   ::give-rate-amount
                   ::give-rate-units
                   ::give-strength
                   ::give-strength-units
                   ::give-indication
                   ::dispense-package-size
                   ::dispense-package-size-unit
                   ::dispense-package-method
                   ::supplementary-code
                   ::original-order-date-time
                   ::give-drug-strength-volume
                   ::give-drug-strength-volume-units
                   ::controlled-substance-schedule
                   ::formulary-status
                   ::pharmaceutical-substance-alternative
                   ::pharmacy-of-most-recent-fill
                   ::initial-dispense-amount
                   ::dispensing-pharmacy
                   ::dispensing-pharmacy-address
                   ::deliver-to-patient-location
                   ::deliver-to-address
                   ::pharmacy-order-type
                   ::pharmacy-phone-number]))

(defn record->hl7
  "Accepts a record of RXE data and returns a map of RXE segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:quantity-timing %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-code %))))
     (parser/create-field (:give-amount-minimum %))
     (parser/create-field (:give-amount-maximum %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-units %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-dosage-form %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:providers-administration-instructions %))))
     (parser/create-field (:deliver-to-location %))
     (parser/create-field (:substitution-status %))
     (parser/create-field (:dispense-amount %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:dispense-units %))))
     (parser/create-field (:number-of-refills %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:ordering-providers-dea-number %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:pharmacist-treatment-suppliers-verifier-id %))))
     (parser/create-field (:prescription-number %))
     (parser/create-field (:number-of-refills-remaining %))
     (parser/create-field (:number-of-refills-doses-dispensed %))
     (parser/create-field (util/trim-nils (segment/ts->field (:dt-of-most-recent-refill-or-dose-dispensed %))))
     (parser/create-field (util/trim-nils (segment/cq->field (:total-daily-dose %))))
     (parser/create-field (:needs-human-review %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:pharmacy-treatment-suppliers-special-dispensing-instructions %))))
     (parser/create-field (:give-per-time-unit %))
     (parser/create-field (:give-rate-amount %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-rate-units %))))
     (parser/create-field (:give-strength %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-strength-units %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-indication %))))
     (parser/create-field (:dispense-package-size %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:dispense-package-size-unit %))))
     (parser/create-field (:dispense-package-method %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:supplementary-code %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:original-order-date-time %))))
     (parser/create-field (:give-drug-strength-volume %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:give-drug-strength-volume-units %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:controlled-substance-schedule %))))
     (parser/create-field (:formulary-status %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:pharmaceutical-substance-alternative %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:pharmacy-of-most-recent-fill %))))
     (parser/create-field (:initial-dispense-amount %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:dispensing-pharmacy %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:dispensing-pharmacy-address %))))
     (parser/create-field (util/trim-nils (pv1/pl->field (:deliver-to-patient-location %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:deliver-to-address %))))
     (parser/create-field (:pharmacy-order-type %))
     (parser/create-field (util/trim-nils (segment/xtn->field (:pharmacy-phone-number %)))))
   record))

(defn hl7->record
  "Accepts a RXC segment of parsed HL7 segment data and returns a RXC record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID
   44
   #(map->record
     {:quantity-timing                                              (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :give-code                                                    (segment/field->cwe (message/get-segment-field % 2))
      :give-amount-minimum                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 3)))
      :give-amount-maximum                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 4)))
      :give-units                                                   (segment/field->cwe (message/get-segment-field % 5))
      :give-dosage-form                                             (segment/field->cwe (message/get-segment-field % 6))
      :providers-administration-instructions                        (segment/field->cwe (message/get-segment-field % 7))
      :deliver-to-location                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 8)))
      :substitution-status                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 9)))
      :dispense-amount                                              (util/read-string (util/unwrap-and-first (message/get-segment-field % 10)))
      :dispense-units                                               (segment/field->cwe (message/get-segment-field % 11))
      :number-of-refills                                            (util/read-string (util/unwrap-and-first (message/get-segment-field % 12)))
      :ordering-providers-dea-number                                (segment/field->xcn (message/get-segment-field % 13))
      :pharmacist-treatment-suppliers-verifier-id                   (segment/field->xcn (segment/field->xcn (message/get-segment-field % 14)))
      :prescription-number                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 15)))
      :number-of-refills-remaining                                  (util/read-string (util/unwrap-and-first (message/get-segment-field % 16)))
      :number-of-refills-doses-dispensed                            (util/read-string (util/unwrap-and-first (message/get-segment-field % 17)))
      :dt-of-most-recent-refill-or-dose-dispensed                   (segment/field->ts (message/get-segment-field % 18))
      :total-daily-dose                                             (segment/field->cq (message/get-segment-field % 19))
      :needs-human-review                                           (util/read-string (util/unwrap-and-first (message/get-segment-field % 20)))
      :pharmacy-treatment-suppliers-special-dispensing-instructions (segment/field->cwe (message/get-segment-field % 21))
      :give-per-time-unit                                           (util/read-string (util/unwrap-and-first (message/get-segment-field % 22)))
      :give-rate-amount                                             (util/read-string (util/unwrap-and-first (message/get-segment-field % 23)))
      :give-rate-units                                              (segment/field->cwe (message/get-segment-field % 24))
      :give-strength                                                (util/read-string (util/unwrap-and-first (message/get-segment-field % 25)))
      :give-strength-units                                          (segment/field->cwe (message/get-segment-field % 26))
      :give-indication                                              (segment/field->cwe (message/get-segment-field % 27))
      :dispense-package-size                                        (util/read-string (util/unwrap-and-first (message/get-segment-field % 28)))
      :dispense-package-size-unit                                   (segment/field->cwe (message/get-segment-field % 29))
      :dispense-package-method                                      (util/read-string (util/unwrap-and-first (message/get-segment-field % 30)))
      :supplementary-code                                           (segment/field->cwe (message/get-segment-field % 31))
      :original-order-date-time                                     (segment/field->ts (message/get-segment-field % 32))
      :give-drug-strength-volume                                    (util/read-string (util/unwrap-and-first (message/get-segment-field % 33)))
      :give-drug-strength-volume-units                              (segment/field->cwe (message/get-segment-field % 34))
      :controlled-substance-schedule                                (segment/field->cwe (message/get-segment-field % 35))
      :formulary-status                                             (util/read-string (util/unwrap-and-first (message/get-segment-field % 36)))
      :pharmaceutical-substance-alternative                         (segment/field->cwe (message/get-segment-field % 37))
      :pharmacy-of-most-recent-fill                                 (segment/field->cwe (message/get-segment-field % 38))
      :initial-dispense-amount                                      (util/read-string (util/unwrap-and-first (message/get-segment-field % 39)))
      :dispensing-pharmacy                                          (segment/field->cwe (message/get-segment-field % 40))
      :dispensing-pharmacy-address                                  (segment/field->xad (message/get-segment-field % 41))
      :deliver-to-patient-location                                  (pv1/field->pl (message/get-segment-field % 42))
      :deliver-to-address                                           (segment/field->xad (message/get-segment-field % 43))
      :pharmacy-order-type                                          (util/read-string (util/unwrap-and-first (message/get-segment-field % 44)))
      :pharmacy-phone-number                                        (segment/field->xtn (message/get-segment-field % 45))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
