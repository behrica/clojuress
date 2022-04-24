;; copied from Rincanter https://github.com/skm-ice/rincanter/blob/master/src/rincanter/proc.clj
;; originally from https://gist.github.com/codification/1984857

(ns clojisr.v1.impl.rserve.proc
  (:require [clojure.java.io :refer [reader writer]]
            [clojure.java.shell :refer [sh]]
            [clojisr.v1.util :refer [file-exists?]]
            [clojure.tools.logging.readable :as log])
  (:import [java.lang ProcessBuilder]))

(defn spawn [& args]
  (log/info [::spawn {:process args}])
  (let [process (-> (ProcessBuilder. ^java.util.List args)
                    (.start))]
    {:out (-> process
              (.getInputStream)
              (reader))
     :err (-> process
              (.getErrorStream)
              (reader))
     :in (-> process
             (.getOutputStream)
             (writer))
     :process process
     :args args}))

;; Running Rserve -- copied from Rojure:
;; https://github.com/behrica/rojure

(defn- r-path
  "Find path to R executable"
  []
  {:post [(not= % "")]}
  (apply str
         (-> (sh "which" "R")
             (get :out)
             (butlast)))) ; avoid trailing newline

(defn alive? [rserve]
  (when rserve
    (.isAlive ^Process (:process rserve))))

(defn close [rserve]
  (when rserve
    (let [p ^Process (:process rserve)]
      (when (.isAlive p)
        (.destroy p)))))

(defn start-rserve
  "Boot up RServe in another process.
   Returns a map with a java.lang.Process that can be 'destroy'ed"
  [port init-r]
  (let [rstr-temp (format
                   (if (file-exists? "/etc/Rserv.conf")
                     "library(Rserve); run.Rserve(port=%s, config.file='/etc/Rserv.conf');"
                     "library(Rserve); run.Rserve(port=%s);")
                   port)
        rstr (if (and init-r (string? init-r) (seq init-r))
               (str init-r ";" rstr-temp )
               rstr-temp)]
    
    (spawn (r-path)
           "--no-save" ; don't save workspace when quitting
           "--no-restore-data"
           "--slave"
           "-e" ; evaluate (boot server)
           rstr)))
