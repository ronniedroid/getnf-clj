(ns ronniedroid.handle-fonts
  (:require [clojure.java.io :as io]
            [babashka.fs :as fs]
            [babashka.process :refer
             [process]]
            [cheshire.core :as chesh]
            [ronniedroid.utils :refer
             [in? font-exists? xdg-data-dir
              fuzzy-search]]
            [ronniedroid.nerd-fonts :refer
             [nerd-fonts nerd-fonts-names
              nerd-fonts-repo]]))

(defn update-font-cache
  []
  (process '[fc-cache -f]))

(defn get-release
  "gets NerdFonts release version"
  []
  (-> (slurp (str nerd-fonts-repo
                  "releases/latest"))
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

(defn list-fonts
  "lists all avilable fonts"
  []
  (->> nerd-fonts-names
       (map #(str "-> " %))
       (run! #(println %))))


(defn download
  "Downloads a single font to the download location"
  [font]
  (with-open [in (io/input-stream
                  (download-link font))
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
  "Extracts the font to the appropriate
  directory"
  [font]
  (fs/unzip (str (xdg-data-dir "NerdFonts/")
                 font
                 ".zip")
            (xdg-data-dir "fonts/")
            {:replace-existing true})
  (println (str "'" font
                "' has been installed in "
                (xdg-data-dir "fonts"))))

(defn download-font
  "Checks if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nerd-fonts-names font)
    (if-not (font-exists? font)
      (download font)
      (println
       (str font " is already downloaded")))
    (println
     (str "by " font
          ", Did you mean '"
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn install-font
  "Checkqs if the font is a nerd font and
  if it has already been
  downloaded, if not, it will download
  it"
  [font]
  (if (in? nerd-fonts-names font)
    (if-not (font-exists? font)
      (println
       (str
        font
        " is not downloaded yet, download
        it first with -d flag."))
      (install font))
    (println
     (str "by " font
          ", Did you mean '"
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn download-and-install-font
  "Checks if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nerd-fonts-names font)
    (if-not (font-exists? font)
      (do (download font) (install font))
      (do
        (println
         (str
          font
          " is already downloaded,
          installing now"))
        (install font)))
    (println
     (str "by " font
          ", Did you mean '"
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn download-multiple-fonts
  "will download as many fonts as you provide it"
  [args]
  (run! #(download-font %) args))

(defn install-multiple-fonts
  "will install as many fonts as you provide it"
  [args]
  (run! #(install-font %) args))

(defn download-and-install-multiple-fonts
  "will download and install as many fonts as you provide it"
  [args]
  (run! #(download-and-install-font %) args))

(defn download-all-fonts
  "Will download all the nerd fonts"
  []
  (run! #(download-font %) nerd-fonts-names)
  (println
   (str
    "All the NerdFonts were downloaded to "
    (xdg-data-dir "NerdFonts"))))

(defn download-and-install-all-fonts
  "Will download and install all the nerd
  fonts"
  []
  (run! #(download-and-install-font %)
        nerd-fonts-names)
  (println
   (str
    "All the NerdFonts were downloaded to
    "
    (xdg-data-dir "NerdFonts")
    " and installed to "
    (xdg-data-dir "fonts"))))
