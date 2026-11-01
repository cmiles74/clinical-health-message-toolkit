(ns com.nervestaple.clinical.message-parser.parser-test
  (:require
   [clojure.string :as string]
   [clojure.test :refer [deftest is testing]]
   [com.nervestaple.clinical.message-parser.interface :as sut]
   [com.nervestaple.clinical.message-parser.sample-message :as sample]))

(def short-message-parsed
  {:delimiters
   {:field 124, :component 94, :subcomponent 38, :repeating 126, :escape 92},
   :segments
   [{:id "MSH",
     :fields
     [{:content ["^~\\&"]}
      {:content ["AcmeHIS"]}
      {:content ["StJohn"]}
      {:content ["CATH"]}
      {:content ["StJohn"]}
      {:content ["20061019172719"]}
      {:content []}
      {:content ["ORM" "O01"]}
      {:content ["1676735383748"]}
      {:content ["P"]}
      {:content ["2.3"]}]}
    {:id "PID",
     :fields
     [{:content []}
      {:content []}
      {:content ["20301"]}
      {:content []}
      {:content ["Durden" "Tyler" "" "" "Mr."]}
      {:content []}
      {:content ["19700312"]}
      {:content ["M"]}
      {:content []}
      {:content []}
      {:content ["88 Punchward Dr." "" "Los Angeles" "CA" "11221" "USA"]}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}]}
    {:id "PV1",
     :fields
     [{:content []}
      {:content ["O"]}
      {:content ["OP" "" ""]}
      {:content []}
      {:content []}
      {:content []}
      {:content ["4652" "Paulson" "Robert"]}
      {:content []}
      {:content []}
      {:content ["OP"]}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content ["9"]}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content []}
      {:content ["20061019172717"]}
      {:content ["20061019172718"]}]}
    {:id "ORC", :fields [{:content ["NW"]} {:content ["20061019172719"]}]}
    {:id "OBR",
     :fields
     [{:content ["1"]}
      {:content ["20061019172719"]}
      {:content []}
      {:content ["76770" "Ultrasound: retroperitoneal" "C4"]}
      {:content []}
      {:content []}
      {:content ["12349876"]}]}]})

(deftest parse-message-test
  (testing "Parses a test message"
    (is (= short-message-parsed (sut/parse (sample/message))))))

(deftest parse-message-test-no-trailing-segment-delimiter
  (testing "Parses a test message"
    (is (= short-message-parsed (sut/parse (string/trim (sample/message)))))))

(deftest parse-message-test-extra-trailing-segment-delimiter
  (testing "Parses a test message"
    (is (thrown? #?(:clj Exception :cljs js/Error) (sut/parse (str (sample/message) (char sut/ASCII_CR)))))))

(deftest emit-message-test
  (testing "Emits the test message"
    (is (= (sample/message) (sut/str-message short-message-parsed)))))

(deftest empty-message-delimiters-test
  (testing "Empty messages contain default delimiters"
    (is (= (:delimiters short-message-parsed)
           (:delimiters (sut/create-empty-message))))))

(deftest pr-delimiters-test
  (testing "Returns a string with the provided delimiters"
    (is (= "^~\\&"
           (sut/pr-delimiters sut/DEFAULT-DELIMITERS)))))

(deftest create-empty-message
  (testing "Creates an empty message"
    (is (= {:delimiters
            {:field 124, :component 94, :subcomponent 38, :repeating 126, :escape 92},
            :segments []}
           (sut/create-empty-message)))))

(deftest create-empty-message-with-delimiters
  (testing "Creates an empty message"
    (is (= {:delimiters
            {:field 1 :component 2 :subcomponent 3 :repeating 4 :escape 5}
            :segments []}
           (sut/create-empty-message
            {:field 1 :component 2 :subcomponent 3 :repeating 4 :escape 5})))))

(deftest create-segment-test
  (testing "Creates an empty segment"
    (is (= {:id "PID" :fields []}
           (sut/create-segment "PID")))))

(deftest create-segment-with-fields
  (testing "Creates a segment with field data"
    (is (= {:id "PID",
            :fields
            [{:content []}
             {:content []}
             {:content ["20301"]}
             {:content []}
             {:content ["Durden" "Tyler" "" "" "Mr."]}
             {:content []}
             {:content ["19700312"]}
             {:content ["M"]}
             {:content []}
             {:content []}
             {:content ["88 Punchward Dr." "" "Los Angeles" "CA" "11221" "USA"]}]}
           (sut/create-segment "PID"
                                  (sut/create-field)
                                  (sut/create-field)
                                  (sut/create-field "20301")
                                  (sut/create-field)
                                  (sut/create-field ["Durden" "Tyler" nil nil "Mr."])
                                  (sut/create-field)
                                  (sut/create-field "19700312")
                                  (sut/create-field "M")
                                  (sut/create-field)
                                  (sut/create-field)
                                  (sut/create-field ["88 Punchward Dr." nil "Los Angeles" "CA" "11221" "USA"]))))))

(deftest add-segment-to-message
  (testing "Creates a message and adds as segment"
    (is (= {:delimiters
            {:field 124, :component 94, :subcomponent 38, :repeating 126, :escape 92},
            :segments
            [{:id "PID",
              :fields
              [{:content []}
               {:content []}
               {:content ["20301"]}
               {:content []}
               {:content ["Durden" "Tyler" "" "" "Mr."]}
               {:content []}
               {:content ["19700312"]}
               {:content ["M"]}
               {:content []}
               {:content []}
               {:content ["88 Punchward Dr." "" "Los Angeles" "CA" "11221" "USA"]}]}]}
           (sut/add-segment
            (sut/create-empty-message)
            (sut/create-segment "PID"
                                   (sut/create-field)
                                   (sut/create-field)
                                   (sut/create-field "20301")
                                   (sut/create-field)
                                   (sut/create-field ["Durden" "Tyler" nil nil "Mr."])
                                   (sut/create-field)
                                   (sut/create-field "19700312")
                                   (sut/create-field "M")
                                   (sut/create-field)
                                   (sut/create-field)
                                   (sut/create-field ["88 Punchward Dr." nil "Los Angeles" "CA" "11221" "USA"])))))))

(deftest add-field-to-segment
  (testing "Adds a field to a segment"
    (is (= {:id "PID",
            :fields
            [{:content []}
             {:content []}
             {:content ["20301"]}
             {:content []}
             {:content ["Durden" "Tyler" "" "" "Mr."]}]}
           (sut/add-field
            (sut/create-segment "PID"
                                   (sut/create-field)
                                   (sut/create-field)
                                   (sut/create-field "20301")
                                   (sut/create-field))
            (sut/create-field ["Durden" "Tyler" nil nil "Mr."]))))))

(deftest add-fields-to-segment
  (testing "Adds a field to a segment"
    (is (= {:id "PID",
            :fields
            [{:content []}
             {:content []}
             {:content ["20301"]}
             {:content []}
             {:content ["Durden" "Tyler" "" "" "Mr."]}]}
           (sut/add-fields
            (sut/create-segment "PID"
                                   (sut/create-field)
                                   (sut/create-field)
                                   (sut/create-field "20301"))
            [(sut/create-field)
             (sut/create-field ["Durden" "Tyler" nil nil "Mr."])])))))

(defn- local-date
  "Returns a date for the provided local time, the month is 1 to 12."
  [year month day hour minute second]
  #?(:clj (java.util.Date. (- year 1900) (dec month) day hour minute second)
     :cljs (js/Date. year (dec month) day hour minute second)))

(deftest format-timestamp-test
  (testing "Formats a date as an HL7 timestamp in local time"
    (is (= "20240105070809" (sut/format-timestamp (local-date 2024 1 5 7 8 9)))))
  (testing "Formats the current time when no date is provided"
    (is (re-matches #"\d{14}" (sut/format-timestamp)))))

(deftest pr-field-with-dates-test
  (testing "Dates in a field are emitted as HL7 timestamps"
    (is (= "20240105070809^20241231235958&x"
           (sut/pr-field sut/DEFAULT-DELIMITERS
                            (sut/create-field [(local-date 2024 1 5 7 8 9)
                                                  [(local-date 2024 12 31 23 59 58) "x"]]))))))

#?(:clj
   (deftest format-timestamp-concurrent-test
     (testing "Formats dates correctly when called from several threads"
       (let [dates (mapv #(java.util.Date. (+ 1000000000000 (* % 86400123))) (range 1000))
             formatter (java.time.format.DateTimeFormatter/ofPattern "yyyyMMddHHmmss")
             expected (mapv #(.format formatter (java.time.LocalDateTime/ofInstant
                                                 (.toInstant ^java.util.Date %)
                                                 (java.time.ZoneId/systemDefault)))
                            dates)
             results (->> (range 8)
                          (mapv (fn [_] (future (mapv sut/format-timestamp dates))))
                          (mapv deref))]
         (is (every? #(= expected %) results))))))

(deftest pr-segment-test
  (testing "Emits header segments with the delimiters as the first field"
    (is (= "MSH|^~\\&|A|B^C"
           (sut/pr-segment sut/DEFAULT-DELIMITERS
                              (sut/create-segment "MSH" (sut/create-field "^~\\&")
                                                     (sut/create-field "A")
                                                     (sut/create-field ["B" "C"])))))
    (is (= "MSH|^~\\&"
           (sut/pr-segment sut/DEFAULT-DELIMITERS
                              (sut/create-segment "MSH" (sut/create-field "^~\\&"))))))
  (testing "Emits FHS and BHS segments"
    (let [text "FHS|^~\\&|APP|FAC\rBHS|^~\\&|APP|FAC\rMSH|^~\\&|A|B\rPID|1\r"]
      (is (= text (sut/str-message (sut/parse text)))))))

(deftest parse-message-without-trailing-segment-delimiter-test
  (testing "Parses a message with only an MSH segment and no trailing segment delimiter"
    (is (= {:delimiters
            {:field 124, :component 94, :repeating 126, :escape 92, :subcomponent 38}
            :segments
            [{:id "MSH" :fields [{:content ["^~\\&"]} {:content ["A"]} {:content ["B" "C"]}]}]}
           (sut/parse "MSH|^~\\&|A|B^C"))))
  (testing "Parses a message that ends with a segment id and no trailing segment delimiter"
    (is (= [{:id "ZZZ" :fields []} {:id "ZZY" :fields []}]
           (rest (:segments (sut/parse "MSH|^~\\&|A\rZZZ\rZZY")))))))

(deftest parse-message-end-of-segment-in-delimiters-test
  (testing "Throws when the segment ends while reading the delimiters"
    (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                          #"End of segment reached while reading delimiters"
                          (sut/parse "MSH|^~\r")))))

(deftest parse-subcomponents-without-trailing-segment-delimiter-test
  (let [header "MSH|^~\\&|A|B\r"
        fields (fn [text] (-> (sut/parse text) :segments second :fields))]
    (testing "Parses a last field that ends with subcomponents"
      (is (= [{:content ["1"]} {:content []} {:content ["123" "" "" ["HOSP" "1.2.3" "ISO"]]}]
             (fields (str header "PID|1||123^^^HOSP&1.2.3&ISO")))))
    (testing "Parses a last component that ends with a subcomponent"
      (is (= [{:content ["1"]} {:content []} {:content ["A" ["B" "C"]]}]
             (fields (str header "PID|1||A^B&C")))))
    (testing "Parses a last field that ends with a repeat"
      (is (= [{:content ["1"]} {:content []} {:content [{:content ["A"]} {:content ["B"]}]}]
             (fields (str header "PID|1||A~B")))))
    (testing "Parses a last field that ends with a subcomponent delimiter"
      (is (= [{:content ["1"]} {:content []} {:content [["X" ""]]}]
             (fields (str header "PID|1||X&")))))
    (testing "Parses the same message with and without a trailing segment delimiter"
      (doseq [segment ["PID|1||123^^^HOSP&1.2.3&ISO" "PID|1||A^B&C" "PID|1||A~B" "PID|1||X&"]]
        (is (= (fields (str header segment "\r"))
               (fields (str header segment))))))))

(deftest parse-message-line-endings-test
  (let [header "MSH|^~\\&|A|B|C|D|20240312083015||ADT^A01|ID1|P|2.5.1"
        pid "PID|1||X"]
    (testing "Parses segments separated by a carriage return"
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse (str header "\r" pid "\r"))))))
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse (str header "\r" pid)))))))
    (testing "Throws for segments separated by a line feed"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Line feed found in the MSH segment"
                            (sut/parse (str header "\n" pid "\n")))))
    (testing "Throws for segments separated by a carriage return and a line feed"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Illegal segment id"
                            (sut/parse (str header "\r\n" pid "\r\n")))))
    (testing "Throws for a message that ends with a carriage return and a line feed"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Illegal segment id"
                            (sut/parse (str header "\r" pid "\r\n")))))
    (testing "Keeps a line feed in the header segment when it ends with a carriage return"
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse "MSH|^~\\&|A\nB|C\rPID|1\r")))))
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse "MSH|^~\\&|A|B^C\nD\rPID|1\r")))))
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse "MSH|^~\\&|A|B~C\nD\rPID|1\r")))))
      (is (= ["MSH" "PID"] (mapv :id (:segments (sut/parse "MSH|^~\\&|A|B&C\nD\rPID|1\r")))))
      (is (= {:content ["A\nB"]}
             (-> (sut/parse "MSH|^~\\&|A\nB|C\rPID|1\r") :segments first :fields second))))
    (testing "Parses segments separated by a line feed and a carriage return"
      (is (= ["MSH" "PID"]
             (mapv :id (:segments (sut/parse (str header "\n\rPID|1||X\n\r")))))))
    (testing "Throws for a header segment that is the whole message"
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Line feed found in the MSH segment"
                            (sut/parse (str header "\nPID|1||X\nOBX|1|TX|||Y\n"))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Line feed found in the MSH segment"
                            (sut/parse (str header "\n"))))
      (is (thrown-with-msg? #?(:clj Exception :cljs js/Error)
                            #"Line feed found in the FHS segment"
                            (sut/parse "FHS|^~\\&|A\nBHS|^~\\&|B\nMSH|^~\\&|C\nPID|1\n"))))
    (testing "Keeps a line feed that is part of the data in a later segment"
      (is (= ["MSH" "OBX"]
             (mapv :id (:segments (sut/parse (str header "\rOBX|1|TX|||one\ntwo\r"))))))
      (is (= {:content ["one\ntwo"]}
             (-> (sut/parse (str header "\rOBX|1|TX|||one\ntwo\r"))
                 :segments second :fields (nth 4)))))))

#?(:clj
   (deftest parse-reader-test
     (testing "Parses the same message from a reader and from a buffered reader"
       (let [text "MSH|^~\\&|A|B\rPID|1||X\r"
             parsed (sut/parse text)]
         (is (= parsed (sut/parse (java.io.StringReader. text))))
         (is (= parsed (sut/parse (java.io.BufferedReader. (java.io.StringReader. text)))))))))

(deftest segment-id-whitespace-test
  (let [segment-id (fn [segment]
                     (-> (sut/parse (str "MSH|^~\\&|A|B\r" segment))
                         :segments second :id))]
    (testing "Strips the whitespace that Character/isWhitespace strips"
      (is (= "PID" (segment-id "  PID |1")))
      (is (= "PID" (segment-id "\tPID\t|1")))
      (is (= "PID" (segment-id "PID|1")))
      (is (= "PID" (segment-id " PID |1"))))
    (testing "Keeps the characters that Character/isWhitespace keeps"
      (is (= "PID " (segment-id "PID |1")))
      (is (= "PID" (segment-id "PID|1")))
      (is (= "PID﻿" (segment-id "PID﻿|1"))))))
