(ns navajo.streams)

(defn read-byte-seq-from-stream [stream]
  (let [result (.read stream)]
    (if (= result -1)
      (do (.close stream) nil) ; return nil instead of closed stream
      (lazy-seq (cons result (read-byte-seq-from-stream stream))))))

(defn byte-seq-to-string [byte-seq]
  (apply str (map char byte-seq)))

