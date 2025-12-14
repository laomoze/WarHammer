

#define ALPHA 0.18
#define step 2.0
#define SCALE 80.0
#define hexratio vec2(1.0, 1.732)
#define sqrt3_hexR (sqrt3 * 0.56)

uniform sampler2D u_texture;
uniform vec2 u_texsize;
uniform vec2 u_invsize;
uniform float u_time;
uniform float u_dp;
uniform vec2 u_offset;

varying vec2 v_texCoords;



mat2 r2(in float a){
    float c = cos(a), s = sin(a);
    return mat2(c, -s, s, c);
}

vec4 getHex(vec2 p){

    // The hexagon centers: Two sets of repeat hexagons are required to fill in the space, and
    // the two sets are stored in a "vec4" in order to group some calculations together. The hexagon
    // center we'll eventually use will depend upon which is closest to the current point. Since
    // the central hexagon point is unique, it doubles as the unique hexagon ID.
    vec4 hC = floor(vec4(p, p - vec2(.5, 1)) / hexratio.xyxy) + .5;

    // Centering the coordinates with the hexagon centers above.
    vec4 h = vec4(p - hC.xy * hexratio, p - (hC.zw + .5) * hexratio);

    // Nearest hexagon center (with respect to p) to the current point. In other words, when
    // "h.xy" is zero, we're at the center. We're also returning the corresponding hexagon ID -
    // in the form of the hexagonal central point. Note that a random constant has been added to
    // "hC.zw" to further distinguish it from "hC.xy."
    //
    // On a side note, I sometimes compare hex distances, but I noticed that Iomateron compared
    // the Euclidian version, which seems neater, so I've adopted that.
    return dot(h.xy, h.xy) < dot(h.zw, h.zw) ? vec4(h.xy, hC.xy) : vec4(h.zw, hC.zw + 9.43);

}

float hash21(vec2 p){
    return fract(sin(dot(p, vec2(141.173, 289.927))) * 43758.5453);
}

float rand(vec2 co){
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

void main(){
    vec2 T = v_texCoords.xy;
    vec2 coords = (T * u_texsize) + u_offset;

    T += vec2(sin(coords.y / 3.0 + u_time / 20.0), sin(coords.x / 3.0 + u_time / 20.0)) / u_texsize;

    vec4 color = texture2D(u_texture, T);
    vec2 v = u_invsize;
    vec2 offsetStep = vec2(step) * v;

    vec4 maxed = max(max(
           texture2D(u_texture, T + vec2(0, offsetStep.y)),
           texture2D(u_texture, T - vec2(0, offsetStep.y))),
           max(texture2D(u_texture, T + vec2(offsetStep.x, 0)),
           texture2D(u_texture, T - vec2(offsetStep.x, 0)))
       );

    if(texture2D(u_texture, T).a < 0.9 && maxed.a > 0.9){

        gl_FragColor = vec4(maxed.rgb, maxed.a * 100.0);
    }else{

        if(color.a > 0.0){

            float iTime = u_time * 0.003333;
            vec2 u = vec2(v_texCoords.x * u_texsize.x + u_offset.x, v_texCoords.y * u_texsize.y + u_offset.y);
            vec2 sc = coords / SCALE + hexratio.yx * iTime;
            vec4 h = getHex(sc);

            // Storing the hexagonal coordinates in "p" to save having to write "h.xy" everywhere.
            vec2 p = h.xy*1.03;

            float cDist = length(p);

            // Using the idetifying coordinate - stored in "h.zw," to produce a unique random number
            // for the hexagonal grid cell.
            float rnd = hash21(h.zw);

            //Base Fade In-Out
            vec3 col = mix(vec3(1.), maxed.rgb, 0.2);
            col *= sin(iTime*3.0 + rnd * 4.0) + 1.0;

            float sqrt3 = sqrt(3.);
            float hexR = 0.56;
            float hexA = mod(acos(dot(p, vec2(0., 1.))/cDist), 1.0472);
            float hexDist = sqrt3_hexR / (sqrt3 * cos(hexA) + sin(hexA));

            if(cDist > hexDist){
                float lineX = mod(u_time, 100.0);
                float lineAngle = sin(floor(u_time / 100.0));

                if(mod((sc.x + lineX) + sc.y * lineAngle, 20.) < 2.){
                    col = vec3(1.);
                    color.a = maxed.a * 100.0;
                }else{
                    col.x = maxed.x;
                }
            }
           color = vec4(maxed.rgb, ALPHA * col.r);
        }

        gl_FragColor = color;
    }
}
