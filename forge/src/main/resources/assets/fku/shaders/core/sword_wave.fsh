#version 150

uniform float Time;
uniform float Progress;
uniform float WaveIntensity;
uniform float WaveSpeed;
uniform float BaseHue;

in vec4 vertexColor;
in vec3 localPos;
in vec3 worldNormal;

out vec4 fragColor;

// ════════════════════════════════════════════
// 工具函数
// ════════════════════════════════════════════

float saturate(float x) { return clamp(x, 0.0, 1.0); }

/** 2D 哈希 — 用于噪声 */
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

/** 2D 值噪声 */
float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    float a = hash(i), b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0)), d = hash(i + vec2(1.0, 1.0));
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

/** HSV → RGB 转换 */
vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

/** 平滑的脉冲波形 */
float pulse(float t, float a, float b) {
    return smoothstep(a, a + (b - a) * 0.5, t) * (1.0 - smoothstep(b - (b - a) * 0.5, b, t));
}

// ════════════════════════════════════════════
// 主函数
// ════════════════════════════════════════════

void main() {
    // 基础参数
    float speed = WaveSpeed * 1.5;
    float t = Time * 0.001 * speed;          // 时间（秒）
    float prog = Progress;                    // 剑波进度 0~1
    float progPulse = sin(prog * 3.14159);    // 先升后降

    // 位置衰减 — 基于距离中心的径向距离
    float radius = length(localPos.xz);
    float height = abs(localPos.y);

    // ★ 弧线效果：沿弧线方向的渐变
    //   localPos.x 沿弧线方向，localPos.y 为弧线宽度方向
    float arcPos = atan(localPos.z, localPos.x);  // 角度位置
    float arcNorm = arcPos / 3.14159;              // 归一化到 -1~1

    // ★ 动态颜色循环（彩虹效果）
    float hueShift = BaseHue + t * 0.15 + prog * 0.3 + arcNorm * 0.2;
    float sat = 0.85 + 0.15 * sin(t * 1.3 + arcPos * 2.0);
    float val = 0.95 + 0.05 * sin(t * 0.7 + prog * 4.0);
    vec3 rainbow = hsv2rgb(vec3(fract(hueShift), sat, val));

    // ★ 用户颜色混合
    vec3 userColor = vertexColor.rgb;
    float colorMix = 0.45 + 0.25 * sin(t * 0.5 + prog * 3.0);
    vec3 baseColor = mix(rainbow, userColor, colorMix);

    // ★ 边缘辉光（Fresnel-like）
    float viewAngle = abs(dot(normalize(worldNormal), vec3(0.0, 0.0, 1.0)));
    float edgeGlow = pow(1.0 - viewAngle, 3.0) * 0.8;

    // ★ 核心发光 — 弧线中心最亮
    float radialDist = length(localPos.xz);
    float coreGlow = exp(-radialDist * 3.0) * 0.6;
    float arcGlow = exp(-pow(arcNorm, 2.0) * 8.0) * 0.4;

    // ★ 脉冲光效 — 沿弧线传播的能量波
    float wavePhase = t * 2.0 - prog * 5.0;
    float waveGlow = 0.3 * (0.5 + 0.5 * sin(wavePhase + arcPos * 4.0 + radius * 6.0));
    waveGlow *= progPulse;

    // ★ 噪声扰动 — 模拟能量波动
    float n1 = noise(vec2(localPos.x * 0.5 + t * 0.3, localPos.z * 0.5 + t * 0.2));
    float n2 = noise(vec2(localPos.x * 1.0 - t * 0.2, localPos.y * 0.8 + t * 0.4));
    float noiseGlow = 0.15 * n1 + 0.10 * n2;

    // ★ 扫掠尾迹 — 从剑波中心向外扩散的发光拖尾
    float trail = exp(-abs(radius - prog * 2.0) * 4.0) * 0.5 * progPulse;

    // ★ 粒子火花 — 沿弧线分布的闪烁亮点
    float sparkSeed = hash(vec2(floor(arcPos * 12.0 + t * 3.0), floor(radius * 8.0)));
    float spark = 0.0;
    if (sparkSeed > 0.85) {
        float sparkPhase = fract(arcPos * 6.0 + t * 2.0 + radius * 4.0);
        spark = (1.0 - sparkPhase) * 0.4 * progPulse;
    }

    // ★ 合成最终亮度
    float totalGlow = edgeGlow + coreGlow + arcGlow + waveGlow + noiseGlow + trail + spark;
    totalGlow *= WaveIntensity * (0.8 + 0.2 * progPulse);

    // ★ 透明度衰减 — 根据进度淡出
    float alphaFade = 1.0 - smoothstep(0.5, 1.0, prog);
    alphaFade *= 0.6 + 0.4 * progPulse;

    // ★ 最终颜色
    vec3 finalColor = baseColor * (1.0 + totalGlow * 1.5);
    // 添加白色核心高亮
    float whiteCore = coreGlow * 2.0 + edgeGlow * 1.5;
    finalColor += vec3(1.0, 1.0, 1.0) * whiteCore * 0.3;
    // 添加蓝色/紫色边缘光晕
    vec3 edgeColor = mix(vec3(0.4, 0.6, 1.0), vec3(0.8, 0.3, 1.0), sin(t * 0.5 + arcPos) * 0.5 + 0.5);
    finalColor += edgeColor * edgeGlow * 0.4;

    float finalAlpha = saturate(alphaFade * (0.5 + totalGlow * 0.8)) * vertexColor.a;

    fragColor = vec4(finalColor, finalAlpha);
}