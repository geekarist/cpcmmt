(ns app.events
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]))

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
  (fn [db [_ value]]
    (assoc db :journey-end value)))

(defn- fake-journey [[num db] _]
  (str "Journey from " (:journey-start db) " to " (:journey-end db) " " num))

(defn- handle-get-journeys [db [_ _]]
  (as-> [1 2 3 4 5 6 7 8 9] v
        (map (fn [num] [num db]) v)                         ; `(map #([% db]) v)` does not work 🤔
        (map fake-journey v)
        (assoc db :journeys v)
        (assoc v :active-panel :panel/journeys)))

(re-frame/reg-event-db ::get-journeys handle-get-journeys)

(comment
  (in-ns 'app.events)
  (handle-get-journeys {:journey-start "a"
                        :journey-end   "b"} []))