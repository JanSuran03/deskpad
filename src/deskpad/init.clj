(ns deskpad.init
  (:require [cljgl.common.debug :as debug]
            [cljgl.common.cleanup :as cleanup]
            [cljgl.common.colors :as colors]
            [cljgl.common.gl-util :as gl-util]
            [cljgl.glfw.glfw :as glfw]
            [cljgl.opengl.gl :as gl]
            [cljgl.opengl.renderer :as renderer]
            [cljgl.opengl.shaders :as shaders]
            [clj-async-profiler.core :as profiler]
            [clojure.core.async :refer [thread]]
            [deskpad.callbacks :as callbacks]
            [deskpad.examples.penguins :as penguins]
            [deskpad.examples.static-rectangles :as rectangles]
            [deskpad.util :as util :refer [window*]]))

(defmacro profile [& body]
  `(profiler/profile
     (try ~@body
          (catch Throwable t# (println "Throwable: ")
                              (.printStackTrace t#)))))

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

(defn init [example-id]
  (init-window)
  (case example-id
    :penguins (penguins/setup)
    :rectangles (rectangles/setup))
  (glfw/show-window @window*))

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

(defn run [example-id]
  (println (str "Hello LWJGL " (gl/get-version) "!"))
  (try (init example-id)
       (gl-loop)
       (finally
         (glfw/free-callbacks @window*)
         (glfw/destroy-window @window*)
         (reset! window* -1)
         (shaders/unbind-shader-program)
         (cleanup/cleanup)
         (glfw/terminate)
         (glfw/unbind-error-callback))))

(defn run-separate-thread [example-id]
  (thread (when-not @running?*
            (reset! running?* true)
            (gl-util/log "Rendering thread started." :important)
            (try (run example-id)
                 (finally
                   (gl-util/log "Rendering thread interrupted." :important)
                   (reset! running?* false))))))