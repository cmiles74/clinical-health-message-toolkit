(ns com.nervestaple.clinical.message-intermediate.segment.acc
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.common-fields :as common]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [clojure.edn :as edn]))

;; segment identifier
(def SEGMENT-ID "ACC")

;; record representing an ACC segment
(defrecord record
    [accidate-date
     accident
     accident-location
     auto-accident-state
     accident-job-related
     accident-death
     accident-entered-by
     accident-description
     accident-brought-in-by
     police-notified
     accident-address
     degree-of-patient-liability
     accident-identifier])

(s/def ::accidate-date (s/nilable ::segment/spec-ts))
(s/def ::accident (s/nilable ::segment/spec-cwe))
(s/def ::accident-location (s/nilable string?))
(s/def ::auto-accident-state (s/nilable ::segment/spec-cwe))
(s/def ::accident-job-related (s/nilable string?))
(s/def ::accident-death (s/nilable string?))
(s/def ::accident-entered-by (s/nilable ::segment/spec-xcn))
(s/def ::accident-description (s/nilable string?))
(s/def ::accident-brought-in-by (s/nilable string?))
(s/def ::police-notified (s/nilable string?))
(s/def ::accident-address (s/nilable ::segment/spec-xad))
(s/def ::degree-of-patient-liability (s/nilable number?))
(s/def ::accident-identifier (s/nilable string?))

(s/def ::spec
  (s/keys :opt-un [::accidate-date
                   ::accident
                   ::accident-location
                   ::auto-accident-state
                   ::accident-job-related
                   ::accident-death
                   ::accident-entered-by
                   ::accident-description
                   ::accident-brought-in-by
                   ::police-notified
                   ::accident-address
                   ::degree-of-patient-liability
                   ::accident-identifier]))

(defn record->hl7
  "Accepts a record of accident data and returns a map of ACC segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (util/trim-nils (segment/ts->field (:accidate-date %))))
     (parser/create-field (util/trim-nils (segment/cwe->field (:accident %))))
     (parser/create-field (:accident-location %))
     (parser/create-field (util/trim-nils (segment/cwe->field (:auto-accident-state %))))
     (parser/create-field (:accident-job-related %))
     (parser/create-field (:accident-death %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:accident-entered-by %))))
     (parser/create-field (:accident-description %))
     (parser/create-field (:accident-brought-in-by %))
     (parser/create-field (:police-notified %))
     (parser/create-field (util/trim-nils (segment/xad->field (:accident-address %))))
     (parser/create-field (:degree-of-patient-liability %))
     (parser/create-field (:accident-identifier %)))
   record))

(defn hl7->record
  "Accepts an ACC segment of parsed HL7 data and returns an ACC record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 14
   #(map->record
     {:accidate-date (segment/field->ts (message/get-segment-field % 1))
      :accident (segment/field->cwe (message/get-segment-field % 2))
      :accident-location (util/unwrap-and-first (message/get-segment-field % 3))
      :auto-accident-state (segment/field->cwe (message/get-segment-field % 4))
      :accident-job-related (util/unwrap-and-first (message/get-segment-field % 5))
      :accident-death (util/unwrap-and-first (message/get-segment-field % 6))
      :accident-entered-by (segment/field->xcn (message/get-segment-field % 7))
      :accident-description (util/unwrap-and-first (message/get-segment-field % 8))
      :accident-brought-in-by (util/unwrap-and-first (message/get-segment-field % 9))
      :police-notified (util/unwrap-and-first (message/get-segment-field % 10))
      :accident-address (segment/field->xad (message/get-segment-field % 11))
      :degree-of-patient-liability (util/unwrap-and-first (message/get-segment-field % 12))
      :accident-identifier (util/unwrap-and-first (message/get-segment-field % 13))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
    (merge {:id SEGMENT-ID}
           data-map)))
