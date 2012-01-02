(ns custer.core-spec
  (:use 
    [speclj.core]
    [custer.core]
    [custer.streams]
))

(defn new-client [server] (java.net.Socket. (.getInetAddress server) (.getLocalPort server)))

(defn read-from-client [client]
  (let [ins (.getInputStream client)] 
    (byte-seq-to-string (read-byte-seq-from-stream ins))))

(describe "core"
  (with server (start-server 8181))
  (with expected-response "HTTP/1.1 200 OK\r\n\r\nHello") 
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
      (new-client @server)
      (. Thread (sleep 1000)) ; action-future never returns
      (should= 1 @counter)))
 
  (it "sends the client a message"
    (let [message "Hello, World!\r\n"
          action (fn [client]
            (let [os (.getOutputStream client)] 
              (.write os (.getBytes message))
              (.flush os))
              (.close client))
          server (atom @server)]
          (future (accept-connection @server action))
          (should= message (read-from-client (new-client @server)))))

;  (it "receives a message from the client"
;    (let [message "GET / HTTP/1.0\r\n"
;          response (atom "")
;          action (fn [client]
;            (let [input (read-from-client client)]
;              (swap! response concat input) (println input)))
;          action-future (future (accept-connection @server action))
;          client (new-client @server)
;          cos (.getOutputStream client)]
;      (.write cos (.getBytes message))
;      (.flush cos)
;      (. Thread (sleep 1000))
;      (should= message @response)))

  (it "should get a response from the server"
    (let [server (atom @server)]
      (future (accept-connection @server (partial accept-fn @server)))
      (should= @expected-response 
        (read-from-client (new-client @server)))))

  (it "should get a response from the server on the second request"
    (let [server (atom @server)]
      (future (accept-connection @server (partial accept-fn @server)))
      (read-from-client (new-client @server))
      (should= @expected-response 
        (read-from-client (new-client @server)))))

  (it "should get a response from the server on the third request"
    (let [server (atom @server)]
      (future (accept-connection @server (partial accept-fn @server)))
      (read-from-client (new-client @server))
      (read-from-client (new-client @server))
      (should= @expected-response 
        (read-from-client (new-client @server))))))

(run-specs)

