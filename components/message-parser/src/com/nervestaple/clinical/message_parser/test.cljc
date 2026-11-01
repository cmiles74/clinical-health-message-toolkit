;;
;; Functions to support testing of the HL7 version 2.x parser.
;;

(ns com.nervestaple.clinical.message-parser.test
  (:gen-class)
  (:use
   [com.nervestaple.clinical.message-parser.parser])
  #?(:clj
     (:import
      (java.util Date))))

(defn get-time-now
  []
  #?(:clj (.getTime (new Date)))
  #?(:cljs (.getTime (js/Date))))

(defn test-message
  []
  (str "MSH|^~\\&|AcmeHIS|StJohn|CATH|StJohn|20061019172719||ORM^O01|"
       (get-time-now) "|P|2.3" (char ASCII_CR)
       "PID|||20301||Durden^Tyler^^^Mr.||19700312|M|||88 Punchward Dr.^^Los Angeles^CA^11221^USA|||||||" (char ASCII_CR)
       "PV1||O|OP^^||||4652^Paulson^Robert|||OP|||||||||9|||||||||||||||||||||||||20061019172717|20061019172718" (char ASCII_CR)
       "ORC|NW|20061019172719" (char ASCII_CR)
       "OBR|1|20061019172719||76770^Ultrasound: retroperitoneal^C4|||12349876" (char ASCII_CR)))
