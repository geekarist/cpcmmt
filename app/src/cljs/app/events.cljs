(ns app.events
  (:require
   [re-frame.core :as re-frame]
   [app.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-fx
  :open-search
  (fn [_ _]
    (js/console.log "Hello")))