(ns app.events
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]
    [goog.string :as gstr]
    [goog.string.format]
    [app.utils :refer [json->clj]]
    [common.effects :as ef]
    [common.config :as conf]
    [common.secrets :as sec]))

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
                                     ::db/autosuggest-initial-query query)
     ::ef/focus-to-element element-id-to-focus}))

(re-frame/reg-event-fx
  ::navigation-to-autosuggest-start
  (nav-to-autosuggest-handler ::db/autosuggest-field-start))

(re-frame/reg-event-fx
  ::navigation-to-autosuggest-end
  (nav-to-autosuggest-handler ::db/autosuggest-field-end))

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

(defn journey-start-or-end [db]
  (if (= ::db/autosuggest-field-start
         (::db/autosuggest-field db))
    ::db/journey-start
    ::db/journey-end))

(re-frame/reg-event-fx
  ::nav-to-start-end-selection
  (fn [{:keys [db]}
       [_ text]]
    {:db (assoc db ::db/active-panel ::db/panel-start-end-selection
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
                                 conf/navitia-coverage)
        :params     {:from            (::db/journey-start-id db)
                     :to              (::db/journey-end-id db)
                     :key             sec/navitia-api-key
                     :disable_geojson "true"}
        :on-success ::journeys-resp-received
        :on-error   ::journeys-err-received}})              ;)

(re-frame/reg-event-db
  ::journeys-err-received
  (fn [db [_ resp]]
    (assoc db ::db/journeys-error resp
              ::db/journeys [])))

(defn seconds->formatted [duration-sec]
  (let [hours (-> duration-sec
                  (/ 3600)
                  (Math/floor))
        hours-as-sec (* hours 3600)
        minutes (-> duration-sec
                    (- hours-as-sec)
                    (/ 60)
                    (Math/floor))
        mins-as-sec (* minutes 60)
        seconds (- duration-sec (+ hours-as-sec mins-as-sec))]
    (str hours " h " minutes " m " seconds " s")))

(comment
  (seconds->formatted (+ 27
                         (* 60 22)
                         (* 3600 12))))

(defn journey-resp->item [journey-resp]
  (let [segments ["Walk" "R" "14"]
        duration (-> journey-resp
                     (:duration)
                     (seconds->formatted))
        start-date "07:10"
        start-station (-> journey-resp
                          (:sections)
                          (first)
                          (:from)
                          (:name))
        end-date "08:34"
        end-station (-> journey-resp
                        (:sections)
                        (last)
                        (:to)
                        (:name))]
    {::segments   segments
     ::duration   duration
     ::start-date start-date ::start-station start-station
     ::end-date   end-date ::end-station end-station}))

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