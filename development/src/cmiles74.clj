(ns cmiles74
  (:require
   [clojure.pprint :as p]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.hl7-parser.message :as parser-message]
   [com.nervestaple.hl7-parser.test :as parser-test]
   [com.nervestaple.clinical.message-intermediate.interface.core :as message]
   [com.nervestaple.clinical.message-intermediate.interface.segment :as segment]))

(def test-message (parser-test/test-message))

(def test-record (message/hl7->record test-message))

(defn gen-segment []
  (gen/generate (s/gen ::segment/msh)))

(defn gen-message []
  (gen/generate (s/gen ::message/message)))

(defn gen-message->hl7 []
  (message/record->hl7 (gen/generate (s/gen ::message/message))))

(defn gen-ack []
  (let [record (gen-message->hl7)
        parsed (message/hl7->parsed-message record)]
    (parser-message/ack-message {:sending-app "TUTORIAL"
                                 :sending-facility "TUTORIAL FACILITY"
                                 :production-mode "T"
                                 :version "2.7.1"
                                 :text-message "Processed successfully!"}
                                "AA"
                                parsed)))

;;(message/hl7->parsed-message (gen-message->hl7))
(defn gen-message []
  (let [record (gen/generate (s/gen ::message/message))
        message (message/record->hl7 record)]
    (message/hl7->parsed-message message)
    message))
