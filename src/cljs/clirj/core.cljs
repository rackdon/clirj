(ns clirj.core
  (:require [reagent.core :as r :refer [atom]]
            [clirj.net :as net]
            [clirj.components :as components]
            [cljs.core.async :refer [<! put! chan]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def web-socket (atom nil))
(def ws-messages (atom []))
(def members (atom #{}))

(defn manage-events
  [event-data]
  (cond
    (:connect event-data) (swap! members conj (:connect event-data))
    (:disconnect event-data) (swap! members #(-> %1 set (disj %2)) (:disconnect event-data))
    (:message event-data) (swap! ws-messages conj (:message event-data))))



(defn make-websocket
  [name]
  (let [ws (js/WebSocket. (str "ws://localhost:8080/connect/" name))
        get-chan (chan)]

    (set! (.-onopen ws) (fn []
                          (prn "Connection open")
                          (reset! web-socket ws)
                          (net/list-members get-chan)
                          (go
                            (reset! members (<! get-chan)))))
    (set! (.-onclose ws) (fn []
                           (prn "Connection closed")
                           (reset! members #{})
                           (reset! web-socket nil)))
    (set! (.-onmessage ws) #(manage-events (cljs.reader/read-string (.-data %))))))




  (defn main-irc
    []
    (let [name (atom nil)]
      [:div
       [:div
        [:h3 "Join to CLIRJ"]
        (components/input #(reset! name (-> % .-target .-value)))
        (if @web-socket
          (components/button "Disconnect" #(.close @web-socket))
          (components/button "Join" #(when @name (make-websocket @name) (reset! name nil))))]
       [:div
        (components/button "Ban user" #(when @name (net/ban-member @name)))
        ]]))


(defn send-message
  []
  (let [message (atom nil)]
    [:div
     (components/input #(reset! message (-> % .-target .-value)))
     (components/button "Send" #(when (and @web-socket @message)
                                  (.send @web-socket @message)
                                  (reset! message nil)))
     ]))

(defn messages-list []
  [:div
   [:h3 "Messages"]
   (for [message @ws-messages]
     [:p message])])

(defn main []
  [:div
   [:div
    [main-irc]
    [messages-list]
    [send-message]
    ]
   [:div
    [:h3 "Members"]
    (for [member (seq @members)]
      [:p {:key member} member])]])

(r/render-component [main]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
