(ns ronniedroid.utils
  (:require [clj-fuzzy.metrics :as fm]
            [babashka.fs :as fs]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (->> coll
       (some #(= elm %))))

(in? [:kyle "is cool" :shado "sucks"]
     :ronnie)

(defn fuzzy-search
  "Does a fuzzy search and returns the best match"
  [coll font]
  (->> coll
       (sort-by #(fm/dice % font))
       (last)))

(defn xdg-data-dir
  "Creates and points to the download location for the NerdFonts"
  [dir]
  (let [distination
        (str (fs/home) "/.local/share/" dir)]
    (if (fs/exists? distination)
      (str distination)
      (do (fs/create-dir distination)
          (str distination)))))

(defn font-exists?
  "Checkes if the fonts is already downloads"
  [font]
  (let [font-file (str font ".zip")]
    (if-not (fs/exists? (str (xdg-data-dir
                              "NerdFonts/")
                             font-file))
      false
      true)))
