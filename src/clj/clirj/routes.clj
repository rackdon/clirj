(ns clirj.routes
  (:require [org.httpkit.server :as server]
            [compojure.api.sweet :refer :all]
            [compojure.api.sweet :as compojure-api
             :refer
             [context defroutes GET PATCH POST PUT DELETE]]
            [schema.core :as s]
            [ring.util.http-response :as http-response]
            [ring.middleware.reload :as reload]
            [ring.middleware.cors :refer [wrap-cors]]
            [clirj.users :as users]
            [clirj.ws :as ws]))

(compojure-api/defapi
  api-routes

  (GET "/" []
       {:status  200
        :headers {"Content-Type" "text/html"}
        :body    "Clojure IRC"})

  (GET "/connect/:user" [user]
        (fn [request]
          (if (users/get user)
            (http-response/conflict)
            (ws/handle request user))))

  (GET "/disconnect/:user" [user]
    (when-let [user-chan (users/get user)]
      (ws/disconnect user-chan))
    (http-response/no-content))

  (GET "/list" []
    (http-response/ok (users/list)))

  )

(defroutes app api-routes)

(def server-handler
  (-> app
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (reload/wrap-reload)))
