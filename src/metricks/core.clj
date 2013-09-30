(ns metricks.core
  (:import com.yammer.metrics.Metrics)
  (:import [com.yammer.metrics.core Timer Gauge MetricName])
  (:import java.util.concurrent.TimeUnit)
  (:import clojure.lang.Atom)
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

(defn- create-metric-name
  [func-meta]
  (let [config @metricks-config
        metadata-mapper (:spec-mapper config)
        name-spec (:spec config)]
    (new-metric-name (metadata-mapper func-meta name-spec))))

(defn update-spec [new-spec]
  (swap! metricks-config assoc :spec new-spec))

(defmulti transform-metric (fn [name metric] (.getClass metric)))
(defmethod transform-metric Timer [name metric]
  {:type :timer
   :name (apply str (interpose "." [(.getGroup name) (.getType name) (.getName name)]))
   :count (.count metric)})
(defmethod transform-metric Gauge [name metric]
  {:type :gauge
   :name (apply str (interpose "." [(.getGroup name) (.getType name) (.getName name)]))
   :value (.value metric)})

(defn get-metrics []
  (let [raw-metrics (.allMetrics (Metrics/defaultRegistry))]
    (map #(transform-metric (key %) (val %)) raw-metrics)))

(defn get-timer [func-meta]
  (Metrics/newTimer
   (create-metric-name func-meta)
   TimeUnit/MILLISECONDS TimeUnit/SECONDS))

(defn- wrap-func-with-timer [meta-func func]
  (let [m (get-timer meta-func)]
    (fn [& args]
      (let [start (. System currentTimeMillis)]
        (try
          (apply func args)
          (finally (.update m (- (. System currentTimeMillis) start) TimeUnit/MILLISECONDS)))))))

(defn- add-timer
  [meta-func func]
  (alter-var-root func (partial wrap-func-with-timer (meta func))))

(defn- atom?
  [ref]
  (instance? Atom ref))

(defn- gauge-value
  [func-meta atom-var]
  (let [atom (var-get atom-var)]
    (when (atom? atom)
      (Metrics/newGauge
       (create-metric-name func-meta)
       (proxy [Gauge] []
         (value [] (deref atom)))))))

(def meta-key-to-wrapper
  {:timer add-timer
   :gauge gauge-value})

(defn- apply-metricks-to-func [func]
  (let [func-ks (set (keys (meta func)))]
    (doall
     (for [k (keys meta-key-to-wrapper)
           :when (contains? func-ks k)]
       ((k meta-key-to-wrapper) (meta func) func)))))

(defn apply-metricks [& name-spaces]
  (doall
   (->>
    (map ns-publics name-spaces)
    (mapcat vals)
    (map apply-metricks-to-func))))
