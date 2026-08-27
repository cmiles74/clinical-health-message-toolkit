 (ns com.nervestaple.clinical.message-intermediate.segment.msh
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as gen]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.lookups :as lookups]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]))

;; Segment identifier
(def SEGMENT-ID "MSH")

(def default-delimiters
  "Map with our default set of delimiters"
  {:field 124
   :component 94
   :subcomponent 38
   :repeating 126
   :escape 92})

;; PT: processing type
(defrecord pt
    [processing-id processing-mode])

(s/def ::processing-id (s/nilable string?))
(s/def ::processing-mode (s/nilable string?))

(s/def ::spec-pt
  (s/keys :opt-un [::processing-id ::processing-mode]))

(s/fdef pt->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-pt))
              :coll (s/cat :coll (s/coll-of ::spec-pt)))
  :ret (s/nilable vector?))

(defn pt->field
  "Accepts one or a sequence of PT records and returns a value, collection or map
  of HL7 v2 data."
  [pt-record]
  (segment/type-to-field pt-record
                         #(vector (lookups/processing-id-by-key (:processing-id %))
                                  (lookups/processing-mode-by-key (:processing-mode %)))))

(s/fdef field->pt
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-pt))
             :record (s/nilable ::spec-pt)))

(defn field->pt
  "Accepts an HL7 v2 field of PT data and returns a single PT record or a sequence
  of records or nil."
  [field]
  (segment/field-to-type field
                         map->pt
                         #(array-map :processing-id (lookups/processing-id-by-value
                                                     (util/get-or-nil % 0))
                                     :processing-mode (lookups/processing-mode-by-value
                                                       (util/get-or-nil % 1)))))

;; commonly used PT values
(def pt-debug (field->pt "D"))
(def pt-test (field->pt "T"))
(def pt-production (field->pt "P"))

;; VID: version identifier
(defrecord vid
    [version-id international-code international-version-id])

(s/def ::version-id (s/nilable string?))
(s/def ::international-code (s/nilable ::segment/spec-ce))
(s/def ::international-version-id (s/nilable ::segment/spec-ce))

(s/def ::spec-vid
  (s/keys :opt-un [::version-id ::international-code ::international-version-id]))

(s/fdef vid->field
  :args (s/or :record (s/cat :record (s/nilable ::spec-vid))
              :coll (s/cat :coll (s/coll-of ::spec-vid)))
  :ret (s/nilable vector?))

(defn vid->field
  "Accepts one or a sequence of VID records and returns a value, collection or map
  of HL7 v2 data."
  [vid-record]
  (segment/type-to-field vid-record
                         #(vector (:version-id %)
                                  (:international-code %)
                                  (:international-version-id %))))

(s/fdef field->vid
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-vid))
             :record (s/nilable ::spec-vid)))

(defn field->vid
  "Accepts an HL7 v2 field of VID data and returns a single VID record or a
  sequence of records or nil."
  [field]
  (segment/field-to-type field
                         map->vid
                         #(array-map :version-id (util/get-or-nil % 0)
                                     :international-code (segment/field->cwe (util/get-or-nil % 1))
                                     :international-version-id (segment/field->cwe (util/get-or-nil % 2)))))

(defn string->vid
  "Accepts a string with version identifier and returns a VID record."
  [version-id]
  (map->vid {:version-id version-id}))

;; MSG: message type
(defrecord msg
    [msg-code msg-trigger msg-structure])

(s/def ::msg-code (s/and (s/nilable string?) #(>= 3 (count %))))
(s/def ::msg-trigger (s/and (s/nilable string?) #(>= 3 (count %))))
(s/def ::msg-structure (s/and (s/nilable string?) #(>= 7 (count %))))

(s/def ::spec-msg
  (s/keys :opt-un [::msg-code ::msg-trigger ::msg-structure]))

(s/fdef msg->field
  :args (s/or :record (s/cat :msg-record (s/nilable ::spec-msg))
              :coll (s/cat :coll (s/coll-of ::spec-msg)))
  :ret (s/nilable vector?))

(defn msg->field
  "Accepts one or a sequence of MSG records and returns a value, collection or map
  of HL7 v2 data."
  [msg-record]
  (segment/type-to-field msg-record
                         #(vector (:msg-code %)
                                  (:msg-trigger %)
                                  (:msg-structure %))))

(s/fdef field->msg
  :args (s/nilable (s/coll-of ::segment/hl7-content))
  :ret (s/or :vector (s/and vector? (s/coll-of ::spec-msg))
             :record (s/nilable ::spec-msg)))

(defn field->msg
  "Accepts an HL7 v2 field of MSG data and returns a single MSG record or a
  sequence of records or nil."
  [field]
  (segment/field-to-type field
                         map->msg
                         #(array-map :msg-code (util/get-or-nil % 0)
                                     :msg-trigger (util/get-or-nil % 1)
                                     :msg-structure (util/get-or-nil % 2))))

;; Record representing an MSH segment
(defrecord record
    [delimiters
     sending-application
     sending-facility
     receiving-application
     receiving-facility
     message-created
     security
     message-type
     message-control-id
     message-processing-id
     message-version-id
     sequence-number
     continuation-pointer
     accept-acknowledgement-type
     application-acknowledgement-type
     country-code
     character-set
     principal-language
     alternate-character-set
     message-profile-indicator
     sending-responsible-organization
     receiving-responsible-organization
     sending-network-address
     receiving-network-address])

(s/def ::delimiters
  (s/with-gen map?
    #(gen/fmap identity (s/gen #{util/default-delimiters}))))
(s/def ::sending-application (s/nilable ::segment/spec-hd))
(s/def ::sending-facility (s/nilable ::segment/spec-hd))
(s/def ::receiving-application (s/nilable ::segment/spec-hd))
(s/def ::receiving-facility (s/nilable ::segment/spec-hd))
(s/def ::message-created ::segment/spec-ts)
(s/def ::security (s/and (s/nilable string?) #(>= 40 (count %))))
(s/def ::message-type ::spec-msg)
(s/def ::message-control-id (s/and (s/nilable string?) #(>= 20 (count %))))
(s/def ::message-processing-id (s/nilable ::spec-pt))
(s/def ::message-version-id (s/nilable ::spec-vid))
(s/def ::sequence-number (s/and (s/nilable pos-int?) #(>= 15 (count (str %)))))
(s/def ::continuation-pointer (s/and (s/nilable string?) #(>= 180 (count %))))
(s/def ::accept-acknowledgement-type (s/nilable string?))
(s/def ::application-acknowledgement-type (s/nilable string?))
(s/def ::country-code (s/and (s/nilable string?) #(>= 3 (count %))))
(s/def ::character-set (s/and (s/nilable string?) #(>= 16 (count %))))
(s/def ::principal-language (s/nilable ::segment/spec-ce))
(s/def ::alternate-character-set (s/and (s/nilable string?) #(>= 20 (count %))))
(s/def ::message-profile-indicator (s/nilable ::segment/spec-ei))
(s/def ::sending-responsible-organization (s/and (s/nilable string?) #(>= 567 (count %))))
(s/def ::receiving-responsible-organization (s/and (s/nilable string?) #(>= 567 (count %))))
(s/def ::sending-network-address (s/nilable ::segment/spec-hd))
(s/def ::receiving-network-address (s/nilable ::segment/spec-hd))

(s/def ::spec
  (s/keys :req-un [::delimiters ::message-created ::message-type
                   ::message-control-id ::message-processing-id ::message-version-id]
          :opt-un [::sending-application ::sending-facility ::receiving-application
                   ::receiving-facility ::security ::sequence-number
                   ::continuation-pointer ::accept-acknowledgement-type
                   ::application-acknowledgement-type ::country-code ::character-set
                   ::principal-language ::alternate-character-set ::message-profile-indicator
                   ::sending-responsible-organization ::receiving-responsible-organization
                   ::sending-network-address ::receiving-network-address]))


(defn record->hl7
  "Accepts a record of message header (MSH) segment data and returns a map of MSH
  segment data."
  [record]
  (message/record-to-hl7
   SEGMENT-ID
   #(vector
     (parser/create-field (parser/pr-delimiters (:delimiters record)))
     (parser/create-field (util/trim-nils (segment/hd->field (:sending-application %))))
     (parser/create-field (util/trim-nils (segment/hd->field (:sending-facility %))))
     (parser/create-field (util/trim-nils (segment/hd->field (:receiving-application %))))
     (parser/create-field (util/trim-nils (segment/hd->field (:receiving-facility %))))
     (parser/create-field (util/trim-nils (segment/ts->field (:message-created %))))
     (parser/create-field (:security %))
     (parser/create-field (msg->field (:message-type %)))
     (parser/create-field (:message-control-id %))
     (parser/create-field (util/trim-nils (pt->field (:message-processing-id %))))
     (parser/create-field (util/trim-nils (vid->field (:message-version-id %))))
     (parser/create-field (:sequence-number %))
     (parser/create-field (:continuation-pointer %))
     (parser/create-field (lookups/acknowledgement-type-by-key (:accept-acknowledgement-type %)))
     (parser/create-field (lookups/acknowledgement-type-by-key (:application-acknowledgement-type %)))
     (parser/create-field (:country-code %))
     (parser/create-field (:character-set %))
     (parser/create-field (util/trim-nils (segment/ce->field (:principal-language %))))
     (parser/create-field (:alternate-character-set %))
     (parser/create-field (util/trim-nils (segment/ei->field (:message-profile-indicator %))))
     (parser/create-field (:sending-responsible-organization %))
     (parser/create-field (:receiving-responsible-organization %))
     (parser/create-field (util/trim-nils (segment/hd->field (:sending-network-address %))))
     (parser/create-field (util/trim-nils (segment/hd->field (:receiving-network-address %)))))
   record))

(defn hl7->record
  "Accepts an MSH segment of parsed HL7 data and retirns a record."
  [segment & [delimiters]]
  (message/hl7-to-record
   SEGMENT-ID 26
   #(map->record
     {:delimiters (or delimiters default-delimiters)
      :sending-application (segment/field->hd (message/get-segment-field % 3))
      :sending-facility (segment/field->hd (message/get-segment-field % 4))
      :receiving-application (segment/field->hd (message/get-segment-field % 5))
      :receiving-facility (segment/field->hd (message/get-segment-field % 6))
      :message-created (segment/field->ts (message/get-segment-field % 7))
      :security (first (message/get-segment-field segment 8))
      :message-type (field->msg (message/get-segment-field % 9))
      :message-control-id (first (message/get-segment-field % 10))
      :message-processing-id (field->pt (message/get-segment-field % 11))
      :message-version-id (field->vid (message/get-segment-field % 12))
      :sequence-number (util/read-string (first (message/get-segment-field % 13)))
      :continuation-pointer (first (message/get-segment-field % 14))
      :accept-acknowledgement-type (lookups/acknowledgement-type-by-value
                                    (first (message/get-segment-field % 15)))
      :application-acknowledgement-type (lookups/acknowledgement-type-by-value
                                         (first (message/get-segment-field % 16)))
      :country-code (first (message/get-segment-field % 17))
      :character-set (message/get-segment-field % 18)
      :principal-language (segment/field->ce (message/get-segment-field % 19))
      :alternate-character-set (first (message/get-segment-field % 20))
      :message-profile-indicator (segment/field->ei (message/get-segment-field % 21))
      :sending-responsible-organization (first (message/get-segment-field % 22))
      :receiving-responsible-organization (first (message/get-segment-field % 23))
      :sending-network-address (segment/field->hd (message/get-segment-field % 24))
      :receiving-network-address (segment/field->hd (message/get-segment-field % 25))})
   segment))

(defn create
  "Creates a new record using the provided map of segment data."
  [data-map]
  (map->record
   (merge {:id SEGMENT-ID}
          data-map)))
