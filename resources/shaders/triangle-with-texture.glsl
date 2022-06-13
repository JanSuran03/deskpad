;;VERTEX;;
#version 330 core
layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec2 in_tex_coord;

out vec2 v_texture_coord;

uniform mat4 u_MVP; // uniform model view projection matrix

void main(){
    gl_Position = u_MVP * vec4(in_pos.xy, 0.0, 1.0);
    //gl_Position = vec4(in_pos.xy, 0.0, 1.0);
    v_texture_coord = in_tex_coord;
}


;;FRAGMENT;;
#version 330 core

in vec2 v_texture_coord;

layout (location = 0) out vec4 out_fragColor;

uniform sampler2D u_texture;

void main(){
    //out_fragColor = vec4(1.0, 1.0, 0.0, 1.0);
    vec4 tex_color = texture(u_texture, v_texture_coord);
    out_fragColor = tex_color;
}