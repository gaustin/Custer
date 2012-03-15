(ns custer.core-spec
  (:import (java.io PrintWriter BufferedReader
                    BufferedWriter InputStream
                    ByteArrayInputStream ByteArrayOutputStream)
           (java.net Socket)) 
  (:use 
    [speclj.core]
    [clojure.string :only (trim)]
    [custer.core]
    [custer.io]
    [custer.request_parsing]
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

(describe "core"
  (with server (start-server 8181))
  (with expected-response
    (trim (print-to-s
      (parse-request
        '("GET / HTTP/1.0"
          "Host: www.example.com"
          "User-Agent: 007"
          "Accept: text/html")))))
  (after (.close @server))

  (it "should start the server"
    (should-not (.isClosed @server)))

  (it "should have the specified port"
    (should= 8181 (.getLocalPort @server)))
  
  (it "should be bound"
    (should (.isBound @server)))
    
  (it "should be bound to localhost"
    (should= "0.0.0.0" (.getCanonicalHostName (.getInetAddress @server))))                                           
  (it "accepts a connection"
    (let [counter (atom 0)
          action (fn [ins outs] (swap! counter inc))
          server (atom @server)
          action-future (future (accept-connection @server action))]
      (fake-client @server)
      (. Thread (sleep 1000)) ; action-future never returns
      (should= 1 @counter)))
 
  (it "sends the client a message"
    (let [message "Hello, World!\r\n\r\n"
          action (fn [reader writer]
            (write-message writer message))
          server (atom @server)
          accept-future (future (accept-connection @server action))
          client (fake-client @server)]
          (should= "Hello, World!" (read-from-socket-ins client))
          (.close client)))

  (it "should shutdown io and close the socket"
    (let [server (atom @server)
          shutdown-in-called (atom false)
          shutdown-out-called (atom false)
          close-called (atom false)
          socket (fake-client-socket
                    server shutdown-in-called shutdown-out-called close-called)]
      (close-socket socket) 
      (should= true @shutdown-in-called)
      (should= true @shutdown-out-called)
      (should= true @close-called)))

  (it "accept-fn should read from ins and write to outs"
    (let [reader-read (atom false)
          writer-written-to (atom false)]
      (accept-fn (fake-reader reader-read) (fake-outs writer-written-to))
      (should= true @reader-read)
      (should= true @writer-written-to)))

  (it "should get a response from the server"
    (let [server (atom @server)]
      (future (accept-connection @server accept-fn))
      (should= @expected-response 
        (read-from-socket-ins (fake-client @server)))))

  (it "should get a response from the server on the second request"
    (let [server (atom @server)]
      (future (accept-connection @server accept-fn))
      (read-from-socket-ins (fake-client @server))
      (should= @expected-response 
        (read-from-socket-ins (fake-client @server)))))

  (it "should get a response from the server on the third request"
    (let [server (atom @server)]
      (future (accept-connection @server accept-fn))
      (read-from-socket-ins (fake-client @server))
      (read-from-socket-ins (fake-client @server))
      (should= @expected-response 
        (read-from-socket-ins (fake-client @server))))))

