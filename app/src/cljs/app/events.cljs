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

(re-frame/reg-event-db
  ::set-journey-start
  (fn [db [_ value]]
    (assoc db :journey-start value)))

(re-frame/reg-event-db
  ::set-journey-end
  (fn [db [_ value]]                                        ; FIXME: Value is always nil
    (assoc db :journey-end value)))

(re-frame/reg-event-db
  ::get-journeys
  (fn [db [_ _]]
    (prn "From " (:journey-start db) " to " (:journey-end db) " and back again")
    (assoc db :journeys ["Hello"])))