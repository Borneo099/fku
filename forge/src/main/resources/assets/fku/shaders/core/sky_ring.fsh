#version 150

uniform float OuterRadius;
uniform float InnerRadius;
uniform float Softness;
uniform float Intensity;
uniform float Time;
uniform float RingPlane;

in vec4 vertexColor;
in vec3 localPos;

out vec4 fragColor;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123); }

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i), b = hash(i+vec2(1,0)), c = hash(i+vec2(0,1)), d = hash(i+vec2(1,1));
    vec2 u = f*f*(3.0-2.0*f);
    return mix(a,b,u.x)+(c-a)*u.y*(1.0-u.x)+(d-b)*u.x*u.y;
}

void main() {
    vec2 planePos = mix(localPos.xz, localPos.xy, step(0.5, RingPlane));
    float radial = length(planePos);
    float angle = atan(planePos.y, planePos.x);
    float angularNoise = noise(vec2(angle*2.2, Time*0.55));
    float ripple = 0.5+0.5*sin(angle*7.0-Time*5.6);
    float radiusWarp = (angularNoise-0.5)*Softness*0.55+(ripple-0.5)*Softness*0.35;
    float wo = OuterRadius+radiusWarp;
    float wi = max(InnerRadius+radiusWarp*0.55, 0.0);
    if (radial<max(wi-Softness*1.35,0.0) || radial>wo+Softness*1.35) { discard; }
    float outerMask = 1.0-smoothstep(wo, wo+Softness, radial);
    float innerMask = smoothstep(max(wi-Softness,0.0), wi, radial);
    float ringMask = innerMask*outerMask;
    float rim = exp(-pow((radial-wo)/max(Softness*0.9,0.0001),2.0));
    float scan = exp(-pow((radial-mix(wi,wo,fract(Time*0.4)))/max(Softness*1.4,0.0001),2.0));
    float arcBreakup = mix(0.72,1.18,angularNoise*0.6+ripple*0.4);
    float arcPulse = 0.72+0.28*sin(angle*9.0+Time*6.4);
    float alpha = saturate((ringMask*0.55+rim*0.65+scan*0.35)*Intensity*arcBreakup*arcPulse)*vertexColor.a;
    if (alpha < 0.003) { discard; }
    vec3 color = vertexColor.rgb*(0.42+ringMask*1.28+rim*0.88);
    color += vec3(1.0)*(rim*0.30+scan*0.24);
    color += vec3(0.34,0.52,0.76)*scan*0.12;
    fragColor = vec4(color, alpha);
}
