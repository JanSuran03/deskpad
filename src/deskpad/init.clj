(ns deskpad.init
  (:require [cljgl.common.debug :as debug]
            [cljgl.common.gl-util :as gl-util]
            [cljgl.glfw.glfw :as glfw]
            [cljgl.opengl.gl :as gl]
            [clojure.core.async :refer [thread]]
            [cljgl.common.colors :as colors]
            [cljgl.glfw.keys :as keys]
            [cljgl.opengl.renderer :as renderer]
            [cljgl.opengl.shaders :as shaders]
            [deskpad.util :as util :refer [window*]]))

(defonce running?* (atom false))

(def default-width 1000)
(def default-height 600)
(def width-height (atom [default-width default-height]))

(def x (atom 0))
(def y (atom 0))

(defn init-callbacks []
  (glfw/set-key-callback @window*
    (fn [window key scancode action mods]
      (util/case action
        keys/release (util/case key
                       keys/esc (glfw/close-window window))
        keys/hold (util/case key
                    keys/left (swap! x #(- % 10))
                    keys/right (swap! x #(+ % 10))
                    keys/down (swap! y #(- % 10))
                    keys/up (swap! y #(+ % 10))))))
  (glfw/set-framebuffer-size-callback @window*
    (fn [window width height]
      (reset! width-height [width height])
      (gl/viewport 0 0 width height))))

(def vertex-positions-indices (list 0 1 2 2 3 0))

(def vertex-positions (list -0.6 -0.5
                            +0.6 -0.5
                            +0.6 +0.5
                            -0.6 +0.5))

(defn init-window []
  (glfw/enable-error-callback-print)
  (when-not (glfw/init)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (glfw/default-window-hints)
  (glfw/window-hint-visible false)
  (glfw/window-hint-resizable true)
  (reset! window* (glfw/create-window default-width default-height "Deskpad"))
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
                            :vertex-positions           vertex-positions
                            :vertex-positions-indices   vertex-positions-indices
                            :attributes-setups          [{:components 2
                                                          :gl-type    :gl-float
                                                          :normalize? false}]
                            :shader-program-lookup-name :shader/hello-rectangle
                            :renderer-id                :renderer/hello-rectangle})
  (renderer/change-rendering-order [:renderer/hello-rectangle])
  (glfw/show-window @window*))

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
         (renderer/reset)
         (shaders/unbind-shader-program)
         (glfw/free-callbacks @window*)
         (glfw/destroy-window @window*)
         (reset! window* nil)
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