#version 300 es

precision highp float;

in vec4 worldNormal;
in vec3 VertexNormal;

out vec4 fragmentColor; //#vec4# A four-element vector [r,g,b,a].; Alpha is opacity, we set it to 1 for opaque.; It will be useful later for transparency.

uniform struct {
    vec4 position;
    vec3 powerDensity;
} lights[8];

void main(void) {
    fragmentColor = vec4(1.0f, 1.0f, 1.0f, 1.0f);
    for(int i = 0; i < 8; i++) {
        float dotProd = dot(worldNormal.xyz, lights[i].position.xyz);
        if(dotProd < 0.0f) {
            fragmentColor = vec4(1.0f-abs(dotProd)+.2f, 1.0f-abs(dotProd)+.2f, 1.0f-abs(dotProd)+.2f, 1);
        }
    }
    fragmentColor.x *= VertexNormal.x;
    fragmentColor.y *= VertexNormal.y;
    fragmentColor.z *= VertexNormal.z;
}
