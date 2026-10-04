(ns com.nervestaple.clinical.message-intermediate.segment.utility
  (:refer-clojure :exclude [read-string])
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]))

(def default-delimiters
  "Map with our default set of delimiters"
  {:field 124
   :component 94
   :subcomponent 38
   :repeating 126
   :escape 92})


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

#?(:clj
   (defn read-string
     "Reads the provided text, returning a String, number, etc."
     [text]
     (try
       (edn/read-string text)
       (catch Exception _
         text))))

#?(:cljs
   (defn read-string
     "Reads the provided text, returning a String, number, etc."
     [text]
     (try
       (edn/read-string text)
       (catch js/Error _
         text))))
