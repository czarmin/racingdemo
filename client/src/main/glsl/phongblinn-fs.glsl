#version 300 es 
precision highp float;

out vec4 fragmentColor;
in vec4 tex;
in vec4 worldPosition;
in vec4 modelPosition;
in vec4 worldNormal;

uniform struct{
    sampler2D diffuse;
    vec3 specular;
    float shininess;
} material;

uniform struct{
	mat4 viewProjMatrix;
	vec3 position;
} camera;

uniform struct {
    vec4 position;
    vec3 powerDensity;
} lights[8];

vec3 shade(
  vec3 normal, vec3 lightDir, vec3 viewDir,
  vec3 powerDensity, vec3 materialColor, vec3 specularColor, float shininess) {

  float cosa = clamp(dot(normal, lightDir), 0.0, 1.0);
  vec3 halfway = normalize(lightDir + viewDir);
  float cosDelta = clamp(dot(normal, halfway), 0.0, 1.0);

  return
    powerDensity * materialColor * cosa
  + powerDensity * specularColor * pow(cosDelta, shininess);
}

void main(void) {
    vec3 normal = normalize(worldNormal.xyz);
    vec3 x = worldPosition.xyz / worldPosition.w;
    vec3 viewDir = normalize(camera.position - x);
  fragmentColor = vec4(0,0,0,1);
  for(int i = 0; i < 8; i++) {
    vec3 lightDir = lights[i].position.xyz;
     vec3 powerDensity = lights[i].powerDensity;
     fragmentColor.rgb += shade(normal,
       lightDir, viewDir, powerDensity,
       texture(material.diffuse, tex.xy/tex.w).rgb,
       material.specular, material.shininess);
  }
}
