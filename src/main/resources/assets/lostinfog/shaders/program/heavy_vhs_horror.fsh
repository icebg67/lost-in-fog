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

    float t = Time * 1.5;
    float glitchPeriod = step(0.85, random(vec2(floor(t * 0.8), 17.0)));

    float lineY = fract(t * 0.12);
    float lineDist = abs(uv.y - lineY);
    float bandGlitch = smoothstep(0.08, 0.0, lineDist) * glitchPeriod;

    float xOffset = 0.0;
    if (bandGlitch > 0.001) {
        xOffset += (random(vec2(floor(t * 20.0), floor(uv.y * 40.0))) - 0.5) * 0.02 * bandGlitch;
    }

    xOffset += (random(vec2(floor(uv.y * 300.0), floor(t * 30.0))) - 0.5) * 0.0008;
    uv.x += xOffset;

    float baseAberration = 0.0012;
    float glitchAberration = bandGlitch * 0.006 + (glitchPeriod * 0.003);
    float totalAberration = baseAberration + glitchAberration;

    vec4 colorR = texture(DiffuseSampler, uv + vec2(totalAberration, 0.0));
    vec4 colorG = texture(DiffuseSampler, uv);
    vec4 colorB = texture(DiffuseSampler, uv - vec2(totalAberration, 0.0));
    vec4 color = vec4(colorR.r, colorG.g, colorB.b, colorG.a);

    float scanline = sin(uv.y * OutSize.y * 1.5) * 0.03;
    color.rgb -= scanline;

    float fineGrain = (random(uv * OutSize.xy + fract(t) * 100.0) - 0.5) * 0.03;
    color.rgb += fineGrain;

    float streak = step(0.9985, random(vec2(floor(uv.y * 500.0), floor(t * 25.0))));
    color.rgb += vec3(streak * 0.15);

    float vignette = smoothstep(0.9, 0.25, distance(uv, vec2(0.5)));
    color.rgb *= mix(0.75, 1.0, vignette);

    color.rgb = mix(color.rgb, vec3(dot(color.rgb, vec3(0.299, 0.587, 0.114))), 0.12);
    color.rgb = pow(color.rgb, vec3(0.96, 1.01, 0.96));

    fragColor = color;
}