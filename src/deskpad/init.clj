(ns deskpad.init
  (:require [cljgl.common.debug :as debug]
            [cljgl.common.cleanup :as cleanup]
            [cljgl.common.colors :as colors]
            [cljgl.common.gl-util :as gl-util]
            [deskpad.callbacks :as callbacks]
            [cljgl.glfw.glfw :as glfw]
            [cljgl.math.matrix4f :as mat4f]
            [cljgl.opengl.gl :as gl]
            [cljgl.opengl.renderer :as renderer]
            [cljgl.opengl.shaders :as shaders]
            [cljgl.opengl.textures :as textures]
            [clojure.core.async :refer [thread]]
            [deskpad.util :as util :refer [window*]])
  (:import (org.lwjgl.opengl GL33)))

(defonce running?* (atom false))

(defn init-window []
  (glfw/enable-error-callback-print)
  (when-not (glfw/init)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (glfw/default-window-hints)
  (glfw/window-hint-visible false)
  (glfw/window-hint-resizable true)
  (reset! window* (glfw/create-window callbacks/default-width callbacks/default-height "Hello GL"))
  (when (nil? @window*)
    (throw (RuntimeException. "Failed to create GLFW window")))
  (callbacks/init-callbacks)
  (glfw/make-context-current @window*)
  (gl/create-capabilities)
  (glfw/center-window @window*)
  (glfw/vsync))

(defn init []
  (init-window)
  ;(gl/blend)
  (renderer/setup-renderer {:shaders-source-path        (util/shaders-root "triangle-with-texture.glsl")
                            :usage-type                 :static-draw
                            :vertex-buffer              {:attributes-setups [{:components 2
                                                                              :gl-type    :gl-float
                                                                              :normalize? false
                                                                              :id         :vertpos}
                                                                             {:components 2
                                                                              :gl-type    :gl-float
                                                                              :normalize? false
                                                                              :id         :texpos}]}
                            :vertex-data                [{:vertpos [100 100] :texpos [0 0]}
                                                         {:vertpos [700 100] :texpos [1 0]}
                                                         {:vertpos [700 500] :texpos [1 1]}
                                                         {:vertpos [100 500] :texpos [0 1]}
                                                         {:vertpos [900 500] :texpos [0 0]}
                                                         {:vertpos [1500 500] :texpos [1 0]}
                                                         {:vertpos [1500 900] :texpos [1 1]}
                                                         {:vertpos [900 900] :texpos [0 1]}]
                            :indices                    [0 1 2 2 3 0, 4 5 6 6 7 4]
                            :shader-program-lookup-name :shader/hello-rectangle
                            :renderer-lookup-name       :renderer/hello-rectangle})
  (textures/texture (util/images-root "penguin.gif") :texture/penguin :texture-slot 0)
  (when true
    (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)
    (GL33/glEnable GL33/GL_BLEND))
  (shaders/set-uniform-1i (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                          "u_texture"
                          (textures/get-texture-slot (textures/get :texture/penguin)))
  (renderer/change-rendering-order [:renderer/hello-rectangle])
  (glfw/show-window @window*)
  (let [mvp (mat4f/model-view-projection-matrix
              {:projection-matrix (mat4f/orthogonal 0 callbacks/default-width 0 callbacks/default-height -1 1)})]
    (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                               "u_MVP"
                               mvp)))

(defn gl-loop-cleanup []
  (glfw/swap-buffers @window*)
  (glfw/poll-events))

(defn gl-loop []
  (let [[r g b] (colors/get :teal)]
    (reset! util/delta-time (System/currentTimeMillis))
    (while (not (glfw/should-window-close? @window*))
      (debug/assert-all
        (gl/clear-color r g b 0)
        (gl/clear-bits gl/color-buffer-bit)
        (renderer/render-all)
        (gl-loop-cleanup)))))

(defn run []
  (println (str "Hello LWJGL " (gl/get-version) "!"))
  (try (init)
       (gl-loop)
       (finally
         (glfw/free-callbacks @window*)
         (glfw/destroy-window @window*)
         (reset! window* -1)
         (shaders/unbind-shader-program)
         (cleanup/cleanup)
         (glfw/terminate)
         (glfw/unbind-error-callback))))

(defn run-separate-thread []
  (thread (when-not @running?*
            (reset! running?* true)
            (gl-util/log "Rendering thread started." :important)
            (try (run)
                 (finally
                   (gl-util/log "Rendering thread interrupted." :important)
                   (reset! running?* false))))))