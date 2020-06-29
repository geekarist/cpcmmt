(ns app.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
  :active-panel
  (fn [db]
    (:active-panel db)))
