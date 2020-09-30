(ns app.events
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]
    [app.effects :as ef]
    [app.secrets :as sec]
    [goog.string :as gstr]
    [goog.string.format]
    [app.config :as conf]
    [app.utils :refer [json->clj]]))

(re-frame/reg-event-db
  ::db-initialization
  (fn [_ _]
    db/default-db))

; App

(re-frame/reg-event-db
  ::navigation-to
  (fn [db [_ value]]
    (assoc db ::db/active-panel value)))

(defn nav-to-autosuggest-handler [field-kw]
  (fn [{:keys [db]} #_(Coeffects)
       [_ query element-id-to-focus]  #_(Event)]
    {:db                   (assoc db ::db/active-panel ::db/panel-autosuggest
                                     ::db/autosuggest-field field-kw
                                     ::db/autosuggest-query query)
     ::ef/focus-to-element element-id-to-focus}))

(re-frame/reg-event-fx
  ::navigation-to-autosuggest-start
  (nav-to-autosuggest-handler ::db/autosuggest-field-start))

(re-frame/reg-event-fx
  ::navigation-to-autosuggest-end
  (nav-to-autosuggest-handler ::db/autosuggest-field-end))

(def navitia-coverage "fr-idf")

(def autosuggest-debounce-delay-ms 1000)

; Autosuggest

(re-frame/reg-event-fx
  ::autosuggest-query-confirmation
  [(re-frame/inject-cofx ::ef/get-current-time)]
  (fn [{db              :db
        current-time-ms ::ef/current-time-ms}
       [_ value]]
    (if (and (not= value "")
             (>= current-time-ms
                 (+ (::db/autosuggest-last-query-time-ms db)
                    autosuggest-debounce-delay-ms)))
      {::ef/get-anything
       {:url        (gstr/format (str conf/navitia-base-url "/v1/coverage/%s/places") navitia-coverage)
        :params     {:q               value
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

; Journeys

(re-frame/reg-event-db
  ::journey-start-change
  (fn [db [_ value]]
    (assoc db ::db/journey-start value)))

(re-frame/reg-event-db
  ::journey-end-change
  (fn [db [_ value]]
    (assoc db ::db/journey-end value)))

(re-frame/reg-event-fx
  ::journey-start-validation
  (fn [{:keys [db]} coeffects-map
       event-vector]
    {:db (assoc db ::db/journey-start
                   (str "Start with map: " coeffects-map
                        " and events: " event-vector))}))

(re-frame/reg-event-fx
  ::journey-end-validation
  (fn [{:keys [db]} coeffects-map
       event-vector]
    {:db (assoc db ::db/journey-end
                   (str "End with map: " coeffects-map " and events: " event-vector))}))

; Autosuggest

(re-frame/reg-event-db
  ::suggestions-resp-received
  (fn [db [_ resp-body]]
    (assoc db ::db/autosuggest-results
              (->> resp-body
                   (:places)
                   (map #(:name %))))))

(defn journey-start-or-end [db]
  (if (= ::db/autosuggest-field-start
         (::db/autosuggest-field db))
    ::db/journey-start
    ::db/journey-end))

(re-frame/reg-event-fx
  ::autosuggest-item-selection
  (fn [{:keys [db]}
       [_ text]]
    {:db (assoc db ::db/autosuggest-query text
                   ::db/active-panel ::db/panel-start-end-selection
                   ::db/autosuggest-results []
                   (journey-start-or-end db) text)}))

(re-frame/reg-event-db
  ::suggestions-err-received
  (fn [db event-vec]
    (assoc db ::db/autosuggest-error
              (str event-vec))))

; Journeys

(defn- fake-journey [[num db] _]
  (comment (str "Journey from " (::db/journey-start db) " to " (::db/journey-end db) " " num))
  {::segments   ["Walk" "R" "14"]
   ::duration   "1 h 24"
   ::start-date "07:10" ::start-station "Montigny-sur-Loing"
   ::end-date   "08:34" ::end-station "Paris Gare de Lyon"})

(defn- handle-get-journeys [{:keys [db]} _]
  ;(as-> [1 2 3 4 5 6 7 8 9] v
  ;(map (fn [num] [num db]) v)                         ; `(map #([% db]) v)` does not work 🤔 - see https://stackoverflow.com/a/13206291/1665730
  ;(map fake-journey v)
  ;(assoc db ::db/journeys v)
  {:db (assoc db ::db/active-panel ::db/panel-journeys)
   ::ef/get-anything
       {:url        (gstr/format "%s/v1/coverage/%s/journeys"
                                 conf/navitia-base-url
                                 navitia-coverage)
        :params     {:from            ::db/journey-start-id
                     :to              ::db/journey-end-id
                     :key             sec/navitia-api-key
                     :disable_geojson "true"}
        :on-success ::journeys-resp-received
        :on-error   ::journeys-err-received}})              ;)

(re-frame/reg-event-db
  ::journeys-err-received
  (fn [db [_ resp]]
    (assoc db ::db/journeys-error resp
              ::db/journeys [])))

(defn journey-resp->item [journey-resp]
  {::segments   ["Walk" "R" "14"]
   ::duration   (:duration journey-resp)
   ::start-date "07:10" ::start-station (-> journey-resp
                                            (:sections)
                                            (first)
                                            (:from)
                                            (:name))
   ::end-date   "08:34" ::end-station (-> journey-resp
                                          (:sections)
                                          (last)
                                          (:to)
                                          (:name))})

(re-frame/reg-event-fx
  ::journeys-resp-received
  (fn [{:keys [db]} [_ resp]]
    {:db (assoc db ::db/journeys-error nil
                   ::db/journeys (->> resp
                                      (:journeys)
                                      (map journey-resp->item)))}))

(re-frame/reg-event-fx ::journey-search-submission handle-get-journeys)

(comment
  (in-ns 'app.events)
  (handle-get-journeys {::db/journey-start "a"
                        ::db/journey-end   "b"} []))