(ns com.nervestaple.clinical.message-intermediate.interface.core-test
  (:require
   #?(:clj  [clojure.test :refer [deftest testing is]]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   [clojure.string :as string]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.interface.core :as core]))

(def test-message
  (str "MSH|^~\\&|AcmeHIS|StJohn|CATH|StJohn|20061019172719||ORM^O01|1788025612436|P|2.3\r"
       "PID|||20301||Durden^Tyler^^^Mr.||19700312|M|||88 Punchward Dr.^^Los Angeles^CA^11221^USA|||||||\r"
       "PV1||O|OP^^||||4652^Paulson^Robert|||OP|||||||||9|||||||||||||||||||||||||20061019172717|20061019172718\r"
       "ORC|NW|20061019172719\r"
       "OBR|1|20061019172719||76770^Ultrasound: retroperitoneal^C4|||12349876\r"))

;; hl7->record

(deftest test-hl7->record
  (testing "parses a full HL7 message into a record with all of its segments"
    (let [record (core/hl7->record test-message)]
      (is (= ["MSH" "PID" "PV1" "ORC" "OBR"] (mapv :id (:segments record))))
      (is (nil? (:sets record))))))

;; record->hl7

(deftest test-record->hl7
  (testing "serializes a record back into an HL7 message string"
    (let [round-tripped (core/record->hl7 (core/hl7->record test-message))]
      (is (string? round-tripped))
      (is (string/starts-with? round-tripped "MSH")))))

;; get-segments

(deftest test-get-segments-match
  (testing "returns every segment matching the given id"
    (let [record (core/hl7->record test-message)]
      (is (= 1 (count (core/get-segments record "PID"))))
      (is (= "PID" (:id (first (core/get-segments record "PID"))))))))

(deftest test-get-segments-no-match
  (testing "returns an empty sequence for a segment id that isn't present"
    (let [record (core/hl7->record test-message)]
      (is (= 0 (count (core/get-segments record "ZZZ")))))))

;; get-set / get-set-segment
;;
;; hl7->record never populates :sets itself (it's hardcoded to nil), so these
;; are tested against a record with a manually-assembled :sets vector.

(def record-with-sets
  (assoc (core/hl7->record test-message)
         :sets [[{:id "NK1" :index 0} {:id "NK1" :index 1}]
                [{:id "NK1" :index 2}]]))

(deftest test-get-set
  (testing "returns the set of segments at the given index"
    (is (= [{:id "NK1" :index 0} {:id "NK1" :index 1}]
           (core/get-set record-with-sets 0)))))

(deftest test-get-set-segment
  (testing "returns only the segments in the set matching the given id"
    (is (= [{:id "NK1" :index 0} {:id "NK1" :index 1}]
           (vec (core/get-set-segment record-with-sets 0 "NK1"))))))

;; hl7->parsed-message / parsed-message->record / record->parsed-message

(deftest test-hl7->parsed-message
  (testing "parses a message into the hl7-parser library's own intermediate shape"
    (let [parsed (core/hl7->parsed-message test-message)]
      (is (= #{:delimiters :segments} (set (keys parsed)))))))

(deftest test-parsed-message->record
  (testing "converting a parsed message into a record matches hl7->record directly"
    (is (= (core/hl7->record test-message)
           (core/parsed-message->record (core/hl7->parsed-message test-message))))))

(deftest test-record->parsed-message
  (testing "converting a record back to a parsed message round-trips through record->hl7"
    (let [record (core/hl7->record test-message)]
      (is (= (core/record->hl7 record)
             (parser/str-message (core/record->parsed-message record)))))))

;; dump

(deftest test-dump
  (testing "prints a human-readable version of the record without throwing"
    (let [output (with-out-str (core/dump (core/hl7->record test-message)))]
      (is (string/includes? output "Segments:"))
      (is (string/includes? output "MSH")))))
