(ns getnf-clj.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chesh]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer
             [parse-opts]]
            [getnf-clj.nerd-fonts-list :as nfl]
            [clj-fuzzy.metrics :as fm])
  (:import [java.util zip.ZipInputStream]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (->> coll
       (map :name)
       (some #(= elm %))))

(defn fuzzy-search
  "Does a fuzzy search and returns the best match"
  [coll font]
  (->> coll
       (sort-by #(fm/dice % font))
       (last)
       (:name)))

(def nerd-fonts-repo
  "https://api.github.com/repos/ryanoasis/nerd-fonts/")

(def download-location
  (str (System/getenv "HOME") "/Downloads/"))

(defn get-release
  "gets NerdFonts release version"
  []
  (-> (client/get (str nerd-fonts-repo
                       "releases/latest"))
      (:body)
      (chesh/decode)
      (get "name")))

(defn download-link
  "The download link to use"
  [font]
  (str
   "https://github.com/ryanoasis/nerd-fonts/releases/download/"
   (get-release)
   "/"
   font
   ".zip"))

(defn font-exsists?
  "Checkes if the fonts is already downloads"
  [font]
  (let [font-file (str font ".zip")]
    (if-not (.exists (io/as-file
                      (str download-location
                           font-file)))
      false
      true)))


(defn download-font
  "Downloads a single font to the download location"
  [font]
  (with-open [in (io/input-stream (download-link
                                   font))
              out (io/output-stream
                   (str download-location
                        font
                        ".zip"))]
    (io/copy in out))
  (println (str "'" font "' was downloaded")))

(defn check-and-download-font
  "Checkes if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nfl/nerd-fonts font)
    (if-not (font-exsists? font)
      (download-font font)
      (println (str font
                    " is already downloaded")))
    (println
     (str "Did you mean '"
          (fuzzy-search nfl/nerd-fonts font)
          "'"))))

(defn download-multiple-fonts
  "will download as many fonts as you provide it"
  [& args]
  (map #(check-and-download-font %) args))

(defn download-all-fonts
  "Will download all the nerd fonts for you"
  []
  (map #(check-and-download-font %)
       nfl/nerd-fonts-names))

(defn install-font
  "Extracts the font to the apprpriate directory"
  [font]
  ())

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
