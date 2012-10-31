(ns metricks.core-test
  (:use clojure.test
        metricks.core)
  (:import com.yammer.metrics.core.MetricName)
  (:import com.yammer.metrics.Metrics))



(defn a-func []
  (delay 100))

(defn add-func [val]
  (+ 5 val))

(deftest test-spec-mapping
  (testing "Test default spec"
    (is (= ["info" "metricks.core" "map-element"]
           (map-metadata-to-name-spec
            (meta (var map-element))
            ["info" :name-space :func-name]))))

  (testing "Test custom spec"
    (is (= ["cust" "core" "map-element"]
           (map-metadata-to-name-spec (meta  (var map-element)) ["cust" :name-space-rest :func-name] )))))

(defn- delay-sum [val]
  (delay val))


(deftest test-application-to-namespace
  (testing "Test application to funcs"
    (apply-metricks 'metricks.core-test)
    (a-func)
    (add-func 10)
    (let [a-funcs-metric
          (first (filter #(= "info.metricks.core-test.a-func" (:name %))
                         (get-metrics)))]
      (is (not (nil? a-funcs-metric)))
      (is (= 1 (:count a-funcs-metric))))
    (let [add-funcs-metric
          (first (filter #(= "info.metricks.core-test.add-func" (:name %))
                         (get-metrics)))]
      (is (not (nil? add-funcs-metric)))
      (is (= 1 (:count add-funcs-metric))))))
