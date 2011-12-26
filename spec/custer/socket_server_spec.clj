(ns custer.socket-server-spec
  (:use
    [speclj.core]
    [custer.socket-server]
    [custer.streams]))

(describe "socket-server"
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
    
  (it "accepts a connection"
    (let [counter (atom 0)
          action (fn [s] (swap! counter inc))
          accept-future (accept-connection @server action)]
      (java.net.Socket. (.getInetAddress @server) 8080)
      @accept-future
      (should= 1 @counter)))
 
  (it "sends the client a message"
    (let [message "Hello, World!\r\n"
          action (fn [client]
            (let [os (.getOutputStream client)] 
              (.write os (.getBytes message))
              (.flush os))
              (.close client))
          accept-future (accept-connection @server action)
          client (java.net.Socket. (.getInetAddress @server) 8080)]
          @accept-future
          (should= message (byte-seq-to-string (read-byte-seq-from-stream (.getInputStream client)))))))

(run-specs)

