(ns com.nervestaple.clinical.log.interface
  (:require
   [com.nervestaple.clinical.log.core :as core]
   #?(:clj [com.nervestaple.clinical.log.macros :as macros]))
  #?(:cljs (:require-macros [com.nervestaple.clinical.log.macros :as macros]
                            [com.nervestaple.clinical.log.interface])))

;; sequence of valid log levels
(def LOG-LEVELS core/LOG-LEVELS)

#?(:clj
   (defn add-file
     "Adds a new \"spit\" appender to the current log configuration, all log
  messages will be written to this file. Returns the current logging
  configuration.

  You probably want to wrap calls to this function in a `(defonce ...)`."
     [file-name]
     (core/add-file file-name)))

(defn set-ns-log-level
  "Accepts either a namespace and a key with a log level or a sequence where each
  item is a sequence with a namespace and a key with a log level. This data is
  used to set the log level for the provided namespaces."
  ([namespace-to-log-levels]
   (core/set-ns-log-level namespace-to-log-levels))
  ([namespace log-level]
   (core/set-ns-log-level namespace log-level)))

(defn set-min-level
  "Sets the minimum logging level and returns the current logging configuration."
  [level]
  (core/set-min-level level))

#?(:clj
   (defmacro with-level
     [level & args]
     `(macros/with-level ~level ~@args)))

#?(:clj
   (defmacro trace
     "Logs a trace message to the log stream"
     [& args]
     `(macros/trace ~@args)))

#?(:clj
   (defmacro debug
     "Logs a debug message to the log stream."
     [& args]
     `(macros/debug ~@args)))

#?(:clj
   (defmacro info
     "Logs an informative message to the log stream."
     [& args]
     `(macros/info ~@args)))

#?(:clj
   (defmacro warn
     "Logs a warning to the log stream."
     [& args]
     `(macros/warn ~@args)))

#?(:clj
   (defmacro error
     "Logs an error to the log stream."
     [& args]
     `(macros/error ~@args)))

#?(:clj
   (defmacro fatal
     "Logs a fatal message to the log stream."
     [& args]
     `(macros/fatal ~@args)))

#?(:clj
   (defmacro report
     "Logs a report message to the log stream."
     [& args]
     `(macros/report ~@args)))
