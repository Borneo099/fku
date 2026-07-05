#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform vec2 ScreenSize;
uniform vec3 CameraPos;
uniform float Time;

in vec4 vertexColor;
in vec3 spherePos;

out vec4 fragColor;

const float RENDER_SPHERE_RADIUS = 1.0;
const int DISK_STEPS = 8;
const float MAX_DISK_HALF_THICKNESS = 0.115;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

float gaussianRing(float x, float center, float width) {
    float delta = (x - center) / max(width, 0.0001);
    return exp(-delta * delta);
}

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123); }

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i), b = hash(i + vec2(1,0)), c = hash(i + vec2(0,1)), d = hash(i + vec2(1,1));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 4; i++) { v += noise(p) * a; p = p * 2.0 + vec2(17.2, 11.8); a *= 0.5; }
    return v;
}

vec3 sampleScene(vec2 uv, vec2 radialDir, vec2 tangentDir, float radialOffset, float tangentialOffset, float chroma) {
    vec2 baseUv = clamp(uv - radialDir * radialOffset + tangentDir * tangentialOffset, vec2(0.001), vec2(0.999));
    float r = texture(DiffuseSampler, clamp(baseUv + tangentDir * chroma, vec2(0.001), vec2(0.999))).r;
    float g = texture(DiffuseSampler, baseUv).g;
    float b = texture(DiffuseSampler, clamp(baseUv - tangentDir * chroma, vec2(0.001), vec2(0.999))).b;
    return vec3(r, g, b);
}

bool intersectSphere(vec3 ro, vec3 rd, float radius, out float tNear, out float tFar) {
    float b = dot(ro, rd);
    float c = dot(ro, ro) - radius * radius;
    float h = b * b - c;
    if (h < 0.0) return false;
    h = sqrt(h);
    tNear = -b - h; tFar = -b + h;
    return true;
}

vec3 sampleAccretionDisk(vec3 ro, vec3 rd) {
    vec3 diskEmission = vec3(0.0);
    if (abs(rd.y) <= 0.0001) return diskEmission;
    float tNear, tFar;
    if (!intersectSphere(ro, rd, RENDER_SPHERE_RADIUS, tNear, tFar)) return diskEmission;
    tNear = max(tNear, 0.0);
    if (tFar <= tNear) return diskEmission;
    float tDA = (-MAX_DISK_HALF_THICKNESS - ro.y) / rd.y;
    float tDB = (MAX_DISK_HALF_THICKNESS - ro.y) / rd.y;
    float diskStart = max(min(tDA, tDB), tNear);
    float diskEnd = min(max(tDA, tDB), tFar);
    if (diskEnd <= diskStart) return diskEmission;
    float diskStep = (diskEnd - diskStart) / float(DISK_STEPS);
    for (int i = 0; i < DISK_STEPS; i++) {
        float t = diskStart + diskStep * (float(i) + 0.5);
        vec3 dp = ro + rd * t;
        float dr = length(dp.xz);
        float da = atan(dp.z, dp.x);
        float ft = Time * 1.35;
        float rt = saturate((dr - 0.22) / 0.72);
        float dh = 0.008 + 0.072 * exp(-4.6 * rt);
        float vf = 1.0 - smoothstep(dh * 0.35, dh, abs(dp.y));
        float ie = smoothstep(0.22, 0.28, dr);
        float oe = 1.0 - smoothstep(0.66, 0.82, dr);
        float ib = exp(-pow((dr - 0.32) / 0.085, 2.0));
        float mb = exp(-pow((dr - 0.50) / 0.13, 2.0));
        float ob = exp(-pow((dr - 0.66) / 0.16, 2.0));
        float dn = fbm(dp.xz * 5.0 + vec2(ft*0.15, -ft*0.10));
        float wv = mix(0.92, 1.08, dn);
        float sf = 0.5 + 0.5 * sin(da*5.0 - dr*18.0 - ft + dn*1.8);
        float sn = fbm(vec2(da*2.8 - dr*1.8 - ft*0.55, dr*5.8 + ft*0.35));
        float lc = mix(0.88, 1.16, saturate(sf*0.72 + sn*0.38));
        float dl = 1.0 - 0.12 * smoothstep(0.45, 0.82, 1.0-sf) * smoothstep(0.25, 0.78, sn);
        float rtf = 1.0 - smoothstep(0.64, 0.80, dr);
        float ir = exp(-pow((dr - 0.285) / 0.042, 2.0));
        float rp = ib*1.42 + mb*1.15 + ob*0.14 + ir*0.62;
        float dm = ie * oe * vf * rp * wv * rtf * lc * dl;
        vec2 orb = dr > 0.0001 ? dp.xz / dr : vec2(1,0);
        float bm = pow(max(dot(orb, normalize(vec2(0.96,0.28))), 0.0), 4.8);
        float vs = 0.5 + 0.5 * dot(orb, normalize(vec2(-0.42,0.91)));
        float temp = saturate((1.0-rt)*0.95 + bm*0.38 + ir*0.58 + sf*0.10);
        vec3 hc = vec3(1.0,0.93,0.82), wc = vec3(0.98,0.73,0.34), ec = vec3(0.86,0.34,0.10);
        vec3 dc = mix(ec, wc, saturate(temp*1.05));
        dc = mix(dc, hc, saturate(temp*0.84+dn*0.10));
        dc = mix(dc, vec3(1.0,0.98,0.92), ir*0.35+bm*0.22);
        dc *= mix(0.88, 1.12, vs);
        float br = smoothstep(0.20, 0.27, dr) * (1.0 - smoothstep(0.35, 0.44, dr)) * vf;
        float em = (0.34 + bm*1.50 + ir*0.95 + sf*0.08) * dm;
        diskEmission += dc * em * diskStep * 7.6;
        diskEmission += vec3(1.0,0.96,0.90) * br * (0.85+ir*0.65) * diskStep * 1.9;
    }
    return diskEmission;
}

void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec3 surface = normalize(spherePos);
    vec3 ro = CameraPos;
    vec3 rd = normalize(surface - ro);
    float tNear, tFar;
    if (!intersectSphere(ro, rd, RENDER_SPHERE_RADIUS, tNear, tFar)) { discard; }
    tNear = max(tNear, 0.0);
    if (tFar <= tNear) { discard; }
    float impact = length(cross(ro, rd));
    if (impact > 1.0) { discard; }

    vec3 originalScene = texture(DiffuseSampler, uv).rgb;

    // 引力透镜扭曲
    float shadowMask = 1.0 - smoothstep(0.205, 0.235, impact);
    float photonRing = gaussianRing(impact, 0.255, 0.018);
    float innerRing = gaussianRing(impact, 0.235, 0.015);
    float haloRing = gaussianRing(impact, 0.315, 0.060);
    float outerFade = 1.0 - smoothstep(0.72, 1.02, impact);
    float farLens = 1.0 - smoothstep(0.82, 1.0, impact);
    float distortion = farLens * (0.014 + 0.040/(impact+0.10)) + photonRing*0.050 + innerRing*0.026 + shadowMask*0.145;
    distortion *= outerFade;

    vec2 ndcFrag = vec2(uv.x*2.0-1.0, uv.y*2.0-1.0);
    float aspect = ScreenSize.x / ScreenSize.y;
    vec2 screenDir = vec2(ndcFrag.x, ndcFrag.y * aspect);
    float sdLen = length(screenDir) + 0.0001;
    screenDir = screenDir / sdLen;
    vec2 tangentDir = vec2(-screenDir.y, screenDir.x);
    float swirl = (photonRing*0.026 + haloRing*0.010) * sign(dot(tangentDir, vec2(0.72,0.28)));
    float chroma = 0.0015 + photonRing*0.0035;

    vec3 warped = sampleScene(uv, screenDir, tangentDir, distortion, swirl, chroma);
    vec3 color = mix(originalScene, warped, saturate((farLens*0.92 + pow(1.0-smoothstep(0.205,0.70,impact),2.2)*0.35)*outerFade));

    // 吸积盘
    vec3 disk = sampleAccretionDisk(ro, rd);
    color += disk * outerFade;

    // 光子环发光
    vec3 ringGlow = mix(vec3(1.0,0.90,0.72), vec3(1.0,0.98,0.95), saturate(photonRing*0.8+haloRing));
    color += ringGlow * photonRing * outerFade * 0.82;
    color += vec3(1.0,0.82,0.62) * innerRing * outerFade * 0.28;
    color += vec3(0.95,0.78,0.55) * haloRing * outerFade * 0.16;

    // 阴影
    color = mix(color, vec3(0.0), shadowMask);

    // 边界混合
    float boundary = smoothstep(0.62, 0.98, impact);
    boundary = 1.0 - boundary * boundary;
    color = mix(originalScene, color, boundary);

    fragColor = vec4(color * vertexColor.rgb, vertexColor.a);
}
