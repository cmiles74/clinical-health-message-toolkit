(ns com.nervestaple.clinical.message-intermediate-lib.api
  (:require
   #?(:clj [clojure.spec.alpha :as s])
   #?(:clj [clojure.spec.gen.alpha :as gen])
   #?(:cljs [cljs.spec.alpha :as s])
   #?(:cljs [cljs.spec.gen.alpha :as gen])
   [com.nervestaple.clinical.log.interface :as log]
   [com.nervestaple.clinical.message-intermediate.interface.core :as message])
  (:gen-class))

(defn -main
  "Bootstrapping function for the application"
  [& args]
  (log/info "Welcome to the Nervestaple Clinical Message Interchange Format!")
  (let [record (gen/generate (s/gen ::message/message))
        message (message/record->hl7 record)]
    (println)
    (println message)))
