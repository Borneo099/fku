#version 150

in vec4 Position;
in vec2 UV;

out vec2 uv;

uniform mat4 ProjMat;

void main() {
    gl_Position = ProjMat * Position;
    uv = UV;
}
