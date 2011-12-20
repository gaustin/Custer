(ns navajo.steams-spec
  (:use
    [speclj.core]
    [navajo.steams]))

(describe "streams"
  (it "converts a byte sequence to a string"
    (let [msg "Hello"]
      (should= msg (byte-seq-to-string (.getBytes msg)))))

  (it "reads a byte sequence from a stream"
    (let [msg "Hello"
          os (java.io.ByteArrayOutputStream.)]
      (.write os (.getBytes msg))
      (.flush os)
      (.close os)
      (should= 
        (seq (.getBytes msg)) 
        (read-byte-seq-from-stream (java.io.ByteArrayInputStream. (.toByteArray os)))))))

(run-specs)
