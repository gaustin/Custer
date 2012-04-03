(ns custer.request_parsing
  (:use [clojure.string :only (lower-case split join)]))

(defprotocol Printable
  (print-to-s [this]))

(defrecord Request [method path headers body]
  Printable
  (print-to-s [this]
    (binding [*print-dup* true] (prn-str this))))

(defn lc-keyword [kw]
  (keyword (lower-case kw)))

(defn parse-header [raw-header]
  (let [pair (split raw-header #":\s")
        name (lc-keyword (first pair))
        value (second pair)]
    (list name value)))

(defn parse-headers [raw-headers headers]
  (if (empty? raw-headers)
    headers
    (let [header-pair (parse-header (first raw-headers))]
      (recur (rest raw-headers) (assoc headers (first header-pair) (second header-pair))))))

(defn parse-method-line [raw-method-line]
  (split raw-method-line #"\s"))

(defn extract-headers-and-body [raw-request]
; Expects everything but the method line
  (let [headers-and-body (split-with (fn [x] (not (empty? x))) raw-request)]
    { 
      :headers (first headers-and-body),
      :body (flatten (rest headers-and-body))
    }
  ))

(defn parse-request-seq [raw-request]
  (let [method-path-pair (parse-method-line (first raw-request))
        headers-and-body (extract-headers-and-body (rest raw-request))
        headers (:headers headers-and-body)
        ; TODO: This isn't the right way to parse the body, but it works for now.
        body (join "" (:body headers-and-body))]
    (Request. (first method-path-pair)
              (second method-path-pair)
              (parse-headers headers {})
              body)))

(defn parse-request-str [raw-request]
  (let [method-path-pair (parse-method-line raw-request)]
    (Request. (first method-path-pair)
              (second method-path-pair)
              {}
              nil)))

(defn parse-request [raw-request]
  (if (seq? raw-request)
    (parse-request-seq raw-request)
    (parse-request-str raw-request))) ; Got a one-line request. No headers.


