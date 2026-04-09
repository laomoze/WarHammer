#define MAX_UV_SHIFT 0.10
#define MAX_LENSES 64

varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_screen;
uniform vec4 u_lenses[MAX_LENSES]; // xy=center(px), z=radius(px), w=strength
uniform int u_lens_count;

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

    delta = clamp(delta, vec2(-MAX_UV_SHIFT), vec2(MAX_UV_SHIFT));
    vec2 sampleUv = clamp(uv + delta, 0.0, 1.0);
    gl_FragColor = texture2D(u_texture, sampleUv);
}
