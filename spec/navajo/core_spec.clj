(ns navajo.core-spec
  (:use 
    [speclj.core]
    [navajo.core]))

(describe "http.parsing"

  (it "should parse a request line"
    (should= "GET" (:method (parse-request-line "GET /"))))

;  (it "should write a message to a socket"
;    (let [server start-http-server 8080)
;    (should= "200 OK\r\nHello" 
;     )
;  )
)
(run-specs)
