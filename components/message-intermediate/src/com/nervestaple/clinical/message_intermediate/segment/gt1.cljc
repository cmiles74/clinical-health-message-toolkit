(ns com.nervestaple.clinical.message-intermediate.segment.gt1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.common-fields :as common]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; segment identifier
(def SEGMENT-ID "GT1")

;; JCC: Job Code or Class
(defrecord jcc
    [job-code job-class job-description])

(s/def ::job-code (s/nilable string?))
(s/def ::job-class (s/nilable string?))
(s/def ::job-description (s/nilable string?))

(s/def ::spec-jcc
  (s/keys :opt-un [::job-code ::job-class ::job-description]))

(s/fdef jcc->field
  :args (s/or :record (s/cat :jcc-record (s/nilable ::spec-jcc))
               :coll (s/cat :coll (s/coll-of ::spec-jcc)))
  :ret (s/nilable vector?))

(defn jcc->field
  "Accepts one or a sequence of JCC records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
    record
    #(vector
       (:job-code %)
       (:job-class %)
       (:job-description %))))

(s/fdef field->jcc
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-jcc))
             :record (s/nilable ::spec-jcc)))

(defn field->jcc
  "Accepts an HL7 v2 field of JCC data and returns a single JCC record, a sequence
  of records or nil."
  [field]
  (segment/field-to-type
   field
   map->jcc
   #(array-map
     :job-code (util/get-or-nil % 0)
     :job-class (util/get-or-nil % 1)
     :job-description (util/get-or-nil % 1))))

;; FC: Financial Class
(defrecord fc
    [financial-class effective-date])

(s/def ::financial-class (s/nilable ::segment/spec-cwe))
(s/def ::effective-date (s/nilable ::segment/spec-ts))

(s/def ::spec-fc
  (s/keys :req-un [::financial-class]
          :opt-un [::effective-date]))

(s/fdef fc->field
  :args (s/or :record (s/cat :jcc-record (s/nilable ::spec-fc))
              :coll (s/cat :coll (s/coll-of ::spec-fc)))
  :ret (s/nilable vector?))

(defn fc->field
  "Accepts one or a sequence of FC records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(vector
     (util/trim-nils (segment/cwe->field (:financial-class %)))
     (util/trim-nils (segment/ts->field (:effective-date %))))))

(s/fdef field->fc
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-fc))
             :record (s/nilable ::spec-fc)))

(defn field->fc
  "Accepts an HL7 v2 field of FCC data and returns a single FCC record, a sequence
  of records or nil."
  [field]
  (segment/field-to-type
    field
    map->fc
    #(array-map
       :financial-class (segment/field->cwe (util/get-or-nil % 0))
       :effective-date (segment/field->ts (util/get-or-nil % 1)))))

;; record representing a GT1 segment
(defrecord record
           [set-id
            guarantor-number
            guarantor-name
            guarantor-spouse-name
            guarantor-address
            guarantor-phone-home
            guarantor-phone-business
            guarantor-birth-date
            guarantor-administrative-sex
            guarantor-type
            guarantor-relationship
            guarantor-social-security-number
            guarantor-begin-date
            guarantor-end-date
            guarantor-priority
            guarantor-employer-name
            guarantor-employer-address
            guarantor-employer-phone
            guarantor-employee-identifier
            guarantor-employment-status
            guarantor-organization
            guarantor-billing-hold-flag
            guarantor-credit-rating
            guarantor-death-date
            guarantor-death-flag
            guarantor-charge-adjustment
            guarantor-household-annual-income
            guarantor-household-size
            guarantor-employer-identifier
            guarantor-marital-status-code
            guarantor-hire-effective-date
            employment-stop-date
            living-dependency-coded
            ambulatory-status
            citizenship
            primary-language
            living-arrangement
            publicity-code
            protection-indicator
            student-indicator-coded
            religion
            mother-maiden-name
            nationality
            ethnic-group
            contact-person
            contact-person-phone
            contact-reason
            job-title
            job-code-class
            guarantor-employer-organization
            handicap-coded
            job-status
            guarantor-financial-class
            guarantor-race
            guarantor-birth-place
            vip-indicator])

(s/def ::set-id ::common/set-id)
(s/def ::guarantor-number (s/nilable ::segment/spec-cx))
(s/def ::guarantor-name (s/nilable ::segment/spec-xpn))
(s/def ::guarantor-spouse-name (s/nilable ::segment/spec-xpn))
(s/def ::guarantor-address (s/nilable ::segment/spec-xad))
(s/def ::guarantor-phone-home (s/nilable ::segment/spec-xtn))
(s/def ::guarantor-phone-business (s/nilable ::segment/spec-xtn))
(s/def ::guarantor-birth-date (s/nilable ::segment/spec-ts))
(s/def ::guarantor-administrative-sex (s/nilable string?))
(s/def ::guarantor-type (s/nilable string?))
(s/def ::guarantor-relationship (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-social-security-number (s/nilable string?))
(s/def ::guarantor-begin-date (s/nilable ::segment/spec-ts))
(s/def ::guarantor-end-date (s/nilable ::segment/spec-ts))
(s/def ::guarantor-priority (s/nilable string?))
(s/def ::guarantor-employer-name (s/nilable ::segment/spec-xpn))
(s/def ::guarantor-employer-address (s/nilable ::segment/spec-xad))
(s/def ::guarantor-employer-phone (s/nilable ::segment/spec-xtn))
(s/def ::guarantor-employee-identifier (s/nilable ::segment/spec-cx))
(s/def ::guarantor-employment-status (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-organization (s/nilable ::segment/spec-xon))
(s/def ::guarantor-billing-hold-flag (s/nilable string?))
(s/def ::guarantor-credit-rating (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-death-date (s/nilable ::segment/spec-ts))
(s/def ::guarantor-death-flag (s/nilable string?))
(s/def ::guarantor-charge-adjustment (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-household-annual-income (s/nilable ::segment/spec-ce))
(s/def ::guarantor-household-size (s/nilable number?))
(s/def ::guarantor-employer-identifier (s/nilable ::segment/spec-cx))
(s/def ::guarantor-marital-status-code (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-hire-effective-date (s/nilable ::segment/spec-ts))
(s/def ::employment-stop-date (s/nilable ::segment/spec-ts))
(s/def ::living-dependency-coded (s/nilable ::segment/spec-cwe))
(s/def ::ambulatory-status (s/nilable ::segment/spec-cwe))
(s/def ::citizenship (s/nilable ::segment/spec-cwe))
(s/def ::primary-language (s/nilable ::segment/spec-cwe))
(s/def ::living-arrangement (s/nilable ::segment/spec-cwe))
(s/def ::publicity-code ::common/publicity-code)
(s/def ::protection-indicator (s/nilable string?))
(s/def ::student-indicator-coded (s/nilable ::segment/spec-cwe))
(s/def ::religion (s/nilable ::segment/spec-cwe))
(s/def ::mother-maiden-name (s/nilable ::segment/spec-xpn))
(s/def ::nationality (s/nilable ::segment/spec-cwe))
(s/def ::ethnic-group (s/nilable ::segment/spec-cwe))
(s/def ::contact-person (s/nilable ::segment/spec-xpn))
(s/def ::contact-person-phone (s/nilable ::segment/spec-xtn))
(s/def ::contact-reason (s/nilable ::segment/spec-cwe))
(s/def ::contact-relationship (s/nilable ::segment/spec-cwe))
(s/def ::job-title (s/nilable string?))
(s/def ::job-code-class (s/nilable ::spec-jcc))
(s/def ::guarantor-employer-organization (s/nilable ::segment/spec-xon))
(s/def ::handicap-coded (s/nilable ::segment/spec-cwe))
(s/def ::job-status (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-financial-class (s/nilable ::spec-fc))
(s/def ::guarantor-race (s/nilable ::segment/spec-cwe))
(s/def ::guarantor-birth-place (s/nilable string?))
(s/def ::vip-indicator (s/nilable ::segment/spec-cwe))

(s/def ::spec
  (s/keys :req-un [::set-id ::guarantor-name]
          :opt-un [::guarantor-number
                   ::guarantor-spouse-name
                   ::guarantor-address
                   ::guarantor-phone-home
                   ::guarantor-phone-business
                   ::guarantor-birth-date
                   ::guarantor-administrative-sex
                   ::guarantor-type
                   ::guarantor-relationship
                   ::guarantor-social-security-number
                   ::guarantor-begin-date
                   ::guarantor-end-date
                   ::guarantor-priority
                   ::guarantor-employer-name
                   ::guarantor-employer-address
                   ::guarantor-employer-phone
                   ::guarantor-employee-identifier
                   ::guarantor-employment-status
                   ::guarantor-organization
                   ::guarantor-billing-hold-flag
                   ::guarantor-credit-rating
                   ::guarantor-death-date
                   ::guarantor-death-flag
                   ::guarantor-charge-adjustment
                   ::guarantor-household-annual-income
                   ::guarantor-household-size
                   ::guarantor-employer-identifier
                   ::guarantor-marital-status-code
                   ::guarantor-hire-effective-date
                   ::employment-stop-date
                   ::living-dependency-coded
                   ::ambulatory-status
                   ::citizenship
                   ::primary-language
                   ::living-arrangement
                   ::publicity-code
                   ::protection-indicator
                   ::student-indicator-coded
                   ::religion
                   ::mother-maiden-name
                   ::nationality
                   ::ethnic-group
                   ::contact-person
                   ::contact-person-phone
                   ::contact-reason
                   ::contact-relationship
                   ::job-title
                   ::job-code-class
                   ::guarantor-employer-organization
                   ::handicap-coded
                   ::job-status
                   ::guarantor-financial-class
                   ::guarantor-race
                   ::guarantor-birth-place
                   ::vip-indicator]))

(defn record->hl7
  "Accepts a record of patient guarantor data and returns a map of GT1 segment
  data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils (segment/cx->field (:guarantor-number %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:guarantor-name %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:guarantor-spouse-name %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:guarantor-address %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:guarantor-phone-home %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:guarantor-phone-business %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:guarantor-birth-date %))))
     (parser/create-field (lookups/gender-by-key (:guarantor-administrative-sex %)))
     (parser/create-field (:guarantor-type %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-relationship %))))
     (parser/create-field (:guarantor-social-security-number %))
     (parser/create-field (util/trim-nils (segment/ts->field (:guarantor-begin-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:guarantor-end-date %))))
     (parser/create-field (:guarantor-priority %))
     (parser/create-field (util/trim-nils (segment/xpn->field (:guarantor-employer-name %))))
     (parser/create-field (util/trim-nils (segment/xad->field (:guarantor-employer-address %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:guarantor-employer-phone %))))
     (parser/create-field (util/trim-nils (segment/cx->field (:guarantor-employee-identifier %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-employment-status %))))
     (parser/create-field (util/trim-nils (segment/xon->field (:guarantor-organization %))))
     (parser/create-field (:guarantor-billing-hold-flag %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-credit-rating %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:guarantor-death-date %))))
     (parser/create-field (:guarantor-death-flag %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-charge-adjustment %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:guarantor-household-annual-income %))))
     (parser/create-field (:guarantor-household-size %))
     (parser/create-field (util/trim-nils (segment/cx->field (:guarantor-employer-identifier %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-marital-status-code %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:guarantor-hire-effective-date %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:employment-stop-date %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:living-dependency %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:ambulatory-status %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:citizenship %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:primary-language %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:living-arrangement %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:publicity-code %))))
     (parser/create-field (:protection-indicator %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:student-indicator-coded %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:religion %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:mother-maiden-name %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:nationality %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:ethnic-group %))))
     (parser/create-field (util/trim-nils (segment/xpn->field (:contact-person %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:contact-person-phone %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:contact-reason %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:contact-relationship %))))
     (parser/create-field (:job-title %))
     (parser/create-field (util/trim-nils (jcc->field (:job-code-class %))))
     (parser/create-field (util/trim-nils (segment/xon->field (:guarantor-employer-organization %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:handicap-coded %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:job-status %))))
     (parser/create-field (util/trim-nils (fc->field (:guarantor-financial-class %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:guarantor-race %))))
     (parser/create-field (:guarantor-birth-place %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:vip-indicator %)))))
   record))

(defn hl7->record
  "Accepts a GT1 segment of parsed HL7 segment data and returns a GT1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 58
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :guarantor-number (segment/field->cx (message/get-segment-field % 2))
      :guarantor-name (segment/field->xpn (message/get-segment-field % 3))
      :guarantor-spouse-name (segment/field->xpn (message/get-segment-field % 4))
      :guarantor-address (segment/field->xad (message/get-segment-field % 5))
      :guarantor-phone-home (segment/field->xtn (message/get-segment-field % 6))
      :guarantor-phone-business (segment/field->xtn (message/get-segment-field % 7))
      :guarantor-birth-date (segment/field->ts (message/get-segment-field % 8))
      :guarantor-administrative-sex (lookups/gender-by-value (util/unwrap-and-first (message/get-segment-field % 9)))
      :guarantor-type (util/unwrap-and-first (message/get-segment-field % 10))
      :guarantor-relationship (segment/field->cwe (message/get-segment-field % 11))
      :guarantor-social-security-number (util/unwrap-and-first (message/get-segment-field % 12))
      :guarantor-begin-date (segment/field->ts (message/get-segment-field % 13))
      :guarantor-end-date (segment/field->ts (message/get-segment-field % 14))
      :guarantor-priority (util/unwrap-and-first (message/get-segment-field % 15))
      :guarantor-employer-name (segment/field->xpn (message/get-segment-field % 16))
      :guarantor-employer-address (segment/field->xad (message/get-segment-field % 17))
      :guarantor-employer-phone (segment/field->xtn (message/get-segment-field % 18))
      :guarantor-employee-identifier (segment/field->cx (message/get-segment-field % 19))
      :guarantor-employment-status (segment/field->cwe (message/get-segment-field % 20))
      :guarantor-organization (segment/field->xon (message/get-segment-field % 21))
      :guarantor-billing-hold-flag (util/unwrap-and-first (message/get-segment-field % 22))
      :guarantor-credit-rating (segment/field->cwe (message/get-segment-field % 23))
      :guarantor-death-date (segment/field->ts (message/get-segment-field % 24))
      :guarantor-death-flag (segment/field->ts (message/get-segment-field % 25))
      :guarantor-charge-adjustment (segment/field->cwe (message/get-segment-field % 26))
      :guarantor-household-annual-income (util/read-string (util/unwrap-and-first (message/get-segment-field % 27)))
      :guarantor-household-size (util/read-string (util/unwrap-and-first (message/get-segment-field % 28)))
      :guarantor-employer-identifier (segment/field->cx (message/get-segment-field % 29))
      :guarantor-marital-status-code (segment/field->cwe (message/get-segment-field % 30))
      :guarantor-hire-effective-date (segment/field->ts (message/get-segment-field % 31))
      :employment-stop-date (segment/field->ts (message/get-segment-field % 32))
      :living-dependency (segment/field->cwe (message/get-segment-field % 33))
      :ambulatory-status (segment/field->cwe (message/get-segment-field % 34))
      :citizenship (segment/field->cwe (message/get-segment-field % 35))
      :primary-language (segment/field->cwe (message/get-segment-field % 36))
      :living-arrangement (segment/field->cwe (message/get-segment-field % 37))
      :publicity-code (segment/field->cwe (message/get-segment-field % 38))
      :protection-indicator (util/unwrap-and-first (message/get-segment-field % 39))
      :student-indicator-coded (segment/field->cwe (message/get-segment-field % 40))
      :religion (segment/field->cwe (message/get-segment-field % 41))
      :mother-maiden-name (segment/field->xpn (message/get-segment-field % 42))
      :nationality (segment/field->cwe (message/get-segment-field % 43))
      :ethnic-group (segment/field->cwe (message/get-segment-field % 44))
      :contact-person (segment/field->xpn (message/get-segment-field % 45))
      :contact-person-phone (segment/field->xtn (message/get-segment-field % 46))
      :contact-reason (segment/field->cwe (message/get-segment-field % 47))
      :contact-relationship (segment/field->cwe (message/get-segment-field % 48))
      :job-title (util/unwrap-and-first (message/get-segment-field % 49))
      :job-code-class (field->jcc (message/get-segment-field % 50))
      :guarantor-employer-organization (segment/field->xon (message/get-segment-field % 51))
      :handicap-coded (segment/field->cwe (message/get-segment-field % 52))
      :job-status (segment/field->cwe (message/get-segment-field % 53))
      :guarantor-financial-class (field->fc (message/get-segment-field % 54))
      :guarantor-race (segment/field->cwe (message/get-segment-field % 55))
      :guarantor-birth-place (util/unwrap-and-first (message/get-segment-field % 56))
      :vip-indicator (segment/field->cwe (message/get-segment-field % 57))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
    (merge {:id SEGMENT-ID}
           data-map)))
