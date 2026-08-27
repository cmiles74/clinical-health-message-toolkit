(ns com.nervestaple.clinical.message-intermediate.segment.pd1
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.common-fields :as common]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [clojure.edn :as edn]))

;; Segment identifier
(def SEGMENT-ID "PD1")

;; Record representing PD1 segment
(defrecord record
    [living-dependency
     living-arrangement
     primary-facility
     primary-care-provider
     student-indicator
     handicap
     living-will-code
     organ-donor-code
     separate-bill
     duplicate-patient
     publicity-code
     protection-indicator
     protection-indicator-effective
     place-of-worship
     advance-directive-code
     immunization-registry-status
     immunization-registry-status-effective
     publicity-code-effective
     military-branch
     military-rank
     military-status])

(s/def ::living-dependency (s/nilable string?))
(s/def ::living-arrangement (s/nilable string?))
(s/def ::primary-facility (s/nilable ::segment/spec-xon))
(s/def ::primary-care-provider (s/nilable ::segment/spec-xon))
(s/def ::student-indicator (s/nilable string?))
(s/def ::handicap (s/nilable string?))
(s/def ::living-will-code (s/nilable string?))
(s/def ::organ-donor-code (s/nilable string?))
(s/def ::separate-bill (s/nilable string?))
(s/def ::duplicate-patient (s/nilable ::segment/spec-cx))
(s/def ::publicity-code ::common/publicity-code)
(s/def ::protection-indicator (s/nilable string?))
(s/def ::protection-indicator-effective (s/nilable ::segment/spec-ts))
(s/def ::place-of-worship (s/nilable ::segment/spec-xon))
(s/def ::advance-directive-code (s/nilable ::segment/spec-ce))
(s/def ::immunization-registry-status (s/nilable string?))
(s/def ::immunization-registry-status-effective (s/nilable ::segment/spec-ts))
(s/def ::publicity-code-effective (s/nilable ::segment/spec-ts))
(s/def ::military-branch (s/nilable string?))
(s/def ::military-rank (s/nilable string?))
(s/def ::military-status (s/nilable string?))

(s/def ::spec
  (s/keys :opt-un [::living-dependency ::living-arrangement ::primary-facility
                   ::primary-care-provider ::student-indicator ::handicap
                   ::living-will-code ::organ-donor-code ::separate-bill
                   ::duplicate-patient ::publicity-code ::protection-indicator
                   ::protection-indicator-effective ::place-of-worship
                   ::advance-directive-code ::immunization-registry-status
                   ::immunization-registry-status-effective ::publicity-code-effective
                   ::military-branch ::military-rank ::military-status]))

(defn record->hl7
  "Accepts a record of patient additional demographic data and returns a map of
  PD1 segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:living-dependency %))
     (parser/create-field (:living-arrangement %))
     (parser/create-field (util/trim-nils
                           (segment/xon->field (:primary-facility %))))
     (parser/create-field (util/trim-nils
                           (segment/xon->field (:primary-care-provider %))))
     (parser/create-field (:student-indicator %))
     (parser/create-field (:handicap %))
     (parser/create-field (:living-will-code %))
     (parser/create-field (:organ-donor-code %))
     (parser/create-field (:separate-bill %))
     (parser/create-field (util/trim-nils
                           (segment/cx->field (:duplicate-patient %))))
     (parser/create-field (util/trim-nils
                           (segment/cwe->field (:publicity-code %))))
     (parser/create-field (:protection-indicator %))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:protection-indicator-effective %))))
     (parser/create-field (util/trim-nils
                           (segment/xon->field (:place-of-worship %))))
     (parser/create-field (util/trim-nils
                           (segment/ce->field (:advance-directive-code %))))
     (parser/create-field (:immunization-registry-status %))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:immunization-registry-status-effective %))))
     (parser/create-field (util/trim-nils
                           (segment/ts->field (:publicity-code-effective %))))
     (parser/create-field (:military-branch %))
     (parser/create-field (:military-rank %))
     (parser/create-field (:military-status %)))
   record))

(defn hl7->record
  "Accepts a PD1 segment of parsed HL7 v2 data and returns a PD1 record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 22
   #(map->record
     {:living-dependency (message/get-segment-field % 1)
      :living-arrangement (util/unwrap-and-first (message/get-segment-field % 2))
      :primary-facility (segment/field->xon (message/get-segment-field % 3))
      :primary-care-provider (segment/field->xon (message/get-segment-field % 4))
      :student-indicator (util/unwrap-and-first (message/get-segment-field % 5))
      :handicap (util/unwrap-and-first (message/get-segment-field % 6))
      :living-will-code (util/unwrap-and-first (message/get-segment-field % 7))
      :organ-donor-code (util/unwrap-and-first (message/get-segment-field % 8))
      :separate-bill (util/unwrap-and-first (message/get-segment-field % 9))
      :duplicate-patient (segment/field->cx (message/get-segment-field % 10))
      :publicity-code (segment/field->cwe (message/get-segment-field % 11))
      :protection-indicator (util/unwrap-and-first (message/get-segment-field % 12))
      :protection-indicator-effective (segment/field->ts (message/get-segment-field % 13))
      :place-of-worship (segment/field->xon (message/get-segment-field % 14))
      :advance-directive-code (segment/field->ce (message/get-segment-field % 15))
      :immunization-registry-status (util/unwrap-and-first (message/get-segment-field % 16))
      :immunization-registry-status-effective (segment/field->ts (message/get-segment-field % 17))
      :publicity-code-effective (segment/field->ts (message/get-segment-field % 18))
      :military-branch (util/unwrap-and-first (message/get-segment-field % 19))
      :military-rank (util/unwrap-and-first (message/get-segment-field % 20))
      :military-status (util/unwrap-and-first (message/get-segment-field % 21))
      })
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
