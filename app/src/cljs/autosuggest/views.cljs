(ns autosuggest.views
  (:require [re-frame.core :as rf]
            [autosuggest.subs :as subs]
            [autosuggest.events :as ev]
            [autosuggest.db :as db]))

(defn autosuggest-button [on-click {:keys [::db/name _]}]
  [:button.list-group-item.list-group-item-action
   {:on-click #(do (on-click name)
                   (rf/dispatch [::ev/reset name]))}
   name])

(defn assoc-unless [coll k v cond?]
  (if cond?
    coll
    (assoc coll k v)))

(defn autosuggest-component [initial-query on-suggestion-selected]
  (let [updated-query @(rf/subscribe [::subs/query])
        query-updated? (some? updated-query)
        query-field-value (if updated-query updated-query
                                            (do (rf/dispatch [::ev/autosuggest-query-change initial-query])
                                                initial-query))
        error @(rf/subscribe [::subs/autosuggest-error])
        results @(rf/subscribe [::subs/autosuggest-results])]
    [:div.container.mt-3
     [:div.form-group
      [:input#autosuggest-query-field.form-control
       (-> {:type      "text"
            :on-change #(rf/dispatch [::ev/autosuggest-query-change (-> % .-target .-value)])}
           (assoc-unless :value query-field-value query-updated?))]]
     (if error
       [:div.alert.alert-warning "Error fetching suggestions: " error]
       (->> results #_(Collection `coll1 `of {:name "place-name" :id "place-id"})
            (map  #_(Map to the result of (autosuggest-button on-suggestion-selected coll1))
              (partial autosuggest-button on-suggestion-selected))
            (cons :div.list-group) #_(Insert hiccup element ID at the head of the collection)
            (vec) #_(Convert collection to vector)))]))




