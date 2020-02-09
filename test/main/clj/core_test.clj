(ns rpp-clj.core-test
  (:require  [clojure.test :as t :refer [deftest is testing]]
             [rpp-clj.core :as rpp]))


(deftest parse-rpp-test
  (testing "Single level RPP"
    (let [rpp-string "<REAPER foo bar\n
                        ATTR param param 1\n
                      >"]
      (is (= [:<
              [:reaper "foo" "bar"]
              [:attr "param" "param" "1"]]
             (rpp/parse-rpp rpp-string)))))

  (testing "Nested structures"
    (let [rpp-string "<REAPER_PROJECT 0.1 \"5.979/linux64\" 1581079158\n
                        LOOP 0\n
                        LOOPGRAN 0 4\n
                        <RECORD_CFG\n
                        >\n
                        <APPLYFX_CFG\n
                        LOOP 0\n
                        LOOPGRAN 0 4\n
                        >\n
                      >"]

      (is (= [:<
              [:reaper-project "0.1" "\"5.979/linux64\"" "1581079158"]
              [:loop "0"]
              [:loopgran "0" "4"]
              [:<
               [:record-cfg]]
              [:<
               [:applyfx-cfg]
               [:loop "0"]
               [:loopgran "0" "4"]]]
             (rpp/parse-rpp rpp-string))))))
