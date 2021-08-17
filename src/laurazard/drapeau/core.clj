(ns laurazard.drapeau.core
  "core logic for the domain"
  (:require [clojure.spec.alpha :as s]
            [failjure.core :as f]))

(s/def :laurazard/name string?)
(s/def :laurazard/description string?)
(s/def :laurazard/enabled boolean?)
(s/def :laurazard/history vector?)
(s/def :laurazard/flag
  (s/keys :req-un [:laurazard/name :laurazard/description :laurazard/enabled]
          :opt-un [:laurazard/history]))

(def flags (atom []))

(defn does-flag-exist?
  [flag]
  (some #(= (:name flag) (:name %)) @flags))

(defn add-flag
  "adds a flag to the flag repository"
  [flag]
  (if (s/valid? :laurazard/flag flag)
    (swap! flags (fn [_] (conj @flags flag)))
    (f/fail (str "invalid flag: " (s/explain-str :laurazard/flag flag)))))

(defn update-flag
  "updates a flag on the flag repository"
  [new-flag]
  (if (does-flag-exist? new-flag)
    (swap! flags (fn [flags] (mapv (fn [flag]
                                     (if (= (:name flag) (:name new-flag))
                                       (assoc new-flag :history [flag])
                                       flag)) flags)))
    (f/fail "flag does not exist")))
