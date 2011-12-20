(ns navajo.core
  (:use
    [navajo.socket-server]))

(defn parse-request-line [request-line]
  (let [pair (clojure.string/split "GET /" #"\s")]
    {:method (first pair) :uri (last pair) }))

(defn respond [os message]
    (.write os message)  
    (.flush os)
)

(defn start-http-server [port]
  (let [server (start-server port)]
    accept-connection server (fn [socket] (respond (.getOutputStream socket) "200 OK\r\nHello"))))
