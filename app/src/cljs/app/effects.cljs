(ns app.effects
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-fx
  ::get-suggestions
  (fn [request]
    (println request)))

(re-frame/reg-cofx
  ::get-current-time
  (fn [coeffects _]
    (assoc coeffects ::current-time-ms (js/Date.))))