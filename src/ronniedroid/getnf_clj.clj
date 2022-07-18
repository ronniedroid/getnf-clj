(ns ronniedroid.getnf-clj
  (:gen-class)
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [ronniedroid.handle-fonts :refer
    [list-fonts download-all-fonts
     download-and-install-all-fonts
     download-and-install-multiple-fonts
     download-multiple-fonts
     install-multiple-fonts]]))

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
