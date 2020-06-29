(ns app.views
  (:require
    [re-frame.core :as re-frame]
    [app.subs :as subs]
    ))

(defn home-panel []
  [:button
   {:on-click #(re-frame/dispatch [:navigate-to :panel/start-end-selection])}
   "Where to?"])

(defn start-end-selection-panel []
  [:p "Please select your start and end points"])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [:active-panel])]
    (fn []
      [:div
       ((condp = @active-panel
          :panel/home home-panel
          :panel/start-end-selection start-end-selection-panel))
       ])))
