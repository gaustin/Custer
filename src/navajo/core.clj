(ns navajo.core
  (:use
    [clojure.string]
    [navajo.socket-server]))

(defn parse-request-line [request-line]
  (let [pair (clojure.string/split "GET /" #"\s")]
    {:method (first pair) :uri (last pair) }))

(defn respond [os message]
  (.write os (.getBytes message))
  (.flush os)
  (.close os))

(defn start-http-server [port]
  (let [server (start-server port)]
    (do (accept-connection server 
      (fn [socket] (respond (.getOutputStream socket) "HTTP/1.1 200 OK\r\n\r\nHello")
        (.close socket)))
    server)))

(defn -main [& args]
  (start-http-server 8080))
