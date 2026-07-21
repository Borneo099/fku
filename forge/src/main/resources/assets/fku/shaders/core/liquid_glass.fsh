#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform vec4 QuadPos;
uniform float QuadRadius;
uniform float BlurRadius;
uniform float RefractionPower;
uniform float RefractionEdge;
uniform float Dispersion;
uniform float GlobalAlpha;
uniform vec4 GlassTint;
uniform int TintMode;
uniform float Noise;
uniform float GlowWeight;
uniform float GlowBias;
uniform float GlowEdge0;
uniform float GlowEdge1;
uniform float ChromaStrength;
uniform float Darkness;

in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec4 color = texture(DiffuseSampler, uv);
    color.a = GlobalAlpha;
    fragColor = color * vertexColor;
}