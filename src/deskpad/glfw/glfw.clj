(ns deskpad.glfw.glfw
  (:require [deskpad.util :refer [window* null]])
  (:import (org.lwjgl.glfw Callbacks GLFW GLFWErrorCallback GLFWFramebufferSizeCallback
                           GLFWKeyCallback GLFWVidMode)
           (org.lwjgl.system MemoryUtil MemoryStack)
           (java.nio IntBuffer)))
;; ------------------------------------------------------
;; common stuff
;; ------------------------------------------------------
(defn bool [x] (if x GLFW/GLFW_TRUE GLFW/GLFW_FALSE))
(defn init [] (GLFW/glfwInit))
(defn terminate [] (GLFW/glfwTerminate))
;; ------------------------------------------------------
;; rendering stuff
;; ------------------------------------------------------
(defn vsync [] (GLFW/glfwSwapInterval 1))
(defn swap-buffers [] (GLFW/glfwSwapBuffers @window*))
(defn poll-events [] (GLFW/glfwPollEvents))

;; callbacks stuff
(defn enable-error-callback-print [] (.set (GLFWErrorCallback/createPrint System/out)))
(defn unbind-error-callback [] (.free (GLFW/glfwSetErrorCallback nil)))

(defmacro set-key-callback [[window key scancode action mode] & body]
  `(GLFW/glfwSetKeyCallback @window* (proxy [GLFWKeyCallback] []
                                       (invoke [~window ~key ~scancode ~action ~mode] ~@body))))

(defmacro set-framebuffer-size-callback [[window width height] & body]
  `(GLFW/glfwSetFramebufferSizeCallback @window* (proxy [GLFWFramebufferSizeCallback] []
                                       (invoke [~window ~width ~height] ~@body))))

(defn free-callbacks [] (Callbacks/glfwFreeCallbacks @window*))
(defn make-context-current [] (GLFW/glfwMakeContextCurrent @window*))
;; ------------------------------------------------------
;; window and monitor stuff
;; ------------------------------------------------------
(defn get-primary-monitor [] (GLFW/glfwGetPrimaryMonitor))
(defn get-video-mode [] (GLFW/glfwGetVideoMode (get-primary-monitor)))

(defn get-monitor-size
  "[width height]"
  [] (let [^GLFWVidMode vid-mode (get-video-mode)]
       [(.width vid-mode) (.height vid-mode)]))

(defn create-window
  ([width height title] (create-window width height title null null))
  ([^Integer width ^Integer height ^String title ^Long monitor ^Long share]
   (reset! window* (GLFW/glfwCreateWindow width height title monitor share))))

(defn show-window [] (GLFW/glfwShowWindow @window*))

(defn destroy-window [] (GLFW/glfwDestroyWindow @window*))

(defn close-window
  ([] (close-window @window*))
  ([window] (GLFW/glfwSetWindowShouldClose window true)))

(defn should-window-close? [] (GLFW/glfwWindowShouldClose @window*))

(defn window-resolutions
  "[width height]"
  [] (let [^MemoryStack stack (MemoryStack/stackPush)
           ^IntBuffer p-width (.mallocInt stack 1)
           ^IntBuffer p-height (.mallocInt stack 1)]
       (GLFW/glfwGetWindowSize ^Integer @window* p-width p-height)
       [(.get p-width) (.get p-height)]))

(defn set-window-pos [x y] (GLFW/glfwSetWindowPos @window* x y))

(defn center-window []
  (let [[window-width window-height] (window-resolutions)
        [monitor-width monitor-height] (get-monitor-size)]
    (set-window-pos (quot (- monitor-width window-width) 2)
                    (quot (- monitor-height window-height) 2))))
;; ------------------------------------------------------
;; window hints stuff
;; ------------------------------------------------------
(defn default-window-hints [] (GLFW/glfwDefaultWindowHints))
(defn window-hint-visible [?] (GLFW/glfwWindowHint GLFW/GLFW_VISIBLE (bool ?)))
(defn window-hint-resizable [?] (GLFW/glfwWindowHint GLFW/GLFW_RESIZABLE (bool ?)))