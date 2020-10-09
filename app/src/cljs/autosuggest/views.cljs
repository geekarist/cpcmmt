(ns autosuggest.views
  (:require [re-frame.core :as rf]
            [autosuggest.subs :as subs]
            [autosuggest.events :as ev]))

(defn autosuggest-button [on-click text]
  [:button.list-group-item.list-group-item-action
   {:on-click #(do (on-click text)
                   (rf/dispatch [::ev/reset text]))}
   text])

(defn assoc-unless "Associate `k` to `v` in `coll` unless `cond?` is verified" [coll k v cond?]
  (if cond? coll
            (assoc coll k v)))

(defn autosuggest-component [initial-query on-suggestion-selected]
  (let [query-updated? @(rf/subscribe [::subs/query])
        error @(rf/subscribe [::subs/autosuggest-error])
        results @(rf/subscribe [::subs/autosuggest-results])]
    [:div.container.mt-3
     [:div.form-group
      [:input#autosuggest-query-field.form-control
       (-> {:type      "text"
            :on-select #(rf/dispatch [::ev/autosuggest-query-change (-> % .-target .-value)])
            :on-change #(rf/dispatch [::ev/autosuggest-query-change (-> % .-target .-value)])}
           (assoc-unless :value initial-query query-updated?))]]
     (if error
       [:div.alert.alert-warning "Error fetching suggestions: " error]
       (->> results
            (map (partial autosuggest-button on-suggestion-selected))
            (cons :div.list-group)
            (vec)))]))




