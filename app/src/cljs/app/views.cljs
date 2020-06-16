(ns app.views
  (:require
    [re-frame.core :as re-frame]
    [app.subs :as subs]
    ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:button {:on-click #(re-frame/dispatch [:open-search])} "Where to?"]
     ]))
