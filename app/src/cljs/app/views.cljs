(ns app.views
  (:require
    [re-frame.core :as rf]
    [app.subs :as subs]
    [app.events :as ev]
    [clojure.string :as str]))

(defn home-panel []
  [:div.container
   [:button.btn.btn-primary.mt-3
    {:on-click #(rf/dispatch [::ev/navigate-to :panel/start-end-selection])}
    "Where to?"]])

(defn start-end-selection-panel []
  [:div.container.mt-3
   [:div.form-group [:input.form-control {:type        "text"
                                          :placeholder "Start"
                                          :on-change   #(rf/dispatch [::ev/set-journey-start (-> % .-target .-value)])}]]
   [:div.form-group [:input.form-control {:type        "text"
                                          :placeholder "End"
                                          :on-change   #(rf/dispatch [::ev/set-journey-end (-> % .-target .-value)])}]]
   [:button.btn.btn-primary {:on-click #(rf/dispatch [::ev/get-journeys])} "Go!"]])

(defn journey-view [journey]
  [:div.card.mb-3 {:key journey}
   [:div.card-body
    [:p (->> (::ev/segments journey)
             (str/join " · "))]
    [:p "Duration: " (::ev/duration journey)]
    [:p "From: " (::ev/start-station journey) " (" (::ev/start-date journey) ")"]
    [:span "To: " (::ev/end-station journey) " (" (::ev/end-date journey) ")"]]])

(defn journeys-panel []
  (let [journeys (rf/subscribe [::subs/journeys])]
    [:div.container.mt-3 (map journey-view @journeys)]))

(defn main-panel []
  (let [active-panel (rf/subscribe [::subs/active-panel])]
    (fn []
      (condp = @active-panel
        :panel/home (home-panel)
        :panel/start-end-selection (start-end-selection-panel)
        :panel/journeys (journeys-panel)))))
