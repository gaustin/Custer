(ns custer.core-spec
  (:import (java.io BufferedReader StringReader PrintWriter InputStreamReader
                    ByteArrayOutputStream ByteArrayInputStream)
           (java.net Socket)) 
  (:use 
    [speclj.core]
    [custer.core]
    [custer.io]))

(defn fake-client [server] 
  (let [socket (java.net.Socket. (.getInetAddress server) (.getLocalPort server))
        outs (.getOutputStream socket)]
    (write-message outs "GET / HTTP/1.0\r\n\r\n")
    socket))

(defn read-from-socket-ins [socket]
  (read-str (.getInputStream socket)))

(defn fake-client-socket
  [shutdown-in-called shutdown-out-called close-called
   get-output-stream-called get-input-stream-called]
  (let [set-true (fn [a] true)] 
    (proxy [Socket] []
      (.shutdownInput [] (swap! shutdown-in-called set-true))
      (.shutdownOutput [] (swap! shutdown-out-called set-true))
      (.close [] (swap! close-called set-true))
      (.getInputStream []
        (swap! get-input-stream-called set-true)))))

(describe "core"
  (with server (start-server 8181))
  (with expected-response "HTTP/1.1 200 OK") 
  (after (.close @server))

  (it "should parse a request line"
    (should= "GET" (:method (parse-request-line "GET /"))))

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
          action (fn [s] (swap! counter inc))
          server (atom @server)
          action-future (future (accept-connection @server action))]
      (fake-client @server)
      (. Thread (sleep 1000)) ; action-future never returns
      (should= 1 @counter)))
 
  (it "sends the client a message"
    (let [message "Hello, World!\r\n\r\n"
          action (fn [client]
            (let [os (.getOutputStream client)] 
              (write-message os message))
            (.close client))
          server (atom @server)
          accept-future (future (accept-connection @server action))
          client (fake-client @server)]
          (should= "Hello, World!" (read-from-socket-ins client))))

  (it "should shutdown io and close the socket"
    (let [shutdown-in-called (atom false)
          shutdown-out-called (atom false)
          close-called (atom false)
          socket (fake-client-socket shutdown-in-called shutdown-out-called close-called nil nil)]
      (close-socket socket) 
      (should= true @shutdown-in-called)
      (should= true @shutdown-out-called)
      (should= true @close-called)))

;  (it "reads writes and closes input on a socket"
;    (let [get-input-stream-called (atom false)
;          get-output-stream-called (atom false)
;          client-socket (fake-client @server)]
;          (accept-fn @server (fake-client-socket client-socket get-input-stream-called get-output-stream-called))
;      (should= true @get-input-stream-called)))
;
;  (it "should get a response from the server"
;    (let [server (atom @server)]
;      (future (accept-connection @server (partial accept-fn @server)))
;      (should= @expected-response 
;        (read-from-socket-ins (fake-client @server)))))

  (it "should get a response from the server on the second request"
    (let [server (atom @server)]
      (future (accept-connection @server (partial accept-fn @server)))
      (read-from-socket-ins (fake-client @server))
      (should= @expected-response 
        (read-from-socket-ins (fake-client @server)))))

  (it "should get a response from the server on the third request"
    (let [server (atom @server)]
      (future (accept-connection @server (partial accept-fn @server)))
      (read-from-socket-ins (fake-client @server))
      (read-from-socket-ins (fake-client @server))
      (should= @expected-response 
        (read-from-socket-ins (fake-client @server))))))

(run-specs)

