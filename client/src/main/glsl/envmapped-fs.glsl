#version 300 es 
precision highp float;

out vec4 fragmentColor;

in vec4 tex;
in vec4 worldPosition;
in vec4 modelPosition;
in vec4 worldNormal;

uniform struct{
    samplerCube envmapTexture;
} material;

uniform struct{
  mat4 viewProjMatrix;
  vec3 position;
} camera;


//LABTODO: uniforms for light source data

void main(void) {
    vec3 normal = normalize(worldNormal.xyz);
    vec3 x = worldPosition.xyz / worldPosition.w;
    vec3 viewDir = normalize(camera.position - x);
    fragmentColor = texture( material.envmapTexture, reflect(-viewDir, normal));
}
