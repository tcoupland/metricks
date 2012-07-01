(ns metricks.core
  (:import com.yammer.metrics.core.MetricName)
  (:import com.yammer.metrics.Metrics)
  (:import com.yammer.metrics.core.Timer)
  (:import java.util.concurrent.TimeUnit)
  (:use [clojure.string :only (split)]))

  (def metric-name-spec ["info" :name-space :func-name])

(defmulti new-metric-name (fn [list] (count list)))
(defmethod new-metric-name 3 [list]
  (new MetricName (nth list 0) (nth list 1) (nth list 2)))

(defn transform-metric [map-entry]
  (let [name (key map-entry) timer (val map-entry)]
    {:name (apply str (interpose "." [(.getGroup name) (.getType name) (.getName name)]))
     :count (.count timer)}))

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

(defn map-metadata-to-name-spec
  ( [func-meta] (map-metadata-to-name-spec func-meta metric-name-spec))
  ( [func-meta name-spec] (map (partial map-element func-meta) name-spec)))

(defn get-timer [func-meta]
  (let [metric-name (new-metric-name (map-metadata-to-name-spec func-meta))]
    (Metrics/newTimer metric-name TimeUnit/MILLISECONDS TimeUnit/SECONDS)))

(defn get-metrics []
  (let [raw-metrics (.allMetrics (Metrics/defaultRegistry))]
    (map transform-metric raw-metrics)))

(defmacro timer [expr]
  `(let [~'func-meta (meta (var ~@expr))
         ~'m (get-timer ~'func-meta)
         ~'start (. System currentTimeMillis)]
     (try
       (~@expr)
       (finally (.update ~'m (- (. System currentTimeMillis) ~'start) TimeUnit/MILLISECONDS)))))
