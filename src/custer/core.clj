(ns custer.core
  (:import (java.net ServerSocket SocketException))
  (:use [custer.io :only (read-str write-message)]
        [clojure.string :only (split)]
        [clojure.java.io :only (reader writer)]))

(defn start-server [port]
  (ServerSocket. port))

(defn parse-request-line [request-line]
  (let [pair (split "GET /" #"\s")]
    {:method (first pair) :uri (last pair) }))

(defn close-socket [socket]
  (doto socket
    (.shutdownInput)
    (.shutdownOutput) 
    (.close)))

(defn accept-fn [reader writer]
    (read-str reader)
    (write-message writer "HTTP/1.1 200 OK\r\n\r\nHello\r\n\r\n"))

(defn accept-connection [server fun]
  (let [socket (.accept server)]
    (future 
      (try 
        (fun (reader (.getInputStream socket))
             (writer (.getOutputStream socket)))
        (close-socket socket)
        (catch SocketException e (println (.getMessage e))))))
  (recur server accept-fn))

(defn -main [& args]
  (let [server (start-server 8282)] 
    (future (accept-connection server accept-fn))
    server))
