(ns app.subs
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]))

; App

(re-frame/reg-sub
  ::active-panel
  (fn [db]
    (::db/active-panel db)))

; Journeys

(re-frame/reg-sub
  ::journeys
  (fn [db]
    (::db/journeys db)))

(re-frame/reg-sub
  ::journeys-error
  (fn [db]
    (::db/journeys-error db)))

(re-frame/reg-sub
  ::journey-start
  (fn [db]
    (::db/journey-start db)))

(re-frame/reg-sub
  ::journey-end
  (fn [db]
    (::db/journey-end db)))

(re-frame/reg-sub
  ::autosuggest-initial-query
  (fn [db]
    (::db/autosuggest-initial-query db)))

