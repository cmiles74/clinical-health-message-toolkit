(ns com.nervestaple.clinical.log.appenders
  (:require
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]))

(defn add-file
  [file-name]
  (timbre/debug (str "Logging to \"" file-name "\""))
  (timbre/merge-config!
   {:appenders (merge (timbre/*config* :appenders)
                      {:spit (appenders/spit-appender {:fname file-name})})}))
