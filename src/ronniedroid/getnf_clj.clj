(set! *warn-on-reflection* true)
(ns ronniedroid.getnf-clj
  (:gen-class)
  (:require
   [cheshire.core :as chesh]
   [clj-file-zip.core :as cfz]
   [clj-fuzzy.metrics :as fm]
   [clj-http.client :as client]
   [clojure.java.io :as io]
   [clojure.tools.cli :refer [parse-opts]]
   [ronniedroid.nerd-fonts-list :refer
    [nerd-fonts nerd-fonts-names]]))


(defn in?
  "true if coll contains elm"
  [coll elm]
  (->> coll
       (some #(= elm %))))

(defn fuzzy-search
  "Does a fuzzy search and returns the best match"
  [coll font]
  (->> coll
       (sort-by #(fm/dice % font))
       (last)))

(def nerd-fonts-repo
  "https://api.github.com/repos/ryanoasis/nerd-fonts/")

(defn xdg-data-dir
  "Creates and points to the download location for the NerdFonts"
  [dir]
  (let [distination (str (System/getenv
                          "HOME")
                         "/.local/share/"
                         dir)]
    (if (.exists (io/as-file distination))
      (str distination)
      (do (.mkdir (io/file distination))
          (str distination)))))

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
  "Extracts the font to the apprpriate directory"
  [font]
  (cfz/unzip (str (xdg-data-dir "NerdFonts/")
                  font
                  ".zip")
             (xdg-data-dir "fonts/"))
  (println (str "'" font
                "' has been installed in "
                (xdg-data-dir "fonts"))))

(defn download-font
  "Checks if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nerd-fonts-names font)
    (if-not (font-exsists? font)
      (download font)
      (println
       (str font " is already downloaded")))
    (println
     (str "Did you mean '"
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn install-font
  "Checkqs if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nerd-fonts-names font)
    (if-not (font-exsists? font)
      (println
       (str
        font
        " is not downloaded yet, download it first with -d flag."))
      (install font))
    (println
     (str "Did you mean '"
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn download-and-install-font
  "Checkqs if the font is a nerd font and if it has already been
  downloaded, if not, it will download it"
  [font]
  (if (in? nerd-fonts-names font)
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
          (:name (fuzzy-search nerd-fonts
                               font)
                 "'")))))

(defn download-multiple-fonts
  "will download and install as many fonts as you provide it"
  [args]
  (run! #(download-font %) args))

(defn install-multiple-fonts
  "will download and install as many fonts as you provide it"
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
  "Will download and install all the nerd fonts"
  []
  (run! #(download-and-install-font %)
        nerd-fonts-names)
  (println
   (str
    "All the NerdFonts were downloaded to "
    (xdg-data-dir "NerdFonts")
    " and installed to " (xdg-data-dir
                          "fonts"))))

(def cli-options
  ;; An option with a required argument
  [["-d" "--download"] ["-i" "--install"]
   ["-A" "--download-all"]
   ["-a" "--download-install-all"]
   ["-l" "--list"] ["-h" "--help"]])

(defn -main
  "parses command line arguments and runs
  operations."
  [& args]
  (let [arguments (parse-opts args
                              cli-options)
        options (:options arguments)
        summary (:summary arguments)
        fonts (:arguments arguments)]
    (if (:help options)
      (println summary)
      (let [{:keys
             [download install download-all
              download-install-all list]}
            options]
        (if (and download install)
          (download-and-install-multiple-fonts
           fonts)
          (cond
            download (download-multiple-fonts
                      fonts)
            install (install-multiple-fonts
                     fonts)
            download-all (download-all-fonts)
            download-install-all
            (download-and-install-all-fonts)
            list (list-fonts)
            :else
            (download-and-install-multiple-fonts
             fonts)))))))
