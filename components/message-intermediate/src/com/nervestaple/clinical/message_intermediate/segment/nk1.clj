(ns com.nervestaple.clinical.message-intermediate.segment.nk1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]))

;; Segment identifier
(def SEGMENT-ID "NK1")

;; JCC: job code or class
(defrecord jcc
    [job-code job-class])

(s/def ::job-code (s/nilable string?))
(s/def ::job-class (s/nilable string?))

(s/def ::spec-jcc
  (s/keys :opt-un [::job-code ::job-class]))

(s/fdef jcc->field
  :args (s/or :record (s/cat :xpn-record (s/nilable ::spec-jcc))
              :coll (s/cat :coll (s/coll-of ::spec-jcc)))
  :ret (s/nilable vector?))

(defn jcc->field
  "Accepts one or a sequence of JCC records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector (:job-code %)
            (:job-class %))))

(s/fdef field->jcc
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-jcc))
             :record (s/nilable ::spec-jcc)))

(defn field->jcc
  "Accepts an HL7 v2 field of JCC data and returns a single JCC record, a
  sequence of records or nil."
  [field]
  (segment/field-to-type
   field
   map->jcc
   #(array-map :job-code (util/get-or-nil % 0)
               :job-class (util/get-or-nil % 1))))

;; Record representing the NK1 segment
(defrecord record
    [set-id associated-party-name relationship address phone-number
     business-phone-number contact-role start-date end-date
     associated-party-job-title associated-party-job-code
     associated-party-employee-number associated-party-organization-name
     martial-status administrative-gender birth-date living-dependency
     ambulatory-status citizenship primary-language living-arrangement
     publicity-code protection-indicator student-indicator religion
     mother-maiden-name nationality ethnic-group contact-reason
     contact-person-name contact-person-telephone contact-person-address
     associated-party-identifiers
     job-status race handicap contact-person-social-security-number])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::associated-party-name (s/nilable ::segment/spec-xpn))
(s/def ::relationship (s/nilable ::segment/spec-ce))
(s/def ::address (s/nilable ::segment/spec-xad))
(s/def ::phone-number (s/nilable ::segment/spec-xtn))
(s/def ::business-phone-number (s/nilable ::segment/spec-xtn))
(s/def ::contact-role (s/nilable ::segment/spec-ce))
(s/def ::start-date (s/nilable ::segment/spec-ts))
(s/def ::end-date (s/nilable ::segment/spec-ts))
(s/def ::associated-party-job-title (s/nilable string?))
(s/def ::associated-party-job-code (s/nilable ::spec-jcc))
(s/def ::associated-party-employee-number (s/nilable ::segment/spec-ce))
(s/def ::associated-party-organization-name (s/nilable ::segment/spec-xon))
(s/def ::marital-status (s/nilable ::segment/spec-ce))
(s/def ::administrative-gender (s/nilable string?))
(s/def ::birth-date (s/nilable ::segment/spec-ts))
(s/def ::living-dependency (s/nilable string?))
(s/def ::ambulatory-status (s/nilable string?))
(s/def ::citizenship (s/nilable ::segment/spec-ce))
(s/def ::primary-language (s/nilable ::segment/spec-ce))
(s/def ::living-arrangement (s/nilable string?))
(s/def ::publicity-code (s/nilable ::segment/spec-ce))
(s/def ::protection-indicator (s/nilable string?))
(s/def ::student-indicator (s/nilable string?))
(s/def ::religion (s/nilable ::segment/spec-ce))
(s/def ::mother-maiden-name (s/nilable ::segment/spec-xpn))
(s/def ::nationality (s/nilable ::segment/spec-ce))
(s/def ::ethnic-group (s/nilable ::segment/spec-ce))
(s/def ::contact-reason (s/nilable ::segment/spec-ce))
(s/def ::contact-person-name (s/nilable ::segment/spec-xpn))
(s/def ::contact-person-telephone (s/nilable ::segment/spec-xtn))
(s/def ::contact-person-address (s/nilable ::segment/spec-xad))
(s/def ::associated-party-identifiers (s/nilable ::segment/spec-cx))
(s/def ::job-status (s/nilable string?))
(s/def ::race (s/nilable ::segment/spec-ce))
(s/def ::handicap (s/nilable string?))
(s/def ::contact-person-social-security-number (s/nilable string?))

(s/def ::spec
  (s/keys :req-un [::set-id]
          :opt-un [::associated-party-name ::relationship ::address ::phone-number
                   ::business-phone-number ::contact-role ::start-date ::end-date
                   ::associated-party-job-title ::associated-party-job-code
                   ::associated-party-employee-number
                   ::associated-party-organization-name ::marital-status
                   ::administrative-gender ::birth-date ::living-dependency
                   ::ambulatory-status ::citizenship ::primary-language
                   ::living-arrangement ::publicity-code ::protection-indicator
                   ::student-indicator ::religion ::mother-maiden-name ::nationality
                   ::ethnic-group ::contact-reason ::contact-person-name
                   ::contact-person-telephone ::contact-person-address
                   ::associated-party-identifiers
                   ::job-status ::race ::handicap
                   ::contact-person-social-security-number]))

(defn record->hl7
  "Accepts a record of patient next of kin data and returns a map of NK1 segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils (segment/xpn->field
                                           (:associated-party-name %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:relationship %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:address %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:phone-number %))))
     (parser/create-field (util/trim-nils
                           (segment/xtn->field (:business-phone-number %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:contact-role %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:start-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:end-date %))))
     (parser/create-field (:associated-party-job-title %))
     (parser/create-field (util/trim-nils
                           (jcc->field (:associated-party-job-code %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:associated-party-employee-number %))))
     (parser/create-field (util/trim-nils
                           (segment/xon->field (:associated-party-organization-name %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:marital-status %))))
     (parser/create-field (lookups/gender-by-key (:administrative-gender %)))
     (parser/create-field (util/trim-nils (segment/ts->field (:birth-date %))))
     (parser/create-field (:living-dependency %))
     (parser/create-field (:ambulatory-status %))
     (parser/create-field (util/trim-nils (segment/ce->field (:citizenship %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:primary-language %))))
     (parser/create-field (:living-arrangement %))
     (parser/create-field (util/trim-nils (segment/ce->field (:publicity-code %))))
     (parser/create-field (:protection-indicator %))
     (parser/create-field (:student-indicator %))
     (parser/create-field (util/trim-nils (segment/ce->field (:religion %))))
     (parser/create-field (util/trim-nils
                           (segment/xpn->field (:mother-maiden-name %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:nationality %))))
     (parser/create-field (lookups/ethnicity-by-key (:ethnic-group %)))
     (parser/create-field (util/trim-nils (segment/ce->field (:contact-reason %))))
     (parser/create-field (util/trim-nils
                           (segment/xpn->field (:contact-person-name %))))
     (parser/create-field (util/trim-nils
                           (segment/xtn->field (:contact-person-telephone %))))
     (parser/create-field (util/trim-nils
                           (segment/xad->field (:contact-person-address %))))
     (parser/create-field (util/trim-nils
                           (segment/cx->field (:associated-party-identifiers %))))
     (parser/create-field (:job-status %))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (segment/race-map (:race %)))))
     (parser/create-field (:handicap %))
     (parser/create-field (:contact-person-social-security-number %)))
   record))

(defn hl7->record
  "Accepts an NK1 segment of parsed HL7 segment data and returns an NK1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 38
   #(map->record
     {:set-id (util/read-string
               (util/unwrap-and-first (message/get-segment-field % 1)))
      :associated-party-name (segment/field->xpn (message/get-segment-field % 2))
      :relationship (segment/field->ce (message/get-segment-field % 3))
      :address (segment/field->xad (message/get-segment-field % 4))
      :phone-number (segment/field->xtn (message/get-segment-field % 5))
      :business-phone-number (segment/field->xtn (message/get-segment-field % 6))
      :contact-role (segment/field->ce (message/get-segment-field % 7))
      :start-date (segment/field->ts (message/get-segment-field % 8))
      :end-date (segment/field->ts (message/get-segment-field % 9))
      :associated-party-job-title (util/unwrap-and-first (message/get-segment-field % 10))
      :associated-party-job-code (field->jcc (message/get-segment-field % 11))
      :associated-party-employee-number (segment/field->ce (message/get-segment-field % 12))
      :associated-party-organization-name (segment/field->xon (message/get-segment-field % 13))
      :marital-status (segment/field->ce (message/get-segment-field % 14))
      :administrative-gender (lookups/gender-by-value
                              (util/unwrap-and-first (message/get-segment-field % 15)))
      :birth-date (segment/field->ts (message/get-segment-field % 16))
      :living-dependency (util/unwrap-and-first (message/get-segment-field % 17))
      :ambulatory-status (util/unwrap-and-first (message/get-segment-field % 18))
      :citizenship (segment/field->ce (message/get-segment-field % 19))
      :primary-language (segment/field->ce (message/get-segment-field % 20))
      :living-arrangement (util/unwrap-and-first (message/get-segment-field % 21))
      :publicity-code (segment/field->ce (message/get-segment-field % 22))
      :protection-indicator (util/unwrap-and-first (message/get-segment-field % 23))
      :student-indicator (util/unwrap-and-first (message/get-segment-field % 24))
      :religion (segment/field->ce (message/get-segment-field % 25))
      :mother-maiden-name (segment/field->xpn (message/get-segment-field % 26))
      :nationality (segment/field->ce (message/get-segment-field % 27))
      :ethnic-group (lookups/ethnicity-by-value
                     (util/unwrap-and-first (message/get-segment-field % 28)))
      :contact-reason (segment/field->ce (message/get-segment-field % 29))
      :contact-person-name (segment/field->xpn (message/get-segment-field % 30))
      :contact-person-telephone (segment/field->xtn (message/get-segment-field % 31))
      :contact-person-address (segment/field->xad (message/get-segment-field % 32))
      :associated-party-identifiers (segment/field->cx (message/get-segment-field % 33))
      :job-status (util/unwrap-and-first (message/get-segment-field % 34))
      :race (segment/field->ce (message/get-segment-field % 35))
      :handicap (util/unwrap-and-first (message/get-segment-field % 36))
      :contact-person-social-security-number
      (util/unwrap-and-first (message/get-segment-field % 37))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
