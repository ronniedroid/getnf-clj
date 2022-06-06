(defproject getnf-clj "0.1.0"
  :description
  "A better way to install NerdFonts, Clojure Version"
  :url "http://github.com/ronniedroid/getnf-clj"
  :license {:name "The MIT License (MIT)",
            :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [cheshire "5.10.2"]
                 [clj-http "3.12.3"]
                 [clj-fuzzy "0.4.1"]
                 [clj-file-zip "0.1.0"]]
  :main getnf-clj.core
  :target-path "target/%s"
  :profiles
  {:uberjar
   {:aot :all,
    :jvm-opts
    ["-Dclojure.compiler.direct-linking=true"]}})
