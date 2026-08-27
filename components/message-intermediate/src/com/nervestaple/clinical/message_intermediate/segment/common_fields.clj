(ns com.nervestaple.clinical.message-intermediate.segment.common-fields
  (:require
   [clojure.spec.alpha :as s]
   [com.nervestaple.hl7-parser.parser :as parser]
   [com.nervestaple.clinical.message-intermediate.message.utility :as message]
   [com.nervestaple.clinical.message-intermediate.segment.common-fields :as common]
   [com.nervestaple.clinical.message-intermediate.segment.main :as segment]
   [com.nervestaple.clinical.message-intermediate.segment.utility :as util]
   [clojure.edn :as edn]))

(s/def ::set-id (s/nilable pos-int?))
(s/def ::publicity-code (s/nilable ::segment/spec-cwe))

