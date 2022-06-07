(ns deskpad.util
  (:refer-clojure :exclude [case])
  (:import (org.lwjgl.system MemoryUtil)))

(defonce window* (atom -1))

(defonce null MemoryUtil/NULL)

(defmacro case [expr & clauses]
  (let [expanded (loop [[condition then :as clauses] clauses
                        ret []]
                   (cond (nil? condition) ret
                         (nil? then) (conj ret condition)
                         :else (recur (nnext clauses)
                                      (conj ret (if (symbol? condition)
                                                  (if-let [v (resolve condition)]
                                                    @v
                                                    (println "Couldn't resolve symbol:" condition))
                                                  condition)
                                            then))))]
    (if (even? (count expanded))
      `(clojure.core/case ~expr ~@expanded nil)
      (list* 'clojure.core/case expr expanded))))