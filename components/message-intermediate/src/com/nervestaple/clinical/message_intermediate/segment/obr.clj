(ns com.nervestaple.clinical.message-intermediate.segment.obr
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]))

;; Segment identifier
(def SEGMENT-ID "OBR")

;; EIP: entity identifier pair
(defrecord eip [placer-assigned filler-assigned])

(s/def ::placer-assigned (s/nilable ::segment/spec-ei))
(s/def ::filler-assigned (s/nilable ::segment/spec-ei))

(s/def ::spec-eip
  (s/keys :opt-un [::placer-assigned ::filler-assigned]))

(s/fdef eip->field
  :args (s/or :record (s/cat :ei-record (s/nilable ::spec-eip))
              :coll (s/cat :coll (s/coll-of ::spec-eip)))
  :ret (s/nilable vector?))

(defn eip->field
  "Accepts one or a sequence of EIP records and returns a value, collection or
  map of HL7 v2 data."
  [record]
  (segment/type-to-field record
                         #(vector (util/trim-nils (segment/ei->field (:placer-assigned %)))
                                  (util/trim-nils (segment/ei->field (:filler-assigned %))))))

(s/fdef field->eip
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-eip))
             :record (s/nilable ::spec-eip)))

(defn field->eip
  "Accepts an HL7 v2 field of EIP data and returns a single record, sequence of
  records or nil."
  [field]
  (segment/field-to-type field
                         map->eip
                         #(array-map :placer-assigned
                                     (segment/field->ei (util/get-or-nil % 0))
                                     :filler-assigned
                                     (segment/field->ei (util/get-or-nil % 1)))))

;; MOC: charge to practice
(defrecord moc
    [dollar-amount charge-code])

(s/def ::dollar-amount (s/nilable ::segment/spec-mo))
(s/def ::charge-code (s/nilable ::segment/spec-ce))

(s/def ::spec-moc
  (s/keys :opt-un [::dollar-amount ::charge-code]))

(s/fdef moc->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-moc))
              :coll (s/cat :coll (s/coll-of ::spec-moc)))
  :ret (s/nilable vector?))

(defn moc->field
  "Accepts one or a sequence of MOC records and returns a value, collection or map
  of HL7 v2 data."
  [moc-record]
  (segment/type-to-field
   moc-record
   #(vector (util/trim-nils (segment/mo->field (:dollar-amount %)))
            (util/trim-nils (segment/ce->field (:charge-code %))))))

(s/fdef field->moc
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-moc))
             :record (s/nilable ::spec-moc)))

(defn field->moc
  "Accepts an HL7 v2 field of MOC data and returns a single record, a sequence of
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->moc
   #(array-map :dollar-amount (segment/field->mo (util/get-or-nil  % 0))
               :charge-code (segment/field->ce (util/get-or-nil % 1)))))

;; PRL: parent result link
(defrecord prl
    [parent-observation-id parent-result-sub-id parent-observation-result])

(s/def ::parent-observation-id (s/nilable ::segment/spec-ce))
(s/def ::parent-result-sub-id (s/nilable string?))
(s/def ::parent-observation-result (s/nilable string?))

(s/def ::spec-prl
  (s/keys :opt-un [::parent-observation-id ::parent-result-sub-id
                   ::parent-observation-result]))

(s/fdef prl->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-prl))
              :coll (s/cat :coll (s/coll-of ::spec-prl)))
  :ret (s/nilable vector?))

(defn prl->field
  "Accepts one or a sequence of PRL records and returns a value, collection or map
  of HL7 v2 data."
  [prl-record]
  (segment/type-to-field
   prl-record
   #(vector (util/trim-nils (segment/ce->field (:parent-observation-id %)))
            (:parent-result-sub-id %)
            (:parent-observation-result %))))

(s/fdef field->prl
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-prl))
             :record (s/nilable ::spec-prl)))

(defn field->prl
  "Accepts an HL7 v2 field of PRL data and returns a single record, a sequence or
  records or nil."
  [field]
  (segment/field-to-type
   field
   map->prl
   #(array-map :parent-observation-id (segment/field->ce (util/get-or-nil % 0))
               :parent-result-sub-id (util/get-or-nil % 1)
               :parent-observation-result (util/get-or-nil % 2))))

(defrecord sps
    [spec-source spec-additives spec-freetext spec-body-site
     spec-site-modifier spec-collection-modifier spec-role])

(s/def ::spec-source (s/nilable ::segment/spec-ce))
(s/def ::spec-additives (s/nilable string?))
(s/def ::spec-freetext (s/nilable string?))
(s/def ::spec-body-site (s/nilable ::segment/spec-ce))
(s/def ::spec-site-modifier (s/nilable ::segment/spec-ce))
(s/def ::spec-collection-modifier (s/nilable ::segment/spec-ce))
(s/def ::spec-specimen-role (s/nilable ::segment/spec-ce))

(s/def ::spec-sps
  (s/keys :opt-un [::spec-source ::spec-additives ::spec-freetext ::spec-body-site
                   ::spec-site-modifier ::spec-collection-modifier ::spec-specimen-role]))

(s/fdef sps->field
  :args (s/or :record (s/cat :sps-record (s/nilable ::spec-sps))
              :coll (s/cat :coll (s/coll-of ::spec-sps)))
  :ret (s/nilable vector?))

;; SPS: specimen source
(defn sps->field
  "Accepts one or a sequence of SPS records and returns a value, collection or map
  of HL7 c2 data."
  [record]
  (segment/type-to-field record
                         #(vector (util/trim-nils (segment/ce->field (:spec-source %)))
                                  (:spec-additives %)
                                  (:spec-freetext %)
                                  (util/trim-nils (segment/ce->field (:spec-body-site %)))
                                  (util/trim-nils (segment/ce->field (:spec-site-modifier %)))
                                  (util/trim-nils (segment/ce->field (:spec-collection-modifier %)))
                                  (util/trim-nils (segment/ce->field (:spec-specimen-role %))))))

(s/fdef field->sps
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-sps))
             :record (s/nilable ::spec-sps)))

(defn field->sps
  "Accepts and HL7 v2 field of SPS data and returns a single record, sequence of
  records or nil."
  [field]
  (segment/field-to-type field
                         map->sps
                         #(array-map :spec-source
                                     (segment/field->ce (util/get-or-nil % 0))
                                     :spec-additives (util/get-or-nil % 1)
                                     :spec-freetext (util/get-or-nil % 2)
                                     :spec-body-site
                                     (segment/field->ce (util/get-or-nil % 3))
                                     :spec-site-modifier
                                     (segment/field->ce (util/get-or-nil % 4))
                                     :spec-collection-modifier
                                     (segment/field->ce (util/get-or-nil % 5))
                                     :spec-specimen-role
                                     (segment/field->ce (util/get-or-nil % 6)))))

;; Record representing an OBR segment
(defrecord record
    [set-id
     placer-order-number
     filler-order-number
     universal-service-identifier
     priority
     requested-date-time
     observation-start
     observation-end
     collection-volume
     collection-identifier
     specimen-action-code
     danger-code
     relevant-clinical-info
     specimen-received
     specimen-source
     ordering-provider
     order-callback-phone
     placer-field-1
     placer-field-2
     filler-field-1
     filler-field-2
     results-reported-status-changed
     charge-to-practice
     diagnostic-service-sect-id
     result-status
     parent-result
     quantity-timing
     result-copies-to
     parent-number
     transportation-mode
     reason-for-study
     principal-result-interpreter
     assistant-result-interpreter
     technician
     transcriptionist
     scheduled
     number-sample-containers
     transport-logistics-collected
     collector-comment
     transport-arrangement-resp
     transport-arranged
     escort-required
     planned-patient-transport-comment
     procedure-code
     procedure-code-modifier
     placer-supplemental-service-info
     filler-supplemental-service-info])

(s/def ::set-id (s/nilable pos-int?))
(s/def ::placer-order-number (s/nilable ::segment/spec-ei))
(s/def ::filler-order-number (s/nilable ::segment/spec-ei))
(s/def ::universal-service-identifier (s/nilable ::segment/spec-ce))
(s/def ::priority (s/nilable string?))
(s/def ::requested-date-time (s/nilable ::segment/spec-ts))
(s/def ::observation-start (s/nilable ::segment/spec-ts))
(s/def ::observation-end (s/nilable ::segment/spec-ts))
(s/def ::collection-volumne (s/nilable ::segment/spec-cq))
(s/def ::collection-identifier (s/nilable ::segment/spec-xcn))
(s/def ::specimen-action-code (s/nilable string?))
(s/def ::danger-code (s/nilable ::segment/spec-ce))
(s/def ::relevant-clinical-info (s/nilable ::segment/spec-ce))
(s/def ::specimen-received (s/nilable ::segment/spec-ts))
(s/def ::specimen-source (s/nilable ::spec-sps))
(s/def ::ordering-provider (s/nilable ::segment/spec-xcn))
(s/def ::order-callback-phone (s/nilable ::segment/spec-xtn))
(s/def ::placer-field-1 (s/nilable string?))
(s/def ::placer-field-2 (s/nilable string?))
(s/def ::filler-field-1 (s/nilable string?))
(s/def ::filler-field-2 (s/nilable string?))
(s/def ::results-reported-status-changed (s/nilable ::segment/spec-ts))
(s/def ::charge-to-practice (s/nilable ::spec-moc))
(s/def ::diagnostic-service-sect-id (s/nilable string?))
(s/def ::result-status (s/nilable string?))
(s/def ::parent-result (s/nilable ::spec-prl))
(s/def ::quantity-timing (s/or :string (s/nilable string?)
                               :coll (s/nilable (s/coll-of string?))))
(s/def ::result-copies-to (s/nilable ::segment/spec-xcn))
(s/def ::parent-number (s/nilable ::spec-eip))
(s/def ::transportation-mode (s/nilable string?))
(s/def ::reason-for-study (s/nilable ::segment/spec-ce))
(s/def ::principal-result-interpreter (s/or :string (s/nilable string?)
                                            :coll (s/nilable (s/coll-of string?))))
(s/def ::assistant-result-interpreter (s/or :string (s/nilable string?)
                                            :coll (s/nilable (s/coll-of string?))))
(s/def ::technician (s/or :string (s/nilable string?)
                          :coll (s/nilable (s/coll-of string?))))
(s/def ::transcriptionist (s/or :string (s/nilable string?)
                                :coll (s/nilable (s/coll-of string?))))
(s/def ::scheduled (s/nilable ::segment/spec-ts))
(s/def ::number-sample-containers (s/nilable number?))
(s/def ::transport-logistics-collected (s/nilable ::segment/spec-ce))
(s/def ::collector-comment (s/nilable ::segment/spec-ce))
(s/def ::transport-arrangement-resp (s/nilable ::segment/spec-ce))
(s/def ::transport-arranged (s/nilable string?))
(s/def ::escort-required (s/nilable string?))
(s/def ::planned-patient-transport-comment (s/nilable ::segment/spec-ce))
(s/def ::procedure-code (s/nilable ::segment/spec-ce))
(s/def ::procedure-code-modifier (s/nilable ::segment/spec-ce))
(s/def ::placer-supplemental-service-info (s/nilable ::segment/spec-ce))
(s/def ::filler-supplemental-service-info (s/nilable ::segment/spec-ce))

(s/def ::spec
  (s/keys :req-un [::universal-service-identifier]
          :opt-un [::set-id ::placer-order-number ::filler-order-number ::priority
                   ::requested-date-time ::observation-start ::observation-end
                   ::collection-volumne ::collection-identifier
                   ::specimen-action-code ::danger-code ::relevant-clinical-info
                   ::specimen-received ::specimen-source ::ordering-provider
                   ::order-callback-phone ::placer-field-1 ::placer-field-2
                   ::filler-field-1 ::filler-field-2
                   ::results-reported-status-changed ::charge-to-practice
                   ::diagnostic-service-sect-id ::result-status ::parent-result
                   ::quantity-timing ::result-copies-to ::parent-number
                   ::transportation-mode ::reason-for-study
                   ::principal-result-interpreter ::assistant-result-interpreter
                   ::technician ::transcriptionist ::scheduled
                   ::number-sample-containers ::transport-logistics-collected
                   ::collector-comment ::transport-arrangement-resp
                   ::transport-arranged ::escort-required
                   ::planned-patient-transport-comment ::procedure-code
                   ::procedure-code-modifier ::placer-supplemental-service-info
                   ::filler-supplemental-service-info]))

(defn record->hl7
  "Accepts a record of observation request data and returns a map of OBR segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (:set-id %))
     (parser/create-field (util/trim-nils (segment/ei->field (:placer-order-number %))))
     (parser/create-field (util/trim-nils (segment/ei->field (:filler-order-number %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:universal-service-identifier %))))
     (parser/create-field (:priority %))
     (parser/create-field (util/trim-nils (segment/ts->field (:requested-date-time %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:observation-start %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:observation-end %))))
     (parser/create-field (util/trim-nils (segment/cq->field (:collection-volume %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:collection-identifier %))))
     (parser/create-field (:specimen-action-code %))
     (parser/create-field (util/trim-nils (segment/ce->field (:danger-code %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:relevant-clinical-info %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:specimen-received %))))
     (parser/create-field (util/trim-nils (sps->field (:specimen-source %))))
     (parser/create-field (util/trim-nils (segment/xcn->field (:ordering-provider %))))
     (parser/create-field (util/trim-nils (segment/xtn->field (:order-callback-phone %))))
     (parser/create-field (:placer-field-1 %))
     (parser/create-field (:placer-field-2 %))
     (parser/create-field (:filler-field-1 %))
     (parser/create-field (:filler-field-2 %))
     (parser/create-field (util/trim-nils (segment/ts->field (:results-reported-status-changed %))))
     (parser/create-field (util/trim-nils (moc->field (:charge-to-practice %))))
     (parser/create-field (:diagnostic-service-sect-id %))
     (parser/create-field (:result-status %))
     (parser/create-field (:parent-result %))
     (parser/create-field (:quantity-timing %))
     (parser/create-field (util/trim-nils (segment/xcn->field (:result-copies-to %))))
     (parser/create-field (util/trim-nils (eip->field (:parent-number %))))
     (parser/create-field (:transportation-mode %))
     (parser/create-field (util/trim-nils (segment/ce->field (:reason-for-study %))))
     (parser/create-field (:principal-result-interpreter %))
     (parser/create-field (:assistant-result-interpreter %))
     (parser/create-field (:technician %))
     (parser/create-field (:transcriptionist %))
     (parser/create-field (util/trim-nils (segment/ts->field (:scheduled %))))
     (parser/create-field (:number-sample-containers %))
     (parser/create-field (util/trim-nils (segment/ce->field (:transport-logistics-collected %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:collector-comment %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:transport-arrangement-resp %))))
     (parser/create-field (:transport-arranged %))
     (parser/create-field (:escort-required %))
     (parser/create-field (util/trim-nils (segment/ce->field (:planned-patient-transport-comment %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:procedure-code %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:procedure-code-modifier %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:placer-supplemental-service-info %))))
     (parser/create-field (util/trim-nils (segment/ce->field (:filler-supplemental-service-info %)))))
   record))

(defn hl7->record
  "Accepts an OBR segment of parsed HL7 segment data and returns an OBR record."
  [segment]
  (message/hl7-to-record
   SEGMENT-ID 48
   #(map->record
     {:set-id (util/read-string (util/unwrap-and-first (message/get-segment-field % 1)))
      :placer-order-number (segment/field->ei (message/get-segment-field % 2))
      :filler-order-number (segment/field->ei (message/get-segment-field % 3))
      :universal-service-identifier (segment/field->ce (message/get-segment-field % 4))
      :priority (util/unwrap-and-first (message/get-segment-field % 5))
      :requested-date-time (segment/field->ts (message/get-segment-field % 6))
      :observation-start (segment/field->ts (message/get-segment-field % 7))
      :observation-end (segment/field->ts (message/get-segment-field % 8))
      :collection-volume (segment/field->cq (message/get-segment-field % 9))
      :collection-identifier (segment/field->xcn (message/get-segment-field % 10))
      :specimen-action-code (util/unwrap-and-first (message/get-segment-field % 11))
      :danger-code (segment/field->ce (message/get-segment-field % 12))
      :relevant-clinical-info (segment/field->ce (message/get-segment-field % 13))
      :specimen-received (segment/field->ts (message/get-segment-field % 14))
      :specimen-source (field->sps (message/get-segment-field % 15))
      :ordering-provider (segment/field->xcn (message/get-segment-field % 16))
      :order-callback-phone (segment/field->xtn (message/get-segment-field % 17))
      :placer-field-1 (util/unwrap-and-first (message/get-segment-field % 18))
      :placer-field-2 (util/unwrap-and-first (message/get-segment-field % 19))
      :filler-field-1 (util/unwrap-and-first (message/get-segment-field % 20))
      :filler-field-2 (util/unwrap-and-first (message/get-segment-field % 21))
      :results-reported-status-changed (segment/field->ts (message/get-segment-field % 22))
      :charge-to-practice (field->moc (message/get-segment-field % 23))
      :diagnostic-service-sect-id (util/unwrap-and-first (message/get-segment-field % 24))
      :result-status (util/unwrap-and-first (message/get-segment-field % 25))
      :parent-result (field->eip (message/get-segment-field % 26))
      :quantity-timing (util/unwrap-field (message/get-segment-field % 27))
      :result-copies-to (segment/field->xcn (message/get-segment-field % 28))
      :parent-number (util/unwrap-field (message/get-segment-field % 29))
      :transportation-mode (util/unwrap-and-first (message/get-segment-field % 30))
      :reason-for-study (segment/field->ce (message/get-segment-field % 31))
      :principal-result-interpreter (util/unwrap-field (message/get-segment-field % 32))
      :assistant-result-interpreter (util/unwrap-field (message/get-segment-field % 33))
      :technician (util/unwrap-field (message/get-segment-field % 34))
      :transcriptionist (util/unwrap-field (message/get-segment-field % 35))
      :scheduled (segment/field->ts (message/get-segment-field % 36))
      :number-sample-containers (util/read-string (util/unwrap-and-first
                                                  (message/get-segment-field % 37)))
      :transport-logistics-collected (segment/field->ce (message/get-segment-field % 38))
      :collector-comment (segment/field->ce (message/get-segment-field % 39))
      :transport-arrangement-resp (segment/field->ce (message/get-segment-field % 40))
      :transport-arranged (util/unwrap-and-first (message/get-segment-field % 41))
      :escort-required (util/unwrap-and-first (message/get-segment-field % 42))
      :planned-patient-transport-comment (segment/field->ce (message/get-segment-field % 43))
      :procedure-code (segment/field->ce (message/get-segment-field % 44))
      :procedure-code-modifier (segment/field->ce (message/get-segment-field % 45))
      :placer-supplemental-service-info (segment/field->ce (message/get-segment-field % 46))
      :filler-supplemental-service-info (segment/field->ce (message/get-segment-field % 47))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
