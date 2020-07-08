(ns app.build-hooks
  (:require [me.raynes.fs :as fs]))

(defn include-file
  {:shadow.build/stage :compile-finish}
  [build-state & args]
  (let [input-path (first args)
        output-path (second args)]
    (fs/copy-dir input-path output-path))
  build-state)