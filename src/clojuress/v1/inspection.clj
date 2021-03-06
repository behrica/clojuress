(ns clojuress.v1.inspection
  (:require [clojuress.v1.using-sessions :as using-sessions]))

(defn r-class [r-object]
  (vec
   (using-sessions/r-function-on-obj
    r-object "class" :strings)))

(defn names [r-object]
  (vec
   (using-sessions/r-function-on-obj
    r-object "names" :strings)))

(defn shape [r-object]
  (vec
   (using-sessions/r-function-on-obj
    r-object "dim" :ints)))

