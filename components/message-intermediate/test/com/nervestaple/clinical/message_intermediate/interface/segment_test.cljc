(ns com.nervestaple.clinical.message-intermediate.interface.segment-test
  (:require
   #?(:clj  [clojure.test :refer [deftest testing is]]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   #?(:clj  [java-time.api :as time])
   #?(:cljs [tick.core :as t])
   #?(:clj  [com.nervestaple.clinical.message-intermediate.segment.java-date-time :as date-time])
   #?(:cljs [com.nervestaple.clinical.message-intermediate.segment.cljs-date-time :as date-time])
   [com.nervestaple.clinical.message-intermediate.interface.segment :as segment]))

(def fixed-local-date-time
  (date-time/parse-timestamp "20260927143005"))

;; time->ts

(deftest test-time->ts
  (testing "wraps a date-time value in a TS record with no precision"
    (let [ts-record (segment/time->ts fixed-local-date-time)]
      (is (= fixed-local-date-time (:time ts-record)))
      (is (nil? (:precision ts-record))))))

;; now->ts

(deftest test-now->ts
  (testing "returns a TS record with the current date and time"
    (let [ts-record (segment/now->ts)]
      (is #?(:clj  (time/zoned-date-time? (:time ts-record))
             :cljs (t/zoned-date-time? (:time ts-record))))
      (is (nil? (:precision ts-record))))))

;; ts->field

(deftest test-ts->field-no-precision
  (testing "a single TS record with no precision becomes a bare [time nil] field"
    (is (= ["20260927143005" nil]
           (segment/ts->field (segment/time->ts fixed-local-date-time))))))

(deftest test-ts->field-with-precision
  (testing "a single TS record with a precision becomes a bare [time precision] field"
    (is (= ["20260927143005" "S"]
           (segment/ts->field (assoc (segment/time->ts fixed-local-date-time)
                                      :precision "S"))))))

(deftest test-ts->field-vector
  (testing "a vector of TS records becomes a vector of wrapped fields"
    (is (= [{:content ["20260927143005" ""]}]
           (segment/ts->field [(segment/time->ts fixed-local-date-time)])))))

(deftest test-ts->field-nil-or-empty
  (testing "a nil-or-empty record produces an empty vector rather than nil"
    (is (= [] (segment/ts->field {})))))

;; field->ts

(deftest test-field->ts-bare-string
  (testing "the common case: a bare timestamp string with no precision component"
    (let [ts-record (segment/field->ts "20260927143005")]
      (is (= fixed-local-date-time (:time ts-record)))
      (is (nil? (:precision ts-record))))))

(deftest test-field->ts-vector
  (testing "a [time precision] field is parsed into a full TS record"
    (let [ts-record (segment/field->ts ["20260927143005" "S"])]
      (is (= fixed-local-date-time (:time ts-record)))
      (is (= "S" (:precision ts-record))))))

(deftest test-field->ts-nil
  (testing "a nil field returns nil rather than throwing"
    (is (nil? (segment/field->ts nil)))))

(deftest test-field->ts-repeating
  (testing "a repeating field (a vector of wrapped fields) returns a vector of TS records"
    (let [[first-ts second-ts]
          (segment/field->ts [{:content ["20260927143005" "S"]}
                               {:content ["20261004194602" ""]}])]
      (is (= fixed-local-date-time (:time first-ts)))
      (is (= "S" (:precision first-ts)))
      (is (nil? (:precision second-ts))))))

(deftest test-field->ts-round-trip
  (testing "ts->field followed by field->ts recovers the original value"
    (let [original (assoc (segment/time->ts fixed-local-date-time) :precision "S")
          round-tripped (segment/field->ts (segment/ts->field original))]
      (is (= (:time original) (:time round-tripped)))
      (is (= (:precision original) (:precision round-tripped))))))
