(ns clirj.core
  (:require [clojure.core.async :as async]
            [org.httpkit.server :as httpkit]))

(def users (atom {}))

(defn add-user
  [user channel]
  (swap! users assoc (keyword user) channel))

(defn delete-user
  [user]
  (when-let [user-chan ((keyword user) @users)]
    (swap! users dissoc (keyword user))))

(defn disconnect-user
  [user]
  (when-let [user-chan ((keyword user) @users)]
    (httpkit/close user-chan)))

(defn list-users
  []
  (map name (keys @users)))

(defn get-user
  [user]
  ((keyword user) @users))
