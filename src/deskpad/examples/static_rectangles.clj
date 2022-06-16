(ns deskpad.examples.static-rectangles
  (:require [cljgl.opengl.renderer :as renderer]
            [deskpad.util :as util]))

(defn renderer []
  (renderer/setup-renderer {:shaders-source-path        (util/shaders-root "hello-rectangle.glsl")
                            :usage-type                 :static-draw
                            :vertex-buffer              {:attributes-setups [{:components 2
                                                                              :gl-type    :gl-float
                                                                              :normalize? false
                                                                              :id         :vertpos}]}
                            :vertex-data                [{:vertpos [-0.8 -0.7]}
                                                         {:vertpos [-0.2 -0.7]}
                                                         {:vertpos [-0.2 -0.3]}
                                                         {:vertpos [-0.8 -0.3]}
                                                         {:vertpos [0.2 0.3]}
                                                         {:vertpos [0.8 0.3]}
                                                         {:vertpos [0.8 0.7]}
                                                         {:vertpos [0.2 0.7]}]
                            :indices                    [0 1 2 2 3 0, 4 5 6 6 7 4]
                            :shader-program-lookup-name :shader/hello-rectangle
                            :renderer-lookup-name       :renderer/hello-rectangle}))

(defn setup []
  (renderer)
  (renderer/change-rendering-order [:renderer/hello-rectangle]))