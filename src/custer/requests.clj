(ns custer.requests
  (:use [clojure.string :only (lower-case split)]))

(defrecord Request [method path headers])

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

(defn parse-request [raw-request]
  (if (seq? raw-request)
    (let [method-path-pair (parse-method-line (first raw-request))]
      (Request. (first method-path-pair) 
                (second method-path-pair)
                (parse-headers (rest raw-request) {})))
    (let [method-path-pair (parse-method-line raw-request)]
      (Request. (first method-path-pair)
                (second method-path-pair)
                {}))))

