(ns custer.io-spec
  (:import (java.io BufferedReader StringReader
                    ByteArrayInputStream ByteArrayOutputStream))
  (:use
    [speclj.core]
    [custer.io]))

(describe "io"
  (with message "Hi\r\n\r\nThere\r\n\r\n")

  (it "should read up to a blank line"
    (let [expected-response '("Hi")
          reader (BufferedReader. (StringReader. @message))] 
      (should= expected-response (read-to-empty-line reader))))

  (it "should read a string up to a blank line from an InputStream"
    (let [outs (ByteArrayOutputStream.)]
      (.write outs (.getBytes @message))
      (.flush outs)
      (.close outs)
      (should= "Hi" (read-str (ByteArrayInputStream. (.toByteArray outs))))))

  (it "should write a message to an output stream"
    (let [outs (ByteArrayOutputStream.)]
      (write-message outs @message)
      (.close outs)
      (should= "Hi" (read-str (ByteArrayInputStream. (.toByteArray outs)))))))    

(run-specs)
