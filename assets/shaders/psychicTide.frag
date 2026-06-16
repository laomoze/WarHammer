
uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 u_campos;
uniform vec2 u_resolution;

varying vec2 v_texCoords;

float hash(vec2 p){
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p){
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(vec2 p){
    float value = 0.0;
    float amplitude = 0.5;

    for(int i = 0; i < 4; i++){
        value += noise(p) * amplitude;
        p = p * 2.03 + vec2(17.1, 9.7);
        amplitude *= 0.5;
    }

    return value;
}

float saturate(float x){
    return clamp(x, 0.0, 1.0);
}

void main(){
    vec2 uv = v_texCoords.xy;
    vec4 base = texture2D(u_texture, uv);

    if(base.a <= 0.001){
        gl_FragColor = vec4(0.0);
        return;
    }

    vec2 world = u_campos + uv * u_resolution;
    float t = u_time * 0.0085;
    float density = smoothstep(0.03, 0.86, base.a);
    vec2 p = world / 165.0;

    vec2 waveDir = normalize(vec2(0.88, 0.47));
    vec2 crossDir = vec2(-waveDir.y, waveDir.x);

    float driftA = fbm(p * 0.95 + vec2(t * 0.22, -t * 0.12));
    float driftB = fbm(p * 1.8 + vec2(-t * 0.38, t * 0.21));
    float turbulence = fbm(p * 3.0 + vec2(t * 0.74, -t * 0.58));

    vec2 warpedP = p
        + waveDir * ((driftA - 0.5) * 1.15 + (turbulence - 0.5) * 0.35)
        + crossDir * ((driftB - 0.5) * 0.9);

    float longWave = sin(dot(warpedP, waveDir) * 8.5 - t * 3.1 + turbulence * 2.4);
    float chopWave = sin(dot(warpedP, crossDir) * 18.0 + t * 5.3 + driftA * 4.8);
    float undertow = sin(dot(p, waveDir) * 4.2 - t * 1.4 + driftB * 2.9);

    float crest = pow(saturate(1.0 - abs(longWave) * 1.55), 3.1);
    float chop = saturate(chopWave * 0.5 + 0.5);
    float trough = saturate((-longWave) * 0.5 + 0.5);
    float seam = smoothstep(0.45, 0.9, fbm(warpedP * 2.6 + vec2(-t * 0.66, t * 0.41)));

    vec2 offset =
        waveDir * longWave * (0.003 + density * 0.009) +
        crossDir * (chopWave * 0.0018 + (driftB - 0.5) * 0.0035) * (0.4 + density * 0.9);

    vec4 warped = texture2D(u_texture, clamp(uv + offset, 0.0, 1.0));
    float warpedAlpha = max(base.a * 0.92, warped.a);
    float mass = smoothstep(0.03, 0.88, warpedAlpha);

    vec3 abyss = vec3(0.05, 0.01, 0.10);
    vec3 warpGlow = vec3(0.31, 0.07, 0.58);
    vec3 crestGlow = vec3(0.78, 0.63, 0.94);

    vec3 color = mix(
        warped.rgb * 0.42 + abyss * 1.05,
        warped.rgb * 1.05 + warpGlow * 0.5,
        mass
    );

    color *= 0.72 + (undertow * 0.5 + 0.5) * 0.28;
    color *= 0.92 - trough * 0.16;
    color += warpGlow * seam * (0.12 + mass * 0.24);
    color += warpGlow * chop * (0.03 + mass * 0.08);
    color += crestGlow * crest * (0.06 + mass * 0.16);
    color = min(color, vec3(0.96, 0.88, 1.0));

    float alpha = warpedAlpha * (0.92 + mass * 1.28 + crest * 0.3 + seam * 0.14);
    alpha = clamp(alpha, 0.0, 1.0);
    gl_FragColor = vec4(color, alpha);
}
