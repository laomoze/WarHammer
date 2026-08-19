varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying vec2 v_texCoords;
varying vec2 v_worldCoords;
uniform sampler2D u_texture;
uniform float u_cutEnabled;
uniform vec2 u_cutPoint;
uniform vec2 u_cutDirection;
uniform float u_cutWidth;
uniform vec3 u_cutColor;
uniform float u_time;

void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    float alpha = c.a * v_color.a;
    if(alpha <= 0.001) discard;
    vec3 rgb = c.rgb * v_color.rgb;

    vec2 lineDirection = normalize(u_cutDirection);
    float lineSide = lineDirection.x * (v_worldCoords.y - u_cutPoint.y) - lineDirection.y * (v_worldCoords.x - u_cutPoint.x);
    float lineDistance = abs(lineSide);
    float cutEdge = u_cutEnabled * (1.0 - smoothstep(u_cutWidth, u_cutWidth * 2.6, lineDistance));
    float pulse = 0.72 + 0.28 * sin(u_time * 0.14 + v_worldCoords.x * 0.24 + v_worldCoords.y * 0.17);

    rgb = mix(rgb, u_cutColor, cutEdge * 0.88);
    rgb += u_cutColor * cutEdge * pulse * 0.45;
    gl_FragColor = vec4(rgb, alpha);
}
