#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform float Time;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

void main() {
    vec2 uv = texCoord;

    float safeTime = mod(Time, 300.0);
    float cycle = mod(safeTime, 10.0);

    float glitchActive = step(9.0, cycle);
    float glitchProgress = clamp((cycle - 9.0) / 1.0, 0.0, 1.0);

    float lineY = glitchProgress;
    float lineDist = abs(uv.y - lineY);
    float bandGlitch = smoothstep(0.2, 0.0, lineDist) * glitchActive;

    float xOffset = 0.0;
    if (bandGlitch > 0.001) {
        xOffset += (random(vec2(floor(cycle * 30.0), floor(uv.y * 30.0))) - 0.5) * 0.04 * bandGlitch;
    }

    xOffset += (random(vec2(floor(uv.y * 200.0), floor(safeTime * 20.0))) - 0.5) * 0.0008;
    uv.x += xOffset;

    float baseAberration = 0.0012;
    float glitchAberration = bandGlitch * 0.015 + glitchActive * 0.005;
    float totalAberration = baseAberration + glitchAberration;

    vec4 colorR = texture(DiffuseSampler, uv + vec2(totalAberration, 0.0));
    vec4 colorG = texture(DiffuseSampler, uv);
    vec4 colorB = texture(DiffuseSampler, uv - vec2(totalAberration, 0.0));
    vec4 color = vec4(colorR.r, colorG.g, colorB.b, colorG.a);

    float scanline = sin(uv.y * OutSize.y * 1.5) * 0.03;
    color.rgb -= scanline;

    float fineGrain = (random(uv * OutSize.xy + fract(safeTime) * 100.0) - 0.5) * 0.03;
    color.rgb += fineGrain;

    float streak = step(0.998, random(vec2(floor(uv.y * 300.0), floor(safeTime * 15.0)))) * glitchActive;
    color.rgb += vec3(streak * 0.2);

    float vignette = smoothstep(0.9, 0.25, distance(uv, vec2(0.5)));
    color.rgb *= mix(0.75, 1.0, vignette);

    color.rgb = mix(color.rgb, vec3(dot(color.rgb, vec3(0.299, 0.587, 0.114))), 0.12);
    color.rgb = pow(color.rgb, vec3(0.96, 1.01, 0.96));

    fragColor = color;
}