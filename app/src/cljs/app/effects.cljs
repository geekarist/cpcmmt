(ns app.effects
  (:require [re-frame.core :as re-frame]
            [ajax.core :refer [GET]]))

(declare GET)
(declare .getTime)

(defn call-get-suggestions [url params on-success-event on-error-event]
  (GET url
       {:params        params
        :handler       #(re-frame/dispatch [on-success-event %])
        :error-handler #(re-frame/dispatch [on-error-event %])}))

(re-frame/reg-fx
  ::get-suggestions
  (fn [request]
    (call-get-suggestions (:url request)
                          (:params request)
                          (:on-success request)
                          (:on-error request))))

(re-frame/reg-cofx
  ::get-current-time
  (fn [coeffects _]
    (assoc coeffects ::current-time-ms (.getTime (js/Date.)))))