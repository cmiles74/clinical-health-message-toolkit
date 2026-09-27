(ns com.nervestaple.clinical.log.macros
  (:require
   [taoensso.timbre :as timbre]))

(defmacro with-level
  [level & args]
  `(timbre/with-min-level ~level ~@args))

(defmacro trace
  [& args]
  `(timbre/trace ~@args))

(defmacro debug
  [& args]
  `(timbre/debug ~@args))

(defmacro info
  [& args]
  `(timbre/info ~@args))

(defmacro warn
  [& args]
  `(timbre/warn ~@args))

(defmacro error
  [& args]
  `(timbre/error ~@args))

(defmacro fatal
  [& args]
  `(timbre/fatal ~@args))

(defmacro report
  [& args]
  `(timbre/report ~@args))

