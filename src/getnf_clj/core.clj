(ns getnf-clj.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chesh]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer
             [parse-opts]]
            [getnf-clj.nerd-fonts-list :as nfl]
            [getnf-clj.fuzzy-search :as fzs])
  (:import [java.util zip.ZipInputStream]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(def nerd-fonts-repo
  "https://api.github.com/repos/ryanoasis/nerd-fonts/")

(def download-location
  (str (System/getenv "HOME") "/Downloads/"))

(defn get-release
  []
  (-> (client/get (str nerd-fonts-repo
                       "releases/latest"))
      (:body)
      (chesh/decode)
      (get "name")))

(defn download-link
  [font]
  (str
   "https://github.com/ryanoasis/nerd-fonts/releases/download/"
   (get-release)
   "/"
   font
   ".zip"))

(defn font-exsists?
  [font]
  (let [font-file (str font ".zip")]
    (if-not (.exists (io/as-file
                      (str download-location
                           font-file)))
      false
      true)))

(defn download-font
  [font]
  (with-open [in (io/input-stream (download-link
                                   font))
              out (io/output-stream
                   (str download-location
                        font
                        ".zip"))]
    (io/copy in out)))

(defn install-font [font] ())

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
