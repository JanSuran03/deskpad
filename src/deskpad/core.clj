(ns deskpad.core
  (:require [deskpad.init :as init]))

(defn -main [& args]
  (init/run-separate-thread))
