#define MAX_UV_SHIFT 0.10
#define MAX_LENSES 64
#define MAX_RECTS 64

varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_screen;
uniform vec4 u_lenses[MAX_LENSES]; // xy=center(px), z=radius(px), w=strength
uniform int u_lens_count;
uniform vec4 u_rectsA[MAX_RECTS]; // xy=center(px), z=halfLength(px), w=halfWidth(px)
uniform vec4 u_rectsB[MAX_RECTS]; // x=cos(angle), y=sin(angle), z=strength, w=unused
uniform int u_rect_count;

void main() {
    vec2 uv = v_texCoords;
    vec2 fragPx = uv * u_screen;
    vec2 delta = vec2(0.0);

    for (int i = 0; i < MAX_LENSES; i++) {
        if (i >= u_lens_count) break;

        vec4 lens = u_lenses[i];
        float radius = lens.z;
        float strength = lens.w;
        if (radius <= 0.0001 || strength <= 0.0) continue;

        vec2 d = fragPx - lens.xy;
        float dist = length(d);
        if (dist >= radius) continue;

        float norm = dist / max(radius, 0.0001);
        float core = 1.0 - norm;
        float ring = 1.0 - smoothstep(0.0, 0.35, abs(norm - 0.62));
        float amp = core * 0.28 + ring * 1.35;

        vec2 dir = d / max(dist, 0.0001);
        float shift = strength * radius * 0.04 * amp;
        delta += -dir * shift / u_screen;
    }

    for (int i = 0; i < MAX_RECTS; i++) {
        if (i >= u_rect_count) break;

        vec4 a = u_rectsA[i];
        vec4 b = u_rectsB[i];

        float halfLen = a.z;
        float halfWid = a.w;
        float strength = b.z;
        if (halfLen <= 0.0001 || halfWid <= 0.0001 || strength <= 0.0) continue;

        vec2 d = fragPx - a.xy;
        float cosv = b.x;
        float sinv = b.y;

        float localX = d.x * cosv + d.y * sinv;
        float localY = -d.x * sinv + d.y * cosv;

        float ay = abs(localY);
        if (localX <= -halfLen || localX >= halfLen || ay >= halfWid) continue;

        // Axial factor is one-directional: 0 at emitter side, 1 at beam tip side.
        // This avoids the "middle starts first" look from symmetric abs(localX).
        float tx = (localX + halfLen) / max(2.0 * halfLen, 0.0001);
        float ny = ay / max(halfWid, 0.0001); // 0 center -> 1 side edge

        // Keep strong on most of the beam, then fade out near the tip.
        float along = 1.0 - smoothstep(0.82, 1.0, tx);
        // Side profile: center weak -> side stronger -> outer edge soft fade.
        float side = smoothstep(0.18, 0.70, ny) * (1.0 - smoothstep(0.82, 1.0, ny));
        float sideSign = localY >= 0.0 ? 1.0 : -1.0;
        float amp = along * max(side, 0.0);

        // Note: uv+delta samples from delta direction, visual movement is opposite.
        // Use negative normal here so the beam does not look like an inward "shrink".
        vec2 perp = vec2(-sinv, cosv);
        float shift = strength * halfWid * 0.105 * amp;
        delta += -perp * (shift * sideSign) / u_screen;
    }

    delta = clamp(delta, vec2(-MAX_UV_SHIFT), vec2(MAX_UV_SHIFT));
    vec2 sampleUv = clamp(uv + delta, 0.0, 1.0);
    gl_FragColor = texture2D(u_texture, sampleUv);
}
