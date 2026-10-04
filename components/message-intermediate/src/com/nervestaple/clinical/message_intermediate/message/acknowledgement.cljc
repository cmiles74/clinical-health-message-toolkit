(ns com.nervestaple.clinical.message-intermediate.message.acknowledgement
  (:require
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.hl7-parser.message :as message]
   [com.nervestaple.clinical.message-intermediate.segment.msa :as msa]
   [com.nervestaple.clinical.message-intermediate.segment.msh :as msh]
   [com.nervestaple.clinical.message-intermediate.segment.err :as err]
   [clojure.spec.alpha :as s]))

(defrecord record
    [message-header message-acknowledgement error])

(s/def ::message-header ::msh/spec)
(s/def ::message-acknowledgement ::msh/spec)
(s/def ::error (s/nilable (s/coll-of ::err/spec)))

(defn ack?
  "Returns true if the provided message is an \"acknowledgement\" message (it has
  an MSH segment with a message type of \"ACK\")."
  [message]
  (= "ACK"
     (first (:content (message/get-field-first message "MSH" 9)))))

(defn control-id-consistent?
  "Returns true if the unique control identifier in MSH segment matches the
  control identifier in the MSA segment."
  [message]
  (= (message/get-field-first-value message "MSH" 10)
     (first (message/get-field-first message "MSA" 2))))

(defn record->hl7
  "Accepts a record of acknowledgement (ACK) message data and returns an ACK
  record."
  [record]
  (let [delimiters (:delimiters (:message-header record))
        segments [(msh/record->hl7 (:message-header record))
                  (msa/record->hl7 (:message-acknowledgement record))]
        errors (mapv err/record->hl7 (:error record))]
    (apply (partial parser/create-message delimiters)
           (into segments errors))))

(defn hl7->message
  "Accepts a parsed HL7 v2 acknowledgement message and returns an
  acknowledgement (ACK) record."
  [record]
  {:message-header (msh/hl7->record
                    (first (filter #(= "MSH" (:id %)) (:segments record)))
                    (:delimiters record))
   :message-acknowledgement (msa/hl7->record (first (filter #(= "MSA" (:id %))
                                                             (:segments record))))
   :error (mapv err/hl7->record (filter #(= "ERR" (:id %)) (:segments record)))})

