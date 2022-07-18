(ns ronniedroid.getnf-clj-test
  (:require [clojure.test :refer :all]
            [ronniedroid.handle-fonts :refer
             :all]
            [ronniedroid.utils :refer :all]))

(deftest is-in?
  (testing
   "testing the in? function from utils"
   (is (= (in? [1 2 3] 1) true))))

(deftest is-fuzzy-search?
  (testing
   "testing the fuzzy-search function from utils"
   (is (= (fuzzy-search ["FiraCode" "Nato"
                         "Reboto"]
                        "code")
          "FiraCode"))))
