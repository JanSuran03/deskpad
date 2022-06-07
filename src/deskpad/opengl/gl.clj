(ns deskpad.opengl.gl
  (:import (org.lwjgl Version)
           (org.lwjgl.opengl GL GL33)))

;; ------------------------------------------------------
;; GL
;; ------------------------------------------------------
(defn create-capabilities [] (GL/createCapabilities))
(defn get-version [] (Version/getVersion))
;; ------------------------------------------------------
;; consts
;; ------------------------------------------------------
(defonce ^{:doc "GL33/GL_TRUE"} T GL33/GL_TRUE)
(defonce ^{:doc "GL33/GL_FALSE"} F GL33/GL_FALSE)
;; ------------------------------------------------------
;; GL33
;; ------------------------------------------------------
(defonce ^Integer triangles GL33/GL_TRIANGLES)
(defonce ^Integer color-buffer-bit GL33/GL_COLOR_BUFFER_BIT)
(defonce ^Integer BYTE GL33/GL_BYTE)
(defonce ^Integer UNSIGNED-BYTE GL33/GL_UNSIGNED_BYTE)
(defonce ^Integer DOUBLE GL33/GL_DOUBLE)
(defonce ^Integer FLOAT GL33/GL_FLOAT)
(defonce ^Integer HALF-FLOAT GL33/GL_HALF_FLOAT)
(defonce ^Integer INT GL33/GL_INT)
(defonce ^Integer INT-2-10-10-10-REV GL33/GL_INT_2_10_10_10_REV)
(defonce ^Integer SHORT GL33/GL_SHORT)
(defonce ^Integer UNSIGNED-BYTE GL33/GL_UNSIGNED_BYTE)
(defonce ^Integer UNSIGNED-SHORT GL33/GL_UNSIGNED_SHORT)
(defonce ^Integer UNSIGNED-INT GL33/GL_UNSIGNED_INT)
(defonce ^Integer UNSIGNED-INT-2-10-10-10-REV GL33/GL_UNSIGNED_INT_2_10_10_10_REV)

(defn clear-color [r g b a] (GL33/glClearColor r g b a))
(defn clear-bits [& bits] (GL33/glClear (reduce bit-or bits)))

(defn draw-elements [mode count] (GL33/glDrawElements mode count UNSIGNED-INT 0))

(defn vertex-attrib-pointer
  [^Integer attribute-location-in-shader
   ^Integer components-count
   ^Integer data-type
   ^Boolean normalize?
   ^Integer vertex-stride
   ^Long byte-pointer-at-vertex]
  (GL33/glVertexAttribPointer attribute-location-in-shader components-count data-type
                              normalize? vertex-stride byte-pointer-at-vertex))

(defn enable-vertex-attrib-array [index] (GL33/glEnableVertexAttribArray index))
(defn viewport [x y width height] (GL33/glViewport x y width height))

;; blending

(defn blend []
  (GL33/glBlendFunc GL33/GL_SRC_ALPHA GL33/GL_ONE_MINUS_SRC_ALPHA)
  (GL33/glEnable GL33/GL_BLEND))