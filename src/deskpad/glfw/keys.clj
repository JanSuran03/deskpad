(ns deskpad.glfw.keys
  (:import (org.lwjgl.glfw GLFW)))

(defonce release GLFW/GLFW_RELEASE)
(defonce hold GLFW/GLFW_REPEAT)
(defonce esc GLFW/GLFW_KEY_ESCAPE)
(defonce space GLFW/GLFW_KEY_SPACE)
(defonce up GLFW/GLFW_KEY_UP)
(defonce down GLFW/GLFW_KEY_DOWN)
(defonce left GLFW/GLFW_KEY_LEFT)
(defonce right GLFW/GLFW_KEY_RIGHT)
(defn ctrl? [mods-integer] (> mods-integer (bit-xor mods-integer GLFW/GLFW_MOD_CONTROL)))
(defn alt? [mods-integer] (> mods-integer (bit-xor mods-integer GLFW/GLFW_MOD_ALT)))
(defn shift? [mods-integer] (> mods-integer (bit-xor mods-integer GLFW/GLFW_MOD_SHIFT)))