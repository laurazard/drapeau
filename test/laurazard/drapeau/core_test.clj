(ns laurazard.drapeau.core-test
  (:require [laurazard.drapeau.core :refer [add-flag update-flag flags]]
            [clojure.test :refer [deftest testing is]]
            [failjure.core :as f]))

(deftest add-flag-test
  (testing "adds flag"
    (reset! flags [])
    (add-flag {:name "a flag" :description "a really cool flag" :enabled true})
    (is (= @flags [{:name "a flag" :description "a really cool flag" :enabled true}])))
  (testing "adds more than one flag"
    (reset! flags [])
    (add-flag {:name "a flag" :description "a really cool flag" :enabled true})
    (add-flag {:name "another flag" :description "a less cool flag" :enabled false})
    (is (= @flags [{:name "a flag" :description "a really cool flag" :enabled true}
                   {:name "another flag" :description "a less cool flag" :enabled false}])))
  (testing "fails when adding invalid flags"
    (is (f/failed? (add-flag "a")))
    (is (f/failed? (add-flag {})))
    (is (f/failed? (add-flag {:name "flag"})))))

(deftest update-flag-test
  (testing "updates flag and keeps history"
    (reset! flags [])
    (add-flag {:name "flag" :description "a flag" :enabled true})
    (is (= [{:name "flag" :description "a flag" :enabled true}] @flags))
    (update-flag {:name "flag" :description "different flag" :enabled true})
    (is (= [{:name "flag" 
             :description "different flag" 
             :enabled true 
             :history [{:name "flag" :description "a flag" :enabled true}]}] @flags))
    (add-flag {:name "flag 2" :description "another flag" :enabled false})
    (is (= [{:name "flag"
             :description "different flag"
             :enabled true
             :history [{:name "flag" :description "a flag" :enabled true}]}
            {:name "flag 2" :description "another flag" :enabled false}] @flags))
    (update-flag {:name "flag 2" :description "another flag" :enabled true})
    (is (= [{:name "flag"
             :description "different flag"
             :enabled true
             :history [{:name "flag" :description "a flag" :enabled true}]}
            {:name "flag 2"
             :description "another flag"
             :enabled true
             :history [{:name "flag 2" :description "another flag" :enabled false}]}] @flags))
    (update-flag {:name "flag 2" :description "new description" :enabled true})
    (is (= [{:name "flag"
             :description "different flag"
             :enabled true
             :history [{:name "flag" :description "a flag" :enabled true}]}
            {:name "flag 2"
             :description "new description"
             :enabled true
             :history [{:name "flag 2" :description "another flag" :enabled true
                        :history [{:name "flag 2" :description "another flag" :enabled false}]}]}]
         @flags))
    ))