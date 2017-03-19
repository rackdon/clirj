(ns clirj.core
  (:require [reagent.core :as r]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def web-socket (atom nil))
(def ws-messages (atom "My messages"))

(defn make-websocket
  [name]
  (when-let [chan (js/WebSocket. (str "ws://localhost:8080/connect/" name))]
    (set! (.-onmessage chan) #(do (js/console.dir (.-data %)) (reset! ws-messages (.-data %))))
    (reset! web-socket chan)))

(defn make-request
  [url]
  (go
    (let [response (<! (http/get url {:with-credentials? false}))]
      (prn (:body response)))))

(defn disconnect-irc
  []
  [:button {:on-click #(make-request (str "http://localhost:8080/disconnect/" @name))} "Disconnect"])

(defn list-members
  []
  [:button {:on-click #(make-request "http://localhost:8080/list")} "List members"])

  (defn main-irc
    []
    (let [name (r/atom "")]
      [:div
       [:div
        [:h3 "Join to IRC"]
        [:input {:on-change #(reset! name (-> % .-target .-value))}]
        [:button {:on-click #(make-websocket @name)} "Join"]]
       [:div
        [:button {:on-click #(make-request (str "http://localhost:8080/disconnect/" @name))} "Ban user"]
        [:button {:on-click #(.close @web-socket)} "Disconnect"]
        (list-members)]]))


(defn messages
  []
  [:div
   [:h3 "Messages"]])

(defn send-message
  []
  (let [message (r/atom "")]
    [:div
     [:input {:on-change #(reset! message (-> % .-target .-value))}]
     [:button {:on-click #(when (= (.-readyState @web-socket) 1) (.send @web-socket @message))} "Send"]]))

(defn messages-list []
  [:p @ws-messages])

(defn irc []
  [:div
   [main-irc]
   [messages]
   [send-message]
   [messages-list]
   [:button {:on-click #(prn @ws-messages)} "Show messages"]
   ])

(r/render-component [irc]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
