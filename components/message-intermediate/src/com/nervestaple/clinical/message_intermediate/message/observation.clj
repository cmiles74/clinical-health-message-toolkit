(ns com.nervestaple.clinical.message-intermediate.message.observation
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as hl7-parser]
   [com.nervestaple.hl7-parser.message :as message]
   [luminare.hl7.parser.message.defaults :as defaults]
   [luminare.hl7.parser.segment.msh :as msh]
   [luminare.hl7.parser.segment.nte :as nte]
   [luminare.hl7.parser.segment.obr :as obr]
   [luminare.hl7.parser.segment.pid :as pid]
   [luminare.hl7.parser.segment.pv1 :as pv1]
   [luminare.hl7.parser.segment.obx :as obx]))

;; patient information, visit, observation orders and results
(defrecord result
    [pid pv1 obr obx])

(s/def ::pid ::pid/spec)
(s/def ::pv1 ::pv1/spec)
(s/def ::obr (s/coll-of ::obr/spec))
(s/def ::obx (s/coll-of ::obx/spec))

(s/def ::spec-result
  (s/keys :req-un [::pid ::pv1 ::obr ::obx]))

(defrecord record
    [msh result])

(s/def ::msh ::msh/spec)
(s/def ::result (s/coll-of ::spec-result))

(s/def ::spec
  (s/keys :req-un [::msh ::result]))

(defn record->hl7
  "Accepts an observation record and returns a parsed HL7 message."
  ([record]
   (record->hl7 defaults/hl7-message-delimiters record))
  ([delimiters record]

   ;; accumulate the segment data from the child records
   (loop [segments []
          item-this (first (:result record))
          items (rest (:result record))]

     ;; create segments from the child patient order and result records
     (if item-this
       (recur (-> (conj segments
                        (pid/record->hl7 (:pid item-this))
                        (pv1/record->hl7 (:pv1 item-this)))
                  (into (mapv obr/record->hl7 (:obr item-this)))
                  (into (mapv obx/record->hl7 (:obx item-this)))
                  (into (mapv nte/record->hl7 (:nte item-this))))
              (first items)
              (rest items))

       ;; add our accumulated segments and return the message
       (let [message (hl7-parser/create-message delimiters
                                            (msh/record->hl7 (:msh record)))]
         (assoc message :segments (into (:segments message) segments)))))))

(defn hl7->record
  "Accepts a parsed HL7 message and returns an observation record."
  [parsed-message]

  ;; group our segments by set id
  (let [segments (filter #(not= "MSH" (:id %)) (:segments parsed-message))]
    (loop [groups {}
           segment-this (first segments)
           segments (rest segments)]

      (if segment-this
        (let [set-id (first (message/get-segment-field segment-this 1))
              segment-id (:id segment-this)
              result (if (groups set-id) (groups set-id) {})]
          (recur (assoc groups
                        set-id
                        (cond (= "PID" segment-id)
                              (assoc result :pid (pid/hl7->record segment-this))

                              (= "PV1" segment-id)
                              (assoc result :pv1 (pv1/hl7->record segment-this))

                              (= "OBR" segment-id)
                              (assoc result :obr (conj (:obr-records result)
                                                       (obr/hl7->record segment-this)))

                              (= "OBX" segment-id)
                              (assoc result :obx (conj (:obx-records result)
                                                       (obx/hl7->record segment-this)))

                              (= "NTE" segment-id)
                              (assoc result :nte (conj (:nte-records result)
                                                       (nte/hl7->record segment-this)))))
                 (first segments)
                 (rest segments)))

        (map->record
         {:msh (msh/hl7->record
                (first (message/get-segments parsed-message "MSH"))
                (:delimiters parsed-message))
          :result (mapv #(map->result (last %)) groups)})))))

