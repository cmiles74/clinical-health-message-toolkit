(ns com.nervestaple.clinical.message-intermediate.segment.cljs-date-time
  (:require
   [cljs.spec.alpha :as s]
   [cljs.spec.gen.alpha :as gen]
   [clojure.test.check.generators :as gens]
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
