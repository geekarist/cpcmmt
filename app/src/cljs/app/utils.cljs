(ns app.utils)

(defn json->clj [json-str]
  (as-> json-str v
        (.parse js/JSON v)
        (js->clj v :keywordize-keys true)))