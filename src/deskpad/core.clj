(ns deskpad.core
  (:require [deskpad.init :as init]
            [clojure.string :as str]))

(defn -main [& args]
  (init/run-separate-thread)
  (comment "for `lein run`"
           (Thread/sleep 100)
           (while @init/running?*)))
