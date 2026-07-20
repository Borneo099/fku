#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 ScreenSize;
uniform vec4 QuadPos;       // x, y, width, height
uniform float QuadRadius;   // 圆角半径
uniform float BlurRadius;
uniform float RefractionPower;
uniform float RefractionEdge;
uniform float Dispersion;
uniform float GlobalAlpha;
uniform vec4 GlassTint;     // rgb + tintStrength
uniform int TintMode;       // 0=Clear, 1=Tinted
uniform float Noise;
uniform float GlowWeight;
uniform float GlowBias;
uniform float GlowEdge0;
uniform float GlowEdge1;
uniform float ChromaStrength;
uniform float Darkness;

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

// 超椭圆有符号距离场
float sdSuperellipse(vec2 p, float n, float r) {
    vec2 absP = abs(p);
    float numerator = pow(absP.x, n) + pow(absP.y, n) - pow(r, n);
    float denominator = n * sqrt(pow(absP.x, 2.0 * n - 2.0) + pow(absP.y, 2.0 * n - 2.0)) + 0.00001;
    return numerator / denominator;
}

// 随机噪声
float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

// 折射衰减函数
float f(float x) {
    const float M_E = 2.718281828459045;
    return 1.0 - 2.3 * pow(5.2 * M_E, -6.9 * x - 0.7);
}

// 边缘发光
float Glow(vec2 uv) {
    vec2 glowUV = uv * 2.0 - 1.0;
    return sin(atan(glowUV.y, glowUV.x) - 0.5);
}

bool OutOfBounds(vec2 uv) {
    return max(uv.x, uv.y) > 1.0 || min(uv.x, uv.y) < 0.0;
}

void main() {
    vec2 screenCoord = gl_FragCoord.xy;
    vec2 quadPos = QuadPos.xy;
    vec2 quadSize = QuadPos.zw;

    // 计算当前片元在四边形中的局部坐标 [0,1]
    vec2 localUV = (screenCoord - quadPos) / quadSize;
    if (OutOfBounds(localUV)) {
        discard;
        return;
    }

    // 超椭圆圆角检测
    vec2 center = vec2(0.5);
    vec2 p = (localUV - center) * 2.0;
    float radius = QuadRadius / min(quadSize.x, quadSize.y) * 2.0;
    float d = sdSuperellipse(p, 6.0, 1.0 - radius);

    // 边缘平滑
    float edge = 1.0 - smoothstep(-0.003, 0.003, d);
    if (edge <= 0.0) {
        discard;
        return;
    }

    // 计算到边缘的距离（用于折射和发光）
    float dist = max(-d, 0.0);

    // 折射偏移
    float refraction = pow(f(dist), RefractionPower);
    vec2 refractOffset = (localUV - center) * refraction * BlurRadius / ScreenSize;

    // 采样屏幕纹理（带折射偏移）
    vec2 sampleUV = localUV - refractOffset;
    // 防止越界
    sampleUV = clamp(sampleUV, 0.001, 0.999);

    vec4 baseColor = texture(DiffuseSampler, sampleUV);

    // 色散效果（Tinted模式）
    if (TintMode == 1 && ChromaStrength > 0.0) {
        vec2 chromaDir = normalize(localUV - center + 0.00001);
        float fresnel = pow(1.0 - dist, 3.0);
        vec2 chromaOffset = chromaDir * fresnel * ChromaStrength * BlurRadius / ScreenSize;
        float r = texture(DiffuseSampler, sampleUV + chromaOffset).r;
        float g = texture(DiffuseSampler, sampleUV).g;
        float b = texture(DiffuseSampler, sampleUV - chromaOffset).b;
        baseColor = vec4(r, g, b, 1.0);
    }

    // 噪声/磨砂效果
    float grain = (rand(gl_FragCoord.xy * 1e-3) - 0.5) * Noise;
    baseColor.rgb += grain;

    // Clear模式：边缘发光
    if (TintMode == 0) {
        float glow = Glow(localUV);
        float glowMask = smoothstep(GlowEdge0, GlowEdge1, dist);
        float glowStrength = glow * GlowWeight * glowMask + 1.0 + GlowBias;
        baseColor.rgb *= glowStrength;
    }

    // Tinted模式：染色
    if (TintMode == 1) {
        float luma = dot(baseColor.rgb, vec3(0.299, 0.587, 0.114));
        // 饱和度调整
        float saturation = mix(0.45, 0.75, luma);
        vec3 grayscale = vec3(luma);
        baseColor.rgb = mix(grayscale, baseColor.rgb, saturation);

        // 染色
        vec3 tintColor = GlassTint.rgb;
        vec3 adaptiveTint = mix(tintColor, vec3(0.08, 0.09, 0.11), Darkness);
        float adaptiveStrength = GlassTint.a * (1.0 - luma * 0.5);
        baseColor.rgb = mix(baseColor.rgb, adaptiveTint, adaptiveStrength);

        // 菲涅尔反射
        float fresnel = pow(1.0 - dist, 3.0);
        baseColor.rgb += tintColor * fresnel * 0.12;

        // 暗度调整
        baseColor.rgb *= mix(1.0, 0.82, Darkness);
    }

    // 边缘透明度衰减
    baseColor.rgb *= edge;
    baseColor.a = edge * GlobalAlpha;

    fragColor = baseColor * vertexColor;
}