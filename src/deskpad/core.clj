#_(require 'cljgl.common.debug)
#_(cljgl.common.debug/disable-assert!)
(ns deskpad.core
  (:require [deskpad.init :as init]))

(defn -main [& [example-id]]
  (init/run-separate-thread (or example-id :penguins))
  (comment "for `lein run`"
           (Thread/sleep 100)
           (while @init/running?*)))