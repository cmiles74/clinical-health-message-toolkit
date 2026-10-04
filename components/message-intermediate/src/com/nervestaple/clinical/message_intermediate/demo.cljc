(ns com.nervestaple.clinical.message-intermediate.demo
  (:gen-class)
  (:require
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.acknowledgement :as ack]))

;;
;; Functions for demonstrating the HL7 v2 Segment parser
;;

(def test-ack-message
  "MSH|^~\\&|ImmTrac24.16|TEXIIS||BURL6343|20210415044526-0500||ACK^V04^ACK|7008167375|P|2.5.1|||NE|NE|||||Z23^CDCPHINVS|TEXIIS|BURL6343\rMSA|AE|7008167375\rERR||NK1^^0|101^Required field missing^HL70357|W|4^Invalid value^HL70533|||IEE-519::Warning. NK1 Segment/Responsible person, missing.")

(defn main
  "Parses a sample HL& acknowledgement message into a parsed message and then
  parses that into an acknowledgement message."
  [& args]
  (let [parsed-message (parser/parse test-ack-message) ;; parse our HL7 v2 message
        message (ack/hl7->message parsed-message)]    ;; parse our message into an ACK
    (println message)))

(defn -main
  "Provides the main function needed to bootstrap the application."
  [& args]
  (main args))
