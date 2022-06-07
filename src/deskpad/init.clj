(ns deskpad.init
  (:require [deskpad.glfw.glfw :as glfw]
            [deskpad.glfw.keys :as keys]
            [deskpad.opengl.gl :as gl]
            [deskpad.util :as util :refer [window*]]
            [clojure.core.async :refer [thread]]))

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

(def vertex-positions (list -0.5 -0.5 0 0
                            +0.5 -0.5 1 0
                            +0.5 +0.5 1 1
                            -0.5 +0.5 0 1))

(defn init []
  (glfw/enable-error-callback-print)
  (when-not (glfw/init)
    (throw (IllegalStateException. "Unable to initialize GLFW"))
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

    (gl/blend)))