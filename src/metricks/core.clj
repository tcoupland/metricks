(ns metricks.core
  (:import com.yammer.metrics.core.MetricName)
  (:import com.yammer.metrics.Metrics)
  (:import com.yammer.metrics.core.Timer)
  (:import java.util.concurrent.TimeUnit)
  (:use [clojure.string :only (split)]))

(defmulti new-metric-name (fn [list] (count list)))
(defmethod new-metric-name 3 [list]
  (new MetricName (nth list 0) (nth list 1) (nth list 2)))

(defn ns-name-str [func-meta] (str (ns-name (:ns func-meta))))
(defn ns-name-drop-first [func-meta]
  (let [name (ns-name-str func-meta)]
    (subs name (+ 1 (.indexOf name ".")))))

(defn map-element [func-meta spec]
  (cond
    (identical? :name-space spec) (ns-name-str func-meta)
    (identical? :name-space-rest spec) (ns-name-drop-first func-meta)
    (identical? :func-name spec) (str (:name func-meta))
    :else (str spec)))

(defn map-metadata-to-name-spec [func-meta name-spec]
  (map
   (partial map-element func-meta)
   name-spec))

(def metricks-config
  (atom
   {:spec ["info" :name-space :func-name]
    :spec-mapper map-metadata-to-name-spec}))

(defn update-spec [new-spec]
  (swap! metricks-config assoc :spec new-spec))

(defn transform-metric [name metric]
  {:name (apply str (interpose "." [(.getGroup name) (.getType name) (.getName name)]))
   :count (.count metric)})

(defn get-metrics []
  (let [raw-metrics (.allMetrics (Metrics/defaultRegistry))]
    (map #(transform-metric (key %) (val %)) raw-metrics)))

(defn get-timer [func-meta]
  (let [metadata-mapper (:spec-mapper @metricks-config)
        name-spec (:spec @metricks-config)
        metric-name (new-metric-name (metadata-mapper func-meta name-spec))]
    (Metrics/newTimer metric-name TimeUnit/MILLISECONDS TimeUnit/SECONDS)))


(defn- wrap-timer [meta-func func]
  (let [m (get-timer meta-func)]
    (fn [& args]
      (let [start (. System currentTimeMillis)]
        (try
          (apply func args)
          (finally (.update m (- (. System currentTimeMillis) start) TimeUnit/MILLISECONDS)))))))

(defn- apply-metricks-to-func [func]
  (let [wrap-with-meta (partial wrap-timer (meta func))]
    (alter-var-root func wrap-with-meta)))

(defn apply-metricks [name-space]
  (doall
   (map
    apply-metricks-to-func
    (vals (ns-publics name-space)))))
