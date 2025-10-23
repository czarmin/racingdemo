#version 300 es

in vec4 vertexPosition;
in vec3 vertexNormal;
in vec4 vertexTexCoord;

uniform struct{
	mat4 modelMatrix;
	mat4 modelMatrixInverse;
} gameObject;

uniform struct{
  mat4 viewProjMatrix;
  vec3 position;
} camera;

out vec4 tex;
out vec4 worldPosition;
out vec4 modelPosition;
out vec4 worldNormal;

float noise(vec3 r) {
  uvec3 s = uvec3(
    0x1D4E1D4E,
    0x58F958F9,
    0x129F129F);
  float f = 0.0;
  for(int i=0; i<16; i++) {
    vec3 sf =
    vec3(s & uvec3(0xFFFF))
  / 65536.0 - vec3(0.5, 0.5, 0.5);

    f += sin(dot(sf, r));
    s = s >> 1;
  }
  return f / 32.0 + 0.5;
}

vec3 noiseGrad(vec3 r) {
  uvec3 s = uvec3(
    0x1D4E1D4E,
    0x58F958F9,
    0x129F129F);
  vec3 f = vec3(0, 0, 0);
  for(int i=0; i<16; i++) {
    vec3 sf =
    vec3(s & uvec3(0xFFFF))
  / 65536.0 - vec3(0.5, 0.5, 0.5);

    f += cos(dot(sf, r)) * sf;
    s = s >> 1;
  }
  return f;
}


void main(void) {
  gl_Position = vertexPosition * gameObject.modelMatrix * camera.viewProjMatrix;

  modelPosition = vertexPosition;
  worldPosition = vertexPosition * gameObject.modelMatrix;

  vec3 perturbed = vertexNormal + .1f * noiseGrad(5.0f * modelPosition.xyz);
  worldNormal = gameObject.modelMatrixInverse * vec4(perturbed, 0);


  tex = vertexTexCoord;
}