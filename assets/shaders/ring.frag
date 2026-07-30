varying vec4 v_col;
varying float v_alpha;

void main(){
    gl_FragColor = vec4(v_col.rgb, v_alpha);
}