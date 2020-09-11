(ns app.events
  (:require
    [re-frame.core :as re-frame]
    [app.db :as db]
    [app.effects :as ef]
    [app.secrets :as sec]
    [goog.string :as gstr]
    [goog.string.format]
    [app.config :as conf]))

(re-frame/reg-event-db
  ::db-initialization
  (fn [_ _]
    db/default-db))

(re-frame/reg-event-db
  ::navigation-to
  (fn [db [_ value]]
    (assoc db ::db/active-panel value)))

(re-frame/reg-event-db
  ::navigation-to-autosuggest
  (fn [db [_ value]]
    (assoc db ::db/active-panel ::db/panel-autosuggest
              ::db/autosuggest-query value)))

(def navitia-coverage "fr-idf")

(def autosuggest-debounce-delay-ms 1000)

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
      {::ef/get-suggestions
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

(re-frame/reg-event-db
  ::suggestions-resp-received
  (fn [db event-vec]
    (println event-vec)
    db))

(re-frame/reg-event-db
  ::suggestions-err-received
  (fn [db event-vec]
    (assoc db ::db/autosuggest-error
              (str event-vec))))

(defn- fake-journey [[num db] _]
  (comment (str "Journey from " (::db/journey-start db) " to " (::db/journey-end db) " " num))
  {::segments   ["Walk" "R" "14"]
   ::duration   "1 h 24"
   ::start-date "07:10" ::start-station "Montigny-sur-Loing"
   ::end-date   "08:34" ::end-station "Paris Gare de Lyon"})

(defn- only-start-and-end [db]
  [(::db/journey-start db)
   (::db/journey-end db)])

(defn- todo [desc & args]
  (println "To do:" desc args))

(defn- to-geocode-request [_]
  (todo "to-geocode-request"))

(defn- send-to-ws [_]
  (todo "send-to-ws"))

(defn- only-coordinates [_]
  (todo "only-coordinates"))

(defn- geocode [address]
  (println "Geocoding" address)
  (-> address
      (to-geocode-request)
      (send-to-ws)
      (only-coordinates)))

(defn- to-url-params [start-and-end]
  (todo "to-url-params")
  start-and-end)

(defn- journeys-request-url [start-and-end-params]
  (todo "journeys-request-url") start-and-end-params)

(defn- journeys-request-obj [request-url]
  {:method :get
   :url    request-url})

(defn- make-journeys-request
  "Build a request map that can be sent to the journeys web service to fetch journeys.
  Makes synchronous calls to a web service ⇒ has to run on a backaground thread."
  [db]
  (->> db
       (only-start-and-end)                                 ; Extract ::db/journey-start and ::db/journey-end
       (map geocode)                                        ; Find coordinates of start and end
       (map to-url-params)                                  ; Convert coordinates to URL params
       (journeys-request-url)                               ; Build URL
       ;(journeys-request-obj)                               ; Build request map
       ))

(comment
  (in-ns 'app.events)
  (make-journeys-request {::db/journey-start "a"
                          ::db/journey-end   "b"})
  (only-start-and-end {::db/journey-start "a"
                       ::db/journey-end   "b"})
  (->> ["a" "b"]
       (map geocode))
  (geocode "a"))

(defn- handle-get-journeys [db [_ _]]
  (as-> [1 2 3 4 5 6 7 8 9] v
        (map (fn [num] [num db]) v)                         ; `(map #([% db]) v)` does not work 🤔 - see https://stackoverflow.com/a/13206291/1665730
        (map fake-journey v)
        (assoc db ::db/journeys v)
        (assoc v ::db/active-panel ::db/panel-journeys)))

(re-frame/reg-event-db ::journey-search-submission handle-get-journeys)

(comment
  (in-ns 'app.events)
  (handle-get-journeys {::db/journey-start "a"
                        ::db/journey-end   "b"} []))