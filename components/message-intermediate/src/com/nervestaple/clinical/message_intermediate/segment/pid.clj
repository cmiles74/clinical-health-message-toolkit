(ns com.nervestaple.clinical.message-intermediate.segment.pid
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]))

;; Segment identifier
(def SEGMENT-ID "PID")

;; DLN: driver's license number
(defrecord dln
    [license-number issuing-authority expiration-date])

(s/def ::license-number string?)
(s/def ::issuing-authority (s/nilable string?))
(s/def ::expiration-date (s/nilable ::segment/spec-ts))

(s/def ::spec-dln
  (s/keys :req-un [::license-number]
          :opt-un [::issuing-authority ::expiration-date]))

(s/fdef dln->field
  :args (s/or :record (s/cat :dln-record (s/nilable ::spec-dln))
              :coll (s/cat :coll (s/coll-of ::spec-dln)))
  :ret (s/nilable vector?))

(defn dln->field
  "Accepts one or a sequence of DLN records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:license-number %)
            (:issuing-authority %)
            (util/trim-nils (segment/ts->field (:expiration-date %))))))

(s/fdef field->dln
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-dln))
             :record (s/nilable ::spec-dln)))

(defn field->dln
  "Accepts an HL7 v2 field of DLN data and returns a single DLN record, a sequence
  or records or nil."
  [field]
  (segment/field-to-type
   field
   map->dln
   #(array-map :license-number (util/get-or-nil % 0)
               :issuing-authority (util/get-or-nil % 1)
               :expiration-date (segment/field->ts (util/get-or-nil % 2)))))

;; Record representing an PID segment
(defrecord record
    [set-id
     patient-id
     patient-id-list
     alternate-id
     patient-name
     mother-maiden-name
     date-of-birth
     admin-gender
     patient-alias
     race
     address
     county
     phone-number-home
     phone-number-business
     primary-language
     marital-status
     religion
     patient-account-number
     social-security-number
     driver-license-number
     mother-id
     ethnic-group
     birth-place
     multiple-birth-indicator
     birth-order
     citizenship
     veteran-military-status
     nationality
     patient-death-date-time
     patient-death-indicator
     identity-unknown-indicator
     identity-reliability-code
     last-update-date-time
     last-update-facility
     species-code
     breed-code
     strain
     production-class-code])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::patient-id ::segment/spec-cx)
(s/def ::patient-id-list ::segment/spec-cx)
(s/def ::alternate-id (s/nilable ::segment/spec-cx))
(s/def ::patient-name ::segment/spec-xpn)
(s/def ::mother-maiden-name (s/nilable ::segment/spec-xpn))
(s/def ::date-of-birth (s/nilable ::segment/spec-ts))
(s/def ::admin-gender (s/nilable string?))
(s/def ::patient-alias (s/nilable ::segment/spec-xpn))
(s/def ::race (s/nilable ::segment/spec-ce))
(s/def ::address (s/nilable ::segment/spec-xad))
(s/def ::county (s/nilable string?))
(s/def ::phone-number-home (s/nilable ::segment/spec-xtn))
(s/def ::phone-number-business (s/nilable ::segment/spec-xtn))
(s/def ::primary-language (s/nilable ::segment/spec-ce))
(s/def ::marital-status (s/nilable ::segment/spec-ce))
(s/def ::religion (s/nilable ::segment/spec-ce))
(s/def ::patient-account-number (s/nilable ::segment/spec-cx))
(s/def ::social-security-number (s/nilable string?))
(s/def ::driver-license-number (s/nilable ::spec-dln))
(s/def ::mother-id (s/nilable ::segment/spec-cx))
(s/def ::ethnic-group (s/nilable ::segment/spec-ce))
(s/def ::birth-place (s/nilable string?))
(s/def ::multiple-birth-indicator (s/nilable string?))
(s/def ::birth-order (s/nilable number?))
(s/def ::citizenship (s/nilable ::segment/spec-ce))
(s/def ::veteran-military-status (s/nilable ::segment/spec-ce))
(s/def ::nationality (s/nilable ::segment/spec-ce))
(s/def ::patient-death-date-time (s/nilable ::segment/spec-ts))
(s/def ::patient-death-indicator (s/nilable string?))
(s/def ::identity-unknown-indicator (s/nilable string?))
(s/def ::identity-reliablility-code (s/nilable string?))
(s/def ::last-update-date-time (s/nilable ::segment/spec-ts))
(s/def ::last-update-facility (s/nilable ::segment/spec-hd))
(s/def ::species-code (s/nilable ::segment/spec-ce))
(s/def ::breed-code (s/nilable ::segment/spec-ce))
(s/def ::strain (s/nilable string?))
(s/def ::production-class-code (s/nilable ::segment/spec-ce))

(s/def ::spec
  (s/keys :req-un [::patient-id-list ::patient-name]
          :opt-un [::set-id ::patient-id ::alternate-id ::mother-maiden-name
                   ::date-of-birth ::admin-gender ::patient-alias ::race
                   ::address ::county ::phone-number-home ::phone-number-business
                   ::primary-language ::marital-status ::religion
                   ::patient-account-number ::social-security-number
                   ::driver-license-number ::mother-id ::ethnic-group
                   ::birth-place ::multiple-birth-indicator ::birth-order
                   ::citizenship ::veteran-military-status ::nationality
                   ::patient-death-date-time ::patient-death-indicator
                   ::identity-unknown-indicator ::identity-reliablility-code
                   ::last-update-date-time ::last-update-facility ::species-code
                   ::breed-code ::strain ::production-class-code]))

(defn record->hl7
  "Accepts a record of patient identification data and returns a map of PID
  segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils (segment/cx->field (:patient-id %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:patient-id-list %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:alternate-id %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:patient-name %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:mother-maiden-name %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:date-of-birth %))))
     (parser/create-field (lookups/gender-by-key (:admin-gender %)))
     (parser/create-field (util/trim-nils (segment/xpn->field (:patient-alias %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:race %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:address %))))
     (parser/create-field (:county %))
     (parser/create-field (util/trim-nils (segment/xtn->field (:phone-number-home %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:phone-number-business %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:primary-language %))))
     (parser/create-field (if (or (string? (:marital-status %)) (keyword? (:marital-status %)))
                            (lookups/marital-status-by-key (:marital-status %))
                            (util/trim-nils (segment/cwe->field (:marital-status %)))))
     (parser/create-field (util/trim-nils (segment/ce->field (:religion %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:patient-account-number %))))
     (parser/create-field (:social-security-number %))
     (parser/create-field (util/trim-nils (dln->field (:drivers-license-number %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:mother-id %))))
     (parser/create-field (if (or (string? (:ethnic-group %)) (keyword? (:ethnic-group %)))
                            (lookups/ethnicity-by-key (:ethnic-group %))
                            (util/trim-nils (segment/cwe->field (:ethnic-group %)))))
     (parser/create-field (:birth-place %))
     (parser/create-field (:multiple-birth-indicator %))
     (parser/create-field (:birth-order %))
     (parser/create-field (util/trim-nils (segment/ce->field (:citizenship %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:veteran-military-status %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:nationality %))))
     (parser/create-field (util/format-time (:patient-death-date-time %)))
     (parser/create-field (lookups/yes-no-by-key (:patient-death-indicator %)))
     (parser/create-field (:identity-unknown-indicator %))
     (parser/create-field (:identity-reliability-code %))
     (parser/create-field (util/format-time (:last-update-date-time %)))
     (parser/create-field (:last-update-facility %))
     (parser/create-field (util/trim-nils (segment/ce->field (:species-code %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:breed-code %))))
     (parser/create-field (:strain %))
     (parser/create-field (util/trim-nils (segment/ce->field (:production-class-code %)))))
   record))

(defn hl7->record
  "Accepts a PID segment of parsed HL7 segment data and returns a PID record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 39
   #(map->record
     {:set-id (util/read-string
               (util/unwrap-and-first (message/get-segment-field % 1)))
      :patient-id (segment/field->cx (message/get-segment-field % 2))
      :patient-id-list (segment/field->cx (message/get-segment-field % 3))
      :alternate-id (segment/field->cx (message/get-segment-field % 4))
      :patient-name (segment/field->xpn
                     (message/get-segment-field % 5))
      :mother-maiden-name (segment/field->xpn (message/get-segment-field % 6))
      :date-of-birth (segment/field->ts (message/get-segment-field % 7))
      :admin-gender (lookups/gender-by-value (util/unwrap-and-first
                                              (message/get-segment-field % 8)))
      :patient-alias (segment/field->xpn
                      (message/get-segment-field % 9))
      :race (segment/field->ce (message/get-segment-field % 10))
      :address (segment/field->xad (message/get-segment-field % 11))
      :county (util/unwrap-and-first (message/get-segment-field % 12))
      :phone-number-home (segment/field->xtn (message/get-segment-field % 13))
      :phone-number-business (segment/field->xtn (message/get-segment-field % 14))
      :primary-language (segment/field->ce (message/get-segment-field % 15))
      :marital-status (segment/field->cwe (message/get-segment-field % 16))
      :religion (segment/field->ce (message/get-segment-field % 17))
      :patient-account-number (segment/field->cx (message/get-segment-field % 18))
      :social-security-number (util/unwrap-and-first (message/get-segment-field % 19))
      :drivers-license-number (field->dln (message/get-segment-field % 20))
      :mother-id (segment/field->cx (message/get-segment-field % 21))
      :ethnic-group (segment/field->cwe (message/get-segment-field % 22))
      :birth-place (util/unwrap-and-first (message/get-segment-field % 23))
      :multiple-birth-indicator (util/unwrap-and-first (message/get-segment-field % 24))
      :birth-order (util/unwrap-and-first (message/get-segment-field % 25))
      :citizenship (segment/field->ce (message/get-segment-field % 26))
      :veteran-military-status (segment/field->ce (message/get-segment-field % 27))
      :nationality (segment/field->ce (message/get-segment-field % 28))
      :patient-death-date-time (util/parse-timestamp (util/unwrap-and-first
                                                      (message/get-segment-field % 29)))
      :patient-death-indicator (lookups/yes-no-by-value (util/unwrap-and-first
                                                         (message/get-segment-field % 30)))
      :identity-unknown-indicator (util/unwrap-and-first (message/get-segment-field % 31))
      :identity-reliability-code (util/unwrap-and-first (message/get-segment-field % 32))
      :last-update-date-time (util/parse-timestamp (util/unwrap-and-first
                                                    (message/get-segment-field % 33)))
      :last-update-facility (util/unwrap-and-first (message/get-segment-field % 34))
      :species-code (segment/field->ce (message/get-segment-field % 35))
      :breed-code (segment/field->ce (message/get-segment-field % 36))
      :strain (util/unwrap-and-first (message/get-segment-field % 37))
      :production-class-code (segment/field->ce (message/get-segment-field % 38))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
