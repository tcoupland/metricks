# metricks

Clojure wrapper for the excellant codahale metrics library (https://github.com/codahale/metrics).

Metric names are created according to a customisable metric-name-spec. This specification vector describes which parts of a methods meta data should be used to build a name.

This allows metrics to be recorded for a function simply by wrapping it with a call to the desired metric:

(time (your-function))

This is still very much a proof of concept and only Timer's are supported so far. However the next step will be to add a way you can get a grip on your metrics and also to support posting recorded metrics to a graphite instance for graphing. Extra metrics will get added either as i need them or they are requested.

Enjoy!
