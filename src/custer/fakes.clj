(ns custer.fakes
  (:import (java.io PrintWriter BufferedReader
                      BufferedWriter InputStream
                      ByteArrayInputStream ByteArrayOutputStream)
             (java.net Socket))
  (:use
    [custer.io :exclude (read-to-empty-line)]
    [clojure.java.io :only (reader writer)]))

(defn fake-reader
  [reader-read]
  (let [first-time (atom true)]
    (proxy
      [BufferedReader] [(reader (ByteArrayInputStream. (.toByteArray (ByteArrayOutputStream.))))]
      (readLine []
        (swap! reader-read (fn [a] true))
          (if (= true @first-time)
            (do (swap! first-time (fn [a] false)) "GET / HTTP/1.1")
            "")))))

(defn fake-outs
  [writer-written-to]
  (proxy [BufferedWriter] [(writer (ByteArrayOutputStream.))]
    (write [message] (swap! writer-written-to (fn [a] true)))))

(defn fake-client [server]
  (let [socket (java.net.Socket. (.getInetAddress server) (.getLocalPort server))
        outs (.getOutputStream socket)]
    (write-message (PrintWriter. outs)
      (apply str '("GET / HTTP/1.0\r\n"
        "Host: www.example.com\r\n"
        "User-Agent: 007\r\n"
        "Accept: text/html"
        "\r\n\r\n"
        "Body"
        "\r\n\r\n")))
    socket))

(defn read-from-socket-ins [socket]
  (read-str (reader (.getInputStream socket))))

(defn fake-client-socket
  [server shutdown-in-called shutdown-out-called close-called]
  (let [set-true (fn [a] true)]
    (proxy [Socket] [(.getInetAddress @server) (.getLocalPort @server)]
      (shutdownInput [] (swap! shutdown-in-called set-true))
      (shutdownOutput [] (swap! shutdown-out-called set-true))
      (close [] (swap! close-called set-true)))))
