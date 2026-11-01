(ns com.nervestaple.clinical.message-parser.interface
  (:require
   [com.nervestaple.clinical.message-parser.parser :as parser]))

(def ASCII_VT parser/ASCII_VT)
(def ASCII_FS parser/ASCII_FS)
(def ASCII_CR parser/ASCII_CR)
(def ASCII_LF parser/ASCII_LF)

(def ASCII_PIPE parser/ASCII_PIPE)
(def ASCII_CARAT parser/ASCII_CARAT)
(def ASCII_AMPERSAND parser/ASCII_AMPERSAND)
(def ASCII_TILDE parser/ASCII_TILDE)
(def ASCII_BACKSLASH parser/ASCII_BACKSLASH)

(def DEFAULT-DELIMITERS parser/DEFAULT-DELIMITERS)

(defn format-timestamp
  "Returns an HL7 compatible timestamp (yyyyMMddHHmmss) in local time for the
  provided date (a java.util.Date or a JavaScript Date), or for the current time
  if no date is provided."
  ([]
   (parser/format-timestamp))
  ([date]
   (parser/format-timestamp date)))

(defn pr-delimiters
  "Returns an HL7 compatible text representation of the provided
  delimiters."
  [delimiters]
  (parser/pr-delimiters delimiters))

(defn pr-field
  "Returns an HL7 compatible String representation of the provided
  field."
  [delimiters field]
  (parser/pr-field delimiters field))

(defn pr-segment
  "Returns an HL7 compatible String representation of the provided
  segment."
  [delimiters segment]
  (parser/pr-segment delimiters segment))

(defn str-message
  "Returns the provided HL7 message as a string."
  [message]
  (parser/str-message message))

(defn pr-message
  "Prints the provided HL7 message to the current *out* stream."
  [message]
  (parser/pr-message))

(defn convert-values
  "If provided a list of values, any in items are replaced with an empty string.
  When provided a single value a list with that value will be returned or an
  empty list of that value is nil."
  [values]
  (parser/convert-values values))

(defn create-empty-message
  "Returns a new, empty message map. if no map of delimiters is provided then the
  default set will be used."
  ([]
   (parser/create-empty-message DEFAULT-DELIMITERS))
  ([delimiters]
   (parser/create-empty-message delimiters)))

(defn create-message
  "Accepts a map of delimiters and segments. Returns a new parsed message using
  the provided delimiters populated with the provided segments."
  [delimiters & segments]
  (apply parser/create-message delimiters segments))

(defn create-segment
  "Returns a new, empty segment map with the provided id."
  [id & fields]
  (apply parser/create-segment id fields))

(defn create-field
  "Returns a new field map populated with the provided data."
  ([]
   (parser/create-field nil))
  ([data]
   (parser/create-field data)))

(defn add-segment
  "Adds the provided segment map to the provided message map and returns a new
  message."
  [message segment]
  (parser/add-segment message segment))

(defn add-field
  "Adds the provided field map to the provided segment map and returns a new
  segment."
  [segment field]
  (parser/add-field segment field))

(defn add-fields
  "Adds the provided field maps to the provided segment map and returns a new
  segment."
  [segment fields]
  (parser/add-fields segment fields))

(def get-reader
  "Returns a pushback reader for the provided Object. We want to wrap
  another Reader but we'll cast to a String and read that if
  required. On ClojureScript the Object is always read as a String."
  parser/get-reader)

(defn parse
  "Reads data from the provided source (a Reader, String, etc.) and parses that
  data into a map that represents the content of the message."
  [message-source]
  (parser/parse message-source))
