(ns app.build-hooks)

(defn include-css
  {:shadow.build/stage :compile-finish}
  [build-state & args]
  (prn :hello-world args)
  build-state)