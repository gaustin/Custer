(ns custer.requests-spec
  (:use [speclj.core]
        [custer.requests]))

(describe "requests"
  (with full-request
    '("GET / HTTP/1.1"
      "Host: localhost"
      "User-Agent: 007"
      "Accept: text/html"))

  (it "should parse method line of a GET"
    (let [method-pair (parse-method-line (first @full-request))]
      (should= "GET" (first method-pair))
      (should= "/" (second method-pair))))
    

  (it "should parse method line of a GET"
    (let [request (parse-request @full-request)]
      (should= "GET" (:method request))
      (should= "/" (:path request))))

  (it "should parse the method line of a OPTIONS request"
    (let [request (parse-request '("OPTIONS /index.html HTTP/1.1"))]
      (should= "OPTIONS" (:method request))
      (should= "/index.html" (:path request))))

  (it "should create a lowercase keyword"
    (should= :host (lc-keyword "Host")))

  (it "should parse a header"
    (let [header (parse-header "Host: www.example.com")]
      (should= :host (first header))
      (should= "www.example.com" (second header))))

  (it "should parse headers"
    (let [request (parse-request @full-request)
          headers (:headers request)]
      (should= 3 (count (keys headers)))
      (should= "localhost" (:host headers))
      (should= "007" (:user-agent headers))
      (should= "text/html" (:accept headers)))))
