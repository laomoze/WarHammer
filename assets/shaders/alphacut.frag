varying lowp vec4 v_color;
varying lowp vec4 v_mix_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;

void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    float alpha = ((c.a * v_color.a) - 0.75) / (1.0 - 0.75);
    gl_FragColor = vec4(mix(c.rgb, v_mix_color.rgb, v_mix_color.a), alpha);
}
