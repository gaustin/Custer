(ns custer.core
  (:require [clojure.string])
  (:use [custer.streams]))

(defn start-server [port]
  (new java.net.ServerSocket port))

(defn parse-request-line [request-line]
  (let [pair (clojure.string/split "GET /" #"\s")]
    {:method (first pair) :uri (last pair) }))

(defn respond [os message]
  (.write os (.getBytes message))
  (.flush os))

(defn accept-fn [server client-socket]
    ;(byte-seq-to-string (read-byte-seq-from-stream (.getInputStream client-socket)))
    (respond (.getOutputStream client-socket) "HTTP/1.1 200 OK\r\n\r\nHello")
    (doto client-socket
      (.shutdownInput)
      (.shutdownOutput) 
      (.close)))

(defn accept-connection [server fun]
  (let [socket (.accept server)]
    (future 
      (try 
        (fun socket)
        (catch java.net.SocketException e (println (.getMessage e))))))
  (recur server (partial accept-fn server)))

(defn -main [& args]
  (let [server (start-server 8282)] 
    (future (accept-connection server (partial accept-fn server)))
    server))
