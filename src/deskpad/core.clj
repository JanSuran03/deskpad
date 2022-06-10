(ns deskpad.core
  (:require [deskpad.init :as init]))

(defn -main [& args]
  (init/run-separate-thread)
  (comment "for `lein run`"
           (Thread/sleep 100)
           (while @init/running?*)))
