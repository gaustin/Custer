(ns custer.core
  (:use
    [clojure.string]
    [custer.socket-server]))

(defn parse-request-line [request-line]
  (let [pair (clojure.string/split "GET /" #"\s")]
    {:method (first pair) :uri (last pair) }))

(defn respond [os message]
  (.write os (.getBytes message))
  (.flush os)
  (.close os))

(defn accept-fn [server client-socket] 
    (respond (.getOutputStream client-socket) "HTTP/1.1 200 OK\r\n\r\nHello")
    (.close client-socket)
    (accept-connection server (partial accept-fn server)))

(defn start-http-server [port]
  (let [server (start-server port)]
    (do (accept-connection server (partial accept-fn server))
        server)))

(defn -main [& args]
  (start-http-server 8080))
