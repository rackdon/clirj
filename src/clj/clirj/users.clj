(ns clirj.users
  (:require [clojure.core.async :as async]
            [org.httpkit.server :as httpkit]))

(def ^:private users (atom {}))

(defn add
  [user channel]
  (swap! users assoc (keyword user) channel))

(defn delete
  [user]
  (when-let [user-chan ((keyword user) @users)]
    (swap! users dissoc (keyword user))))

(defn list
  []
  (map name (keys @users)))

(defn get
  [user]
  ((keyword user) @users))
