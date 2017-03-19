(ns clirj.main
  (:require [org.httpkit.server :refer [run-server]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clirj.routes :as routes]))

(defonce server (atom nil))

(defn start [& args]
  (reset! server (run-server routes/server-handler {:port 8080}))
  (prn "Server started in 8080"))

(defn stop []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)
    (prn "Server stopped")))

(defn reset []
  (stop)
  (refresh :after 'clirj.main/start))

