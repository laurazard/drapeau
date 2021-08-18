(ns laurazard.drapeau.api
  (:gen-class)
  (:require [clojure.data.json :refer [write-str read-str]]
            [org.httpkit.server :refer [run-server server-stop!]]
            [failjure.core :refer [attempt-all when-failed]]
            [laurazard.drapeau.core :refer [flags add-flag update-flag]]))


(def server (atom nil))

(defn get-flags-handler
  []
  {:status 200 :body (write-str @flags)})

(defn add-flag-handler
  [{:keys [body]}]
  (attempt-all [_ (add-flag (read-str (slurp body) :key-fn keyword))]
               {:status 201 :body "created successfully"}
               (when-failed [e]
                            {:status 500 :body (:message e)})))

(defn update-flag-handler
  [{:keys [body]}]
  (attempt-all [_ (update-flag (read-str (slurp body) :key-fn keyword))]
               {:status 204 :body "updated successfully"}
               (when-failed [e]
                            {:status 500 :body (:message e)})))

(defn router
  [{:keys [uri request-method] :as request}]
  (case [request-method uri]
    [:get "/api"] (get-flags-handler)
    [:post "/api"] (add-flag-handler request)))

(defn start-server!
  []
  (reset! server (run-server router {:port 3000 :legacy-return-value? false})))

(defn stop-server!
  []
  @(server-stop! @server))


(defn -main
  "API entrypoint"
  [& args]
  (start-server!)
  (read-line)
  (stop-server!))
