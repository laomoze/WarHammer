#ifndef MAX_COUNT
#define MAX_COUNT 16
#endif

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform vec2 u_campos;
uniform float u_time;
uniform vec4 u_voidshields[MAX_COUNT];
uniform vec4 u_voidshield_states[MAX_COUNT];
uniform vec4 u_voidshield_alpha[MAX_COUNT];
uniform vec4 u_voidshield_colors[MAX_COUNT];
uniform int u_voidshield_count;
uniform vec4 u_voidshield_hits[MAX_COUNT];
uniform int u_voidshield_hit_count;

varying vec2 v_texCoords;

mat2 rotation(float angle){
    float cosine = cos(angle);
    float sine = sin(angle);
    return mat2(cosine, -sine, sine, cosine);
}

float ellipseMask(float distanceToCenter){
    return 1.0 - smoothstep(0.970, 1.025, distanceToCenter);
}

float ellipseEdge(float distanceToCenter){
    return smoothstep(0.875, 0.965, distanceToCenter)
        * (1.0 - smoothstep(1.000, 1.040, distanceToCenter));
}

float softBand(float value, float center, float width){
    return 1.0 - smoothstep(width, width * 4.0, abs(value - center));
}

vec4 hexData(vec2 grid){
    const vec2 period = vec2(1.0, 1.7320508);
    vec2 first = mod(grid, period) - period * 0.5;
    vec2 second = mod(grid - period * 0.5, period) - period * 0.5;
    vec2 cell = dot(first, first) < dot(second, second) ? first : second;
    return vec4(cell, grid - cell);
}

float hexMetric(vec2 cell){
    cell = abs(cell);
    return max(cell.x, dot(cell, normalize(vec2(1.0, 1.7320508))));
}

float hash(vec2 value){
    return fract(sin(dot(value, vec2(127.1, 311.7))) * 43758.5453);
}

vec2 hexTexture(vec2 shieldLocal, float travel){
    vec4 hex = hexData((shieldLocal + vec2(travel, 0.0)) / 26.0);
    float outline = 1.0 - smoothstep(0.012, 0.038, abs(hexMetric(hex.xy) - 0.425));
    float phase = sin(hex.z * 1.35 - u_time * 0.028 + hash(hex.zw) * 6.2831853) * 0.5 + 0.5;
    return vec2(outline, 0.30 + phase * 0.70);
}

float longitudinalWave(vec2 shieldUv, float phase, float shieldId){
    float path = sin(shieldUv.x * 6.0 - u_time * 0.035 + shieldId) * 0.10;
    path += sin(shieldUv.x * 13.0 + u_time * 0.021 + shieldId * 1.7) * 0.035;
    float verticalLine = exp(-pow((shieldUv.y - path) / 0.095, 2.0));
    float progress = cos((shieldUv.x - phase) * 8.0 - u_time * 0.040) * 0.5 + 0.5;
    return verticalLine * progress;
}

void main(){
    vec2 worldPosition = u_campos + v_texCoords * u_resolution;
    vec3 light = vec3(0.0);
    float alpha = 0.0;

    for(int shieldIndex = 0; shieldIndex < MAX_COUNT; shieldIndex++){
        if(shieldIndex >= u_voidshield_count) break;

        vec4 shield = u_voidshields[shieldIndex];
        vec4 state = u_voidshield_states[shieldIndex];
        vec2 axes = max(shield.zw, vec2(2.0));
        vec2 shieldLocal = rotation(-state.w) * (worldPosition - shield.xy);
        vec2 shieldUv = shieldLocal / axes;

        float edgeAngle = atan(shieldUv.y, shieldUv.x);
        float edgeOffset = sin(edgeAngle * 14.0 - u_time * 0.075 + float(shieldIndex) * 1.91) * 0.006;
        edgeOffset += sin(edgeAngle * 27.0 + u_time * 0.042 + float(shieldIndex) * 0.73) * 0.003;
        edgeOffset += sin(edgeAngle * 6.0 - u_time * 0.020 + float(shieldIndex) * 2.53) * 0.004;

        float distanceToCenter = length(shieldUv) - edgeOffset;
        float inside = ellipseMask(distanceToCenter);
        float edge = ellipseEdge(distanceToCenter);
        float opacity = clamp(u_voidshield_alpha[shieldIndex].x, 0.0, 1.0);
        float shieldHealth = max(state.z, 0.0);

        float mode = floor(state.x + 0.5);
        float overload = step(0.5, mode) * (1.0 - step(1.5, mode));
        float recovery = step(1.5, mode);
        float stateActive = max(overload, recovery);
        float stateProgress = fract(state.y);

        float scanPosition = 1.10 - stateProgress * 2.20;
        float recoveryFill = smoothstep(scanPosition - 0.07, scanPosition + 0.07, shieldUv.y);
        float overloadFill = 1.0 - recoveryFill;
        float scanFill = mix(overloadFill, recoveryFill, recovery);
        float scanBand = softBand(shieldUv.y, scanPosition, 0.032);

        float firstWave = longitudinalWave(shieldUv, 1.16 - fract(u_time * 0.0028 + float(shieldIndex) * 0.23) * 2.32, float(shieldIndex));
        float secondWave = longitudinalWave(shieldUv, 1.16 - fract(u_time * 0.0028 + float(shieldIndex) * 0.23 + 0.42) * 2.32, float(shieldIndex) + 2.0);
        float waves = inside * (firstWave * 0.28 + secondWave * 0.14);
        vec2 hexValues = hexTexture(shieldLocal, u_time * 0.24 + float(shieldIndex) * 6.5);
        float hexes = inside * hexValues.x;
        float hexBrightness = hexValues.y;

        float fillLight = inside * (0.20 + 0.06 * shieldHealth);
        float edgeLight = edge * (0.48 + 0.12 * sin(edgeAngle * 10.0 + u_time * 0.045 + float(shieldIndex)));
        float textureLight = hexes * (0.12 + 0.22 * hexBrightness) + waves * 0.72;
        float stateLight = stateActive * (inside * scanFill * 0.18 + scanBand * (0.20 + edge * 0.55));
        float strength = (fillLight + edgeLight + textureLight + stateLight) * opacity;

        vec3 baseColor = u_voidshield_colors[shieldIndex].rgb;
        vec3 overloadColor = baseColor * vec3(0.46, 0.32, 0.92);
        vec3 recoveryColor = mix(baseColor, vec3(0.72, 1.00, 1.00), 0.45);
        vec3 color = mix(baseColor, overloadColor, overload);
        color = mix(color, recoveryColor, recovery);
        color = mix(color, vec3(0.92, 1.00, 1.00), clamp(hexes * (0.22 + 0.36 * hexBrightness) + waves * 0.42, 0.0, 0.62));

        light += color * strength * 1.28;
        alpha = max(alpha, strength * 0.40);
    }

    gl_FragColor = vec4(light, alpha);
}