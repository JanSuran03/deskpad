(ns deskpad.opengl.renderer
  (:require [deskpad.debug :as debug]
            [deskpad.opengl.buffers :as buffers]
            [deskpad.opengl.shaders :as shaders]
            [deskpad.opengl.gl :as gl]
            [deskpad.util :as util]))

(defprotocol IRenderer
  (render [this]))

(deftype Renderer [shader-program vao ebo ebo-size]
  IRenderer
  (render [this]
    (shaders/use-shader-program shader-program)
    (buffers/bind-VAO vao)
    (buffers/bind-EBO ebo)
    (gl/draw-elements gl/triangles ebo-size)))

(defn setup-rendering
  "(renderer/setup-rendering
       {:vertex-source-path   \"vert-triangle.glsl\"
        :fragment-source-path \"frag-triangle.glsl\"
        :vertex-positions (list -0.5 -0.5, 0.5 -0.5, +0.5 +0.5, -0.5 0.5)
        :attributes-setups [{:components 2           ;; layout (location = 0) in vec2;
                             :data-type  gl/FLOAT
                             :normalize? false}
                            {:components 1           ;; layout (location = 1) in float;
                             :data-type  gl/FLOAT
                             :normalize? false])
  => ^Integer shader-program"
  [{:keys [shaders-source-path vertex-positions vertex-positions-indices
           attributes-setups shader-program-lookup-name]}]
  (let [shader-program (shaders/make-shader-program shader-program-lookup-name shaders-source-path)
        VAO (buffers/gen-vao)
        VBO (buffers/gen-buffer)
        EBO (buffers/gen-buffer)
        vertex-buffer-stride (reduce (fn [offset {:keys [components gl-type]}]
                                       (+ offset (* components (util/sizeof gl-type))))
                                     0
                                     attributes-setups)]
    (debug/assert-all (buffers/bind-VAO VAO)
                      (buffers/bind-VBO VBO)
                      (buffers/bind-EBO EBO)
                      (buffers/VBO-data (float-array vertex-positions) :static-draw)
                      (buffers/EBO-data (int-array vertex-positions-indices))
                      (reduce (fn [[i byte-offset] {:keys [components gl-type normalize?]}]
                                (gl/vertex-attrib-pointer i components (util/gl-type gl-type)
                                                                normalize? vertex-buffer-stride byte-offset)
                                (gl/enable-vertex-attrib-array i)

                                [(inc i) (+ byte-offset (* components (util/sizeof gl-type)))])
                              [0 0]
                              attributes-setups))
    (Renderer. shader-program VAO EBO (count vertex-positions-indices))))