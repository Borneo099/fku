#version 150

uniform sampler2D DiffuseSampler;
uniform float Progress;    // 0.0 → 1.0 特效进度
uniform float Intensity;   // 扭曲强度

in vec2 uv;

out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = uv - center;
    float dist = length(offset);
    float maxDist = length(vec2(0.5, 0.5));
    float normDist = dist / maxDist; // 0.0 ~ 1.0

    // 黑洞大小：随进度先快后慢
    float blackHoleRadius = 0.02 + 0.35 * smoothstep(0.0, 0.6, Progress) * (1.0 - smoothstep(0.6, 1.0, Progress));

    // 扭曲强度：随进度先强后弱
    float warpStrength = Intensity * (1.0 - Progress) * 0.8;

    // 径向扭曲：离黑洞中心越近扭曲越强
    float falloff = 1.0 - smoothstep(0.0, 0.8, normDist);
    float angleOffset = warpStrength * 3.0 * falloff * (1.0 - normDist);
    float radialPull = warpStrength * 0.3 * falloff;

    // 扭曲坐标
    float angle = atan(offset.y, offset.x) + angleOffset;
    float radius = max(0.0, normDist - radialPull);
    radius = radius / (1.0 - radialPull * 0.5);

    vec2 distortedUv = center + vec2(cos(angle), sin(angle)) * radius * maxDist;
    vec4 color = texture(DiffuseSampler, distortedUv);

    // 黑洞中心：纯黑
    float holeEdge = smoothstep(blackHoleRadius * 0.8, blackHoleRadius * 1.2, normDist);
    color.rgb *= holeEdge;

    // 吸积盘光晕：蓝紫色光晕环绕黑洞边缘
    float glow = exp(-pow((normDist - blackHoleRadius * 1.5) / 0.08, 2.0));
    vec3 glowColor = vec3(0.3, 0.1, 0.8) * glow * warpStrength * 2.0;

    // 引力透镜：边缘色差（蓝移/红移）
    float redShift = 1.0 + 0.2 * warpStrength * falloff;
    float blueShift = 1.0 + 0.3 * warpStrength * falloff;
    color.r *= redShift;
    color.b *= blueShift;

    // 组合最终颜色
    fragColor = vec4(color.rgb + glowColor, color.a);
}
