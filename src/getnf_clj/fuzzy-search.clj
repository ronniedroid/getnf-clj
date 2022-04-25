(ns getnf-clj.fuzzy-search
  (:gen-class)
  (:require [clj-fuzzy.metrics :as fm]))

(defn index-of
  [item coll]
  (count (take-while (partial not= item) coll)))

(defn fuzzy-search-match-dice
  [font coll]
  (map #(fm/dice % font) coll))

(defn index-of-highist-match
  [font coll]
  (let [diced-list (fuzzy-search-match-dice font
                                            coll)]
    (index-of (apply max diced-list) diced-list)))

(defn fuzzy-search
  [font coll]
  (nth coll (index-of-highist-match font coll)))
