(ns metricks.core-test
  (:use clojure.test
        metricks.core)
  (:import com.yammer.metrics.core.MetricName))

(deftest test-spec-mapping
  (testing "Test default spec"
    (is (= ["info" "metricks.core" "map-element"] (map-metadata-to-name-spec (meta  (var map-element))))))

  (testing "Test custom spec"
    (is (= ["cust" "core" "map-element"] (map-metadata-to-name-spec (meta  (var map-element)) ["cust" :name-space-rest :func-name] ))))
  )
