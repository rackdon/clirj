(ns clirj.core
  (:require [reagent.core :as r]
            [clirj.net :as net]
            [cljs.core.async :refer [<! put! chan]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def web-socket (atom nil))
(def ws-messages (r/atom []))
(def members (r/atom #{}))

(defn manage-events
  [event-data]
  (cond
    (:connect event-data) (swap! members conj (:connect event-data))
    (:disconnect event-data) (swap! members disj (:disconnect event-data))
    (:message event-data) (swap! ws-messages conj (:message event-data))))



(defn make-websocket
  [name]
  (let [ws (js/WebSocket. (str "ws://localhost:8080/connect/" name))
             get-chan (chan)]
    (set! (.-onopen ws) #(prn "Connection open"))
    (set! (.-onclose ws) (fn [ev]
                           (prn "Connection closed")
                           (reset! members #{})
                           (reset! web-socket nil)))
    (set! (.-onmessage ws) #(manage-events (cljs.reader/read-string (.-data %))))
    (reset! web-socket ws)
    (net/list-members get-chan)
    (go
      (reset! members (<! get-chan)))
    ))




  (defn main-irc
    []
    (let [name (r/atom "")]
      [:div
       [:div
        [:h3 "Join to IRC"]
        [:input {:on-change #(reset! name (-> % .-target .-value))}]
        [:button {:on-click #(when @name (make-websocket @name))} "Join"]]
       [:div
        [:button {:on-click #(when @name (net/ban-member @name))} "Ban user"]
        [:button {:on-click #(when @web-socket (.close @web-socket))} "Disconnect"]
        ]]))


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
  [:div
   (for [message @ws-messages]
     [:p message])])

(defn main []
  [:div
   [:div
    [main-irc]
    [messages]
    [messages-list]
    [send-message]
    ]
   [:div
    [:h3 "Members"]
    (for [member (seq @members)]
      [:p member])]])

(r/render-component [main]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
