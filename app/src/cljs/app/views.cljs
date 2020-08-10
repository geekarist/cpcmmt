(ns app.views
  (:require
    [re-frame.core :as rf]
    [app.subs :as subs]
    [app.events :as ev]
    [clojure.string :as str]
    [app.db :as db]))

(defn home-panel []
  [:div.container
   [:button.btn.btn-primary.mt-3
    {:on-click #(rf/dispatch [::ev/navigate-to ::db/panel-start-end-selection])}
    "Where to?"]])

(defn start-end-selection-panel []
  (let [journey-start (rf/subscribe [::subs/journey-start])
        journey-end (rf/subscribe [::subs/journey-end])]
    [:div.container.mt-3
     [:div.form-group
      [:input.form-control
       {:type        "text"
        :placeholder "Start"
        :value       @journey-start
        :on-change   #(rf/dispatch [::ev/set-journey-start (-> % .-target .-value)])
        :on-focus    #(rf/dispatch [::ev/nav-to-autosuggest (-> % .-target .-value)])
        ;:on-blur     #(rf/dispatch [::ev/journey-start-validation])
        }]]
     [:div.form-group
      [:input.form-control
       {:type        "text"
        :placeholder "End"
        :value       @journey-end
        :on-change   #(rf/dispatch [::ev/set-journey-end (-> % .-target .-value)])
        :on-focus    #(rf/dispatch [::ev/nav-to-autosuggest (-> % .-target .-value)])
        ;:on-blur     #(rf/dispatch [::ev/journey-end-validation])
        }]]
     [:button.btn.btn-primary {:on-click #(rf/dispatch [::ev/get-journeys])} "Go!"]]))

(defn journey-view [journey]
  [:div.card.mb-3 {:key journey}
   [:div.card-body.pl-2.pr-2
    [:div.container
     [:div.row.no-gutters.pb-2
      [:div.col (->> (::ev/segments journey)
                     (str/join " · "))]
      [:div.col.text-right (::ev/duration journey)]]
     [:div.row.no-gutters.pl-1
      [:div.col-2.text-right.pr-1 "From:"]
      [:div.col-7 (::ev/start-station journey)]
      [:div.col-3.text-right (::ev/start-date journey)]]
     [:div.row.no-gutters.pl-1
      [:div.col-2.text-right.pr-1 "To:"]
      [:div.col-7 (::ev/end-station journey)]
      [:div.col-3.text-right (::ev/end-date journey)]]]]])

(defn journeys-panel []
  (let [journeys (rf/subscribe [::subs/journeys])]
    [:div.container.mt-3 (map journey-view @journeys)]))

(defn autosuggest-panel []
  (let [value (rf/subscribe [::subs/autosuggest-value])]
    [:p (str "Yo! " @value)]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (fn []
      (condp = @active-panel
        ::db/panel-home (home-panel)
        ::db/panel-start-end-selection (start-end-selection-panel)
        ::db/panel-journeys (journeys-panel)
        ::db/panel-autosuggest (autosuggest-panel)))))
