(ns deskpad.init
  (:require [cljgl.common.debug :as debug]
            [cljgl.common.cleanup :as cleanup]
            [cljgl.common.colors :as colors]
            [cljgl.common.gl-util :as gl-util]
            [cljgl.glfw.glfw :as glfw]
            [cljgl.glfw.keys :as keys]
            [cljgl.math.matrix4f :as mat4f]
            [cljgl.opengl.gl :as gl]
            [cljgl.opengl.renderer :as renderer]
            [cljgl.opengl.shaders :as shaders]
            [cljgl.opengl.textures :as textures]
            [clojure.core.async :refer [thread]]
            [deskpad.util :as util :refer [window*]])
  (:import (org.lwjgl.opengl GL33)))

(defonce running?* (atom false))

(def default-width 1000)
(def default-height 600)
(def width-height (atom [default-width default-height]))

(def x (atom 0))
(def y (atom 0))

(def vertex-positions (list 100 100 0 0, 700 100 1 0, 700 500 1 1, 100 500 0 1))
(def vertpos {:data  (float-array vertex-positions)
              :usage :static-draw})

(def vertex-positions-indices (list 0 1 2 2 3 0))
(def vertposidxs {:data  (int-array vertex-positions-indices)
                  :usage :static-draw})

(defn init-callbacks []
  (glfw/set-key-callback @window*
    (fn [window key scancode action mods]
      (util/case action
        keys/release (util/case key
                       keys/esc (glfw/close-window window)
                       keys/space (aset-float vertpos 0 -0.8))
        keys/hold (util/case key
                    keys/left (swap! x #(- % 10))
                    keys/right (swap! x #(+ % 10))
                    keys/down (swap! y #(- % 10))
                    keys/up (swap! y #(+ % 10))))))
  (glfw/set-framebuffer-size-callback @window*
    (fn [window width height]
      (reset! width-height [width height])
      (gl/viewport 0 0 width height)
      (let [mvp (mat4f/model-view-projection-matrix
                  {:projection-matrix (mat4f/orthogonal 0 width 0 height -1 1)})]
        (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                                   "u_MVP"
                                   mvp)))))

(defn init-window []
  (glfw/enable-error-callback-print)
  (when-not (glfw/init)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (glfw/default-window-hints)
  (glfw/window-hint-visible false)
  (glfw/window-hint-resizable true)
  (reset! window* (glfw/create-window default-width default-height "Hello GL"))
  (when (nil? @window*)
    (throw (RuntimeException. "Failed to create GLFW window")))
  (init-callbacks)
  (glfw/make-context-current @window*)
  (gl/create-capabilities)
  (glfw/center-window @window*)
  (glfw/vsync))

(defn init []
  (init-window)
  ;(gl/blend)
  (renderer/setup-renderer {:shaders-source-path        (util/shaders-root "triangle-with-texture.glsl")
                            :vertex-positions           vertpos
                            :vertex-positions-indices   vertposidxs
                            :attributes-setups          [{:components 2
                                                          :gl-type    :gl-float
                                                          :normalize? false}
                                                         {:components 2
                                                          :gl-type    :gl-float
                                                          :normalize? false}]
                            :shader-program-lookup-name :shader/hello-rectangle
                            :renderer-id                :renderer/hello-rectangle})
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
              {:projection-matrix (mat4f/orthogonal 0 default-width 0 default-height -1 1)})]
    (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                               "u_MVP"
                               mvp)))

(defn gl-loop-cleanup []
  (glfw/swap-buffers @window*)
  (glfw/poll-events))

(defn gl-loop []
  (let [[r g b] (colors/get :teal)]
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
         (reset! window* nil)
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