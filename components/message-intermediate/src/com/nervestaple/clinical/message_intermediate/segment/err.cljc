(ns com.nervestaple.clinical.message-intermediate.segment.err
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "ERR")

;; ELD: error location data
(defrecord eld
    [segment-id segment-sequence field-position code-error])

(s/def ::segment-id (s/nilable string?))
(s/def ::segment-sequence (s/nilable number?))
(s/def ::field-position (s/nilable number?))
(s/def ::code-error (s/nilable ::segment/spec-ce))

(s/def ::spec-eld
  (s/keys :opt-un [::segment-id ::segment-sequence ::field-position
                   ::code-error]))

(s/fdef eld->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-eld))
              :coll (s/cat :coll (s/coll-of ::spec-eld)))
  :ret (s/nilable vector?))

(defn eld->field
  "Accepts one or a sequence of ELD records and returns a value, collection or map
  of HL7 v2 data."
  [eld-record]
  (segment/type-to-field eld-record
                         #(vector (:segment-id %)
                                  (:segment-sequence %)
                                  (:field-position %)
                                  (segment/ce->field (:code-error %)))))

(s/fdef field->eld
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-eld))
             :record (s/nilable ::spec-eld)))

(defn field->eld
  "Accepts an HL7 v2 field of ELD data and returns a single ELD record or a
  sequence of records or nil."
  [field]
  (segment/field-to-type field
                         map->eld
                         #(array-map :segment-id (util/get-or-nil % 0)
                                     :segment-sequence (util/read-string (util/get-or-nil % 1))
                                     :field-position (util/read-string (util/get-or-nil % 2))
                                     :code-error (segment/field->ce (util/get-or-nil % 3)))))

;; ERL: error location
(defrecord erl
    [segment-id segment-sequence field-position field-repetition
     component-number sub-component-number])

(s/def ::field-repetition (s/nilable number?))
(s/def ::component-number (s/nilable number?))
(s/def ::sub-component-number (s/nilable number?))

(s/def ::spec-erl
  (s/keys :req-un [::segment-id]
          :opt-un [::segment-sequence ::field-position ::field-repetition
                   ::component-number ::sub-component-number]))

(s/fdef erl->field
  :args (s/or :record (s/cat :ce-record (s/nilable ::spec-erl))
              :coll (s/cat :coll (s/coll-of ::spec-erl)))
  :ret (s/nilable vector?))

(defn erl->field
  "Accepts one or a sequence of ERL records and returns a value, collection or map
  of HL7 v2 data."
  [erl-record]
  (segment/type-to-field erl-record
                         #(vector (:segment-id %)
                                  (:segment-sequence %)
                                  (:field-position %)
                                  (:field-repetition %)
                                  (:component-number %)
                                  (:sub-component-number %))))

(s/fdef field->erl
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-erl))
             :record (s/nilable ::spec-erl)))

(defn field->erl
  "Accepts an HL7 v2 field of ERL data and returns a single ERL record or a
  sequence of records or nil."
  [field]
  (segment/field-to-type field
                         map->erl
                         #(array-map :segment-id (util/get-or-nil % 0)
                                     :segment-sequence (util/read-string (util/get-or-nil % 1))
                                     :field-position (util/read-string (util/get-or-nil % 2))
                                     :field-repetition (util/read-string (util/get-or-nil % 3))
                                     :component-number (util/read-string (util/get-or-nil % 4))
                                     :sub-component-number (util/read-string (util/get-or-nil % 5)))))

;; Record represeting an ERR segment
(defrecord record
    [error-code-location
     error-location
     error-code
     severity
     application-error-code
     application-error-parameter
     diagnostic-information
     user-message])

(s/def ::error-code-location (s/nilable ::spec-eld))
(s/def ::error-location (s/nilable ::spec-erl))
(s/def ::error-code ::segment/spec-ce)
(s/def ::severity string?)
(s/def ::application-error-code (s/nilable ::segment/spec-ce))
(s/def ::application-error-parameter (s/and (s/nilable string?)
                                            #(>= 80 (count %))))
(s/def ::diagnostic-information (s/and (s/nilable string?)
                                       #(>= 2048 (count %))))
(s/def ::user-message (s/and (s/nilable string?)
                             #(>= 250 (count %))))

(s/def ::spec
  (s/keys :req-un [::error-code ::severity]
          :opt-un [::error-code-location ::error-location
                   ::application-error-code ::application-error-parameter
                   ::diagnostic-information ::user-message]))

(defn record->hl7
  "Accepts a record of error (ERR) segment data and returns an ERR record."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils (eld->field (:error-code-location %))))
     (parser/create-field (util/trim-nils (erl->field (:error-location %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:error-code %))))
     (parser/create-field (:severity %))
     (parser/create-field (util/trim-nils (segment/ce->field (:application-error-code %))))
     (parser/create-field (:application-error-parameter %))
     (parser/create-field (:diagnostic-information %))
     (parser/create-field (:user-message %)))
   record))

(defn hl7->record
  "Accepts an ERR segment of parsed HL7 data and retirns an error record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 9
   #(map->record
     {:error-code-location (field->eld (message/get-segment-field % 1))
      :error-location (field->erl (message/get-segment-field % 2))
      :error-code (segment/field->ce (message/get-segment-field % 3))
      :severity (first (message/get-segment-field % 4))
      :application-error-code (segment/field->ce (message/get-segment-field % 5))
      :application-error-parameter (first (message/get-segment-field % 6))
      :diagnostic-information (first (message/get-segment-field % 7))
      :user-message (first (message/get-segment-field % 8))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
