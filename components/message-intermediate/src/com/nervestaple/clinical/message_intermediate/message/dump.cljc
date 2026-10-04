(ns com.nervestaple.clinical.message-intermediate.message.dump
  (:require
   [clojure.pprint :as pprint]
   [com.nervestaple.hl7-parser.dump :as hl7-dump]))

;;
;; Functions for viewing message data
;;

(defn dump-seq
  "Returns a set of strings that represent a human-readable version of the
  sequence of values. If an `indent` value is provided it will be used to pad
  the front of the returned strings. If a `dump-record-fn` is provided, records
  will be dumped using that function."
  ([dump-record-fn seq-in]
   (dump-seq dump-record-fn seq-in 0))
  ([dump-record-fn seq-in indent]
   (let [this-indent (apply str (doall (take indent (repeat "  "))))]
     (map #(if (map? %)
             (str this-indent "<" (.getSimpleName (class %)) "> {\n"
                  (apply str (dump-record-fn  % (inc indent)))
                  this-indent "}\n")
             (if %
               (str this-indent "<" (.getSimpleName (class %))  "> " % "\n")
               (str this-indent % "\n")))
          seq-in))))

(defn dump-record
  "Returns a set of strings that represent a human-readable version of the
   record. If an `indent` value is provided it will be used to pad the front
   of the returned strings."
  ([map-in]
   (dump-record map-in 0))
  ([map-in indent]
   (let [this-indent (apply str (doall (take indent (repeat "  "))))]
     (map #(str this-indent
                (first %) " <" (.getSimpleName (class (second %))) ">"
                (cond (map? (second %))
                      (str "\n" (apply str (dump-record (second %) (inc indent))))

                      (sequential? (second %))
                      (if (< 0 (count (second %)))
                        (str " [\n"
                             (apply str (dump-seq dump-record (second %) (inc indent)))
                             this-indent " ]\n")
                        " []\n")

                      :else
                      (str " " (second %) "\n")))
          (filter (comp some? val) map-in)))))

(defn dump-segments
  "Returns a set of strings representing a human-readable version of the provided
  segments."
  [segments]
  (apply concat (map #(into [(str "  " (:id %) "\n")]
                            (dump-record % 2))
                     segments)))

(defn dump-sets
  "Returns a sequence of human-readable versions of the provided set data."
  [sets-in]
  (loop [output [] index 0 set (first sets-in) sets (rest sets-in)]
    (if set
      (recur (concat output
                     (map #(concat [(str "  Segment: " (:id %)
                                         index ", Set ID: " (:set-id %) "\n")]
                                   (dump-record % 2))
                          set))
             (inc index)
             (first sets)
             (rest sets))
      output)))

(defn message
  [message]
  (println "Segments:")
  (apply println (dump-segments (:segments message)))
  (println)
  (println "Sets:")
  (dorun (map #(apply println %) (dump-sets (:sets message))))
  (println)
  (pprint/pprint (:remainder message)))
