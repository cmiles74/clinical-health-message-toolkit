(ns cmiles74
  (:require
   [com.nervestaple.clinical.message-intermediate.interface :as message]))

(def test-message
  "MSH|^~\\&|ImmTrac24.16|TEXIIS||BURL6343|20210415044526-0500||ACK^V04^ACK|7008167375|P|2.5.1|||NE|NE|||||Z23^CDCPHINVS|TEXIIS|BURL6343\rMSA|AE|7008167375\rERR||NK1^^0|101^Required field missing^HL70357|W|4^Invalid value^HL70533|||IEE-519::Warning. NK1 Segment/Responsible person, missing.")

