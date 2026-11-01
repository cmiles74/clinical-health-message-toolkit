;;
;; Functions to make it easier to work with parsed HL7 messages.
;;
(ns com.nervestaple.clinical.message-parser.message
  (:require
   [com.nervestaple.clinical.message-parser.parser
    :refer [create-message create-segment
            create-field pr-delimiters pr-field
            format-timestamp]]))

(def REGEX-MESSAGE-ID
     #"MSH\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|[^\|]*\|([^\|]*)\|")

(defn message-id-unparsed
  [message]
  (let [matches (re-find REGEX-MESSAGE-ID message)]
    (if (and matches (second matches))
      (second matches))))

(defn segment-ids
  [message]
  (map (fn [segment] (:id segment)) (:segments message)))

(defn get-segments
  [message segment-id]
  (filter (fn [segment] (= segment-id (:id segment)))
          (:segments message)))

(defn get-segment-field
  ([segment index]
   (get-segment-field segment index false))
  ([segment index raw?]
   (cond

     ;; handle MSH differently
     (= "MSH" (:id segment))
     (cond

       ;; index 0 returns the segment id
       (= 0 index)
       (:id segment)

       ;; index 1 should return the field delimiter
       (= 1 index)
       nil

       ;; correct our index and return the field
       :else
       (let [real-index (- index 2)]
         (when (> (count (:fields segment)) real-index)
           (nth (:fields segment) real-index))))

     :else
     (cond

       ;; index 0 returns the segment id
       (= 0 index)
       (:id segment)

       ;; correct our index and return the field
       :else
       (let [real-index (dec index)
             field (when (< real-index (count (:fields segment)))
                     (nth (:fields segment) real-index))]
         (if raw? field
             (if (map? field)
               (:content field)
               field)))))))

(defn get-segment-field-raw
  [segment index]
  (get-segment-field segment index true))

(defn get-field
  [message segment-id field-index]
  (map (fn [segment] (get-segment-field segment field-index))
       (get-segments message segment-id)))

(defn- get-nth-field
  "Returns the item at index in the collection of field data. If
  passed a collection of fields, the item at index from each field is
  returned. If passed a collection that contains subcomponents, the
  item at index for each subcomponent is returned."
  [index field-or-fields]
  (cond
    (map? (first field-or-fields))
    (map (fn [field]
           (get-nth-field index (:content field)))
         field-or-fields)

    (coll? (first field-or-fields))
    (map (partial get-nth-field index) field-or-fields)

    :else
    (nth field-or-fields index)))

(defn get-field-component
  [message segment-id field-index component-index]
  (let [data (flatten (get-field message segment-id field-index))]
    (get-nth-field component-index data)))

(defn set-field
  [message segment-id field-index value]
  ;; correct our index and value (put an atom in a collection)
  (let [field-index-fixed (if (= "MSH" segment-id)
                            (- field-index 2) (dec field-index))
        field-value (if (coll? value) value [value])]

    ;; throw an error if we have an illegal HL7 index
    (when (< field-index-fixed 0)
      (throw #?(:clj (Exception. "The first field is at index 1")
                :cljs (js/Error. "The first field is at index 1"))))

    ;; create a whole new message
    {:delimiters (:delimiters message)

      ;; map over our segments looking for the one we're changing
     :segments (map (fn [segment]

                      (if (= segment-id (:id segment))

                        (do
                          ;; the field has to be one this segment has, assoc
                          ;; past the end of the fields fails differently on
                          ;; each platform
                          (when (< (count (:fields segment)) field-index-fixed)
                            (throw #?(:clj (Exception. (str "There is no field at index "
                                                           field-index " in the "
                                                           segment-id " segment"))
                                      :cljs (js/Error. (str "There is no field at index "
                                                            field-index " in the "
                                                            segment-id " segment")))))

                          ;; associate our new fields
                          (assoc segment :fields

                                ;; associate our new value with the
                                ;; field collections
                                 (assoc (:fields segment)
                                        field-index-fixed
                                        (create-field field-value))))

                         ;; return the segment unaltered
                        segment))

                    (:segments message))}))

(defn extract-text-from-segments
  [parsed-message segment-type index & options]
  (apply str (if (first options)
               (interpose (first options)
                          (flatten (get-field parsed-message segment-type index)))
               (flatten (get-field parsed-message segment-type index)))))

(defn get-field-first
  [parsed-message segment-id field-index]
  (first (get-field parsed-message segment-id field-index)))

(defn get-field-first-value
  [parsed-message segment-id field-index]
  (let [field (first (map #(get-segment-field-raw % field-index)
                          (get-segments parsed-message segment-id)))]
    (pr-field (:delimiters parsed-message) field)))

(defn ack-message
  [options ack-status parsed-message]
  ;; make sure the sender of this message is looking to receive an
  ;; acknowledgement
  (let [accept-ack-type (get-field-first-value parsed-message "MSH" 15)]
    (when-not (or (= "NE" accept-ack-type)
                  (= "ER" accept-ack-type))

      ;; we are returning an acknowledgement
      (create-message (:delimiters parsed-message)
                      (create-segment "MSH"
                                      (create-field (pr-delimiters (:delimiters parsed-message)))
                                      (create-field [(:sending-app options)])
                                      (create-field [(:sending-facility options)])
                                      (get-field-first parsed-message "MSH" 3)
                                      (get-field-first parsed-message "MSH" 4)
                                      (create-field [(or (:message-id options)
                                                         (format-timestamp))])
                                      (create-field [])
                                      (create-field ["ACK"])
                                      (get-field-first parsed-message "MSH" 10)
                                      (create-field [(:production-mode options)])
                                      (create-field [(:version options)]))
                      (create-segment "MSA"
                                      (create-field [ack-status])
                                      (get-field-first parsed-message "MSH" 10)
                                      (create-field [(:text-message options)]))))))

(defn ack-message-fallback
  "Returns a parsed message that contains an acknowledgement message for the
  provided un-parsed message, the acknowledgement message will use default
  delimiters. Use this function when you have an HL7v2 message that you need to
  acknowledge but you cannot parse.

  The 'option' should be a hash-map with the following keys:

      :sending-app, :sending-facility, :production-mode, :version,
      :text-message

  Optionally, a `:message-id` key may be provided if you need a specific
  value.

  These values will be used to fill out the ACK message. The
  'ack-status' field should be a valid HL7 version 2.x acknowledgment
  status:

      AA (accepted), AE (error), AR (rejected)"
  [options ack-status message]
  ;; we are returning an acknowledgement
  (create-message {:field 124, :component 94, :subcomponent 38,
                   :repeating 126, :escape 92}
                  (create-segment "MSH"
                                  (create-field (pr-delimiters {:field 124, :component 94, :subcomponent 38,
                                                                :repeating 126, :escape 92}))
                                  (create-field [(:sending-app options)])
                                  (create-field [(:sending-facility options)])
                                  (create-field ["UNKNOWN"])
                                  (create-field ["UNKNOWN"])
                                  (create-field [(or (:message-id options)
                                                     (format-timestamp))])
                                  (create-field [])
                                  (create-field ["ACK"])
                                  (create-field (message-id-unparsed message))
                                  (create-field [(:production-mode options)])
                                  (create-field [(:version options)]))
                  (create-segment "MSA"
                                  (create-field [ack-status])
                                  (create-field (message-id-unparsed message))
                                  (create-field [(:text-message options)]))))
