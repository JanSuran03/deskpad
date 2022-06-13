(ns deskpad.callbacks
  (:require [cljgl.glfw.callbacks :as callbacks]
            [cljgl.glfw.glfw :as glfw]
            [cljgl.glfw.keys :as keys]
            [deskpad.util :as util :refer [window*]]
            [cljgl.opengl.shaders :as shaders]
            [cljgl.math.matrix4f :as mat4f]
            [cljgl.opengl.gl :as gl]
            [cljgl.opengl.renderer :as renderer])
  (:import (org.lwjgl.glfw GLFW GLFWCursorPosCallback GLFWCursorEnterCallback GLFWScrollCallback GLFWCharCallback)))

(def default-width 1760)
(def default-height 990)
(def scale-base 0.95)

(def state* (atom {:translate-x    0
                   :translate-y    0
                   :mouse-x        0
                   :mouse-y        0
                   :mouse-pressed? false
                   :window-width   default-width
                   :window-height  default-height
                   :scroll         0}))

(defn calculate-mvp [{:keys [window-width window-height translate-x translate-y scroll] :as state}]
  (let [pow (Math/pow scale-base scroll)]
    (mat4f/model-view-projection-matrix
      {:projection-matrix (mat4f/orthogonal translate-x (+ translate-x window-width)
                                            translate-y (+ translate-y window-height)
                                            -1 1)
       :scale             (repeat 3 pow)})))

(defn init-callbacks []
  (callbacks/set-key-callback @window*
    (fn [window key scancode action mods]
      (util/case action
        keys/release (util/case key
                       keys/esc (glfw/close-window window)))))

  (callbacks/set-framebuffer-size-callback @window*
    (fn [window width height]
      (gl/viewport 0 0 width height)
      (swap! state* assoc :window-width width :window-height height)
      (let [mvp (calculate-mvp @state*)]
        (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                                   "u_MVP"
                                   mvp))))

  (callbacks/set-cursor-pos-callback @window*
    (fn [window x-pos y-pos]
      (let [{:keys [mouse-x mouse-y mouse-pressed?] :as state} @state*
            new-state (assoc state :mouse-x x-pos, :mouse-y y-pos)]
        (if mouse-pressed?
          (let [new-state (-> new-state (update :translate-x + (- mouse-x x-pos))
                              (update :translate-y + (- y-pos mouse-y)))
                mvp (calculate-mvp new-state)]
            (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                                       "u_MVP"
                                       mvp)
            (reset! state* new-state))
          (reset! state* new-state)))))

  (callbacks/set-scroll-callback @window*
    (fn [window x-offset y-offset]
      (swap! state* update :scroll - y-offset)
      (let [mvp (calculate-mvp @state*)]
        (shaders/set-uniform-mat4f (.-shader_program (renderer/get-renderer :renderer/hello-rectangle))
                                   "u_MVP"
                                   mvp))))

  (callbacks/set-mouse-callback @window*
    (fn [^Long window button ^Integer action mods]
      (swap! state* assoc :mouse-pressed? (case action 0 false, 1 true))
      #_(let [[^doubles bufx ^doubles bufy] (repeatedly #(double-array 1))]
          (GLFW/glfwGetCursorPos window bufx bufy)))))
