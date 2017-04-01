(ns clirj.net
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<! put!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def request-types {:get    http/get
                    :put    http/put
                    :patch  http/patch
                    :post   http/post
                    :delete http/delete})
(defn make-request
  [req-type url & [channel]]
  (go
    (let [response (<! ((req-type request-types) url {:with-credentials? false}))]
      (when channel
        (put! channel (:body response))))))

(defn list-members
  [channel]
  (make-request :get "http://localhost:8080/list" channel))

(defn ban-member
  [user]
  (make-request :get (str "http://localhost:8080/disconnect/" user)))
