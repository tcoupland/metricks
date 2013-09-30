# metricks

Clojure wrapper for the excellant codahale metrics library (https://github.com/codahale/metrics).

Metric names are created according to a customisable metric-name-spec. This specification vector describes which parts of a methods meta data should be used to build a name.

In short it takes two steps to get this working:

First you have to register the namespaces you want to use metrics in:

(apply-metricks 'my.namespace)

Then you simply add metadata to the functions you want metrics for:

(defn ^:timer foo [] "bar")

Enjoy!

#Usage

Add metricks to your project.clj

[org.clojars.mantree/metricks "0.1.9"]

Bring in the apply-metricks function to your setup namespace

(:require [metricks.core :refer [apply-metricks])

Tell metricks about teh namespaces you want to monitor

(apply-metricks 'my.namespace 'my.othernamespace)

Now you can add meta data to your functions to time them

(defn ^:timer foo [] "bar")

Or you can gauge your atoms:

(def ^:gauge fizz (atom "buzz"))

This will use the default name specification:

["info" :name-space :func-name]

So for the examples above, you'd get timer metric called "info.my.namespace.foo" and a gauge called "info.my.namespace.fizz"
