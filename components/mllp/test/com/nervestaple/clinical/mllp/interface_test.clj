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

(deftest read-message-with-empty-body
  (testing "Read an empty MLLP message followed by a normal one")
  (let [messages (str "\u000B" "\u001C" "\r" "\u000B" "second" "\u001C" "\r")
        received (atom [])]
    (mllp/read-stream (ByteArrayInputStream. (.getBytes messages))
                      #(swap! received conj %))
    (is (and (= 2 (count @received))
             (= "" (first @received))
             (= "second" (second @received))))))

(deftest read-throws-on-eof-mid-message
  (testing "Reading a stream that closes before the end block throws")
  (let [messages (str "\u000B" "Hello")]
    (is (thrown-with-msg?
         Exception #"Expected end block but found end of stream"
         (mllp/read-stream (ByteArrayInputStream. (.getBytes messages)))))))

(deftest write-then-read-preserves-non-ascii-characters
  (testing "Writing and reading back a message preserves Latin-1 characters")
  (let [original "José Müller"
        sent (ByteArrayOutputStream.)]
    (mllp/write-stream sent original)
    (let [received (atom nil)]
      (mllp/read-stream (ByteArrayInputStream. (.toByteArray sent))
                        #(reset! received %))
      (is (= original @received)))))
