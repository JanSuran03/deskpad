(ns deskpad.core
  (:require [deskpad.init :as init]
            [clojure.string :as str]))

(defn -main [& args]
  (init/run-separate-thread)
  (comment "for `lein run`"
           (Thread/sleep 100)
           (while @init/running?*)))

(comment
  (def min-per-day (* 60 24))

  (def palis (set (for [hr (range 24)
                        :when (< (rem hr 10) 6)
                        :let [min10 (rem hr 10)
                              min01 (quot hr 10)]]
                    (+ (* hr 60) (* min10 10) min01))))

  (defn solve []
    (let [process (fn []
                    (let [[^String start interval] (str/split (read-line) #"\ ")
                          [hr min] (str/split start #"\:")
                          [hr min] (map #(cond-> % (= (.charAt ^String % 0) \0) (subs 1)) [hr min])
                          [hr min interval] (map #(Integer/parseInt %) [hr min interval])]
                      (loop [cur (+ (* hr 60) min)
                             ret 0
                             gone #{}]
                        (if (contains? gone cur)
                          (println ret)
                          (recur (rem (+ cur interval) min-per-day)
                                 (if (contains? palis cur)
                                   (inc ret)
                                   ret)
                                 (conj gone cur))))))]
      (process))))