(ns app.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
  ::active-panel
  (fn [db]
    (:active-panel db)))

(re-frame/reg-sub
  ::journeys
  (fn [db]
    (:journeys db)))

(re-frame/reg-sub
  ::journey-start
  (fn [db]
    (:journey-start db)))

(re-frame/reg-sub
  ::journey-end
  (fn [db]
    (:journey-end db)))