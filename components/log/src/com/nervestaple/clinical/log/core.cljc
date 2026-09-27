(ns com.nervestaple.clinical.log.core
  (:require
   [taoensso.timbre :as timbre]
   #?(:clj [com.nervestaple.clinical.log.appenders :as appenders])))

(def LOG-LEVELS [:trace :debug :info :warn :error :fatal :report])

(defn set-ns-log-level
  ([namespace log-level]
   (set-ns-log-level [[namespace log-level]]))
  ([namespace-to-log-levels]
   (timbre/merge-config!
    (assoc timbre/*config*
           :min-level
           (if (keyword? (:min-level timbre/*config*))
             (into namespace-to-log-levels
                   [["*" (:min-level timbre/*config*)]])
             (vec (distinct (into namespace-to-log-levels
                                  (:min-level timbre/*config*)))))))))

(defn set-min-level
  [level]
  (timbre/set-min-level! level))

#?(:clj
   (defn add-file [file-name]
     (appenders/add-file file-name)))
