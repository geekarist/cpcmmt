(ns app.build-hooks
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import (java.io File)))

(defn- to-output-path [input-path]
  (as-> input-path v
        (str/split v (re-pattern (File/separator)))
        (last v)))

(defn include-file
  {:shadow.build/stage :compile-finish}
  [build-state & args]
  (let [input-path (first args)
        output-path (to-output-path input-path)]
    (io/copy (io/file input-path) (io/file output-path)))
  build-state)