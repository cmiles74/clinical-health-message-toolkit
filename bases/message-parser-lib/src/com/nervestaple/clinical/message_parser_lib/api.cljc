(ns com.nervestaple.clinical.message-parser-lib.api
  (:require
   [com.nervestaple.clinical.log.interface :as log]
   [com.nervestaple.clinical.message-parser.interface :as parser])
  (:gen-class))

(defn -main
  "Bootstrapping function for the application"
  [& args]
  (log/info "Welcome to the Nervestaple Clinical Message Parser!")
  (let [message (parser/test-message)
        parsed (parser/parse message)]
    (println message)
    (println)
    (println parsed)))
