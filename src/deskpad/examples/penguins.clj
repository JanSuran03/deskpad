(ns deskpad.examples.penguins
  (:require [cljgl.common.gl-util :as gl-util]
            [cljgl.math.matrix4f :as mat4f]
            [cljgl.opengl.renderer :as renderer]
            [cljgl.opengl.shaders :as shaders]
            [cljgl.opengl.textures :as textures]
            [deskpad.callbacks :as callbacks]
            [deskpad.util :as util])
  (:import (org.lwjgl.opengl GL33)))

(def MAX-PENGUINS 64)

(defn renderer []
  (renderer/setup-renderer {:shaders-source-path        (util/shaders-root "triangle-with-texture.glsl")
                            :usage-type                 :dynamic-draw
                            :vertex-buffer              {:attributes-setups [{:components 2
                                                                              :gl-type    :gl-float
                                                                              :normalize? false
                                                                              :id         :vertpos}
                                                                             {:components 2
                                                                              :gl-type    :gl-float
                                                                              :normalize? false
                                                                              :id         :texpos}]}
                            :vbo-byte-size              (* (gl-util/sizeof :gl-float)
                                                           4 ; vertices by penguin
                                                           (+ 2 2) ; 2 vertex coords, 2 relative penguin image coordinates
                                                           MAX-PENGUINS)
                            :ebo-byte-size              (* (gl-util/sizeof :gl-int)
                                                           6 ; elements by penguin
                                                           MAX-PENGUINS)
                            :shader-program-lookup-name :shader/dynamic-penguins
                            :renderer-lookup-name       :renderer/dynamic-penguins}))

(defn textures []
  (textures/texture (util/images-root "penguin.gif") :texture/penguin :texture-slot 0)
  (shaders/set-uniform-1i (.-shader_program (renderer/get-renderer :renderer/dynamic-penguins))
                          "u_texture"
                          (textures/get-texture-slot (textures/get :texture/penguin))))

(defn setup []
  (renderer)
  (textures)
  (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)
  (GL33/glEnable GL33/GL_BLEND)
  (renderer/change-rendering-order [:renderer/dynamic-penguins])
  (let [mvp (mat4f/model-view-projection-matrix
              {:projection-matrix (mat4f/orthogonal 0 callbacks/default-width 0 callbacks/default-height -1 1)})]
    (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/dynamic-penguins))
                               "u_MVP"
                               mvp)))