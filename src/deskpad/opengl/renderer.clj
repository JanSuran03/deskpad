(ns deskpad.opengl.renderer)

(defprotocol IRenderer
  (draw [this]))

(deftype Renderer [shader-program vao ebo ebo-size])