(ns com.nervestaple.clinical.log.interface-test
  (:require [clojure.string :as string]
            #?(:clj  [clojure.test :refer [deftest testing is]]
               :cljs [cljs.test :refer-macros [deftest testing is]])
            [com.nervestaple.clinical.log.interface :as log]))

#?(:clj
   (defmacro with-err-str
     "Evaluates expressions in a context in which *err* is bound to a fresh
     StringWriter. Returns the string created by any nested printing calls."
     [& body]
     `(let [writer# (new java.io.StringWriter)]
        (binding [*err* writer#]
          ~@body
          (str writer#)))))

(deftest test-with-level
  (testing "Ensure the minimum log level changes"
    (log/with-level :trace
      (let [output (with-out-str (log/trace "Boom!"))]
        (is (and (string/index-of output "TRACE")
                 (string/index-of output "Boom!")))))))

(deftest test-set-level
  (testing "Ensure the minimum log level changes"
    (log/set-min-level :trace)
    (let [output (with-out-str (log/trace "Boom!"))]
      (is (and (string/index-of output "TRACE")
               (string/index-of output "Boom!"))))
    (log/set-min-level :debug)))

(deftest test-trace
  (testing "Ensure trace messages are emitted"
    (log/with-level :trace
      (let [output (with-out-str (log/trace "Boom!"))]
        (is (and (string/index-of output "TRACE")
                 (string/index-of output "Boom!")))))))

(deftest test-debug
  (testing "Ensure debug messages are emitted"
    (let [output (with-out-str (log/debug "Boom!"))]
      (is (and (string/index-of output "DEBUG")
               (string/index-of output "Boom!"))))))

(deftest test-info
  (testing "Ensure info messages are emitted"
    (let [output (with-out-str (log/info "Boom!"))]
      (is (and (string/index-of output "INFO")
               (string/index-of output "Boom!"))))))

(deftest test-warn
  (testing "Ensure warning messages are emitted"
    (let [output (with-out-str (log/warn "Boom!"))]
      (is (and (string/index-of output "WARN")
               (string/index-of output "Boom!"))))))

(deftest test-error
  (testing "Ensure error messages are emitted"
    (let [output #?(:clj (with-err-str (log/error "Boom!"))
                     :cljs (with-out-str (log/error "Boom!")))]
      (is (and (string/index-of output "ERROR")
               (string/index-of output "Boom!"))))))

(deftest test-fatal
  (testing "Ensure fatal messages are emitted"
    (let [output #?(:clj (with-err-str (log/fatal "Boom!"))
                     :cljs (with-out-str (log/fatal "Boom!")))]
      (is (and (string/index-of output "FATAL")
               (string/index-of output "Boom!"))))))

(deftest test-report
  (testing "Ensure report messages are emitted"
    (let [output (with-out-str (log/report "Boom!"))]
      (is (and (string/index-of output "REPORT")
               (string/index-of output "Boom!"))))))
