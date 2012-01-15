(ns custer.io
  (:import (java.io PrintWriter BufferedReader StringReader InputStreamReader))) 

(defn read-to-empty-line [reader]
  (let [result (.readLine reader)]
    (if (= result "") 
    nil   
    (lazy-seq (cons result (read-to-empty-line reader))))))

(defn read-str [reader]
  (apply str (read-to-empty-line reader)))

(defn write-message [writer message]
  (.write writer message)
  (.flush writer))

