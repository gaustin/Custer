(ns custer.request_parsing-spec
  (:use [speclj.core]
        [custer.request_parsing]))

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

  (it "should return a request from a one-line request"
    (let [request (parse-request-str (first @full-request))]
      (should= "GET" (:method request))
      (should= "/" (:path request))
      (should= {} (:headers request))
      (should= nil (:body request))))

  (it "should parse method line of a GET"
    (let [request (parse-request @full-request)]
      (should= "GET" (:method request))
      (should= "/" (:path request))))

  (it "should parse the method line of an OPTIONS request"
    (let [request (parse-request '("OPTIONS /index.html HTTP/1.1"))]
      (should= "OPTIONS" (:method request))
      (should= "/index.html" (:path request))))

  (it "should create a lowercase keyword"
    (should= :host (lc-keyword "Host")))

  (it "should parse a header"
    (let [header (parse-header "Host: www.example.com")]
      (should= :host (first header))
      (should= "www.example.com" (second header))))

  (it "should return a request from a sequence"
    (let [request (parse-request-seq @full-request)
          headers (:headers request)]
      (should= 3 (count (keys headers)))
      (should= "localhost" (:host headers))
      (should= "007" (:user-agent headers))
      (should= "text/html" (:accept headers))))

  (it "should parse headers"
    (let [request (parse-request @full-request)
          headers (:headers request)]
      (should= 3 (count (keys headers)))
      (should= "localhost" (:host headers))
      (should= "007" (:user-agent headers))
      (should= "text/html" (:accept headers))))

  (it "should be able to print a request object to a string"
    (let [request (custer.request_parsing.Request. "GET" "/" {:foo "bar"} nil)]
      (should= (binding [*print-dup* true] (prn-str request)) (print-to-s request))))

  (it "should read the request body"
    (let [post-request '("POST /new HTTP/1.1"
                         "User-Agent: 007"
                         "Content-Type: application/x-www-form-urlencoded"
                         "Content-Length: 10"
                         ""
                         "user=grant")
          request (parse-request post-request)]
    (should= "user=grant" (:body request)))))

