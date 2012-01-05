(ns custer.io
  (:import (java.io BufferedReader StringReader PrintWriter InputStreamReader))) 

(defn read-to-empty-line [#^BufferedReader reader]
  (let [result (.readLine reader)]
    (if (= result "")
      nil   
      (lazy-seq (cons result (read-to-empty-line reader))))))

(defn read-str [ins]
  (let [reader (BufferedReader. (InputStreamReader. ins))]
    (apply str (read-to-empty-line reader))))

(defn write-message [os message]
  (let [writer (PrintWriter. os)]
    (.write writer message)
    (.flush writer)))

