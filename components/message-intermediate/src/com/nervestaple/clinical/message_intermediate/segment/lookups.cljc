(ns com.nervestaple.clinical.message-intermediate.segment.lookups
  (:require
   [clojure.string :as str]))

(defn value-for-key
  "Accepts a sequence of value maps (in the format {:keys [...] :code \"XXX\"})
  and returns the :code value for the map that contains the provided key-string
  in under its :key value. If there are no matches, the :code value for the map
  with default-key in its :key value is returned. If there are no matches and
  there is no default key, the key string is returned."
  [value-seq default-key key-string]
  (cond
    key-string
    (let [match-fn #(= (str/lower-case key-string) (str/lower-case %))
          match (first (filter #(some match-fn (:keys %)) value-seq))]
      (if match (:code match)
          (if key-string key-string
              (value-for-key value-seq nil default-key))))

    default-key (value-for-key value-seq nil default-key)))

(defn key-by-value
  "Accepts a sequence of value maps (in the format {:keys [...] :code \"XXX\"})
  and returns the first key value for the map that contains the provided value
  under its :code value. If there are no matches, the first key value for the
  map with default-value in its :key value is returned If there are no matches
  and there is no default-value, the value is returned.

  Note that the value could be a string or a vector of strings: if a string is
  supplied and the value under the :code key is a vector then the string must
  match the first item in that vector; if a vector is supplied it must match the
  entire value under the :code key."
  [value-seq default-value value]
  (cond
    (vector? value)
    (let [match (first (filter #(= value (:code %)) value-seq))]
      (if match (first (:keys match))
          (first (:keys (last value-seq)))))

    (and (nil? value) (nil? default-value))
    nil

    (nil? value)
    (when default-value
      (key-by-value value-seq nil default-value))

    :else
    (let [match-fn #(= (str/lower-case value) (str/lower-case %))
          match (first (filter #(match-fn
                                 (if (vector? (:code %)) (first (:code %))
                                     (:code %)))
                               value-seq))]
      (if match
        (first (:keys match))
        (if value value
          (key-by-value value-seq nil default-value))))))

(def address-type-data
  "Sequence of address type keys (where the first key represents the \"official\"
  or most widely used key) and the matching code value."
  [{:keys ["Business" "Firm" ::address-business] :code "B"}
   {:keys ["Bad Address" "Bad" ::address-bad] :code "BA"}
   {:keys ["Birth Delivery Location" "Birth Location" :address-birth-location] :code "BDL"}
   {:keys ["Residence at Birth" "Birth" ::address-birth-residence] :code "BR"}
   {:keys ["Current" "temporary" ::address-temporary] :code "C"}
   {:keys ["Country of Origin" "Origin" ::address-origin] :code "F"}
   {:keys ["Home" "Residence" ::address-residence] :code "H"}
   {:keys ["Legal Address" "Legal" ::address-legal] :code "L"}
   {:keys ["Mailing Address" "Mailing" ::address-mailing] :code "M"}
   {:keys ["Birth" "Birth Address" ::address-birth] :code "N"}
   {:keys ["Office" ::address-office] :code "O"}
   {:keys ["Permanent" ::address-permanent]:code "P"}
   {:keys ["Registry Home" ::address-registry-home] :code "RH"}])

(defn address-by-key
  "Returns the matching address type code for the provided key or nil if there are
  no matches."
  [key-string]
  (value-for-key address-type-data nil key-string))

(defn address-by-value
  "Returns the matching address value for the provided code or the value for
  \"C\" code (current) if there are no matches."
  [key-string]
  (key-by-value address-type-data "C" key-string))

(def marital-status-data
  "Sequence of martial type keys (where the first key represents the \"official\"
  value) and the matching code value."
  [{:keys ["Separated" ::marital-status-separated] :code "A"}
   {:keys ["Divorced" ::martial-status-divorced] :code "D"}
   {:keys ["Married" ::martial-status-married] :code "M"}
   {:keys ["Widowed" ::martial-status-widowed] :code "W"}
   {:keys ["Living Together" "Cohabitating" ::marital-status-cohabitating] :code "G"}
   {:keys ["Domestic Partner" "Partner" ::marital-status-domestic-partner] :code "P"}
   {:keys ["Unknown" ::martial-status-unknown] :code "U"}])

(defn marital-status-by-key
  "Returns the matching code value for the provided textual marital status or the
  code for \"Unknown\" of the value has no matches."
  [key-string]
  (value-for-key marital-status-data "Unknown" key-string))

(defn marital-status-by-value
  "Returns the matching textual string for the provided marital status code or
  the textual value for \"U\" if no matches are found."
  [key-string]
  (key-by-value marital-status-data "U" key-string))

(def ethnicity-data
  "Sequence of ethnicity types (where the first key represents the \"official\"
  value) and the matching code value."
  [{:keys ["Hispanic" ::ethnicity-hispanic] :code "H"}
   {:keys ["Not Hispanic or Latino" "Not Hispanic" "Not Latino"
           ::ethnicity-not-hispanic] :code "N"}
   {:keys ["Unknown" ::ethnicity-unknown] :code "U"}])

(defn ethnicity-by-key
  "Returns the matching code value for the provided textual ethnicity or the code
  for \"Unknown\" of the value has no matches."
  [key-string]
  (value-for-key ethnicity-data nil key-string))

(defn ethnicity-by-value
  "Returns the matching textual string for the provided ethnicity code or the
  textual value for \"U\" if no matches are found."
  [key-string]
  (key-by-value ethnicity-data nil key-string))

(def yes-no-data
  "Sequence of yes or no type keys (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Yes" "True" "Postive"  ::yes] :code "Y"}
   {:keys ["No" "False" "Negative" ::no]  :code "N"}])

(defn yes-no-by-key
  "Returns the matching code value for the provided textual yes/no value or the
  code for \"No\" of the value has no matches."
  [key-string]
  (value-for-key yes-no-data nil key-string))

(defn yes-no-by-value
  "Returns the matching textual string for the provided yes/no code or the textual
  value for \"N\" if no matches are found."
  [key-string]
  (key-by-value yes-no-data nil key-string))

(def patient-class-data
  "Sequence of patient class types (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Obstetrics" ::patient-class-obstetrics] :code "B"}
   {:keys ["Commercial Account" "Commercial" ::patient-class-commercial] :code "C"}
   {:keys ["Emergency" ::patient-class-emergency] :code "E"}
   {:keys ["Inpatient" ::patient-class-inpatient] :code "I"}
   {:keys ["Not Applicable" "NA" ::patient-class-na] :code "N"}
   {:keys ["Outpatient" ::patient-class-outpatient] :code "O"}
   {:keys ["Preadmit" ::patient-class-preadmt] :code "P"}
   {:keys ["Recurring Patient" "Recurring" ::patient-class-recurring] :code "R"}
   {:keys ["Unknown" ::patient-class-unknown] :code "U"}])

(defn patient-class-by-key
  "Returns the matching code value for the provided textual patient class value or
  the code for \"Unknown\" of the value has no matches."
  [key-string]
  (value-for-key patient-class-data "Unknown" key-string))

(defn patient-class-by-value
  "Returns the matching textual string for the provided patient class code or the
  textual value for \"U\" if no matches are found."
  [key-string]
  (key-by-value patient-class-data "U" key-string))

(def gender-data
  "Sequence of gender types (where the first key represents the \"official\"
  value) and the matching code value."
  [{:keys ["Ambiguous" ::gender-ambiguous] :code "A"}
   {:keys ["Female" ::gender-female] :code "F"}
   {:keys ["Male" ::gender-male] :code "M"}
   {:keys ["Not Applicable" "NA" ::gender-na] :code "N"}
   {:keys ["Other" ::gender-other] :code "O"}
   {:keys ["Unknown" ::gender-unknown] :code "U"}])

(defn gender-by-key
  "Returns the matching code value for the provided textual gender value or the
  code for \"Unknown\" of the value has no matches."
  [key-string]
  (value-for-key gender-data nil key-string))

(defn gender-by-value
  "Returns the matching textual string for the provided gender code or the
  textual value for \"U\" if no matches are found."
  [key-string]
  (key-by-value gender-data nil key-string))

(def race-data
  "Sequence of race types (where the first key represents the \"official\" value)
  and the matching vector of code values. Note that the code values here are not
  authoritative, they represent the values that appear to be the most popular."
  [{:keys ["African American" "Black" ::race-black]
    :code ["2054-5" "Black or African American" "HL70005"]}
   {:keys ["American Indian" "Native" ::race-american-indian]
    :code ["1002-5" "American Indian or Alaska Native" "HL70005"]}
   {:keys ["Alaskan Native" ::race-alaskan-native]
    :code ["1002-5" "American Indian or Alaska Native" "HL70005"]}
   {:keys ["Hawaiian" "Native Hawaiian" "Pacific Islander" ::race-hawaiian]
    :code ["2076-8" "Native Hawaiian or Other Pacific Islander" "HL70005"]}
   {:keys ["Hispanic" ::race-hispance] :code ["213502" "Hispanic" "CDCREC"]}
   {:keys ["White" "Caucasian" ::race-white]
    :code ["2106-3" "White" "HL70005"]}
   {:keys ["Other" ::race-other]
    :code ["2131-1" "Other Race" "HL70005"]}])

(defn race-by-key
  "Returns the matching vector with the code value for the provided textual race
  value or the vector with the code value for \"Other\" of the value has no
  matches."
  [key-string]
  (value-for-key race-data ["2131-1" "Other Race" "HL70005"] key-string))

(defn race-by-value
  "Returns the matching textual string for the provided vector of race code data
  or the vector with the code value for \"Other\" if no matches are found."
  [key-vector]
  (key-by-value race-data "Other" key-vector))

(def xtn-use-code-data
  "Sequence of telecommunication use type (wher the first key represents the
  \"official\" value) and the matching string or vector of code values."
  [{:keys ["Answering Service" ::xtn-use-primary] :code "ASN"}
   {:keys ["Beeper" ::xtn-use-beeper] :code "BPN"}
   {:keys ["Emergency" ::xtn-use-emergency] :code "EMR"}
   {:keys ["Email" "Network" ::xtn-use-email] :code "NET"}
   {:keys ["Other" "Other Residence" ::xtn-use-other] :code "ORN"}
   {:keys ["Primary" "Primary Residence" ::xtn-use-primary] :code "PRN"}
   {:keys ["Vacation" "Vacation Residence" "Vacation Home" ::xtn-use-vacation] :code "VHN"}
   {:keys ["Work" "Business" ::xtn-use-work] :code "WPN"}])

(defn xtn-use-by-key
  "Returns the matching vector with the code value for the provided textual XTN
  use code value or the vector with the code value for \"Other\" if the value
  has no matches."
  [key-string]
  (value-for-key xtn-use-code-data "Other" key-string))

(defn xtn-use-by-value
  "Returns the matching textual string for the provided XTN use code or the code value for \"Other\"
  if no matches are found."
  [key-vector]
  (key-by-value xtn-use-code-data "ORN" key-vector))

(def xtn-equipment-type-data
  "Sequence of telecommunication equipement type (where the first key represents
  the \"official\" value) and the matching string or vector of code values."
  [{:keys ["Beeper" ::xtn-equip-beeper] :code "BP"}
   {:keys ["Cell" "Cellular Phone" "Cell Phone" "Mobile"
           "Mobile Phone" ::xtn-equip-mobile] :code "CP"}
   {:keys ["Fax" ::xtn-equip-fax] :code "FX"}
   {:keys ["Internet" "Network" ::xtn-equip-network] :code "Internet"}  ;; Use only if use code is "NET"
   {:keys ["Modem"::xtn-equip-modem] :code "MD"}
   {:keys ["Telephone" "Phone" ::xtn-equip-telephone] :code "PH"}
   {:keys ["X.400" "X.400 Address" "X.400 Email" ::xtn-equip-x400] :code "X.400"}])

(defn xtn-equip-by-key
  "Returns the matching vector with the code value for the provided textual XTN
  equipment type code value or nil if the value has no matches."
  [key-string]
  (value-for-key xtn-equipment-type-data nil key-string))

(defn xtn-equip-by-value
  "Returns the matching textual string for the provided XTN use code or nil if no
  matches are found."
  [key-vector]
  (key-by-value xtn-equipment-type-data nil key-vector))

(def processing-id-data
  "Sequence of processing identifier data (where the first key represents the
  \"official\" value) and the matching code values."
  [{:keys ["Debug" ::process-debug] :code "D"}
   {:keys ["Test" ::process-test] :code "T"}
   {:keys ["Production" ::process-production] :code "P"}])

(defn processing-id-by-key
  "Returns the matching code value for the provided textual processing ID value or
  nil if the value has no matches."
  [key-string]
  (value-for-key processing-id-data nil key-string))

(defn processing-id-by-value
  "Returns the matching textual string for the provided processing ID or nil if no
  matches are found."
  [key-vector]
  (key-by-value processing-id-data nil key-vector))

(def processing-mode-data
  "Sequence of processing mode data (where the first key represents the
  \"official\" value) and the matching code values."
  [{:keys ["Archive" ::process-mode-archive] :code "A"}
   {:keys ["Initial Load" "Initial" ::process-mode-initial-load] :code "I"}
   {:keys ["Restore" ::process-mode-restore] :code "R"}
   {:keys ["Current" "Transmitted" ::process-mode-current] :code "T"}])

(defn processing-mode-by-key
  "Returns the matching code value for the provided textual processing mode value
  or nil if the value has no matches."
  [key-string]
  (value-for-key processing-mode-data nil key-string))

(defn processing-mode-by-value
  "Returns the matching textual string for the provided processing mode or nil if
  no matches are found."
  [key-vector]
  (key-by-value processing-mode-data nil key-vector))

(def acknowledgement-type-data
  "Sequence of acknowledment type data (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Always" ::ack-type-always] :code "AL"}
   {:keys ["Error" ::ack-type-error] :code "ER"}
   {:keys ["Never" ::ack-type-never] :code "NE"}
   {:keys ["Success" ::ack-type-success] :code "SU"}])

(defn acknowledgement-type-by-key
  "Returns the matching code value for the provided textual acknowledment type or
  nil if the value has no matches."
  [key-string]
  (value-for-key acknowledgement-type-data nil key-string))

(defn acknowledgement-type-by-value
  "Returns the matching textual string for the provided acknowledgement type or
  nil if no matches are found."
  [key-vector]
  (key-by-value acknowledgement-type-data nil key-vector))

(def acknowledgement-code-data
  "Sequence of acknowledgement types (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Application Accept" ::ack-app-accept] :code "AA"}
   {:keys ["Application Error" ::ack-app-error] :code "AE"}
   {:keys ["Application Reject" ::ack-app-reject] :code "AR"}
   {:keys ["Commit Accept" ::ack-commit-accept] :code "CA"}
   {:keys ["Commit Error" ::ack-commit-error] :code "CE"}
   {:keys ["Commit Reject" ::ack-commit-reject] :code "CR"}])

(defn acknowledgement-code-by-key
  "Returns the matching code value for the provided textual acknowledment code or
  nil if the value has no matches."
  [key-string]
  (value-for-key acknowledgement-code-data nil key-string))

(defn acknowledgement-code-by-value
  "Returns the matching textual string for the provided acknowledgement code or
  nil if no matches are found."
  [key-vector]
  (key-by-value acknowledgement-code-data nil key-vector))

(def acknowledgement-error-code-data
  "Sequence of acknowledgement error codes (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["Error" ::ack-error] :code "E"}
   {:keys ["Fatal" ::ack-fatal] :code "F"}
   {:keys ["Info" ::ack-info] :code "I"}
   {:keys ["Warning" ::ack-warning] :code "W"}])

(defn acknowledgement-error-code-by-key
  "Returns the matching code value for the provided textual acknowledment error
  code or nil if the value has no matches."
  [key-string]
  (value-for-key acknowledgement-error-code-data nil key-string))

(defn acknowledgement-error-code-by-value
  "Returns the matching textual string for the provided acknowledgement error code
  or nil if no matches are found."
  [key-vector]
  (key-by-value acknowledgement-error-code-data nil key-vector))

(def message-types
  "Map of MSH message types"
  {
   :vaccination ["VXU" "V04" "VXU_V04"]  ; Unsolicited Vaccination Record Update
   })

(defn convert-gender
  "Converts a textual gender value to the matching HL7 code"
  [gender]
  (gender-by-value gender))

(defn convert-race
  "Converts a textual race value to the matching HL7 values"
  [race]
  (race-by-value race))

(def rxa-action-code-data
  "Sequence of RXA action code values (where the first key represents the
  \"official\" value) and the matching code values."
  [{:keys ["Add" "Insert" "Add/Insert" ::rxa-add] :code "A"}
   {:keys ["Delete" ::rxa-delete] :code "D"}
   {:keys ["Update" ::rxa-update] :code "U"}
   {:keys ["No change" ::rxa-no-change] :code "X"}])

(defn rxa-action-code-by-key
  "Returns the matching code value for the provided textual RXA value or nil if
  the value has no matches."
  [key-string]
  (value-for-key rxa-action-code-data nil key-string))

(defn rxa-action-code-by-value
  "Returns the matching textual string for the provided RXA value or nil if no
  matches are found."
  [key-vector]
  (key-by-value rxa-action-code-data nil key-vector))

(def rxa-completion-status-data
  "Sequence of RXA completion status values (where the first key represents the
  \"official\" value) and the matching code values."
  [{:keys ["Complete" ::rxa-completion-complete] :code "CP"}
   {:keys ["Not Administered" ::rxa-completion-not-administered] :code "NA"}
   {:keys ["Partially Administered" ::rxa-completion-partial] :code "PA"}
   {:keys ["Refused" ::rxa-completion-refused] :code "RE"}])

(defn rxa-completion-status-by-key
  "Returns the matching code value for the provided textual RXA completion status
  or nil if the value has no matches."
  [key-string]
  (value-for-key rxa-completion-status-data nil key-string))

(defn rxa-completion-status-by-value
  "Returns the matching textual string for the provided RXA completion status or
  nil if no matches are found."
  [key-vector]
  (key-by-value rxa-completion-status-data nil key-vector))

(def rxa-pharmacy-order-type-data
  "Sequence of RXA pharmacy order type values (where the first key represents the
  \"official\" value) and the matching code values."
  [{:keys ["Medication" ::rxa-order-type-medication] :code "M"}
   {:keys ["Other solution" ::rxa-order-type-other] :code "O"}
   {:keys ["IV large volume solutions" ::rxa-order-type-large-iv] :code "S"}])

(defn rxa-pharmacy-order-type-by-key
  "Returns the matching code value for the provided textual RXA pharmacy order
  type code or nil if the value has no matches."
  [key-string]
  (value-for-key rxa-pharmacy-order-type-data nil key-string))

(defn rxa-pharmacy-order-type-by-value
  "Returns the matching textual string for the provided RXA pharmacy order type
  or nil if no matches are found."
  [key-vector]
  (key-by-value rxa-pharmacy-order-type-data nil key-vector))

(def trigger-event-data
  "Sequence of trigger event values (where the first key represents the
  \"official\" value) and the matching code value."
  [{:keys ["General laboratory order response" "ORL" ::event-general-lab-order] :code "022"}
   {:keys ["Admit" ::event-admit] :code "A01"}
   {:keys ["Transfer" ::event-transfer] :code "A02"}
   {:keys ["Discharge" ::event-discharge] :code "A03"}
   {:keys ["Register" ::event-register] :code "A04"}
   {:keys ["Pre-Admit" ::event-pre-admit] :code "A05"}
   {:keys ["Outpatient to Inpatient" ::event-outpatient->inpatient] :code "A06"}
   {:keys ["Inpatient to Outpatient" ::event-inpatient->outpatient] :code "A07"}
   {:keys ["Update" ::event-update] :code "A08"}
   {:keys ["Departing" ::event-departing] :code "A09"}
   {:keys ["Arriving" ::event-arriving] :code "A10"}
   {:keys ["Cancel Admit" :event-cancel-admit] :code "A11"}
   {:keys ["Cancel Transfer" ::event-cancel-transfer] :code "A12"}
   {:keys ["Cancel Discharge" ::event-cancel-discharge] :code "A13"}
   {:keys ["Pending Admit" ::event-pending-admit] :code "A14"}
   {:keys ["Pending Transfer" ::event-pending-transfer] :code "A15"}
   {:keys ["Pending Discharge" ::event-pending-discharge] :code "A16"}
   {:keys ["Swap Patients" ::event-swap-patients] :code "A17"}
   {:keys ["Merge Patients" ::event-merge-patients] :code "A18"}
   {:keys ["Patient Query" ::event-patient-query] :code "A19"}
   {:keys ["Bed Status Update" ::event-bed-status-update] :code "A20"}
   {:keys ["Query for Vaccination Record" ::event-query-vaccination] :code "V01"}
   {:keys ["Response to Vaccination Query, Multiple PID matches"
           ::event-response-vaccination-multiple-pids] :code "V02"}
   {:keys ["Vaccination Record Response" ::event-vaccination-response] :code "V03"}
   {:keys ["Unsolicited Vaccination Record Update"
           ::event-vaccination-unsolicited-update] :code "V04"}])

(defn string->trigger-event
  "Returns the matching code value for the provided trigger event textual string
  or nil if the provided value has no matches."
  [key-string]
  (value-for-key trigger-event-data nil key-string))

(defn trigger-event->string
  "Returns the matching textual string for the provided trigger event code or nil
  if the provided value has no matches."
  [key-vector]
  (key-by-value trigger-event-data nil key-vector))

(def message-structure-data
  "Sequence of message structure values (where the first key represents the
  \"official\" value) and their matching code values."
  [{:keys ["Acknowledgement" "Ack" ::message-structure-ack] :code "ACK"}
   {:keys ["A19" ::message-structure-a19] :code "ADR_A19"}
   {:keys ["A01" "A04" "A08" "A13" ::message-structure-A01] :code "ADT_A01"}
   {:keys ["A02" ::message-structure-a02] :code "ADT_A02"}
   {:keys ["A03" ::message-structure-a03] :code "ADT_A03"}
   {:keys ["A05" "A14" "A28" "A31" ::message-structure-a05] :code "ADT_A05"}
   {:keys ["A06" "A07" ::message-structure-a06] :code "ADT_A06"}
   {:keys ["A09" "A10" "A11" "A12" ::message-structure-a09] :code "ADT_A09"}
   {:keys ["A15" ::message-structure-a15] :code "ADT_A15"}
   {:keys ["A16" ::message-structure-a16] :code "ADT_A16"}
   {:keys ["A17" ::message-structure-a17] :code "ADT_A17"}
   {:keys ["A18" ::message-structure-a18] :code "ADT_A18"}
   {:keys ["A20" ::message-structure-a20] :code "ADT_A20"}
   {:keys ["V01" ::message-structure-v01] :code "VXQ-V01"}
   {:keys ["V02" ::message-structure-v02] :code "VXX_V02"}
   {:keys ["V03" ::message-structure-v03] :code "VXR_V03"}
   {:keys ["V04" ::message-structure-v04] :code "VXU_V04"}])

(defn string->message-structure
  "Returns the matching code value for the provided message structure textual
  string or nil if the provided value has no matches."
  [key-string]
  (value-for-key message-structure-data nil key-string))

(defn message-structure->string
  "Returns the matching textual string for the provided message structure code or
  nil if the provided value has no matches."
  [key-vector]
  (key-by-value message-structure-data nil key-vector))

