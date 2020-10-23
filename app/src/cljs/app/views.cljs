(ns app.views
  (:require
    [re-frame.core :as rf]
    [app.subs :as subs]
    [app.events :as ev]
    [app.db :as db]
    [autosuggest.views]))

; Home

(defn home-panel []
  [:div.container
   [:button.btn.btn-primary.mt-3
    {:on-click #(rf/dispatch [::ev/navigation-to ::db/panel-start-end-selection])}
    "Where to?"]])

; Start/end selection

(defn start-end-selection-panel []
  (let [journey-start (rf/subscribe [::subs/journey-start])
        journey-end (rf/subscribe [::subs/journey-end])]
    [:div.container.mt-3
     [:div.form-group
      [:input.form-control
       {:type        "text"
        :placeholder "Start"
        :value       @journey-start
        :on-change   #(rf/dispatch [::ev/journey-start-change (-> % .-target .-value)])
        :on-focus    #(rf/dispatch [::ev/navigation-to-autosuggest-start
                                    (-> % .-target .-value)
                                    "autosuggest-query-field"])}]]
     [:div.form-group
      [:input.form-control
       {:type        "text"
        :placeholder "End"
        :value       @journey-end
        :on-change   #(rf/dispatch [::ev/journey-end-change (-> % .-target .-value)])
        :on-focus    #(rf/dispatch [::ev/navigation-to-autosuggest-end
                                    (-> % .-target .-value)
                                    "autosuggest-query-field"])}]]
     [:button.btn.btn-primary {:on-click #(rf/dispatch [::ev/journey-search-submission])} "Go!"]]))

; Journeys

(comment
  (->> ["a" "b" "c"]
       (map (fn [text] [:span text]))
       (interpose "·")))

(defn journey-view [journey]
  [:div.card.mb-3 {:key journey}
   [:div.card-body.pl-2.pr-2
    [:div.container
     [:div.row.no-gutters.pb-2
      [:div.col-8.d-flex.flex-row.flex-wrap
       (->> (::ev/segments journey)
            (map (fn [text] [:span.m-1.p-1.text-nowrap.border text])))]
      [:div.col-4.text-right.lead (::ev/duration journey)]]
     [:div.row.no-gutters.pl-1
      [:div.col-2.text-right.pr-1 "From:"]
      [:div.col-7 (::ev/start-station journey)]
      [:div.col-3.text-right (::ev/start-date journey)]]
     [:div.row.no-gutters.pl-1
      [:div.col-2.text-right.pr-1 "To:"]
      [:div.col-7 (::ev/end-station journey)]
      [:div.col-3.text-right (::ev/end-date journey)]]]]])

(defn journeys-panel []
  (let [journeys @(rf/subscribe [::subs/journeys])
        error @(rf/subscribe [::subs/journeys-error])]
    (if error
      [:div.alert.alert-warning "Error fetching journeys: " (str error)]
      [:div.container.mt-3 (map journey-view journeys)])))

; Autosuggest

(defn autosuggest-panel []
  (let [query @(rf/subscribe [::subs/autosuggest-initial-query])
        on-suggestion-selected #(rf/dispatch [::ev/nav-to-start-end-selection %])]
    [autosuggest.views/autosuggest-component query on-suggestion-selected]))

; App

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (fn []
      (condp = @active-panel
        ::db/panel-home (home-panel)
        ::db/panel-start-end-selection (start-end-selection-panel)
        ::db/panel-journeys (journeys-panel)
        ::db/panel-autosuggest (autosuggest-panel)))))
