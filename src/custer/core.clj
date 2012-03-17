(ns custer.core
  (:import (java.net ServerSocket SocketException))
  (:use [custer.io :only (read-str read-to-empty-line write-message)]
        [custer.request_parsing]
        [clojure.java.io :only (reader writer)]))

(defn start-server [port]
  (ServerSocket. port))

(defn close-socket [socket]
  (doto socket
    (.shutdownInput)
    (.shutdownOutput) 
    (.close)))

(defn accept-fn [reader writer]
    (let [raw-request (read-to-empty-line reader)
          request (parse-request raw-request)]
      (write-message writer (.concat
                              (print-to-s request)
                              "\r\n\r\n" ))))

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
  (let [port (if (not (nil? args))
                (. Integer parseInt (first args))
                8080)
        server (start-server port)] 
    (future (accept-connection server accept-fn))
    server))
