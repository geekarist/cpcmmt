(ns autosuggest.events
  (:require [re-frame.core :as re-frame])
  (:require [autosuggest.db :as db]
            [common.config :as conf]
            [common.secrets :as sec]
            [common.effects :as ef]
            [goog.string :as gstr]
            [goog.string.format]))

(re-frame/reg-event-db
  ::suggestions-resp-received
  (fn [db [_ resp-body]]
    (assoc db ::db/autosuggest-results
              (->> resp-body
                   (:places)
                   (map #(:name %))))))

(def autosuggest-debounce-delay-ms 1000)

(defn debounce-delay-expired? [db current-time-ms]
  (>= current-time-ms
      (+ (::db/autosuggest-last-query-time-ms db)
         autosuggest-debounce-delay-ms)))

(re-frame/reg-event-fx
  ::autosuggest-query-confirmation
  [(re-frame/inject-cofx ::ef/get-current-time)]
  (fn [{db              :db
        current-time-ms ::ef/current-time-ms}
       [_ query]]
    (if (and (debounce-delay-expired? db current-time-ms)
             (not= query ""))
      {::ef/get-anything
       {:url        (gstr/format (str conf/navitia-base-url "/v1/coverage/%s/places") conf/navitia-coverage)
        :params     {:q               query
                     :key             sec/navitia-api-key
                     :disable_geojson "true"}
        :on-success ::suggestions-resp-received
        :on-error   ::suggestions-err-received}})))

(re-frame/reg-event-fx
  ::autosuggest-query-change
  [(re-frame/inject-cofx ::ef/get-current-time)]
  (fn [{db              :db
        current-time-ms ::ef/current-time-ms}
       [_ value]]
    {:db             (assoc db ::db/autosuggest-query value
                               ::db/autosuggest-last-query-time-ms current-time-ms
                               ::db/autosuggest-error nil
                               ::db/autosuggest-results [])
     :dispatch-later [{:ms       autosuggest-debounce-delay-ms
                       :dispatch [::autosuggest-query-confirmation value]}]}))

(re-frame/reg-event-db
  ::reset
  (fn [db _]
    (assoc db ::db/autosuggest-query nil
              ::db/autosuggest-results [])))
