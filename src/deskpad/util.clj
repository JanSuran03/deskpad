(ns deskpad.util
  (:refer-clojure :exclude [case]))

(defonce window* (atom -1))

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

(defn log
  ([message] (log message false))
  ([message important?]
   (.println System/err (if important? (str "***************************************\n"
                                            "LOG: " message
                                            "\n***************************************")
                                       message))))

(defn shaders-root [s] (str "resources/shaders/" s))