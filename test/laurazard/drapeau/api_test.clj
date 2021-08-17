(ns laurazard.drapeau.api-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.data.json :refer [write-str]]
            ;; [org.httpkit.client :as http]
            [laurazard.drapeau.api :refer [start-server! stop-server! get-flags-handler add-flag-handler update-flag-handler]]
            [laurazard.drapeau.core :refer [flags add-flag]]))


(defn string->stream
  [s]
  (-> s
      (.getBytes "UTF-8")
      (java.io.ByteArrayInputStream.)))

(deftest get-flags-test
  (testing "returns expected payload on get /"
    (reset! flags [])
    (add-flag {:name "flag" :description "a flag" :enabled true})
    (is (= (get-flags-handler) {:status 200 :body (write-str [{:name "flag" :description "a flag" :enabled true}])}))
    (add-flag {:name "another flag" :description "a different flag" :enabled false})
    (is (= (get-flags-handler)
           {:status 200 :body (write-str [{:name "flag" :description "a flag" :enabled true}
                                          {:name "another flag" :description "a different flag" :enabled false}])}))))

(deftest add-flags-test
  (testing "successfully adds flags"
    (reset! flags [])
    (add-flag-handler {:body (string->stream (write-str {:name "flag" :description "a flag" :enabled true}))
                       :headers {"Content-Type" "application/json"} :as :text})
    (is (= @flags [{:name "flag" :description "a flag" :enabled true}]))
    (add-flag-handler {:body (string->stream (write-str {:name "another flag" :description "a cool flag" :enabled false}))
                       :headers {"Content-Type" "application/json"} :as :text})
    (is (= @flags [{:name "flag" :description "a flag" :enabled true}
                   {:name "another flag" :description "a cool flag" :enabled false}])))
  (testing "fails correctly on invalid payload and returns failure reason"
    (reset! flags [])
    (let [response (add-flag-handler {:body (string->stream (write-str {:description "a flag" :enabled true}))
                                      :headers {"Content-Type" "application/json"} :as :text})]
      (is (= "invalid flag: {:description \"a flag\", :enabled true} - failed: (contains? % :name) spec: :laurazard/flag\n"
             (:body response))))
    (is (= [] @flags))))

(deftest update-flags-test
  (testing "successfully updates flags"
    (reset! flags [])
    (add-flag {:name "flag" :description "a flag" :enabled true})
    (update-flag-handler {:body (string->stream (write-str {:name "flag"
                                                            :description "a flag"
                                                            :enabled false}))})
    (is (= [{:name "flag"
             :description "a flag"
             :enabled false
             :history [{:name "flag"
                        :description "a flag"
                        :enabled true}]}]
           @flags)))
  (testing "fails correctly when flag does not exist"
    (reset! flags [])
    (let [response (update-flag-handler
                    {:body (string->stream (write-str {:name "flag"
                                                       :description "a flag"
                                                       :enabled false}))})]
      (is (= {:status 500, :body "flag does not exist"} response))))          ; this should be a 404
  ;; (testing "fails correctly when flag is invalid"
  ;;   (reset! flags [])
  ;;   (let [response (update-flag-handler {:name "bad flag"
  ;;                                        :description "a flag with missing fields"})]
  ;;     (is (= "something" response))))
  )

(deftest router-test
  )
