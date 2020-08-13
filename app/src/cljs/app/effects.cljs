(ns app.effects
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-fx
  ::get-suggestions
  (fn [query]
    (println query)))