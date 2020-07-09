(ns app.views
  (:require
    [re-frame.core :as rf]
    [app.subs :as subs]
    [app.events :as ev]))

(defn home-panel []
  [:div.container
   [:button.btn.btn-primary.mt-3
    {:on-click #(rf/dispatch [::ev/navigate-to :panel/start-end-selection])}
    "Where to?"]])

(defn start-end-selection-panel []
  [:div
   [:p [:input {:type        "text"
                :placeholder "Start"
                :on-change   #(rf/dispatch [::ev/set-journey-start (-> % .-target .-value)])}]]
   [:p [:input {:type        "text"
                :placeholder "End"
                :on-change   #(rf/dispatch [::ev/set-journey-end (-> % .-target .-value)])}]]
   [:p [:button {:on-click #(rf/dispatch [::ev/get-journeys])} "Go!"]]])

(defn journey-view [journey]
  [:div {:key journey} journey])

(defn journeys-panel []
  (let [journeys (rf/subscribe [::subs/journeys])]
    [:div (map journey-view @journeys)]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (fn []
      (condp = @active-panel
        :panel/home (home-panel)
        :panel/start-end-selection (start-end-selection-panel)
        :panel/journeys (journeys-panel)))))
