(ns navajo.core-spec
  (:use 
    [speclj.core]
    [navajo.core]))

(describe "Server"
;  (around [it] 
;    (binding [@server (start-server 8080)]
;      (it)
;      (.close @server)))
  
  (with server (start-server 8080))
  (after (.close @server))

  (it "should start the server"
    (should-not (.isClosed @server)))

  (it "should have the specified port"
    (should= 8080 (.getLocalPort @server)))

  (it "should be bound"
    (should (.isBound @server)))

  (it "should be bound to localhost"
    (should= "0.0.0.0" (.getCanonicalHostName (.getInetAddress @server))))

;  (it "should respond with 200 OK \r\n Hello"
;    (should= "200 OK\r\nHello"
;      (do (accept-connection @server (fn [socket] (.close socket)))
;          (let [client (new java.net.Socket (.getInetAddress @server) 8080)]
;            (.read client)))))

  (it "accepts a connection"
    (let [counter (atom 0)
          action (fn [s] (println "Action called") (swap! counter inc))
          accept-future (accept-connection @server action)]
      (java.net.Socket. (.getInetAddress @server) 8080)
      @accept-future
      (should= 1 @counter)))
)

(run-specs)
