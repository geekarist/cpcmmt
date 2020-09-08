(ns app.effects
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET]]))

(defn call [req]
  (GET (:url req)
       {:params (:params req)}))

(re-frame/reg-fx
  ::get-suggestions
  (fn [request]
    (if request (call request))))

(re-frame/reg-cofx
  ::get-current-time
  (fn [coeffects _]
    (assoc coeffects ::current-time-ms (.getTime (js/Date.)))))