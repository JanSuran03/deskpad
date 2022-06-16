;;VERTEX;;
#version 330 core
layout (location = 0) in vec2 in_pos;
layout (location = 1) in vec3 in_color;

out vec3 v_color;

void main(){
    gl_Position = vec4(in_pos.xy, 0.0, 1.0);
    v_color = in_color;
}


;;FRAGMENT;;
#version 330 core

in vec3 v_color;

out vec4 out_fragColor;

void main(){
    out_fragColor = v_color;
}