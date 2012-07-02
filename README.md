# metricks

Clojure wrapper for the excellant codahale metrics library (https://github.com/codahale/metrics).

Metric names are created according to a customisable metric-name-spec. This specification vector describes which parts of a methods meta data should be used to build a name.

This allows metrics to be recorded for a function simply by wrapping it with a call to the desired metric:
```clj
(timer (your-function))
```
This is still very much a proof of concept and only Timer's are supported so far. However the next step will be to add a way you can get a grip on your metrics and also to support posting recorded metrics to a graphite instance for graphing. Extra metrics will get added either as i need them or they are requested.

Enjoy!

#Usage

Add metricks to your project.clj
```clj
[org.clojars.mantree/metricks "0.1.6"]
```
Then add a :require to your name space
```clj
(:require [metricks.core :as mc])
```
Now you can decorate your function calls to generate metrics for them
```clj
(mc/timer (your-func args))
```
This will use the default name specification:
```clj
["info" :name-space :func-name]
```
So if you wrapped a call to println like so:
```clj
(mc/timer (println "Hello Metricks!"))
```
You'd get a metric called: "info.clojure.core.println"

You can modify the name-spec to fit your naming patterns:
```clj
(mc/update-spec ["metrics" :name-space-rest :func-name])
```
Now the call would create a metric called: "metrics.core.println"

The mapping function used to map function meta-data to metric names is also modifiable (their both in the atom 'metricks-config').
