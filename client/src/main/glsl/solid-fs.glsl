#version 300 es

precision highp float;

in vec4 worldNormal;

out vec4 fragmentColor; //#vec4# A four-element vector [r,g,b,a].; Alpha is opacity, we set it to 1 for opaque.; It will be useful later for transparency.

uniform struct {
    vec4 position;
    vec3 powerDensity;
} lights[8];

void main(void) {
    fragmentColor = vec4(.2f, .2f, .2f, 1);
    for(int i = 0; i < 8; i++) {
        if(dot(worldNormal.xyz, lights[i].position.xyz) > .5f) {
            fragmentColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }
}
