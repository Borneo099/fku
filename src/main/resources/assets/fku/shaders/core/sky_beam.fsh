#version 150

uniform float Time;
uniform float BeamHeight;
uniform float BeamRadius;
uniform float CoreRadius;
uniform float Intensity;
uniform float RevealFraction;

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
    float y01 = BeamHeight > 0.0 ? clamp(localPos.y / BeamHeight, 0.0, 1.0) : 0.0;
    float reveal = clamp(RevealFraction, 0.0, 1.0);
    if (reveal <= 0.0001) { discard; }
    float revealStart = 1.0 - reveal;
    if (y01 < revealStart) { discard; }
    float revealY = (y01 - revealStart) / max(reveal, 0.0001);
    float beamCoord = mix(y01, revealY, 0.82);
    float angle = atan(localPos.z, localPos.x);
    float axialNoise = noise(vec2(beamCoord*12.0-Time*0.010, angle*4.0+Time*0.004));
    float helicalBand = 0.5+0.5*sin(angle*6.0+beamCoord*18.0-Time*0.07);
    float taper = mix(1.10, 0.94, pow(y01, 0.70));
    float eR = BeamRadius * taper;
    float eC = max(CoreRadius*mix(1.06,0.86,pow(y01,0.65)), 0.0001);
    float radial = length(localPos.xz);
    if (radial > eR) { discard; }
    float shell = 1.0 - smoothstep(eR*0.18, eR, radial);
    float glow = 1.0 - smoothstep(eR*0.42, eR*1.02, radial);
    float core = 1.0 - smoothstep(eC*0.14, eC*1.08, radial);
    float coreHalo = 1.0 - smoothstep(eC*0.55, eC*1.90, radial);
    float pulse = 0.86+0.14*sin(beamCoord*8.0-Time*0.04+radial*8.0);
    float shimmer = 0.92+0.08*noise(vec2(beamCoord*6.0-Time*0.010, radial*4.0));
    float soft = 0.96+0.04*noise(vec2(localPos.x*1.1-Time*0.006, localPos.z*1.1+beamCoord*4.0));
    float bottomFade = smoothstep(0.0, 0.04, revealY);
    float heightFade = bottomFade;
    float hf = smoothstep(0.0, 0.10, revealY);
    float frontCore = pow(1.0-hf, 1.8), frontHalo = pow(1.0-smoothstep(0.0,0.24,revealY),1.35);
    float sourceCap = smoothstep(0.84, 0.96, revealY);
    float bottomImpact = pow(1.0-smoothstep(0.0,0.12,y01),2.4)*smoothstep(0.94,1.0,reveal);
    float bottomShell = 1.0-smoothstep(eR*0.25, eR*1.30, radial);
    float axialStr = mix(0.82,1.22,axialNoise*0.7+helicalBand*0.3);
    float sf = shell*pulse*shimmer*soft*axialStr;
    float hf2 = glow*(0.90+0.10*pulse)*soft*mix(0.92,1.08,axialNoise);
    float imf = bottomImpact*bottomShell*(1.25+helicalBand*0.45);
    float ff = frontHalo*(1.10+helicalBand*0.40)+frontCore*1.80;
    float ss = sourceCap*(1.0-smoothstep(eR*0.20,eR*1.12,radial));
    float sc = sourceCap*(1.0-smoothstep(eC*0.22,eC*1.40,radial));
    float beam = (sf*1.55+hf2*1.35+coreHalo*1.45+core*2.75+imf*2.20+ff*2.10+ss*2.40+sc*2.85)*heightFade;
    float alpha = saturate((sf*0.16+hf2*0.13+coreHalo*0.10+core*0.22+imf*0.16+ff*0.14+ss*0.18+sc*0.22)*Intensity)*vertexColor.a;
    vec3 ot = mix(vertexColor.rgb*1.55, vec3(0.96,0.99,1.0), 0.60+glow*0.22);
    vec3 it = mix(vertexColor.rgb*1.52+vec3(0.18,0.24,0.32), vec3(1.0), saturate(core*0.72+coreHalo*0.28));
    vec3 color = ot*(0.95+sf*2.10+hf2*1.45);
    color += it*coreHalo*(1.15+Intensity*0.32);
    color += it*core*(1.95+Intensity*0.44);
    color += vec3(0.92,0.97,1.0)*hf2*0.62;
    color += vec3(0.76,0.90,1.0)*imf*(1.30+Intensity*0.30);
    color += vec3(0.96,0.99,1.0)*frontCore*(1.40+Intensity*0.36);
    color += vec3(0.82,0.92,1.0)*frontHalo*(0.86+Intensity*0.22);
    color += vec3(0.94,0.98,1.0)*ss*(1.55+Intensity*0.38);
    color += vec3(1.0)*sc*(1.85+Intensity*0.42);
    color += vec3(0.30,0.42,0.58)*beam*0.10;
    fragColor = vec4(color, alpha);
}
