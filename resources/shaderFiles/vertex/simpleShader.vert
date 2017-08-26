#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
//layout (location=2) in vec4 color;
layout (location=2) in vec3 vertexNormal;


out vec2 outTexCoord;
out vec4 outColor;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main()
{
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    mvVertexPos = mvPos.xyz;

    outColor = vec4(1.0,1.0,1.0,1.0);
}
