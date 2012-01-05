(ns custer.core
  (:import (java.net ServerSocket SocketException))
  (:use [custer.io :only (read-str write-message)]
        [clojure.string :only (split)]))

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

(defn accept-fn [server client-socket]
  (let [ins (.getInputStream client-socket)
        outs (.getOutputStream client-socket)]
    (read-str ins)
    (write-message outs "HTTP/1.1 200 OK\r\n\r\nHello\r\n\r\n"))
    (close-socket client-socket))

(defn accept-connection [server fun]
  (let [socket (.accept server)]
    (future 
      (try 
        (fun socket)
        (catch SocketException e (println (.getMessage e))))))
  (recur server (partial accept-fn server)))

(defn -main [& args]
  (let [server (start-server 8282)] 
    (future (accept-connection server (partial accept-fn server)))
    server))
