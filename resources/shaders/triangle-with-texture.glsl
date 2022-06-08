;;VERTEX;;
#version 330 core
layout (location = 0) in vec2 in_pos;

void main(){
    gl_Position = vec4(in_pos.xy, 0.0, 1.0);
}


;;FRAGMENT;;
#version 330 core

out vec4 out_fragColor;

void main(){
    out_fragColor = vec4(1.0, 1.0, 0.0, 1.0);
}