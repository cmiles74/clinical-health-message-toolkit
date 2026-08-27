(ns com.nervestaple.clinical.message-intermediate.segment.utility
  (:refer-clojure :exclude [read-string])
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [java-time :as time]
   [taoensso.timbre :as logger
    :refer (log  trace  debug  info  warn  error  fatal  report)]
   [clojure.string :as str])
  (:import
   [java.time LocalDateTime ZonedDateTime ZoneOffset]
   [java.time.format DateTimeFormatter]))

(def default-delimiters
  "Map with our default set of delimiters"
  {:field 124
   :component 94
   :subcomponent 38
   :repeating 126
   :escape 92})

(def timestamp-zoned-format-hl7
  "HL7 v2 timestamp format, note that it includes the time zone."
  (DateTimeFormatter/ofPattern "yyyyMMddHHmmssZ"))

(def timestamp-format-hl7
  "HL7 v2 timestamp format, note that it does not include the time zone."
  (DateTimeFormatter/ofPattern "yyyyMMddHHmmss"))

(def timestamp-format-med-hl7
  "HL7 v2 timestamp format, note that it does not include the time zone."
  (DateTimeFormatter/ofPattern "yyyyMMddHHmmssSS"))

(def timestamp-format-short-hl7
  "HL7 v2 timestamp format, without seconds"
  (DateTimeFormatter/ofPattern "yyyyMMddHHmm"))

(def date-format-hl7
  "HL7 v2 date format."
  (DateTimeFormatter/ofPattern "yyyyMMdd"))

(def date-format-hl7-fallback
  "HL7 v2 date format."
  (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(def date-format-short-hl7
  "HL7 v2 short date format."
  (DateTimeFormatter/ofPattern "yyyyMM"))

(def year-format-short-hl7
  "HL7 v2 even shorter date format, only the year."
  (DateTimeFormatter/ofPattern "yyyy"))

(defn pad
  "Pads a collection that should be the provided size by appending the specified
  value until that collection is the required size."
  [size coll value]
  (take size (concat coll (repeat value))))

(defn unwrap-field
  "Accepts a field of parsed HL7 v2 data, which may be one item of data or a
  sequence of data items, and unwraps either the single item or each item in the
  sequence and returns the data either the single item or vector of items."
  [field]
  (if (and (vector? field) (map? (first field)))
    (mapv #(:content %) field)
    (if (map? field) (:content field) field)))

(defn unwrap-and-first
  "Accepts a field of parsed HL7 v2 data, which may be one item of data or a
  sequence of data items, and unwraps either the single item or each item in the
  sequence and returns only the first data item."
  [field]
  (first (unwrap-field field)))

(defn is-field?
  "Returns true if the provided HL7 field data is a map of field data or false if
  it's an atom or sequence of data. Typically this would be used to test if a
  field is repeating."
  [field]
  (when (and (map? field)
             (= 1 (count (keys field)))
             (= :content (first (keys field))))
    true))

(defn vector-of-vector?
  "Returns true if the provided item is a vector that contains one or more vector
  instances."
  [data]
  (when (and (vector? data)
             (vector? (first data))) true))

(defn all-vectors?
  "Returns true if the provided item is a vector where each element is another
  vector."
  [data]
  (when (vector? data)
    (every? true? (mapv vector? data))))

(defn get-or-nil
  "Returns the value at the given index of the provided vector or a nil if there
  is no index or if the value equals \"\"."
  [vec index]
  (let [value (get vec index)]
    (if (= "" value) nil value)))

(defn map-nil-or-empty
  "Returns true if all of the values of the map are either nil or empty
  string (\"\")."
  [map-this]
  (or (every? #(nil? %) (vals map-this))
      (every? #(= "\"\"" %) (vals map-this))))

(defn format-time
  "Formats the provided date and time into an HL7 messaging date."
  [time]
  (when time
    (cond (time/instant? time)
          (time/format "yyyyMMddHHmmssZ"
                       (ZonedDateTime/ofInstant time
                                                ZoneOffset/UTC))

          (time/zoned-date-time? time)
          (time/format "yyyyMMddHHmmssZ" time)

          (time/local-date-time? time)
          (time/format "yyyyMMddHHmmss" time)

          (time/local-date? time)
          (time/format "yyyyMMdd" time)

          (time/year-month? time)
          (time/format "yyyyMM")

          (instance? java.sql.Timestamp time)
          (format-time (.toLocalDateTime time))

          (instance? java.sql.Date time)
          (format-time (.toLocalDate time))

          :else
          (str time))))

(defn format-date
  "Formats the provided date into an HL7 messaging date, delegates to the
  `format-time` function."
  [date]
  (format-time date))

(defn parse-timestamp
  "Parses an HL7 v2 formatted field or string with a date or timestamp into a
  local date, local date time or a zoned local date and time. If the value
  cannot be parsed, it is returned unaltered."
  [timestamp-in]
  (when-not (str/blank? timestamp-in)
    (let [timestamp (str/trim timestamp-in)]
      (cond (map? timestamp)
            (parse-timestamp (unwrap-field timestamp))

            (vector? timestamp)
            (mapv #(parse-timestamp %) timestamp)

            :else
            (try
              (cond (and (<= 14 (count timestamp))
                         (or (string/includes? timestamp "+")
                             (string/includes? timestamp "-")))
                    (time/zoned-date-time timestamp-zoned-format-hl7 timestamp)

                    (<= 16 (count timestamp))
                    (time/local-date-time timestamp-format-med-hl7 timestamp)

                    (<= 14 (count timestamp))
                    (time/local-date-time timestamp-format-hl7 timestamp)

                    (<= 12 (count timestamp))
                    (time/local-date-time timestamp-format-short-hl7 timestamp)

                    (<= 8 (count timestamp))
                    (try
                      (time/local-date date-format-hl7 timestamp)
                      (catch Exception _
                        (time/local-date date-format-hl7-fallback timestamp)))

                    (<= 6 (count timestamp))
                    (time/year-month date-format-short-hl7 timestamp)

                    (<= 6 (count timestamp))
                    (time/year year-format-short-hl7 timestamp)

                    :else
                    timestamp)
              (catch Exception exception
                (logger/warn (str "Couldn't parse HL7 date/time \"" timestamp "\":")
                             (.getMessage exception))
                timestamp))))))

(defn parse-date
  "Parsed an HL7 v2 formatted field or string with a date into a local date,
  delegates to the `parse-timestamp` function. If the value cannot be parsed,
  it is returned unaltered."
  [date-string]
  (when date-string (parse-timestamp date-string)))


(defn parse-phone
  "Parses a phone number and returns a list of HL7 messaging phone number values:

      [country-code area-code phone-number extension]

  The extension must be preceded by an upper or lowercase \"x\"."
  [phone]
  (let [digits-fn #(apply str (re-seq #"[0-9]+" %))
        ext-split (string/split (string/lower-case phone) #"x" 2)
        remaining (reverse (digits-fn (first ext-split)))
        ext (if (second ext-split) (digits-fn (second ext-split)) "")
        local (apply str (reverse (take 4 remaining)))
        exchange (apply str (reverse (take 3 (drop 4 remaining))))
        area (apply str (reverse (take 3 (drop 7 remaining))))
        country (apply str (reverse (drop 10 remaining)))]
    [country area (str exchange local) ext]))

(defn trim-nils
  "Removes any `nil` or empty set values at the end of a collection. For
  instance, for the vector [1 2 3 nil nil nil] this function would return
  [1 2 3]. If the optional `no-empty-set` parameter is set to true, then
  nil will be returned instead of an empty set."
  ([coll no-empty-set]
   (let [result (trim-nils coll)]
     (if no-empty-set
       (when (and (coll? result) (< 0 (count result)))
         result)
       result)))
  ([coll]
   (let [filter-nil (fn [coll]
                      (filter #(not (or (= [] %)
                                        (= nil %))) coll))]
     (if (= 0 (count (filter-nil coll)))
       []
       (loop [not-nil []
              val-next (first coll)
              val-rest (rest coll)]
         (if (< 0 (count (filter-nil val-rest)))
           (recur (conj not-nil val-next)
                  (first val-rest)
                  (rest val-rest))
           (conj not-nil val-next)))))))

(defn read-string
  "Reads the provided text, returning a String, number, etc."
  [text]
  (try
    (edn/read-string text)
    (catch Exception _
      text)))
