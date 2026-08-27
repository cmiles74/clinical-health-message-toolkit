(ns com.nervestaple.clinical.message-intermediate.segment.date-time
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [clojure.test.check.generators :as gens]
   [java-time :as time]))

;;
;; Spec and Generators for Java date and time instances
;;

(s/def ::local-date-time
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
                      #(gen/fmap (fn [val] (time/format "YYYYMMddHHmmss"
                                                       val))
                                 (s/gen ::local-date-time)))))
