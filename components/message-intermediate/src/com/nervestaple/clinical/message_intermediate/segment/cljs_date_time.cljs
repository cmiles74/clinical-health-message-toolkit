(ns com.nervestaple.clinical.message-intermediate.segment.cljs-date-time
  (:require
   [cljs.spec.alpha :as s]
   [cljs.spec.gen.alpha :as gen]
   [clojure.string :as string]
   [clojure.test.check.generators :as gens]
   [com.nervestaple.clinical.log.interface :as log]
   [tick.core :as t]
   [tick.timezone]
   ["@js-joda/locale_en-us" :as locale]))

;;
;; Spec and Generators for Java date and time instances
;;

(s/def ::local-date-time
  (s/with-gen t/date-time?
    #(gen/fmap (fn [[year month day hour minutes seconds]]
                 (t/at (t/new-date year month day)
                       (t/new-time hour minutes seconds)))
               (gens/let [year (s/gen (s/int-in
                                       (- (t/int (t/year)) 50)
                                       (t/int (t/year))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::zoned-date-time
  (s/with-gen t/zoned-date-time?
    #(gen/fmap (fn [[year month day hour minutes seconds]]
                 (t/in (t/at (t/new-date year month day)
                             (t/new-time hour minutes seconds))
                       "America/New_York"))
               (gens/let [year (s/gen (s/int-in
                                       (- (t/int (t/year)) 50)
                                       (t/int (t/year))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::instant
  (s/with-gen t/instant?
    #(gen/fmap (fn [[year month day hour minutes seconds]]
                 (t/instant
                  (t/in (t/at (t/new-date year month day)
                              (t/new-time hour minutes seconds))
                        "America/New_York")))
               (gens/let [year (s/gen (s/int-in
                                       (- (t/int (t/year)) 50)
                                       (t/int (t/year))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::offset-date-time
  (s/with-gen t/offset-date-time?
    #(gen/fmap (fn [[year month day hour minutes seconds]]
                 (t/offset-by
                  (t/at (t/new-date year month day)
                        (t/new-time hour minutes seconds))
                  -5))
               (gens/let [year (s/gen (s/int-in
                                       (- (t/int (t/year)) 50)
                                       (t/int (t/year))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))
                          hour (s/gen (s/int-in 1 24))
                          minutes (s/gen (s/int-in 1 60))
                          seconds (s/gen (s/int-in 1 60))]
                 [year month day hour minutes seconds]))))

(s/def ::local-date
  (s/with-gen t/date?
    #(gen/fmap (fn [date-parts] (apply t/new-date date-parts))
               (gens/let [year (s/gen (s/int-in
                                       (- (t/int (t/year)) 50)
                                       (t/int (t/year))))
                          month (s/gen (s/int-in 1 13))
                          day (s/gen (s/int-in 1 29))]
                 [year month day]))))

(s/def ::timestamp (s/nilable
                    (s/with-gen (s/and string? #(re-matches #"\d{1,24}" %))
                      #(gen/fmap (fn [val]
                                   (t/format
                                    (t/formatter "yyyyMMddHHmmss" (.. locale -Locale -US))
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
  "Returns the current date and time in the Eastern time zone."
  []
  (t/in (t/at (t/new-date) (t/new-time))
        "America/New_York"))

(defn format-time
  "Formats the provided date and time into an HL7 messaging date."
  [time]
  (when time
    (cond (t/instant? time)
          (t/format (t/formatter "yyyyMMddHHmmssZ" locale)
                    (t/in time "UTC"))

          (t/zoned-date-time? time)
          (t/format (t/formatter "yyyyMMddHHmmssZ" locale) time)

          (t/date-time? time)
          (t/format (t/formatter "yyyyMMddHHmmss" locale) time)

          (t/date? time)
          (t/format (t/formatter "yyyyMMdd" locale) time)

          (t/year-month? time)
          (t/format (t/formatter "yyyyMM" locale) time)

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
                    (t/parse-zoned-date-time timestamp
                                             (t/formatter "yyyyMMddHHmmssZ" locale))

                    (<= 16 (count timestamp))
                    (t/parse-date-time timestamp
                                       (t/formatter "yyyyMMddHHmmssSS" locale))

                    (<= 14 (count timestamp))
                    (t/parse-date-time timestamp
                                       (t/formatter "yyyyMMddHHmmss" locale))

                    (<= 12 (count timestamp))
                    (t/parse-date-time timestamp
                                       (t/formatter "yyyyMMddHHmm" locale))

                    (<= 8 (count timestamp))
                    (try
                      (t/parse-date timestamp
                                    (t/formatter "yyyyMMdd" locale))
                      (catch js/Error _
                        (t/parse-date timestamp
                                      (t/formatter "yyyy-MM-dd" locale))))

                    (<= 5 (count timestamp))
                    (t/parse-year-month timestamp
                                        (t/formatter "yyyyMM" locale))

                    (= 4 (count timestamp))
                    (t/parse-year timestamp
                                  (t/formatter "yyyy" locale))

                    :else
                    timestamp)
              (catch js/Error error
                (log/warn (str "Couldn't parse HL7 date/time \"" timestamp "\":")
                          error)
                timestamp))))))


