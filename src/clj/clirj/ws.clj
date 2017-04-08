(ns clirj.ws
  (:require [clojure.core.async :as async
             :refer [<! >! chan mult go-loop go]]
            [org.httpkit.server :as server]
            [ring.util.http-response :as http-response]
            [clirj.users :as users]))

(def root-chan (chan (async/buffer 10)))
(def mlt (mult root-chan))

(defn handle
  [request user]
  (let [cha (chan (async/sliding-buffer 16))]
    (async/tap mlt cha)
    (server/with-channel request browser-connection

                         (users/add user browser-connection)
                         (go (>! root-chan (format "{:connect %s}" user)))
                         (server/on-close browser-connection
                                          (fn [status]
                                            (users/delete user)
                                            (async/untap mlt cha)
                                            (async/close! cha)
                                            (go (>! root-chan (format "{:disconnect %s}" user)))
                                            (prn "channel closed: " status)))
                         (server/on-receive browser-connection (fn [data]
                                                                 (go
                                                                   (>! root-chan (format "{:message %s}" data)))))

                         (go-loop
                           []
                           (when-let [data (<! cha)]
                             (server/send! browser-connection data)
                             (recur))))))

(defn disconnect
  [ws]
  (server/send! ws "" true))
