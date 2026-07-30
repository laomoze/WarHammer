attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec4 a_color;
attribute vec4 a_emissive;

uniform mat4 u_proj;
uniform mat4 u_trans;
uniform vec3 u_lightdir;
uniform vec3 u_campos;
uniform vec3 u_ambientColor;
uniform float u_emissive;

varying vec4 v_col;
varying float v_alpha;

const vec3 diffuse = vec3(0.01);

void main(){
    vec3 worldPosition = (u_trans * a_position).xyz;
    vec3 center = (u_trans * vec4(0.0, 0.0, 0.0, 1.0)).xyz;
    vec3 radial = normalize(worldPosition - center);
    vec3 light = normalize(u_lightdir);
    float localLight = max(dot(radial, light), 0.0);
    float hotspot = pow(localLight, 4.0);

    vec3 normal = normalize((u_trans * vec4(a_normal, 0.0)).xyz);
    vec3 specular = vec3(0.0);
    vec3 lightReflect = normalize(reflect(normal, light));
    vec3 vertexEye = normalize(u_campos - worldPosition);

    float albedo = 1.0 - a_color.a;
    float specularFactor = dot(vertexEye, lightReflect);
    if(specularFactor > 0.0){
        specular = vec3(pow(specularFactor, 40.0)) * albedo;
    }

    vec3 norc = (u_ambientColor + specular) * (diffuse + vec3(clamp((dot(normal, light) + 1.0) / 2.0, 0.0, 1.0)));
    float emissive = a_emissive.a * u_emissive * min(pow(max(0.0, (1.0 - norc.r) * 1.2), 3.0), 1.1);

    vec3 ringColor = mix(a_color.rgb, a_emissive.rgb, emissive) * mix(norc, vec3(1.0), emissive);
    ringColor += vec3(1.0, 0.82, 0.25) * hotspot * 1.5;
    v_col = vec4(ringColor, 1.0);
    v_alpha = clamp(a_color.a + hotspot * 0.15, 0.0, 1.0);
    gl_Position = u_proj * u_trans * a_position;
}