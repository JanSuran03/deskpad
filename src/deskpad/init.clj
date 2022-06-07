(ns deskpad.init
  (:require [deskpad.opengl.colors :as colors]
            [deskpad.glfw.glfw :as glfw]
            [deskpad.glfw.keys :as keys]
            [deskpad.opengl.gl :as gl]
            [deskpad.opengl.renderer :as renderer]
            [deskpad.util :as util :refer [window*]]
            [clojure.core.async :refer [thread]]
            [deskpad.opengl.shaders :as shaders]
            [deskpad.debug :as debug]))

(defonce running?* (atom false))

(def default-width 1000)
(def default-height 600)
(def width-height (atom [default-width default-height]))

(def x (atom 0))
(def y (atom 0))

(defn init-callbacks []
  (glfw/set-key-callback [window key scancode action mods]
    (util/case action
      keys/release (util/case key
                     keys/esc (glfw/close-window window))
      keys/hold (util/case key
                  keys/left (swap! x #(- % 10))
                  keys/right (swap! x #(+ % 10))
                  keys/down (swap! y #(- % 10))
                  keys/up (swap! y #(+ % 10)))))
  (glfw/set-framebuffer-size-callback [window width height]
    (reset! width-height [width height])))

(def vertex-positions-indices (list 0 1 2 2 3 0))

(def vertex-positions (list -0.5 -0.5
                            +0.5 -0.5
                            +0.5 +0.5
                            -0.5 +0.5))

(defn init []
  (glfw/enable-error-callback-print)
  (when-not (glfw/init)
    (throw (IllegalStateException. "Unable to initialize GLFW")))
  (glfw/default-window-hints)
  (glfw/window-hint-visible false)
  (glfw/window-hint-resizable true)
  (glfw/create-window default-width default-height "Deskpad")
  (when (nil? @window*)
    (throw (RuntimeException. "Failed to create GLFW window")))

  (init-callbacks)
  (glfw/make-context-current)
  (gl/create-capabilities)
  (glfw/center-window)

  ;(gl/blend)
  (let [renderer (renderer/setup-rendering
                   {:shaders-source-path        "triangle-with-texture.glsl"
                    :vertex-positions           vertex-positions
                    :vertex-positions-indices   vertex-positions-indices
                    :attributes-setups          [{:components 2
                                                  :gl-type    :gl-float
                                                  :normalize? false}]
                    :shader-program-lookup-name :hello-rectangle})]
    (glfw/vsync)
    (glfw/show-window)
    renderer))

(defn gl-loop-cleanup []
  (glfw/swap-buffers)
  (glfw/poll-events))

(defn gl-loop [renderer]
  (let [[r g b] (colors/get :teal)]
    (println r g b)
    (while (not (glfw/should-window-close?))
      (debug/assert-all
        (gl/clear-color r g b 0)
        (gl/clear-bits gl/color-buffer-bit)
        (renderer/render renderer)
        (gl-loop-cleanup)))))

(defn run []
  (println (str "Hello LWJGL " (gl/get-version) "!"))
  (try (gl-loop
         (init))
       (finally
         (shaders/delete-shader-programs)
         #_(texture/delete-textures)
         (glfw/free-callbacks)
         (glfw/destroy-window)
         (reset! window* nil)
         (glfw/terminate)
         (glfw/unbind-error-callback))))

(defn run-separate-thread []
  (thread (when-not @running?*
            (reset! running?* true)
            (util/log "Rendering thread started." :important)
            (try (run)
                 (finally
                   (util/log "Rendering thread interrupted." :important)
                   (reset! running?* false))))))