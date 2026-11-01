(ns com.nervestaple.clinical.message-parser.interface
  (:require
   [com.nervestaple.clinical.message-parser.parser :as parser]
   [com.nervestaple.clinical.message-parser.message :as message]))

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

(defn message-id-unparsed
  "Returns the message id for an HL7 message by doing some simple
  regular expression matching on the message. This function does *not*
  involve parsing the message and may be faster."
  [message]
  (message/message-id-unparsed message))

(defn segment-ids
  "Returns a list of the segment ids present in the message."
  [message]
  (message/segment-ids))

(defn get-segments
  "Returns all of the segments in the message that have the provided
  segment id."
  [message segment-id]
  (message/get-segments message segment-id))

(defn get-segment-field
  "Returns the field with the provided index from the given
  segment. Keep in mind that this function expects the index to adhere
  to the HL7 specification where the first field of data is located at
  index 1. Another gotcha in the MSH segment, the first field of data
  starts at index 2 and that's the list of delimiters.

  This function will return the id of the segment if you ask for index 0. For
  the MSH segment, it will return nil for index 1 instead of returning the field
  delimiter. If you want the field delimiter you can get it under the :delimiter
  key of the message. If the provided index is out of bounds then nil will be
  returned."
  ([segment index]
   (message/get-segment-field segment index false))
  ([segment index raw?]
   (message/get-segment-field segment index raw?)))

(defn get-field
  "Returns the field with the provided index from the segment with the
  given id of the provided message."
  [message segment-id field-index]
  (message/get-field message segment-id field-index))

(defn get-field-component
  "Returns the component at the provided index from the field with the
  provided index from the segment with the given id in the provided
  message."
  [message segment-id field-index component-index]
  (message/get-field-component message segment-id field-index component-index))

(defn set-field
  "Updates the message by altering the field value for the specified
  segment. When specifying field indexes, be sure to use the correct
  HL7 index (the segment id would be 0, the first field is at index
  1).

  Your value should be an atom or an collection, a collection
  indicates a field with components. Subcomponents are represented as
  a collection containing a collection. Pass in a collection of fields
  to indicate repeating fields."
  [message segment-id field-index value]
  (message/set-field message segment-id field-index value))

(defn extract-text-from-segments
  "Extracts the text from the parsed message for the supplied index of
  the given segments, the text will be concatenated and returned as
  one String. For instance, this function would extract all of the text
  from the fifth index of all of the OBX segments:

      (extract-text-from-segments parsed-message 'OBX' 5)

  You may pass in an optional argument that contains a character to
  interleave between the chunks of extracted text (for instance,
  '\n')."
  [parsed-message segment-type index & options]
  (apply message/extract-text-from-segments parsed-message segment-type index options))

(defn get-field-first
  "Returns the first instance of the field with the provided index
  from the segment with the given id of the provided message. This
  function is handy when you know there's only one instance of a
  particular segment (like 'MSH'), you won't have to grab the first
  element; it will be returned by this function."
  [parsed-message segment-id field-index]
  (message/get-field-first parsed-message segment-id field-index))

(defn get-field-first-value
  "Returns the value of the first instance of the field with the
  provided index from the segment with the given id of the provided
  message. This function is handy when you know there's only one
  instance of a particular segment (like 'MSH'), you won't have to
  grab the first element and then it's :content value; it will be
  returned by this function."
  [parsed-message segment-id field-index]
  (message/get-field-first-value parsed-message segment-id field-index))

(defn ack-message
  "Returns a parsed message that contains an acknowledgement message
  for the provided parsed message, the acknowledgement message will
  use the same delimiters. If the message indicates that no
  acknowledgement should be returned, this function will return nil.

  The 'option' should be a hash-map with the following keys:

      :sending-app, :sending-facility, :production-mode, :version,
      :text-message

  Optionally, a `:message-id` key may be provided if you need a specific
  value.

  These values will be used to fill out the ACK message. The
  'ack-status' field should be a valid HL7 version 2.x acknowledgment
  status:

      AA (accepted), AE (error), AR (rejected)"
  [options ack-status parsed-message]
  (message/ack-message options ack-status parsed-message))

(defn ack-message-fallback
  "Returns a parsed message that contains an acknowledgement message for the
  provided un-parsed message, the acknowledgement message will use default
  delimiters. Use this function when you have an HL7v2 message that you need to
  acknowledge but you cannot parse.

  The 'option' should be a hash-map with the following keys:

      :sending-app, :sending-facility, :production-mode, :version,
      :text-message

  Optionally, a `:message-id` key may be provided if you need a specific
  value.

  These values will be used to fill out the ACK message. The
  'ack-status' field should be a valid HL7 version 2.x acknowledgment
  status:

      AA (accepted), AE (error), AR (rejected)"
  [options ack-status message]
  (message/ack-message-fallback options ack-status message))
