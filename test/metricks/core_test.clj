(ns metricks.core-test
  (:use clojure.test
        metricks.core)
  (:import com.yammer.metrics.core.MetricName)
  (:import com.yammer.metrics.Metrics))

(deftest test-spec-mapping
  (testing "Test default spec"
    (is (= ["info" "metricks.core" "map-element"] (map-metadata-to-name-spec (meta  (var map-element))))))

  (testing "Test custom spec"
    (is (= ["cust" "core" "map-element"] (map-metadata-to-name-spec (meta  (var map-element)) ["cust" :name-space-rest :func-name] ))))

  (testing "Test timing something"
    (time (delay 100)) (prn  (get-metrics) ))

  )
