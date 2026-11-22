(ns com.nervestaple.clinical.mllp.interface-test
  (:require
   [clojure.test :refer [deftest is testing]]
   [com.nervestaple.clinical.mllp.core :as mllp])
  (:import
   [java.io ByteArrayInputStream ByteArrayOutputStream]))

(deftest read-one-message
  (testing "Read one MLLP wrapped message")
  (let [messages "B\r"
        received (atom [])]
    (mllp/read-stream (ByteArrayInputStream. (.getBytes messages))
                      #(swap! received conj %))
    (is (and (= 1 (count @received))
             (= "B" (first @received))))))

(deftest read-two-messages
  (testing "Read two MLLP wrapped messages")
  (let [messages "Blerg!\rBlerg Two!\r"
        received (atom [])]
    (mllp/read-stream (ByteArrayInputStream. (.getBytes messages))
                      #(swap! received conj %))
    (is (and (= 2 (count @received))
             (= "Blerg!" (first @received))
             (= "Blerg Two!" (second @received))))))

(deftest write-one-message
  (testing "Write one MLLP wrapped message")
  (let [sent (ByteArrayOutputStream.)]
    (mllp/write-stream sent "Blerg!")
    (is (= "Blerg!\r" (String. (.toByteArray sent))))))

(deftest write-two-messages
  (testing "Write two MLLP wrapped messages")
  (let [sent (ByteArrayOutputStream.)]
    (mllp/write-stream sent "Blerg!")
    (mllp/write-stream sent "Blerg Two!")
    (is (= "Blerg!\rBlerg Two!\r"
           (String. (.toByteArray sent))))))

(deftest read-one-blocking-message
  (testing "Read one MLLP wrapped message, blocking style")
  (let [messages "Blerg!\r"
        received (atom [])]
    (let [message (mllp/read-stream
                   (ByteArrayInputStream. (.getBytes messages)))]
      (swap! received conj message))
    (is (and (= 1 (count @received))
             (= "Blerg!" (first @received))))))
