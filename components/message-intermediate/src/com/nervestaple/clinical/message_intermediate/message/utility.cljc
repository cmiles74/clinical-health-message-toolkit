(ns com.nervestaple.clinical.message-intermediate.message.utility
  (:require
   [com.nervestaple.hl7-parser.parser :as hl7-parser]
   [com.nervestaple.hl7-parser.message :as hl7-message]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

(defn get-segment-field
  "Returns the field with the provided index from the given
  segment. Keep in mind that this function expects the index to adhere
  to the HL7 specification where the first field of data is located at
  index 1. Another gotcha in the MSH segment, the first field of data
  starts at index 2 and that's the list of delimiters.

  This function will return the id of the segment if you ask for index
  0. For the MSH segment, it will return nil for index 1 instead of
  returning the field delimiter. If you want the field delimiter you
  can get it under the :delimiter key of the message.

  If there is no data at the given index, nil will be returned."
  ([segment index]
   (get-segment-field segment index false))
  ([segment index raw?]
   (when (<= index (inc (count (:fields segment))))                   ;; HL7 fields start at index 1
     (util/unwrap-field (hl7-message/get-segment-field segment index raw?)))))

(defn replace-first
  "Removes the first item in a collection and replaces it with the result of
  applying that item to the lookup-fn. The lookup-fn should be a one argument
  function."
  [lookup-fn data]
  (if (coll? data)
    (vec (conj (drop 1 data) (lookup-fn (first data))))
    (lookup-fn data)))

(defn extract-trailing
  "Extracts and unwraps all of the data from a segment of parsed HL7 v2 data
  starting at the provided HL7 v2 index and returns that data as a vector."
  [segment index]
  (when (<= index (count (:fields segment)))                        ;; make sure there's data to extract
    (let [fields (range index (+ 2 (count (:fields segment))))]     ;; figure out our indexes
      (mapv #(get-segment-field segment %)                          ;; get the data at those indexes
            fields))))

(defn segment-to-hl7-remainder
  "Accepts a function that converts a segment record into a segment of parsed HL7
  v2 data and returns another function that converts the record, in addition any
  data on the :remainder key of the record to be converted is appended to the
  end of the returned HL7 v2 data."
  [to-hl7-fn]
  #(let [fields (to-hl7-fn %)
         remainder (:remainder %)]
     (if remainder
       (into fields (mapv hl7-parser/create-field remainder))
       fields)))

(defn record-to-hl7
  "Converts a segment record into a segment of parsed HL7 v2 data by creating a
  new parsed HL7 segment of with the provided `segment-id` and then applying the
  `to-hl7-fn` to the supplied record of data and adding it to the segment."
  [segment-id to-hl7-fn record]
  (let [to-field-fn (segment-to-hl7-remainder to-hl7-fn)]
    (apply (partial hl7-parser/create-segment segment-id) (to-field-fn record))))

(defn validate-segment
  "Accepts a segment identifier and a segment, if the segment's identifier doesn't
  match the supplied `segment-id` then an exception is thrown."
  [segment-id segment]
  (when (not= segment-id (:id segment))
    (throw (Exception. (str "Parsed segment data is not of type \"" segment-id
                           "\", found " (:id segment))))))

(defn hl7-to-record
  "Converts a segment of parsed HL7 data into an HL7 record by applying the
  supplied map of segment data to the `to-type` function. Any segment data that
  is not included in the record (that is, has an index greater that the HL7
  index `field-limit`) will be present on the record under the key `:remainder`."
  [segment-id field-limit to-type-fn segment]
  (validate-segment segment-id segment)
  (let [remainder (extract-trailing segment field-limit)
        record (to-type-fn segment)]
    (if remainder
      (assoc record :id segment-id :remainder remainder)
      (assoc record :id segment-id))))
