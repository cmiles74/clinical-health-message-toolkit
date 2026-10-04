(ns com.nervestaple.clinical.message-intermediate.utility
  (:require
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   #?(:clj [com.nervestaple.clinical.message-intermediate.segment.java-date-time :as date-time])
   #?(:cljs [com.nervestaple.clinical.message-intermediate.segment.cljs-date-time :as date-time])))

(def default-message-version
  "Default HL7 Messaging specification version"
  "2.3")

(defn current-time
  "Returns the current time with a time zone"
  []
  (date-time/zoned-date-time))

(defn parse-date
  "Parses a date in the format YYYY-MM-DD into an HL7 date"
  [date]
  (date-time/parse-date date))

(defn parse-phones
  "Parses a map of incoming phone data and a set of email addresses into a set of
  HL7 messaging fields"
  [phone-map email-addresses]
  (let [phones (dissoc phone-map :Business)
        phones-out (when email-addresses
                     (map #(parser/create-field (segment/string->xtn-email %))
                          email-addresses))]
    (into phones-out (map #(parser/create-field
                            (segment/string->xtn-phone
                             (keyword (first %)) :Phone (last %))) phones))))

(defn filter-segment-id
  "Filters incoming messages by including only those messages that have at least
  one segment with the matching segment identifier."
  [segment-id messages]
  (filter (fn [message]
            (some #(= segment-id %)
                  (map :id (:segments message))))
          messages))
