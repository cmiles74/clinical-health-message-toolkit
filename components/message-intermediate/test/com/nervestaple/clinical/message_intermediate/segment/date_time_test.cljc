(ns com.nervestaple.clinical.message-intermediate.segment.date-time-test
  (:require
   #?(:clj  [clojure.test :refer [deftest testing is]]
      :cljs [cljs.test :refer-macros [deftest testing is]])
   #?(:clj  [java-time.api :as time])
   #?(:cljs [tick.core :as t])
   #?(:clj  [com.nervestaple.clinical.message-intermediate.segment.java-date-time :as date-time])
   #?(:cljs [com.nervestaple.clinical.message-intermediate.segment.cljs-date-time :as date-time])))

;; A fixed instant, since `parse-timestamp` never produces one directly (it's
;; only reachable via `format-time`'s type dispatch) and "now" isn't a stable
;; thing to assert against.
(def fixed-instant
  #?(:clj  (time/instant "2026-09-27T14:30:05Z")
     :cljs (t/instant "2026-09-27T14:30:05Z")))

;; format-time

(deftest test-format-time-instant
  (testing "an instant is formatted as a UTC HL7 timestamp"
    (is (= "20260927143005+0000" (date-time/format-time fixed-instant)))))

(deftest test-format-time-zoned-date-time
  (testing "a zoned date-time is formatted with its own offset"
    (is (= "20260927143005+0000"
           (date-time/format-time (date-time/parse-timestamp "20260927143005+0000"))))))

(deftest test-format-time-local-date-time
  (testing "a local date-time is formatted without an offset"
    (is (= "20260927143005"
           (date-time/format-time (date-time/parse-timestamp "20260927143005"))))))

(deftest test-format-time-local-date
  (testing "a local date is formatted as yyyyMMdd"
    (is (= "20260927"
           (date-time/format-time (date-time/parse-timestamp "20260927"))))))

(deftest test-format-time-year-month
  (testing "a year-month is formatted as yyyyMM"
    (is (= "202609"
           (date-time/format-time (date-time/parse-timestamp "202609"))))))

(deftest test-format-time-fallback
  (testing "anything else just gets stringified"
    (is (= "something-else" (date-time/format-time "something-else")))))

(deftest test-format-time-nil
  (testing "nil in, nil out"
    (is (nil? (date-time/format-time nil)))))

;; parse-timestamp

(deftest test-parse-timestamp-zoned
  (testing "a timestamp with a sign is parsed as a zoned date-time"
    (is (= "2026-09-27T14:30:05-04:00"
           (str (date-time/parse-timestamp "20260927143005-0400"))))))

(deftest test-parse-timestamp-local-date-time-with-fraction
  (testing "a 16+ digit timestamp is parsed with its 2-digit fractional second"
    (is (= "2026-09-27T14:30:05.120"
           (str (date-time/parse-timestamp "2026092714300512"))))))

(deftest test-parse-timestamp-local-date-time
  (testing "a 14 digit timestamp is parsed as a local date-time"
    (is (= "2026-09-27T14:30:05"
           (str (date-time/parse-timestamp "20260927143005"))))))

(deftest test-parse-timestamp-local-date-time-no-seconds
  (testing "a 12 digit timestamp is parsed as a local date-time with no seconds"
    (is (= "2026-09-27T14:30"
           (str (date-time/parse-timestamp "202609271430"))))))

(deftest test-parse-timestamp-local-date
  (testing "an 8 digit timestamp is parsed as a local date"
    (is (= "2026-09-27"
           (str (date-time/parse-timestamp "20260927"))))))

(deftest test-parse-timestamp-local-date-hyphenated-fallback
  (testing "a hyphenated date falls back to the alternate date format"
    (is (= "2026-09-27"
           (str (date-time/parse-timestamp "2026-09-27"))))))

(deftest test-parse-timestamp-year-month
  (testing "a 6 digit timestamp is parsed as a year-month"
    (is (= "2026-09"
           (str (date-time/parse-timestamp "202609"))))))

(deftest test-parse-timestamp-year
  (testing "a 4 digit timestamp is parsed as a year"
    (is (= "2026"
           (str (date-time/parse-timestamp "2026"))))))

(deftest test-parse-timestamp-unparseable
  (testing "an unparseable timestamp is returned unaltered rather than throwing"
    (is (= "12349876" (date-time/parse-timestamp "12349876")))))

(deftest test-parse-timestamp-too-short
  (testing "a timestamp shorter than a year is returned unaltered"
    (is (= "12" (date-time/parse-timestamp "12")))))

(deftest test-parse-timestamp-blank
  (testing "a blank timestamp returns nil"
    (is (nil? (date-time/parse-timestamp "   ")))))

(deftest test-parse-timestamp-nil
  (testing "a nil timestamp returns nil"
    (is (nil? (date-time/parse-timestamp nil)))))

(deftest test-parse-timestamp-map
  (testing "a parsed HL7 field map is unwrapped and then parsed"
    (is (= "2026-09-27"
           (str (date-time/parse-timestamp {:content "20260927"}))))))

(deftest test-parse-timestamp-vector
  (testing "a vector of timestamps is parsed element-wise"
    (is (= ["2026-09-27" "2026-09"]
           (mapv str (date-time/parse-timestamp ["20260927" "202609"]))))))

;; parse-date

(deftest test-parse-date
  (testing "a hyphenated date string is parsed into a local date"
    (is (= "2026-09-27" (str (date-time/parse-date "2026-09-27"))))))

;; zoned-date-time

(deftest test-zoned-date-time
  (testing "returns the current date and time as a real zoned date-time"
    (is #?(:clj  (time/zoned-date-time? (date-time/zoned-date-time))
           :cljs (t/zoned-date-time? (date-time/zoned-date-time))))))
