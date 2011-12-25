(ns navajo.core-spec
  (:use 
    [speclj.core]
    [navajo.core]
    [navajo.streams]
))

(defn new-client [server] (java.net.Socket. (.getInetAddress server) (.getLocalPort server)))

(defn read-from-client [client] (byte-seq-to-string (read-byte-seq-from-stream (.getInputStream client))))

(describe "core"
  (with server (start-http-server 8080))
  (with expected-response "HTTP/1.1 200 OK\r\n\r\nHello") 
  (after (.close @server))

  (it "should parse a request line"
    (should= "GET" (:method (parse-request-line "GET /"))))

  (it "should get a response from the server"
    (let [client (new-client @server)]
      (should= @expected-response 
        (read-from-client client))))

  (it "should get a response from the server on the second request"
    (let [client (new-client @server)]
      (read-from-client client))
      (should= @expected-response 
        (read-from-client (new-client @server))))

  (it "should get a response from the server on the third request"
    (read-from-client (new-client @server))
    (read-from-client (new-client @server))
    (should= @expected-response 
      (read-from-client (new-client @server))))    
)

(run-specs)
