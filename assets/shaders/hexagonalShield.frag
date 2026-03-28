//#define HIGHP

#define ALPHA 0.18
#define STEP 2.0
#define SCALE 80.0
#define HEX_RATIO vec2(1.0, 1.732)

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;

varying vec2 v_texCoords;

vec4 getHex(vec2 p){
    // 两套错位蜂窝中心，选最近的一套，保证平铺无缝
    vec4 hC = floor(vec4(p, p - vec2(.5, 1.0)) / HEX_RATIO.xyxy) + .5;
    vec4 h = vec4(p - hC.xy * HEX_RATIO, p - (hC.zw + .5) * HEX_RATIO);
    return dot(h.xy, h.xy) < dot(h.zw, h.zw) ? vec4(h.xy, hC.xy) : vec4(h.zw, hC.zw + 9.43);
}

float hash21(vec2 p){
    return fract(sin(dot(p, vec2(141.173, 289.927))) * 43758.5453);
}

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;
    float dp = max(u_dp, 1.0);

    // 护盾表面轻微扰动，避免完全静态
    T += vec2(
    sin(coords.y / (3.0 * dp) + u_time / 20.0),
    sin(coords.x / (3.0 * dp) + u_time / 20.0)
    ) / u_texsize;

    vec4 color = texture2D(u_texture, T);
    float centerA = color.a;
    vec2 v = u_invsize;
    vec2 offsetStep = vec2(STEP) * v;

    // 四方向膨胀采样：用于描边和颜色继承
    vec4 maxed = max(max(
           texture2D(u_texture, T + vec2(0, offsetStep.y)),
           texture2D(u_texture, T - vec2(0, offsetStep.y))),
           max(texture2D(u_texture, T + vec2(offsetStep.x, 0)),
           texture2D(u_texture, T - vec2(offsetStep.x, 0)))
       );

    // 边缘外扩发光（保持原版强描边观感）
    if (centerA < 0.9 && maxed.a > 0.9) {
        gl_FragColor = vec4(maxed.rgb, maxed.a * 100.0);
    }else{
        if (centerA > 0.0) {
            // 保留原版条纹扫光
            if (mod(
                coords.x / dp + coords.y / dp +
                sin(coords.x / dp / 5.0) * 3.0 +
                sin(coords.y / dp / 5.0) * 3.0 +
                u_time / 4.0, 10.0) < 2.0) {
                color.rgb *= 1.65;
            }

            // 六边形域坐标
            float iTime = u_time * 0.003333;
            vec2 sc = coords / (SCALE * dp) + HEX_RATIO.yx * iTime;
            vec4 h = getHex(sc);
            float rnd = hash21(h.zw);

            vec2 p = h.xy * 1.03;
            float cDist = max(length(p), 1e-4);
            float sqrt3 = sqrt(3.0);
            float hexA = mod(acos(clamp(dot(p, vec2(0.0, 1.0)) / cDist, -1.0, 1.0)), 1.0472);
            float denom = max(sqrt3 * cos(hexA) + sin(hexA), 1e-4);
            float hexDist = (sqrt3 * 0.56) / denom;

            // 基础呼吸：每个格子随机相位，避免整屏同频
            vec3 col = mix(vec3(1.0), maxed.rgb, 0.2);
            col *= sin(iTime * 3.0 + rnd * 4.0) + 1.0;

            // 保留你要的“硬六边形分区”风格：格外触发扫描高亮
            if(cDist > hexDist){
                float lineX = mod(u_time, 100.0);
                float lineAngle = sin(floor(u_time / 100.0));

                if (mod((sc.x + lineX) + sc.y * lineAngle, 20.0) < 2.0) {
                    col = vec3(1.0);
                    color.a = maxed.a * 100.0;
                }else{
                    col.x = maxed.x;
                }
            }

            // 用 alpha 调制显示强度，维持原版护盾层次
            color = vec4(maxed.rgb, clamp(ALPHA * col.r, 0.0, 1.0));
        }

        gl_FragColor = color;
    }
}
