(ns getnf-clj.core
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as chesh]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer
             [parse-opts]]
            [getnf-clj.nerd-fonts-list :as nfl]
            [clj-fuzzy.metrics :as fm]
            [clj-file-zip.core :as cfz])
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

(defn xdg-data-dir
  "Creates and points to the download location for the NerdFonts"
  [dir]
  (let [distination (str (System/getenv "HOME")
                         "/.local/share/"
                         dir)]
    (if-not (.exists (io/as-file distination))
      (.mkdir (io/file distination)))
    (str distination)))

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
  (as-> (get-release) release
    (str
     "https://github.com/ryanoasis/nerd-fonts/releases/download/"
     release
     "/"
     font
     ".zip")))

(defn font-exsists?
  "Checkes if the fonts is already downloads"
  [font]
  (let [font-file (str font ".zip")]
    (if-not (.exists
             (io/as-file (str (xdg-data-dir
                               "NerdFonts/")
                              font-file)))
      false
      true)))


(defn download
  "Downloads a single font to the download location"
  [font]
  (with-open [in (io/input-stream (download-link
                                   font))
              out (io/output-stream
                   (str (xdg-data-dir
                         "NerdFonts/")
                        font
                        ".zip"))]
    (io/copy in out))
  (println (str "'" font
                "' has been downloaded to "
                (xdg-data-dir "NerdFonts"))))

(defn install
  "Extracts the font to the apprpriate directory"
  [font]
  (cfz/unzip
   (str (xdg-data-dir "NerdFonts/") font ".zip")
   (xdg-data-dir "fonts/"))
  (println (str "'" font
                "' has been installed in "
                (xdg-data-dir "fonts"))))

(defn download-font
  "Checks if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nfl/nerd-fonts font)
    (if-not (font-exsists? font)
      (download font)
      (println (str font
                    " is already downloaded")))
    (println
     (str "Did you mean '"
          (fuzzy-search nfl/nerd-fonts font)
          "'"))))

(defn install-font
  "Checkqs if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nfl/nerd-fonts font)
    (if-not (font-exsists? font)
      (println
       (str
        font
        " is not downloaded yet, download it first with -d flag."))
      (install font))
    (println
     (str "Did you mean '"
          (fuzzy-search nfl/nerd-fonts font)
          "'"))))

(defn download-and-install-font
  "Checkqs if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nfl/nerd-fonts font)
    (if-not (font-exsists? font)
      (do (download font) (install font))
      (do
        (println
         (str
          font
          " is already downloaded, installing now"))
        (install font)))
    (println
     (str "Did you mean '"
          (fuzzy-search nfl/nerd-fonts font)
          "'"))))

(defn download-and-or-install-multiple-fonts
  "will download/install as many fonts as you provide it"
  [func & args]
  (map #(func %) args))

(defn download-all-fonts
  "Will download all the nerd fonts"
  []
  (map #(download-font %) nfl/nerd-fonts-names)
  (println
   (str "All the NerdFonts were downloaded to "
        (xdg-data-dir "NerdFonts"))))

(defn download-and-install-all-fonts
  "Will download and install all the nerd fonts"
  []
  (map #(download-and-install-font %)
       nfl/nerd-fonts-names)
  (println
   (str "All the NerdFonts were downloaded to "
        (xdg-data-dir "NerdFonts")
        " and installed to " (xdg-data-dir
                              "fonts"))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args])
