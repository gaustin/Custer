(ns navajo.core-spec
  (:use 
    [speclj.core]
    [navajo.core]
    [navajo.streams]))

(describe "core"
;  (with server (start-server 8080))
;  (after (.close @server))

  (it "should parse a request line"
    (should= "GET" (:method (parse-request-line "GET /"))))

  (it "should get a response from the server"
    (let [server (start-http-server 8080)
          client (java.net.Socket. (.getInetAddress server) (.getLocalPort server))]
      (should= "200 OK\r\nHello" 
        (byte-seq-to-string (read-byte-seq-from-stream (.getInputStream client)))))))

(run-specs)
