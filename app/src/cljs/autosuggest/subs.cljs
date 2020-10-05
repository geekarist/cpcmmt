(ns autosuggest.subs
  (:require [re-frame.core :as re-frame]
            [autosuggest.db :as db]))

(re-frame/reg-sub
  ::autosuggest-error
  (fn [db]
    (::db/autosuggest-error db)))

(re-frame/reg-sub
  ::autosuggest-results
  (fn [db]
    (::db/autosuggest-results db)))

(re-frame/reg-sub
  ::query
  (fn [db]
    (::db/autosuggest-query db)))