(ns com.nervestaple.clinical.message-intermediate-lib.api
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
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
