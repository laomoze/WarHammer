
#define HIGHP

#define ALPHA 0.18
#define STEP 2.0
#define NSCALE 200.0
#define PI 3.14159265359
#define TAU 6.28318530718
#define hexratio vec2(1.0, 1.732)

uniform sampler2D u_texture;

uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;

varying vec2 v_texCoords;

float square(vec2 uv, float s) {
    return max(abs(uv.x), abs(uv.y)) - s;
}

void moda(inout vec2 p, float rep){
    float per = TAU / rep;
    float a = atan(p.y, p.x);
    a = mod(a, per) - per * 0.5;
    p = vec2(cos(a), sin(a)) * length(p);
}

float hexagone(vec2 uv){
    uv = abs(uv);
    return max(uv.x, dot(uv, normalize(hexratio)));
}
vec2 random2(vec2 p){
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}

vec4 hexgrid(vec2 uv){
    vec2 grid_a = mod(uv, hexratio) - hexratio * 0.5;
    vec2 grid_b = mod(uv - hexratio * 0.5, hexratio) - hexratio * 0.5;
    vec2 hex_uv = (length(grid_a) < length(grid_b)) ? grid_a : grid_b;
    vec2 hex_id = uv - hex_uv;
    return vec4(hex_uv, hex_id);
}

mat2 rotation (float angle)
{
    return mat2( cos(angle), sin(angle), -sin(angle), cos(angle) );
}

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;

    T += vec2(sin(coords.y / 3.0 + u_time / 20.0), sin(coords.x / 3.0 + u_time / 20.0)) / u_texsize;

    vec4 color = texture2D(u_texture, T);
    vec2 v = u_invsize;

    vec4 maxed = max(max(max(texture2D(u_texture, T + vec2(0, STEP) * v), texture2D(u_texture, T + vec2(0, -STEP) * v)), texture2D(u_texture, T + vec2(STEP, 0) * v)), texture2D(u_texture, T + vec2(-STEP, 0) * v));

    if(texture2D(u_texture, T).a < 0.9 && maxed.a > 0.9){

        gl_FragColor = vec4(maxed.rgb, maxed.a * 100.0);
    }else{

        if(color.a > 0.0){
            float coordFactor = 1.0 / u_dp;
            float base = (coords.x + coords.y) * coordFactor;
            float wave = 3.0 * (sin(coords.x * coordFactor / 5.0) + sin(coords.y * coordFactor / 5.0));
            float timing = u_time * 0.25;
            float timing2 = u_time /30.0;
            if(mod(base + wave + timing, 10.0) < 2.0){
                color *= 1.65;
            }
            vec2 st = coords / NSCALE;
            st *= sin(timing2/10.0)*0.01+0.7;
            st *= 4.3;
            //st *= rotation(timing2/50.0);
            moda(st, 3.0);
            st.x -= 2.0;
            vec4 hg = hexgrid(st);
            float mix_cursor = sin(sin(length(hg.zw * 0.8)-timing2)) * 0.5 + 0.5;
            float threshold = mix(0.1, 0.45, clamp(mix_cursor, 0.0, 1.0));
            vec3 col = vec3(step(threshold, hexagone(hg.xy)));
            color = vec4(maxed.rgb, ALPHA * (1-col.r) /* *abs(sin(coords.x / 5.0 + coords.y / 3.0 +timing))*/);
        }
        gl_FragColor = color;
    }
}