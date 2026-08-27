(ns com.nervestaple.clinical.message-intermediate.message
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.nervestaple.hl7-parser.parser :as hl7-parser]
   [com.nervestaple.clinical.message-intermediate.segment.parser :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; record representing an HL7 v2 message
(defrecord message
    [delimiters segments sets remainder])

(s/def ::delimiters
  (s/with-gen map?
    #(gen/fmap identity (s/gen #{util/default-delimiters}))))
(s/def ::segments ::segment/segments)
(s/def ::sets ::segment/segment-set)
(s/def ::remainder (s/nilable (s/coll-of string?)))

(s/def ::spec
  (s/keys :req-un [::delimiters ::segments]
          :opt-un [::sets ::remainder]))

(defn hl7->record
  "Accepts a parsed HL7 v2 message and returns a message record with the same
  data. Data segments that are part of a set are stored in on the `:sets`
  key (in order), segments that cannot be parsed are stored on the `:remainder`
  key."
  [message]
  (loop [index 0
         segment-this (first (:segments message))
         segments (rest (:segments message))
         parsed-segments []
         remainder []]
    (if (not-empty segment-this)
      (let [ps (segment/hl7->record segment-this (:delimiters message))
            ps-indexed (when ps (assoc ps :index index))]
        (recur (inc index)                             ;; increment our index
               (first segments)                        ;; pull out our current segment
               (rest segments)                         ;; hang onto the remaining segments
               (conj parsed-segments ps-indexed)       ;; segments we have parsed

               ;; unparsed segments added to the remainder
               (if (not ps) (conj remainder (assoc segment-this :index index))
                   remainder)))
      (message. (:delimiters message)
                (remove nil? parsed-segments)
                nil
                remainder))))

(defn record->hl7
  "Accepts a message record and returns a parsed HL7 v2 message."
  [message]
  (let [segments (into [] (concat (:segments message)             ;; collect segments in a vector
                                  (:remainder message)
                                  (flatten (:sets message))))
        segments-sorted (remove nil? (sort-by :index segments))]  ;; remove nil, sort the segments

    ;; add our segments to a new message
    (apply (partial hl7-parser/create-message (:delimiters message))
           (map #(if (record? %) (segment/record->hl7 %)
                     %)
                segments-sorted))))

(defn get-segments
  "Returns all of the segments (those that are not part of a set) with the
   matching segment ID from the provided message record."
  [message segment-id]
  (filter #(= segment-id (:id %)) (:segments message)))

(defn get-set
  "Returns the set of segment data that is at the provided index in the given
   message."
  [message set-index]
  (nth (:sets message) set-index))

(defn get-set-segment
  "Returns all of the segments from the set at the provided index that match
   the given segment id in the message."
  [message set-index segment-id]
  (filter #(= segment-id (:id %))
          (get-set message set-index)))
