(ns com.nervestaple.clinical.message-intermediate.segment.main
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.date-time :as date-time]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as segment-util]
   [java-time.api :as time]))

(defn to-field-remainder
  "Accepts a function that converts a record into a field of parsed HL7 v2 data
  and returns another function that converts the record, in additon any data on
  the :remainder key of the record to be converted is appended to the end of
  returned HL7 v2 data."
  [to-field-fn]
  #(let [field (to-field-fn %)]
     (if (not-empty (:remainder %))
       (into field (:remainder %))
       field)))

(defn type-to-field
  "Converts a record into a field of parsed HL7 data by applying the
  \"to-field-fn\" to the supplied record of data. If a sequence of records is
  provided then the \"to-field-fn\" is applied to each record and a sequence of
  parsed HL7 fields is returned."
  [record to-field-fn]
  (let [to-field-fn-out (to-field-remainder to-field-fn)]
    (cond (sequential? record)
          (mapv #(parser/create-field (to-field-fn-out %)) record)

          (and (map? record)
               (segment-util/map-nil-or-empty record))
          []

          :else
          (to-field-fn-out record))))

(defn field-to-map
  "Converts a field of parsed HL7 data into a map of data by unwrapping the data
  in the HL7 field and then applying the result to the `to-map-fn` function. If
  the HL7 field contains repeating fields then they will be unwrapped, each
  applied to the `to-map-fn`, and a sequence of maps will be returned. If the HL7
  record contains only one data item then that item will be passed in a vector
  to the `to-map-fn`.

  Any field data that is not included in the record will be present on the
  record under the key `:remainder`."
  [field to-map-fn]
  (let [data (segment-util/unwrap-field field)
        field (cond (segment-util/is-field? field)   ;; repeating field, one record
                    (to-map-fn field)

                    (or (nil? field) (= [] field))   ;; nil or empty collection, nil record
                    nil

                    (segment-util/all-vectors? data) ;; vector of data, multiple records
                    (mapv to-map-fn data)

                    (vector? data)                   ;; one vector, one record
                    (to-map-fn data)

                    :else                            ;; one data item, one record with one value
                    (to-map-fn [data]))
        remainder (when (coll? data)                 ;; collect unused data as our "remainder"
                    (vec (drop (count (keys field)) data)))]
    (if (not-empty remainder)
      (assoc field :remainder remainder)
      field)))

(defn field-to-type
  "Converts a field of parsed HL7 data into one or a series of maps of data with
  the `to-map-fn` and then applies each map to the `to-rec-fn` returning either
  one record or a map of records. If sub-fields or repeating fields are
  encountered they will be applied to the `to-type-fn` recursively."
  [field to-rec-fn to-map-fn]
  (when field
    (let [maps (field-to-map field to-map-fn)]
      (if (vector? maps)
        (mapv to-rec-fn maps)
        (when maps (to-rec-fn maps))))))

;; parsed HL7 message content is a collection of strings
(s/def ::content (s/or :atom (s/nilable string?)
                       :coll (s/coll-of (s/or :atom (s/nilable string?)
                                              :coll (s/coll-of (s/nilable string?))))))

;; parsed HL7 message fields are one or a collection of HL7 message content
(s/def ::spec-hl7-field
  (s/keys :req-un [::content]))

;; we can parse either parsed HL7 message content, vector of strings or a string
(s/def ::hl7-content (s/or :flat ::content                                     ;; ["A" "B" "C"]
                           :flat-repeating (s/coll-of ::content)               ;; [["A" "B"]["C"]]
                           :fields (s/coll-of ::spec-hl7-field)                ;; [{:content [...]}]
                           :repeating (s/coll-of (s/coll-of ::spec-hl7-field)) ;; [[{:content ...}{...}]]
                           :field ::spec-hl7-field                             ;; {:content ["A" "B"]}
                           :unwrapped (s/nilable string?)))                    ;; "ABCDEFG"

;; TS: timestamp
(defrecord ts
    [time precision])

(s/def ::time (s/or :zoned (s/nilable ::date-time/zoned-date-time)
                    :local (s/nilable ::date-time/local-date-time)
                    :instant (s/nilable ::date-time/instant)
                    :offset (s/nilable ::date-time/offset-date-time)
                    :date (s/nilable ::date-time/local-date)
                    :timestamp (s/nilable ::date-time/timestamp)))
(s/def ::precision (s/nilable
                    (s/with-gen (s/and string? #(= 1 (count %)))
                      #(gen/fmap identity (s/gen #{"S"})))))

(s/def ::spec-ts
  (s/keys :req-un [::time]
          :opt-un [::precision]))

(defn ts->field
  "Accepts one or a sequence of TS records and returns a value, collection or map
  of HL7 v2 data."
  [ts-record]
  (type-to-field ts-record
                 #(vector (segment-util/format-time (:time %))
                          (:precision %))))

(s/fdef field->ts
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-ts))
             :record (s/nilable ::spec-ts)))

(defn field->ts
  "Accepts an HL7 v2 field of TS data and returns a single TS record or a sequence
  of records or nil."
  [field]
  (field-to-type field
                 map->ts
                 #(array-map :time (segment-util/parse-timestamp
                                    (segment-util/get-or-nil % 0))
                             :precision (segment-util/get-or-nil % 1))))

(defn time->ts
  "Accepts a zonded date time or a local data time and returns a TS record."
  [date-time]
  (map->ts {:time date-time}))

(defn now->ts
  "Returns a TS record with the current date and time."
  []
  (map->ts {:time (time/zoned-date-time)}))

;; DR: Date/Time Range
(defrecord dr
    [date-time-start date-time-end])

(s/def ::date-time-start (s/nilable ::spec-ts))
(s/def ::date-time-end (s/nilable ::spec-ts))

(s/def ::spec-dr
  (s/keys :opt-un [::date-time-start ::date-time-end]))

(s/fdef dr->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-dr))
              :coll (s/cat :coll (s/coll-of ::spec-dr)))
  :ret (s/nilable vector?))

(defn dr->field
  "Accepts one or a sequence of DR records and returns a value, collection or map
  of HL7 v2 data."
  [dr-record]
  (type-to-field dr-record
                 #(vec (concat (segment-util/trim-nils (ts->field (:date-time-start %)))
                               (segment-util/trim-nils (ts->field (:date-time-end %)))))))

(s/fdef field->dr
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-dr))
             :record (s/nilable ::spec-dr)))

(defn field->dr
  "Accepts an HL7 v2 field of DR data and returns a single DR record or a sequence
  of records or nil."
  [field]
  (field-to-type field
                 map->dr
                 #(array-map :date-time-start (field->ts (segment-util/get-or-nil % 0))
                             :date-time-end (field->ts (segment-util/get-or-nil % 1)))))

(defn time->dr
  "Accepts two Java time instances (a start and an end) and returns DR record"
  [start end]
  (map->dr {:date-time-start (time->ts start)
            :date-time-end (time->ts end)}))

;; CE: coded element
(defrecord ce
    [identifier
     text
     coding-system
     alternate-identifier
     alternate-text
     alternate-coding-system])

(s/def ::identifier (s/nilable string?))
(s/def ::text (s/nilable string?))
(s/def ::coding-system (s/nilable string?))
(s/def ::alternate-identifier (s/nilable string?))
(s/def ::alternate-text (s/nilable string?))
(s/def ::alternate-coding-system (s/nilable string?))

(s/def ::spec-ce
  (s/keys :req-un [::identifier]
          :opt-un [::text ::coding-system ::alternate-identifier
                   ::alternate-text ::alternate-coding-system]))

(s/fdef ce->field
  :args (s/or :record (s/cat :ce-record (s/nilable ::spec-ce))
              :coll (s/cat :coll (s/coll-of ::spec-ce)))
  :ret (s/nilable vector?))

(defn ce->field
  "Accepts one or a sequence of CE records and returns a value, collection or map
  of HL7 v2 data."
  [ce-record]
  (type-to-field ce-record
                 #(vector (:identifier %)
                          (:text %)
                          (:coding-system %)
                          (:alternate-identifier %)
                          (:alternate-text %)
                          (:alternate-coding-system %))))

(s/fdef field->ce
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-ce))
             :record (s/nilable ::spec-ce)))

(defn field->ce
  "Accepts an HL7 v2 field of CE data and returns a single CE record or a sequence
  of records or nil."
  [field]
  (field-to-type field
                 map->ce
                 #(array-map :identifier (segment-util/get-or-nil % 0)
                             :text (segment-util/get-or-nil % 1)
                             :coding-system (segment-util/get-or-nil % 2)
                             :alternate-identifier (segment-util/get-or-nil % 3)
                             :alternate-text (segment-util/get-or-nil % 4)
                             :alternate-coding-system (segment-util/get-or-nil % 5))))

;; commonly used race CE values
(def race-black
  (map->ce {:identifier "2054-5" :text "Black or African American"
            :coding-system "HL70005"}))
(def race-hispanic
  (map->ce {:identifier "213502" :text "Hispanic" :coding-system "CIDREC"}))
(def race-native
  (map->ce {:identifier "1002-5" :text "American Indian or Alaskan Native"
            :coding-system "HL70005"}))
(def race-other
  (map->ce {:identifier "2131-1" :text "Other Race" :coding-system "HL70005"}))
(def race-pacific-islander
  (map->ce {:identifier "2076-8" :text "Native Hawaiian or Other Pacific Islander"
            :coding-system "HL70005"}))
(def race-white  (map->ce {:identifier "2106-3" :text "White"
                           :coding-system "HL70005"}))

(def race-map
  "Map of namespaced keywords to CE records representing the appropriate race."
  {::race-black race-black
   ::race-hispanic race-hispanic
   ::race-native race-native
   ::race-other race-other
   ::race-pacific-islander race-pacific-islander
   ::race-white race-white})

(def ce-keys-race-map
  "Map of unique values in a race CE record to race keywords"
  (let [key-value-fn #(str (:identifier %) (:coding-system %))]
    (into {} (map #(vector (key-value-fn (last %)) (first %)) race-map))))

(defn ce->race
  "Returns the race keyword for the supplied CE record of race data or nil if
  there isn't a keyword."  [ce-race]
  (ce-keys-race-map (str (:identifier ce-race) (:coding-system ce-race))))

(defn string->ce
  "Accepts a string with an identifier value and returns a CE record."
  [identifier]
  (map->ce {:identifier identifier}))

;; CWE: coded element with exceptions
(defrecord cwe
    [identifier
     text
     coding-system
     alternate-identifier
     alternate-text
     alternate-coding-system
     coding-system-version
     alternate-coding-system-version
     original-text])

(s/def ::coding-system-version (s/nilable string?))
(s/def ::alternate-coding-system-version (s/nilable string?))
(s/def ::original-text (s/nilable string?))

(s/def ::spec-cwe
  (s/keys :opt-un [::identifier ::text ::coding-system ::alternate-identifier
                   ::alternate-text ::alternate-coding-system ::coding-system-version
                   ::alternate-coding-system-version ::original-text]))

(s/fdef cwe->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-cwe))
              :coll (s/cat :coll (s/coll-of ::spec-cwe)))
  :ret (s/nilable vector?))

(defn cwe->field
  "Accepts one or a sequence of CWE records and returns a value, collection or
  map of HL7v2 data."
  [record]
  (type-to-field record
                 #(vector (:identifier %)
                          (:text %)
                          (:coding-system %)
                          (:alternate-identifier %)
                          (:alternate-text %)
                          (:alternate-coding-system %)
                          (:coding-system-version %)
                          (:alternate-coding-system-version %)
                          (:original-text %))))

(s/fdef field->cwe
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-cwe))
             :record (s/nilable ::spec-cwe)))

(defn field->cwe
  "Accepts an HL7 v2 field of CWE data and returns a single CWE record or a
  sequence or records or nil."
  [field]
  (field-to-type field
                 map->cwe
                 #(array-map :identifier (segment-util/get-or-nil % 0)
                             :text (segment-util/get-or-nil % 1)
                             :coding-system (segment-util/get-or-nil % 2)
                             :alternate-identifier (segment-util/get-or-nil % 3)
                             :alternate-text (segment-util/get-or-nil % 4)
                             :alternate-coding-system (segment-util/get-or-nil % 5)
                             :coding-system-version (segment-util/get-or-nil % 6)
                             :alternate-coding-system-version (segment-util/get-or-nil % 7)
                             :original-text (segment-util/get-or-nil % 8))))

;; CNE: Coded with no exceptions
(defrecord cne
           [identifier
            text
            coding-system
            alternate-identifier
            alternate-text
            alternate-coding-system
            coding-system-version
            alternate-coding-system-version
            original-text])

(s/def ::spec-cne
  (s/keys :req-un [::identifier]
          :opt-un [::text ::coding-system ::alternate-identifier
                   ::alternate-text ::alternate-coding-system ::coding-system-version
                   ::alternate-coding-system-version ::original-text]))

(s/fdef cne->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-cne))
              :coll (s/cat :coll (s/coll-of ::spec-cne)))
  :ret (s/nilable vector?))

(defn cne->field
  "Accepts one or a sequence of CNE records and returns a value, collection or
  map of HL7v2 data."
  [record]
  (type-to-field record
                 #(vector (:identifier %)
                          (:text %)
                          (:coding-system %)
                          (:alternate-identifier %)
                          (:alternate-text %)
                          (:alternate-coding-system %)
                          (:coding-system-version %)
                          (:alternate-coding-system-version %)
                          (:original-text %))))

(s/fdef field->cne
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-cwe))
             :record (s/nilable ::spec-cwe)))

(defn field->cne
  "Accepts an HL7 v2 field of CNE data and returns a single CNE record or a
  sequence or records or nil."
  [field]
  (field-to-type field
                 map->cne
                 #(array-map :identifier (segment-util/get-or-nil % 0)
                             :text (segment-util/get-or-nil % 1)
                             :coding-system (segment-util/get-or-nil % 2)
                             :alternate-identifier (segment-util/get-or-nil % 3)
                             :alternate-text (segment-util/get-or-nil % 4)
                             :alternate-coding-system (segment-util/get-or-nil % 5)
                             :coding-system-version (segment-util/get-or-nil % 6)
                             :alternate-coding-system-version (segment-util/get-or-nil % 7)
                             :original-text (segment-util/get-or-nil % 8))))

;; CQ: composite quantity with units
(defrecord cq [quantity units])

(s/def ::quantity (s/nilable number?))
(s/def ::units (s/nilable ::spec-ce))

(s/def ::spec-cq
  (s/keys :opt-un [::quantity ::units]))

(s/fdef cq->field
  :args (s/or :record (s/cat :cq-record (s/nilable ::spec-cq))
              :coll (s/cat :coll (s/coll-of ::spec-cq)))
  :ret (s/nilable vector?))

(defn cq->field
  "Accepts one or a sequence of CQ records and returns a value, collection or map
  of HL7 v2 data."
  [cq-record]
  (type-to-field cq-record
                 #(conj (vector (str (:quantity %)))
                        (segment-util/trim-nils (ce->field (:units %))))))

(s/fdef field->cq
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-cq))
             :record (s/nilable ::spec-cq)))

(defn field->cq
  "Accepts an HL7 v2 field of CQ data and returns a single CQ record or a sequence
  of records or nil."
  [field]
  (field-to-type field
                 map->cq
                 #(array-map :quantity  (segment-util/read-string
                                         (segment-util/get-or-nil % 0))
                             :units (field->ce (segment-util/get-or-nil % 1)))))

(defn number->cq
  "Accepts a number and returns a CQ record."
  [number-in]
  (map->cq {:quantity number-in}))

;; HD: heirarchic designator
(defrecord hd
    [namespace-id universal-id universal-id-type])

(s/def ::namespace-id (s/and (s/nilable string?) #(>= 20 (count %))))
(s/def ::universal-id (s/and (s/nilable string?) #(>= 199 (count %))))
(s/def ::universal-id-type (s/and (s/nilable string?) #(>= 6 (count %))))

(s/def ::spec-hd
  (s/keys :opt-un [::namespace-id ::universal-id ::universal-id-type]))

(s/fdef hd->field
  :args (s/or :record (s/cat :hd-record (s/nilable ::spec-hd))
              :coll (s/cat :coll (s/coll-of ::spec-hd)))
  :ret (s/nilable vector?))

(defn hd->field
  "Accepts one or a sequence of HD records and returns a value, collection or map
  of HL& v2 data."
  [hd-record]
  (type-to-field hd-record
                 #(vector (:namespace-id %)
                          (:universal-id %)
                          (:universal-id-type %))))

(s/fdef field->hd
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-hd))
             :record (s/nilable ::spec-hd)))

(defn field->hd
  "Accepts an HL7 v2 field of HD data and returns a single record, a sequence of
  HD records or nil."
  [field]
  (field-to-type field
                 map->hd
                 #(array-map :namespace-id (segment-util/get-or-nil % 0)
                             :universal-id (segment-util/get-or-nil % 1)
                             :universal-id-type (segment-util/get-or-nil % 2))))

(defn string->hd
  "Accepts a string with a namespace identifier and returns an HD record."
  [namespace-id]
  (map->hd {:namespace-id namespace-id}))

;; CX: compound identifier record and specifications
(defrecord cx
    [identifier
     check-digit
     check-scheme
     assigning-authority
     identifier-type-code
     assigning-facility
     effective-date
     expiration-date
     assigning-jurisdiction
     assigning-agency])

(s/def ::check-digit (s/and (s/nilable pos-int?) #(>= 1 (count (str %)))))
(s/def ::check-scheme (s/and (s/nilable string?) #(>= 3 (count %))))
(s/def ::assigning-authority (s/nilable ::spec-hd))
(s/def ::identifier-type-code (s/and (s/nilable string?) #(>= 5 (count %))))
(s/def ::assigning-facility (s/nilable ::spec-hd))
(s/def ::effective-date (s/nilable ::spec-ts))
(s/def ::expiration-date (s/nilable ::spec-ts))
(s/def ::assigning-jurisdiction (s/nilable ::spec-cwe))
(s/def ::assigning-agency (s/nilable ::spec-cwe))

(s/def ::spec-cx
  (s/keys :req-un [::identifier]
          :opt-un [::check-digit ::check-scheme ::assigning-authority
                   ::identifier-type-code ::assigning-facility ::effective-date
                   ::expiration-date ::assigning-jurisdiction ::assigning-agency]))

(s/fdef cx->field
  :args (s/or :record (s/cat :cx-record (s/nilable ::spec-cx))
              :coll (s/cat :coll (s/coll-of ::spec-cx)))
  :ret (s/nilable vector?))

(defn cx->field
  "Accepts one or a sequence of CX record and returns a value, collection or map
  of HL7 v2 data."
  [cx-record]
  (type-to-field cx-record
                 #(vector (:identifier %)
                          (:check-digit %)
                          (:check-scheme %)
                          (segment-util/trim-nils (hd->field (:assigning-authority %)))
                          (:identifier-type-code %)
                          (segment-util/trim-nils (hd->field (:assigning-facility %)))
                          (segment-util/trim-nils (ts->field (:effective-date %)))
                          (segment-util/trim-nils (ts->field (:expiration-date %)))
                          (segment-util/trim-nils (cwe->field (:assigning-jurisdiction %)))
                          (segment-util/trim-nils (cwe->field (:assigning-agency %))))))

(s/def ::patient-identifier
  (s/and ::spec-cx #(string? (:identifier %))))

(s/fdef patient-identifier
  :args (s/cat :identifier string?)
  :ret ::spec-cx)

(defn patient-identifier
  "Returns a CX record with the provided identifier and default values."
  [identifier]
  (map->cx {:identifier identifier
            :assigning-authority (string->hd "MR")
            :identifier-type-code "PI"}))

(s/fdef field->cx
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-cx))
             :record (s/nilable ::spec-cx)))

(defn field->cx
  "Accepts an HL7 v2 field of CX data and returns a single CX record or a sequence
  of records or nil."
  [field]
  (field-to-type
   field
   map->cx
   #(array-map :identifier (segment-util/get-or-nil % 0)
               :check-digit (segment-util/read-string (segment-util/get-or-nil % 1))
               :check-scheme (segment-util/get-or-nil % 2)
               :assigning-authority (field->hd (segment-util/get-or-nil % 3))
               :identifier-type-code (segment-util/get-or-nil % 4)
               :assigning-facility (field->hd (segment-util/get-or-nil % 5))
               :effective-date (field->ts (segment-util/get-or-nil % 6))
               :expiration-date (field->ts (segment-util/get-or-nil % 7))
               :assigning-jurisdiction (field->cwe (segment-util/get-or-nil % 8))
               :assigning-agency (field->cwe (segment-util/get-or-nil % 9)))))

(defn string->cx
  "Accepts a string with an identifier and returns a CX record."
  [identifier]
  (map->cx {:identifier identifier}))

;; EI: entity identifier
(defrecord ei [identifier namespace-id universal-id universal-id-type])

(s/def ::namespace-id (s/nilable string?))
(s/def ::universal-id (s/nilable string?))
(s/def ::universal-id-type (s/nilable string?))

(s/def ::spec-ei
  (s/keys :opt-un [::identifier ::namespace-id ::universal-id ::universal-id-type]))

(s/fdef ei->field
  :args (s/or :record (s/cat :ei-record (s/nilable ::spec-ei))
              :coll (s/cat :coll (s/coll-of ::spec-ei)))
  :ret (s/nilable vector?))

(defn ei->field
  "Accepts one or a sequence of EI records and returns a value, collection or map
  of HL7 v2 data."
  [ei-record]
  (type-to-field ei-record
                 #(vector (:identifier %)
                          (:namespace-id %)
                          (:universal-id %)
                          (:universal-id-type %))))

(s/fdef field->ei
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-ei))
             :record (s/nilable ::spec-ei)))

(defn field->ei
  "Accepts an HL7 v2 field of EI data and returns a single record, a sequence of
  EI records or nil."
  [field]
  (field-to-type field
                 map->ei
                 #(array-map :identifier (segment-util/get-or-nil % 0)
                             :namespace-id (segment-util/get-or-nil % 1)
                             :universal-id (segment-util/get-or-nil % 2)
                             :universal-id-type (segment-util/get-or-nil % 3))))

(defn string->ei
  "Accepts a string with an identifier and returns an EI record."
  [identifier]
  (map->ei {:identifier identifier}))


;; MO: money
(defrecord mo
    [money-quantity money-denomination])

(s/def ::money-quantity (s/nilable number?))
(s/def ::money-denomination (s/nilable string?))

(s/def ::spec-mo
  (s/keys :opt-un [::money-quantity ::money-denomination]))

(s/fdef mo->field
  :args (s/or :record (s/cat :mo-record (s/nilable ::spec-mo))
              :coll (s/cat :coll (s/coll-of ::spec-mo)))
  :ret (s/nilable vector?))

(defn mo->field
  "Accepts one or a sequence of MO records and returns a value, collection or map
  of HL7 v2 data."
  [mo-record]
  (type-to-field mo-record
                 #(vector (:money-quantity %)
                          (:money-denomination %))))

(s/fdef field->mo
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-mo))
             :record (s/nilable ::spec-mo)))

(defn prepend-zero-money
  [text]
  (when (and text (string? text))
    (let [text-out (string/trim text)]
      (if (= \. (first text-out))
        (str "0" text-out)
        text-out))))

(defn read-money
  [text]
  (let [text-out (prepend-zero-money text)]
    (segment-util/read-string text-out)))

(defn field->mo
  "Accepts an HL7 v2 field of MO data and returns a single record, a sequence of
  MO records or nil."
  [field]
  (field-to-type field
                 map->mo
                 #(array-map :money-quantity (read-money (segment-util/get-or-nil % 0))
                             :money-denomination (segment-util/get-or-nil % 1))))

(defn number->mo
  "Accepts a number with the currency value and returns an MO record in US dollars."
  [number-in]
  (map->mo {:money-quantity number-in :money-denomination "USD"}))

;; CP: composite price
(defrecord cp
    [price price-type value-from value-to range-units range-type])

(s/def ::price (s/nilable ::spec-mo))
(s/def ::price-type (s/nilable string?))
(s/def ::value-from (s/nilable number?))
(s/def ::value-to (s/nilable number?))
(s/def ::range-units (s/nilable ::spec-ce))
(s/def ::range-type (s/nilable string?))

(s/def ::spec-cp
  (s/keys :opt-un [::price ::price-type ::value-from ::value-to ::range-units
                   ::range-type]))

(s/fdef cp->field
  :args (s/or :record (s/cat :mo-record (s/nilable ::spec-cp))
              :coll (s/cat :coll (s/coll-of ::spec-cp)))
  :ret (s/nilable vector?))

(defn cp->field
  "Accepts one or a sequence of CP records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (type-to-field record
                 #(vector (segment-util/trim-nils (mo->field (:price %)))
                          (:price-type %)
                          (:value-from %)
                          (:value-to %)
                          (segment-util/trim-nils (ce->field (:range-units %)))
                          (:range-type %))))

(s/fdef field->cp
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-cp))
             :record (s/nilable ::spec-cp)))

(defn field->cp
  "Accepts an HL7 v2 field of CP data and returns a single record, a sequence
  of records or nil."
  [field]
  (field-to-type
   field
   map->cp
   #(array-map :price (field->mo (segment-util/get-or-nil % 0))
               :price-type (segment-util/get-or-nil % 1)
               :value-from (segment-util/read-string (segment-util/get-or-nil % 2))
               :value-to (segment-util/read-string (segment-util/get-or-nil % 3))
               :range-units (field->ce (segment-util/get-or-nil % 4))
               :range-type (segment-util/get-or-nil % 5))))

;; FN: family name
(defrecord fn-name
    [surname
     own-surname-prefix
     own-surname
     spouse-surname-prefix
     spouse-surname])

(s/def ::surname (s/nilable string?))
(s/def ::own-surname-prefix (s/nilable string?))
(s/def ::own-surname (s/nilable string?))
(s/def ::spouse-surname-prefix (s/nilable string?))
(s/def ::spouse-surname (s/nilable string?))

(s/def ::spec-fn-name
  (s/keys :opt-un [::surname ::own-surname-prefix ::own-surname
                   ::spouse-surname-prefix ::spouse-surname]))

(s/fdef fn-name->field
  :args (s/or :record (s/cat :fn-name-record (s/nilable ::spec-fn-name))
              :coll (s/cat :coll (s/coll-of ::spec-fn-name)))
  :ret (s/nilable vector?))

(defn fn-name->field
  "Accepts one or a sequence of FN records and returns a value, collection or
  map of HL7 v2 data."
  [fn-name-record]
  (type-to-field fn-name-record
                 #(vector (:surname %)
                          (:own-surname-prefix %)
                          (:own-surname %)
                          (:spouse-surname-prefix %)
                          (:spouse-surname %))))

(s/fdef field->fn-name
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-fn-name))
             :record (s/nilable ::spec-fn-name)))

(defn field->fn-name
  "Accepts an HL7 v2 field of FN data and returns a single FN record, a sequence
  of FN records or nil."
  [field]
  (field-to-type field
                 map->fn-name
                 #(array-map :surname (segment-util/get-or-nil % 0)
                             :own-surname-prefix (segment-util/get-or-nil % 1)
                             :own-surname (segment-util/get-or-nil % 2)
                             :spouse-surname-prefix (segment-util/get-or-nil % 3)
                             :spouse-surname (segment-util/get-or-nil % 4))))

(defn string->fn-name
  "Accepts a string with a family name and returns a FN record."
  [family-name]
  (map->fn-name {:surname family-name}))

;; XPN: extended person name record and specification
(defrecord xpn
    [family-name
     given-name
     further-given-name
     suffix
     prefix
     degree
     name-type-code
     name-representation-code
     name-context
     name-validity-range
     name-assembly-order])

(s/def ::family-name (s/nilable ::spec-fn-name))
(s/def ::given-name (s/nilable string?))
(s/def ::further-given-name (s/nilable string?))
(s/def ::suffix (s/nilable string?))
(s/def ::prefix (s/nilable string?))
(s/def ::degree (s/nilable string?))
(s/def ::name-type-code (s/nilable string?))
(s/def ::name-representation-code (s/nilable string?))
(s/def ::name-context (s/nilable ::spec-ce))
(s/def ::name-validity-range (s/nilable ::spec-dr))
(s/def ::name-assembly-order (s/nilable string?))

(s/def ::spec-xpn
  (s/keys :opt-un [::family-name ::given-name ::further-given-name ::suffix
                   ::prefix ::degree ::name-type-code ::name-representation-code
                   ::name-context ::name-validity-range ::name-assembly-order]))

(s/fdef xpn->field
  :args (s/or :record (s/cat :xpn-record (s/nilable ::spec-xpn))
              :coll (s/cat :coll (s/coll-of ::spec-xpn)))
  :ret (s/nilable vector?))

(defn xpn->field
  "Accepts one or a sequence of XPN records and returns a value, collection or map
  of HL7 v2 data."
  [xpn-record]
  (type-to-field xpn-record
                 #(conj (vector (segment-util/trim-nils (fn-name->field (:family-name %)))
                                (:given-name %)
                                (:further-given-name %)
                                (:suffix %)
                                (:prefix %)
                                (:degree %)
                                (:name-type-code %)
                                (:name-representation-code %))
                        (segment-util/trim-nils (ce->field (:name-context %)))
                        (segment-util/trim-nils (dr->field (:name-validity-range %)))
                        (:name-assembly-order %))))

(s/fdef patient-name
  :args (s/cat :first (s/nilable string?)
               :middle (s/nilable string?)
               :last (s/nilable string?))
  :ret ::spec-xpn)

(defn patient-name
  "Returns an XPN record populated with the provided last, first and middle
  names."
  [last first middle]
  (map->xpn {:family-name (string->fn-name last)
             :given-name first
             :further-given-name middle}))

(s/fdef field->xpn
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-xpn))
             :record (s/nilable ::spec-xpn)))

(defn field->xpn
  "Accepts an HL7 v2 field of XPN data and returns a single record, a sequence of
  XPN records or nil."
  [field]
  (field-to-type field
                 map->xpn
                 #(array-map :family-name (field->fn-name (segment-util/get-or-nil % 0))
                             :given-name (segment-util/get-or-nil % 1)
                             :further-given-name (segment-util/get-or-nil % 2)
                             :suffix (segment-util/get-or-nil % 3)
                             :prefix (segment-util/get-or-nil % 4)
                             :degree (segment-util/get-or-nil % 5)
                             :name-type-code (segment-util/get-or-nil % 6)
                             :name-representation-code (segment-util/get-or-nil % 7)
                             :name-context (field->ce (segment-util/get-or-nil % 8))
                             :name-validity-range (field->dr (segment-util/get-or-nil % 9))
                             :name-assembly-order (segment-util/get-or-nil % 10))))

(defn string->xpn
  "Accepts strings with last, first and middle name and returns an XPN record."
  ([last-name]
   (map->xpn {:family-name last-name}))
  ([last-name first-name]
   (map->xpn {:family-name last-name :given-name first-name}))
  ([last-name first-name middle-name]
   (map->xpn {:family-name last-name :given-name first-name
              :further-given-name middle-name})))

;; SAD: street address
(defrecord sad
    [street-address
     street-name
     dwelling-number])

(s/def ::street-address (s/and (s/nilable string?) #(>= 120 (count %))))
(s/def ::street-name (s/and (s/nilable string?) #(>= 50 (count %))))
(s/def ::dwelling-number (s/and (s/nilable string?) #(>= 12 (count %))))

(s/def ::spec-sad
  (s/keys :opt-un [::street-address ::street-name ::dwelling-number]))

(s/fdef sad->field
  :args (s/or :record (s/cat :sad-record (s/nilable ::spec-sad))
              :coll (s/cat :coll (s/coll-of ::spec-sad)))
  :ret (s/nilable vector?))

(defn sad->field
  "Accepts one or a sequence of SAD records and returns a value, collection or map
  of HL7 v2 data."
  [sad-record]
  (type-to-field sad-record
                 #(vector (:street-address %)
                          (:street-name %)
                          (:dweling-number %))))

(s/fdef field->sad
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-sad))
             :record (s/nilable ::spec-sad)))

(defn field->sad
  "Accepts an HL7 v2 field of SAD data and returns a single record, a sequence of
  SAD records or nil."
  [field]
  (field-to-type field
                 map->sad
                 #(array-map :street-address (segment-util/get-or-nil % 0)
                             :street-name (segment-util/get-or-nil % 1)
                             :dwelling-number (segment-util/get-or-nil % 2))))

(defn string->sad
  "Accepts a string with an address and returns a SAD record"
  [street-address]
  (map->sad {:street-address street-address}))

;; XAD: extended address
(defrecord xad
    [street-1
     street-2
     city
     state
     postal-code
     country
     address-type
     other-geo-designation
     county-code
     census-tract
     representation-code
     validity-range])

(s/def ::street-1 (s/nilable ::spec-sad))
(s/def ::street-2 (s/nilable string?))
(s/def ::city (s/nilable string?))
(s/def ::state (s/nilable string?))
(s/def ::postal-code (s/nilable string?))
(s/def ::country (s/nilable string?))
(s/def ::address-type (s/nilable string?))
(s/def ::other-geo-designation (s/nilable string?))
(s/def ::county-code (s/nilable string?))
(s/def ::census-tract (s/nilable string?))
(s/def ::representation-code (s/nilable string?))
(s/def ::validity-range (s/nilable ::spec-dr))

(s/def ::spec-xad
  (s/keys :opt-un [::street-1 ::street-2 ::city ::state ::postal-code ::country
                   ::address-type ::other-geo-designation ::county-code ::census-tract
                   ::representation-code ::validity-range]))

;; common XAD types
(def xad-business "Business")
(def xad-invalid "Bad Address")
(def xad-birth "Birth Delivery Location")
(def xad-birth-residence "Residence at Birth")
(def xad-current "Current")
(def xad-country-origin "Country of Origin")
(def xad-home "Home")
(def xad-legal "Legal")
(def xad-mailing "Mailing")
(def xad-permanent "Permanent")
(def xad-registry "Registry")

(s/fdef xad->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-xad))
              :coll (s/cat :coll (s/coll-of ::spec-xad)))
  :ret (s/nilable vector?))

(defn xad->field
  "Accepts an XAD record and returns a value, collection or map of HL7 v2
  data."
  [xad-record]
  (type-to-field xad-record
                 #(vector (segment-util/trim-nils (sad->field (:street-1 %)))
                          (:street-2 %)
                          (:city %)
                          (:state %)
                          (:postal-code %)
                          (:country %)
                          (lookups/address-by-key (:address-type %))
                          (:other-geo-designation %)
                          (:county-code %)
                          (:census-tract %)
                          (:representation-code %)
                          (segment-util/trim-nils (dr->field (:validity-range %))))))

(s/fdef field->xad
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-xad))
             :record (s/nilable ::spec-xad)))

(defn field->xad
  "Accepts an HL7 v2 field of XAD data and returns a single XAD record, a sequence
  of XAD records or nil."
  [field]
  (field-to-type field
                 map->xad
                 #(array-map :street-1 (if (coll? (segment-util/get-or-nil % 0))
                                         (field->sad (segment-util/get-or-nil % 0))
                                         (string->sad (segment-util/get-or-nil % 0)))
                             :street-2 (segment-util/get-or-nil % 1)
                             :city (segment-util/get-or-nil % 2)
                             :state (segment-util/get-or-nil % 3)
                             :postal-code (segment-util/get-or-nil % 4)
                             :country (segment-util/get-or-nil % 5)
                             :address-type (lookups/address-by-value (segment-util/get-or-nil % 6))
                             :other-geo-designation (segment-util/get-or-nil % 7)
                             :county-code (segment-util/get-or-nil % 8)
                             :census-tract (segment-util/get-or-nil % 9)
                             :representation-code (segment-util/get-or-nil % 10)
                             :validity-range (field->dr (segment-util/get-or-nil % 11)))))

(defn xad-of-type
  "Returns a XAD record of the given type, populated with the provided map of
  data. The `street-1` value will be transformed into a SAD record.`"
  [xad-type data-map]
  (map->xad
   (merge data-map
          {:street-1 (string->sad (:street-1 data-map))
           :address-type xad-type})))

;; FN: family name
(defrecord hfn
    [surname own-surname-prefix own-surname spouse-surname-prefix spouse-surname])

(s/def ::surname (s/nilable string?))
(s/def ::own-surname-prefix (s/nilable string?))
(s/def ::own-surname (s/nilable string?))
(s/def ::spouse-surname-prefix (s/nilable string?))
(s/def ::spouse-surname (s/nilable string?))

(s/def ::spec-hfn
  (s/keys :opt-un [::surname ::own-surname-prefix ::own-surname
                   ::spouse-surname-prefix ::spouse-surname]))

(s/fdef hfn->field
  :args (s/or :record (s/cat :hfn-record (s/nilable ::spec-hfn))
              :coll (s/cat :coll (s/coll-of ::spec-hfn)))
  :ret (s/nilable vector?))

(defn hfn->field
  "Accepts one or a sequence of FN records and returns a value, collection or map
  of HL7 v2 data."
  [hfn-record]
  (type-to-field hfn-record
                 #(vector (:surname %)
                          (:own-surname-prefix %)
                          (:own-surname %)
                          (:spouse-surname-prefix %)
                          (:spouse-surname %))))

(s/fdef field->hfn
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-hfn))
             :record (s/nilable ::spec-hfn)))

(defn field->hfn
  "Accepts an HL7 v2 field of FN data and returns a single record, a sequence of
  FN records or nil."
  [field]
  (field-to-type field
                 map->hfn
                 #(array-map :surname (segment-util/get-or-nil % 0)
                             :own-surname-prefix (segment-util/get-or-nil % 1)
                             :own-surname (segment-util/get-or-nil % 2)
                             :spouse-surname-prefix (segment-util/get-or-nil % 3)
                             :spouse-surname (segment-util/get-or-nil % 4))))

(defn string->hfn
  "Accepts a string with a family name value and returns a HFN record."
  [family-name]
  (map->hfn {:surname family-name}))

;; XON: extended composite name and identification number for organizations
(defrecord xon
    [organization-name
     organization-name-type-code
     organization-id-number
     xcn-check-digit
     check-digit-scheme
     identifier-authority
     identifier-type-code
     assigning-facility-id
     name-representation-code
     organization-identifier])

(s/def ::organization-name (s/nilable string?))
(s/def ::organization-name-type-code (s/nilable string?))
(s/def ::organization-id-number (s/nilable string?))
(s/def ::check-digit-scheme (s/nilable string?))
(s/def ::identifier-authority (s/nilable ::spec-hd))
(s/def ::assigning-facility-id (s/nilable ::spec-hd))
(s/def ::organization-identifier (s/nilable string?))

(s/def ::spec-xon
  (s/keys :opt-un [::organization-name ::organization-name-type-code
                   ::organization-id-number ::xcn-check-digit
                   ::check-digit-scheme ::identifier-authority
                   ::identifier-type-code  ::assigning-facility-id
                   ::name-representation-code ::organization-identifier]))

(s/fdef xon->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-xon))
              :coll (s/cat :coll (s/coll-of ::spec-xon)))
  :ret (s/nilable vector?))

(defn xon->field
  "Accepts one or a sequence of XON records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (type-to-field record
                 #(vector (:organization-name %)
                          (:organization-name-type-code %)
                          (:organization-id-number %)
                          (:xcn-check-digit %)
                          (:check-digit-scheme %)
                          (segment-util/trim-nils (hd->field (:identifier-authority %)))
                          (:identifier-type-code %)
                          (segment-util/trim-nils (hd->field (:assigning-facility-id %)))
                          (:name-representation-code %)
                          (:organization-identifier %))))

(s/fdef field->xon
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-xon))
             :record (s/nilable ::spec-xon)))

(defn field->xon
  "Accepts an HL7 v2 field of XON data and returns a single record, a sequence
  of records or nil."
  [field]
  (field-to-type field
                 map->xon
                 #(array-map :organization-name (segment-util/get-or-nil % 0)
                             :organization-name-type-code (segment-util/get-or-nil % 1)
                             :organization-id-number (segment-util/get-or-nil % 2)
                             :xcn-check-digit (segment-util/get-or-nil % 3)
                             :check-digit-scheme (segment-util/get-or-nil % 4)
                             :identifier-authority (field->hd (segment-util/get-or-nil % 5))
                             :identifier-type-code (segment-util/get-or-nil % 6)
                             :assigining-facility-id (field->hd (segment-util/get-or-nil % 7))
                             :name-representation-code (segment-util/get-or-nil % 8)
                             :organization-identifier (segment-util/get-or-nil % 9))))


;; XTN: extended telecommunication number
(defrecord xtn
    [telephone-number
     use-code
     equipment-type
     email-address
     country-code
     area-code
     phone-number
     extension
     text])

(s/def ::telephone-number (s/and (s/nilable string?) #(>= 199 (count %))))
(s/def ::use-code (s/nilable string?))
(s/def ::equipment-type (s/nilable string?))
(s/def ::email-address (s/and (s/nilable string?) #(>= 199 (count %))))
(s/def ::country-code (s/nilable
                       (s/with-gen (s/and string? #(re-matches #"\d{1,3}" %))
                         #(gen/fmap str
                                    (s/gen (s/int-in 100 999))))))
(s/def ::area-code (s/nilable
                       (s/with-gen (s/and string? #(re-matches #"\d{1,6}" %))
                         #(gen/fmap str
                                    (s/gen (s/int-in 10000 99999))))))
(s/def ::phone-number (s/nilable
                    (s/with-gen (s/and string? #(re-matches #"\d{1,9}" %))
                      #(gen/fmap str
                                 (s/gen (s/int-in 100000000 999999999))))))
(s/def ::extension (s/nilable
                    (s/with-gen (s/and string? #(re-matches #"\d{1,5}" %))
                      #(gen/fmap str
                                 (s/gen (s/int-in 1000 9999))))))
(s/def ::text (s/and (s/nilable string?) #(>= 199 (count %))))

(s/def ::spec-xtn
  (s/keys :opt-un [::telephone-number ::use-code ::equipment-type ::email-address
                   ::country-code ::area-code ::phone-number ::extension
                   ::text]))

;; common XTN use codes
(def xtn-use-answering-service "Answering Service")
(def xtn-use-beeper "Beeper")
(def xtn-use-emergency "Emergency")
(def xtn-use-email "Email")
(def xtn-use-other "Other")
(def xtn-use-primary "Primary")
(def xtn-use-vacation "Vacation")
(def xtn-use-business "Business")

;; common XTN equipment-codes
(def xtn-equip-beeper "Beeper")
(def xtn-equip-mobile "Mobile")
(def xtn-equip-fax "Fax")
(def xtn-equip-modem "Modem")
(def xtn-equip-phone "Telephone")
(def xtn-equip-email "Network")
(def xtn-equip-x400 "X.400")

(s/fdef xtn->field
  :args (s/or :record (s/cat :xtn-record (s/nilable ::spec-xtn))
              :coll (s/cat :coll (s/coll-of ::spec-xtn)))
  :ret (s/nilable vector?))

(defn xtn->field
  "Accepts one or a sequence of XTN records and returns a value, collection or map
  of HL7 v2 data."
  [xtn-record]
  (type-to-field xtn-record
                 #(vector (:telephone-number %)
                          (when (:use-code %)
                            (lookups/xtn-use-by-key (:use-code %)))
                          (when (:equipment-type %)
                            (lookups/xtn-equip-by-key (:equipment-type %)))
                          (:email-address %)
                          (:country-code %)
                          (:area-code %)
                          (:phone-number %)
                          (:extension %)
                          (:text %))))

(s/fdef field->xtn
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-xtn))
             :record (s/nilable ::spec-xtn)))

(defn field->xtn
  "Accepts an HL7 v2 field of XTN data and returns a single XTN record, a sequence
  of XTN records or nil."
  [field]
  (field-to-type field
                 map->xtn
                 #(array-map :telephone-number (segment-util/get-or-nil % 0)
                             :use-code (lookups/xtn-use-by-value
                                        (segment-util/get-or-nil % 1))
                             :equipment-type (lookups/xtn-equip-by-value
                                              (segment-util/get-or-nil % 2))
                             :email-address (segment-util/get-or-nil % 3)
                             :country-code (segment-util/get-or-nil % 4)
                             :area-code (segment-util/get-or-nil % 5)
                             :phone-number (segment-util/get-or-nil % 6)
                             :extension (segment-util/get-or-nil % 7)
                             :text (segment-util/get-or-nil % 8))))

(defn string->xtn-phone
  "Converts a phone number (as a string) into an XTN record. The 'use' and
  'equip-type' parameters should be a textual description or a namespaced symbol
  from the segment. If no country code is provided then \"1\" is used."
  ([phone]
   (string->xtn-phone nil nil phone))
  ([use-code equip-type phone]
   (when phone
     (let [parsed (segment-util/parse-phone phone)]
       (map->xtn
        {:telephone-number phone
         :use-code (or use-code xtn-use-primary)
         :equipment-type (or equip-type xtn-equip-phone)
         :country-code (if (not (empty? (get parsed 0))) (get parsed 0) "1")
         :area-code (get parsed 1)
         :phone-number (get parsed 2)
         :extension (if (not (empty? (get parsed 3))) (get parsed 3) nil)})))))

(defn string->xtn-email
  "Converts an email address into an XTN record."
  [email]
  (when email
    (map->xtn
     {:use-code xtn-use-email
      :equipment-type xtn-equip-email
      :email-address email})))

;; XCN: extended composite id number and name
(defrecord xcn
    [id-number
     qualified-family-name
     given-name
     middle-name
     suffix
     prefix
     degree
     source-table
     authority-designator
     name-type
     xcn-check-digit
     check-scheme
     identifier-type-code
     facility
     name-representation-code
     name-representation-context
     name-validity-range
     name-assembly-order
     effective-date
     expiration-date
     professional-suffix
     assigning-jurisdiction
     assigning-agency])

(s/def ::id-number (s/and (s/nilable string?) #(>= 15 (count %))))
(s/def ::qualified-family-name (s/nilable ::spec-fn-name))
(s/def ::middle-name (s/and (s/nilable string?) #(>= 30 (count %))))
(s/def ::source-table (s/and (s/nilable string?) #(>= 4 (count %))))
(s/def ::authority-designator (s/nilable ::spec-hd))
(s/def ::name-type (s/and (s/nilable string?) #(>= 1 (count %))))
(s/def ::xcn-check-digit (s/and (s/nilable string?) #(>= 1 (count %))))
(s/def ::facility (s/nilable ::spec-hd))
(s/def ::name-representation-context (s/nilable ::spec-cwe))
(s/def ::professional-suffix (s/nilable string?))

(s/def ::spec-xcn
  (s/keys :opt-un [::id-number ::qualified-family-name ::given-name ::middle-name
                   ::suffix ::prefix ::degree ::source-table
                   ::authority-designator ::name-type ::xcn-check-digit
                   ::check-scheme ::identifier-type-code ::facility
                   ::name-representation-code ::name-representation-context
                   ::name-validity-range ::name-assembly-order ::effective-date
                   ::expiration-date ::professional-suffix ::assigning-jurisdiction
                   ::assigning-agency]))

(s/fdef xcn->field
  :args (s/or :record (s/cat :xcn-record (s/nilable ::spec-xcn))
              :coll (s/cat :coll (s/coll-of ::spec-xcn)))
  :ret (s/nilable vector?))

(defn xcn->field
  "Accepts one or a sequence of XCN records and returns a value, collection or map
  of HL7 v2 data."
  [xcn-record]
  (type-to-field
   xcn-record
   #(conj (vector (:id-number %))
          (segment-util/trim-nils (hfn->field (:qualified-family-name %)))
          (:given-name %)
          (:middle-name %)
          (:suffix %)
          (:prefix %)
          (:degree %)
          (:source-table %)
          (segment-util/trim-nils (hd->field (:authority-designator %)))
          (:name-type %)
          (:xcn-check-digit %)
          (:check-scheme %)
          (:identifier-type-code %)
          (segment-util/trim-nils (hd->field (:facility %)))
          (:name-representation-code %)
          (segment-util/trim-nils (cwe->field (:name-representation-context %)))
          (segment-util/trim-nils (dr->field (:name-validity-range %)))
          (:name-assembly-order %)
          (segment-util/trim-nils (ts->field (:effective-date %)))
          (segment-util/trim-nils (ts->field (:expiration-date %)))
          (:professional-suffix %)
          (segment-util/trim-nils (cwe->field (:assigning-jurisdiction %)))
          (segment-util/trim-nils (cwe->field (:assigning-agency %))))))

(s/fdef field->xcn
  :args (s/nilable (s/coll-of ::hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-xcn))
             :record (s/nilable ::spec-xcn)))

(defn field->xcn
  "Accepts an HL7 v2 field of XCN data and returns a single record, a sequence of
  XCN records or nil."
  [field]
  (field-to-type
   field
   map->xcn
   #(array-map :id-number (segment-util/get-or-nil % 0)
               :qualified-family-name (field->hfn (segment-util/get-or-nil % 1))
               :given-name (segment-util/get-or-nil % 2)
               :middle-name (segment-util/get-or-nil % 3)
               :suffix (segment-util/get-or-nil % 4)
               :prefix (segment-util/get-or-nil % 5)
               :degree (segment-util/get-or-nil % 6)
               :source-table (segment-util/get-or-nil % 7)
               :authority-designator (field->hd (segment-util/get-or-nil % 8))
               :name-type (segment-util/get-or-nil % 9)
               :xcn-check-digit (segment-util/get-or-nil % 10)
               :check-scheme (segment-util/get-or-nil % 11)
               :identifier-type-code (segment-util/get-or-nil % 12)
               :facility (field->hd (segment-util/get-or-nil % 13))
               :name-representation-code (segment-util/get-or-nil % 14)
               :name-representation-context (field->cwe (segment-util/get-or-nil % 15))
               :name-validity-range (field->dr (segment-util/get-or-nil % 16))
               :name-assembly-order (segment-util/get-or-nil % 17)
               :effective-date (field->ts (segment-util/get-or-nil % 18))
               :expiration-date (field->ts (segment-util/get-or-nil % 19))
               :professional-suffix (segment-util/get-or-nil % 20)
               :assigning-jurisdiction (field->cwe (segment-util/get-or-nil % 21))
               :assigning-agency (field->cwe (segment-util/get-or-nil % 22))
               )))

