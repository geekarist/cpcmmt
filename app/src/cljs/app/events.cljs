(ns app.events
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]
    ))

(re-frame/reg-event-db
  ::initialize-db
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  ::navigate-to
  (fn [db [_ value]]
    (assoc db :active-panel value)))