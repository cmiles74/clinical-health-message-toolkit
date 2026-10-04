(ns com.nervestaple.clinical.message-intermediate.segment.java-date-time
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [clojure.test.check.generators :as gens]
   [com.nervestaple.clinical.log.interface :as log]
   [java-time.api :as time]))

;;
;; Spec and Generators for Java date and time instances
;;

(s/def ::local-date-timeb
  (s/with-gen time/local-date-time?
    #(gen/fmap (fn [date-parts] (apply time/local-date-time date-parts))
               (gens/let [year (s/gen (s/int-in
                                       (- (.getValue (java.time.Year/now)) 50)
                                       (.getValue (java.time.Year/now))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::zoned-date-time
  (s/with-gen time/zoned-date-time?
    #(gen/fmap (fn [date-parts] (apply time/zoned-date-time date-parts))
               (gens/let [year (s/gen (s/int-in
                                       (- (.getValue (java.time.Year/now)) 50)
                                       (.getValue (java.time.Year/now))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::instant
  (s/with-gen time/instant?
    #(gen/fmap (fn [date-parts] (time/instant (apply time/zoned-date-time date-parts)))
               (gens/let [year (s/gen (s/int-in
                                       (- (.getValue (java.time.Year/now)) 50)
                                       (.getValue (java.time.Year/now))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::offset-date-time
  (s/with-gen time/offset-date-time?
    #(gen/fmap (fn [date-parts] (apply time/offset-date-time date-parts))
               (gens/let [year (s/gen (s/int-in
                                       (- (.getValue (java.time.Year/now)) 50)
                                       (.getValue (java.time.Year/now))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::local-date
  (s/with-gen time/local-date?
    #(gen/fmap (fn [date-parts] (apply time/local-date date-parts))
               (gens/let [year (s/gen (s/int-in
                                       (- (.getValue (java.time.Year/now)) 50)
                                       (.getValue (java.time.Year/now))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))]
                 [year month day]))))

(s/def ::timestamp (s/nilable
                    (s/with-gen (s/and string? #(re-matches #"\d{1,24}" %))
                      #(gen/fmap (fn [val] (time/format "yyyyMMddHHmmss"
                                                       val))
                                 (s/gen ::local-date-time)))))

(defn unwrap-field
  "Accepts a field of parsed HL7 v2 data, which may be one item of data or a
  sequence of data items, and unwraps either the single item or each item in the
  sequence and returns the data either the single item or vector of items."
  [field]
  (if (and (vector? field) (map? (first field)))
    (mapv #(:content %) field)
    (if (map? field) (:content field) field)))

(defn zoned-date-time
  "Returns the current date and time in the current timezone."
  []
  (time/zoned-date-time))

(defn format-time
  "Formats the provided date and time into an HL7 messaging date."
  [time]
  (when time
    (cond (time/instant? time)
          (time/format "yyyyMMddHHmmssZ" "UTC")

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

(defn parse-timestamp
  "Parses an HL7 v2 formatted field or string with a date or timestamp into a
  local date, local date time or a zoned local date and time. If the value
  cannot be parsed, it is returned unaltered."
  [timestamp-in]
  (when-not (string/blank? timestamp-in)
    (let [timestamp (string/trim timestamp-in)]
      (cond (map? timestamp)
            (parse-timestamp (unwrap-field timestamp))

            (vector? timestamp)
            (mapv #(parse-timestamp %) timestamp)

            :else
            (try
              (cond (and (<= 14 (count timestamp))
                         (or (string/includes? timestamp "+")
                             (string/includes? timestamp "-")))
                    (time/zoned-date-time "yyyyMMddHHmmssZ" timestamp)

                    (<= 16 (count timestamp))
                    (time/local-date-time "yyyyMMddHHmmssSS" timestamp)

                    (<= 14 (count timestamp))
                    (time/local-date-time "yyyyMMddHHmmss" timestamp)

                    (<= 12 (count timestamp))
                    (time/local-date-time "yyyyMMddHHmm" timestamp)

                    (<= 8 (count timestamp))
                    (try
                      (time/local-date "yyyyMMdd" timestamp)
                      (catch Exception _
                        (time/local-date "yyyy-MM-dd" timestamp)))

                    (>= 5 (count timestamp))
                    (time/year-month "yyyyMM" timestamp)

                    (= 4 (count timestamp))
                    (time/year "yyyy" timestamp)

                    :else
                    timestamp)
              (catch Exception exception
                (log/warn (str "Couldn't parse HL7 date/time \"" timestamp "\":")
                          (.getMessage exception))
                timestamp))))))

(defn parse-date
  "Parses a hypen separated date."
  [date]
  (time/local-date "yyyy-MM-dd" date))
