(ns com.nervestaple.clinical.message-intermediate.segment.orc
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.obr :as obr]
   [com.nervestaple.clinical.message-intermediate.segment.pv1 :as pv1]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "ORC")

;; OSD: order sequence definition
(defrecord osd
    [sequence-flag
     placer-order-number-identifier
     placer-order-number-namespace
     filler-order-number-identifier
     filler-order-number-namespace
     condition-value
     max-number-repeats
     placer-order-number-universal-identifier
     placer-order-number-universal-type
     filler-order-number-universal-identifier
     filler-order-number-universal-type])

(s/def ::sequence-flag (s/nilable string?))
(s/def ::placer-order-number-identifier (s/nilable string?))
(s/def ::placer-order-number-namespace (s/nilable string?))
(s/def ::filler-order-number-identifier (s/nilable string?))
(s/def ::filler-order-number-namespace (s/nilable string?))
(s/def ::condition-value (s/nilable string?))
(s/def ::max-number-repeats (s/nilable pos-int?))
(s/def ::placer-order-number-universal-identifier (s/nilable string?))
(s/def ::placer-order-number-universal-type (s/nilable string?))
(s/def ::filler-order-number-universal-identifier (s/nilable string?))
(s/def ::filler-order-number-universal-type (s/nilable string?))

(s/def ::spec-osd
  (s/keys :req-un [::sequence-flag ::placer-order-number-identifier
                   ::filler-order-number-identifier
                   ::placer-order-number-universal-identifier]
          :opt-un [::placer-order-number-namespace
                   ::filler-order-number-namespace ::condition-value
                   ::max-number-repeats ::placer-order-number-universal-type
                   ::filler-order-number-universal-type]))

(s/fdef osd->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-osd))
              :coll (s/cat :coll (s/coll-of ::spec-osd)))
  :ret (s/nilable vector?))

(defn osd->field
  "Accepts one or a sequence of OSD records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
    record
    #(vector (:sequence-flag %)
             (:placer-order-number-identifier %)
             (:placer-order-number-namespace %)
             (:filler-order-number-identifier %)
             (:filler-order-number-namespace %)
             (:condition-value %)
             (:max-number-repeats %)
             (:placer-order-number-universal-identifier %)
             (:placer-order-number-universal-type %)
             (:filler-order-number-universal-identifier %)
             (:filler-order-number-universal-type %))))

(s/fdef field->osd
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-osd))
             :record (s/nilable ::spec-osd)))

(defn field->osd
  "Accepts an HL7 v2 field of data and returns a single record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->osd
   #(array-map :sequence-flag (util/get-or-nil % 0)
               :placer-order-number-identifier (util/get-or-nil % 1)
               :placer-order-number-namespace (util/get-or-nil % 2)
               :filler-order-number-identifier (util/get-or-nil % 3)
               :filler-order-number-namespace (util/get-or-nil % 4)
               :condition-value (util/get-or-nil % 5)
               :max-number-repeats (util/read-string (util/get-or-nil % 6))
               :placer-order-number-universal-identifier (util/get-or-nil % 7)
               :placer-order-number-universal-type (util/get-or-nil % 8)
               :filler-order-number-universal-identifier (util/get-or-nil % 9)
               :filler-order-number-universal-type (util/get-or-nil % 10))))

;; RI: repeat interval
(defrecord ri
    [repeat-pattern time-interval])

(s/def ::repeat-pattern (s/nilable string?))
(s/def ::time-interval (s/nilable string?))

(s/def ::spec-ri
  (s/keys :opt-un [::repeat-pattern ::time-interval]))

(s/fdef ri->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-ri))
              :coll (s/cat :coll (s/coll-of ::spec-ri)))
  :ret (s/nilable vector?))

(defn ri->field
  "Accepts one or a sequence of RI records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
    record
    #(vector (:repeat-pattern %)
             (:time-interval %))))

(s/fdef field->ri
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-ri))
             :record (s/nilable ::spec-ri)))

(defn field->ri
  "Accepts an HL7 v2 field of data and returns a single record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->ri
   #(array-map :repeat-pattern (util/get-or-nil % 0)
               :time-interval (util/get-or-nil % 1))))

;; TQ: timing quantity
(defrecord tq
    [quantity
     interval
     duration
     start-date
     end-date
     priority
     condition
     text
     conjuniction
     order-sequencing
     occurence-duration
     total-occurences])

(s/def ::quantity (s/nilable ::segment/spec-cq))
(s/def ::interval (s/nilable ::spec-ri))
(s/def ::duration (s/nilable string?))
(s/def ::start-date (s/nilable ::segment/spec-ts))
(s/def ::end-date (s/nilable ::segment/spec-ts))
(s/def ::priority (s/nilable string?))
(s/def ::condition (s/nilable string?))
(s/def ::text (s/nilable string?))
(s/def ::conjunction (s/nilable string?))
(s/def ::order-sequencing (s/nilable ::spec-osd))
(s/def ::occurence-duration (s/nilable ::segment/spec-ce))
(s/def ::total-occurences (s/nilable pos-int?))

(s/def ::spec-tq
  (s/keys :opt-un [::quantity ::interval ::duration ::start-date ::end-date
                   ::priority ::condition ::text ::conjunction
                   ::order-sequencing ::occurence-duration ::total-occurences]))

(s/fdef tq->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-tq))
              :coll (s/cat :coll (s/coll-of ::spec-tq)))
  :ret (s/nilable vector?))

(defn tq->field
  "Accepts one or a sequence of TQ records and returns a value, collection or map
  of HL7 v2 data."
  [record]
  (segment/type-to-field
   record
   #(conj (util/trim-nils (segment/cq->field (:quantity %)))
            (util/trim-nils (ri->field (:interval %)))
            (:duration %)
            (util/trim-nils (segment/ts->field (:start-date %)))
            (util/trim-nils (segment/ts->field (:end-date %)))
            (:priority %)
            (:condition %)
            (:text %)
            (:conjunction %)
            (util/trim-nils (osd->field (:order-sequencing %)))
            (util/trim-nils (segment/ce->field (:occurence-duration %)))
            (:total-occurences %))))

(s/fdef field->tq
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-tq))
             :record (s/nilable ::spec-tq)))

(defn field->tq
  "Accepts an HL7 v2 field of data and returns a single record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->tq
   #(array-map :quantity (segment/field->cq (util/get-or-nil % 0))
               :interval (field->ri (util/get-or-nil % 1))
               :duration (util/get-or-nil % 2)
               :start-date (segment/field->ts (util/get-or-nil % 3))
               :end-date (segment/field->ts (util/get-or-nil % 4))
               :priority (util/get-or-nil % 5)
               :condition (util/get-or-nil % 6)
               :text (util/get-or-nil % 7)
               :conjunction (util/get-or-nil % 8)
               :order-sequencing (field->osd (util/get-or-nil % 9))
               :occurence-duration (segment/field->ce (util/get-or-nil % 10))
               :total-occurences (util/read-string (util/get-or-nil % 11)))))

;; Record representing an ORC segment
(defrecord record
    [order-control
     placer-order-number
     filler-order-number
     placer-group-number
     order-status
     response-flag
     timing-quantity
     parent-order
     transaction-date
     entered-by
     verified-by
     ordering-provider
     entered-by-location
     callback-phone-number
     effective-date
     control-code-reason
     entering-org
     entering-device
     action-by
     advanced-beneficiary-notice-code
     ordering-facility
     ordering-facility-address
     ordering-facility-phone
     ordering-provider-address
     order-status-modifier
     advanced-beneficiary-notice-override-reason
     filler-expected-availability-date
     confidentiality-code
     order-type
     enterer-authorization-mode
     parent-universal-service-identifier])

(s/def ::order-control (s/nilable string?))
(s/def ::placer-order-number (s/nilable ::segment/spec-ei))
(s/def ::filler-order-number (s/nilable ::segment/spec-ei))
(s/def ::placer-group-number (s/nilable ::segment/spec-ei))
(s/def ::order-status (s/nilable string?))
(s/def ::response-flag (s/nilable string?))
(s/def ::timing-quantity (s/nilable ::spec-tq))
(s/def ::parent-order (s/nilable ::obr/spec-eip))
(s/def ::transaction-date (s/nilable ::segment/spec-ts))
(s/def ::entered-by (s/nilable ::segment/spec-xcn))
(s/def ::verified-by (s/nilable ::segment/spec-xcn))
(s/def ::ordering-provider (s/nilable ::segment/spec-xcn))
(s/def ::entered-by-location (s/nilable ::pv1/spec-pl))
(s/def ::callback-phone-number (s/nilable ::segment/spec-xtn))
(s/def ::effective-date (s/nilable ::segment/spec-ts))
(s/def ::control-code-reason (s/nilable ::segment/spec-ce))
(s/def ::entering-org (s/nilable ::segment/spec-ce))
(s/def ::entering-device (s/nilable ::segment/spec-ce))
(s/def ::action-by (s/nilable ::segment/spec-xcn))
(s/def ::advanced-benficiary-notice-code (s/nilable ::segment/spec-ce))
(s/def ::ordering-facility (s/nilable ::segment/spec-xon))
(s/def ::ordering-facility-address (s/nilable ::segment/spec-xad))
(s/def ::ordering-facility-phone (s/nilable ::segment/spec-xtn))
(s/def ::ordering-provider-address (s/nilable ::segment/spec-xad))
(s/def ::order-status-modifier (s/nilable ::segment/spec-cwe))
(s/def ::advanced-beneficiary-notice-override-reason (s/nilable ::segment/spec-cwe))
(s/def ::filler-expected-availability-date (s/nilable ::segment/spec-ts))
(s/def ::confidentiality-code (s/nilable ::segment/spec-cwe))
(s/def ::order-type (s/nilable ::segment/spec-cwe))
(s/def ::enterer-authorization-mode (s/nilable ::segment/spec-cne))
(s/def ::parent-universal-service-identifier (s/nilable ::segment/spec-cwe))

(s/def ::spec
  (s/keys :req-un [::order-control]
          :opt-un [::placer-order-number ::filler-order-number
                   ::placer-group-number ::order-status ::response-flag
                   ::timing-quantity ::parent-order ::transaction-date
                   ::entered-by ::verified-by ::ordering-provider
                   ::entered-by-location ::callback-phone-number ::effective-date
                   ::control-code-reason ::entering-org ::entering-device
                   ::action-by ::advanced-benficiary-notice-code
                   ::ordering-facility ::ordering-facility-address
                   ::ordering-facility-phone
                   ::ordering-provider-address ::order-status-modifier
                   ::advanced-beneficiary-notice-override-reason
                   ::filler-expected-availability-date ::confidentiality-code
                   ::order-type ::enterer-authorization-mode
                   ::parent-universal-service-identifier]))

(defn record->hl7
  "Accepts a record of common order data and returns a record of ORC segment
  data."
  [record]
  (message/record-to-hl7
    SEGMENT-ID
    #(vector
       (parser/create-field (:order-control %))
       (parser/create-field (util/trim-nils
                              (segment/ei->field (:placer-order-number %))))
       (parser/create-field (util/trim-nils
                              (segment/ei->field (:filler-order-number %))))
       (parser/create-field (util/trim-nils
                              (segment/ei->field (:placer-group-number %))))
       (parser/create-field (:order-status %))
       (parser/create-field (:response-flag %))
       (parser/create-field (util/trim-nils
                              (tq->field (:timing-quantity %))))
       (parser/create-field (util/trim-nils
                              (obr/eip->field (:parent-order %))))
       (parser/create-field (util/trim-nils
                              (segment/ts->field (:transaction-date %))))
       (parser/create-field (util/trim-nils
                              (segment/xcn->field (:entered-by %))))
       (parser/create-field (util/trim-nils
                              (segment/xcn->field (:verified-by %))))
       (parser/create-field (util/trim-nils
                              (segment/xcn->field (:ordering-provider %))))
       (parser/create-field (util/trim-nils
                              (pv1/pl->field (:entered-by-location %))))
       (parser/create-field (util/trim-nils
                              (segment/xtn->field (:callback-phone-number %))))
       (parser/create-field (util/trim-nils
                              (segment/ts->field (:effective-date %))))
       (parser/create-field (util/trim-nils
                              (segment/ce->field (:control-code-reason %))))
       (parser/create-field (util/trim-nils
                              (segment/ce->field (:entering-org %))))
       (parser/create-field (util/trim-nils
                              (segment/ce->field (:entering-device %))))
       (parser/create-field (util/trim-nils
                              (segment/xcn->field (:action-by %))))
       (parser/create-field (util/trim-nils
                              (segment/ce->field (:advanced-beneficiary-notice-code %))))
       (parser/create-field (util/trim-nils
                              (segment/xon->field (:ordering-facility %))))
       (parser/create-field (util/trim-nils
                              (segment/xad->field (:ordering-facility-address %))))
       (parser/create-field (util/trim-nils
                              (segment/xtn->field (:ordering-facility-phone %))))
       (parser/create-field (util/trim-nils
                              (segment/xad->field (:ordering-provider-address %))))
       (parser/create-field (util/trim-nils
                              (segment/cwe->field (:order-status-modifier %))))
       (parser/create-field (util/trim-nils
                              (segment/cwe->field (:advanced-beneficiary-notice-override-reason %))))
       (parser/create-field (util/trim-nils
                              (segment/ts->field (:filler-expected-availability-date %))))
       (parser/create-field (util/trim-nils
                              (segment/cwe->field (:confidentiality-code %))))
       (parser/create-field (util/trim-nils
                              (segment/cwe->field (:order-type %))))
       (parser/create-field (util/trim-nils
                              (segment/cne->field (:enterer-authorization-mode %))))
       (parser/create-field (util/trim-nils
                              (segment/cwe->field (:parent-universal-service-identifier %)))))
    record))

(defn hl7->record
  "Accepts an ORC segment of parsed HL7 data and returns an ORC record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 32
   #(map->record
     {:order-control (util/unwrap-and-first (message/get-segment-field % 1))
      :placer-order-number (segment/field->ei (message/get-segment-field % 2))
      :filler-order-number (segment/field->ei (message/get-segment-field % 3))
      :placer-group-number (segment/field->ei (message/get-segment-field % 4))
      :order-status (util/unwrap-and-first (message/get-segment-field % 5))
      :response-flag (util/unwrap-and-first (message/get-segment-field % 6))
      :timing-quantity (field->tq (message/get-segment-field % 7))
      :parent-order (obr/field->eip (message/get-segment-field % 8))
      :transaction-date (segment/field->ts (message/get-segment-field % 9))
      :entered-by (segment/field->xcn (message/get-segment-field % 10))
      :verified-by (segment/field->xcn (message/get-segment-field % 11))
      :ordering-provider (segment/field->xcn (message/get-segment-field % 12))
      :entered-by-location (pv1/field->pl (message/get-segment-field % 13))
      :callback-phone-number (segment/field->xtn (message/get-segment-field % 14))
      :effective-date (segment/field->ts (message/get-segment-field % 15))
      :control-code-reason (segment/field->ce (message/get-segment-field % 16))
      :entering-org (segment/field->ce (message/get-segment-field % 17))
      :entering-device (segment/field->ce (message/get-segment-field % 18))
      :action-by (segment/field->xcn (message/get-segment-field % 19))
      :advanced-beneficiary-notice-code (segment/field->ce (message/get-segment-field % 20))
      :ordering-facility (segment/field->xon (message/get-segment-field % 21))
      :ordering-facility-address (segment/field->xad (message/get-segment-field % 22))
      :ordering-facility-phone (segment/field->xtn (message/get-segment-field % 23))
      :ordering-provider-address (segment/field->xad (message/get-segment-field % 24))
      :order-status-modifier (segment/field->cwe (message/get-segment-field % 25))
      :advanced-beneficiary-notice-override-reason (segment/field->cwe (message/get-segment-field % 26))
      :filler-expected-availability-date (segment/field->ts (message/get-segment-field % 27))
      :confidentiality-code (segment/field->cwe (message/get-segment-field % 28))
      :order-type (segment/field->cwe (message/get-segment-field % 29))
      :enterer-authorization-mode (segment/field->cne (message/get-segment-field % 30))
      :parent-universal-service-identifier (segment/field->cwe (message/get-segment-field % 31))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
