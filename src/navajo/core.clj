(ns navajo.core
  (:use
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
      (fn [socket] (respond (.getOutputStream socket) "200 OK\r\nHello")
        (.close socket)))
    server)))

(defn -main [& args]
  (start-http-server 8080))
