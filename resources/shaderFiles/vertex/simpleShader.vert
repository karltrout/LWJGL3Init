#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
//layout (location=2) in vec4 color;
layout (location=2) in vec3 vertexNormal;


out vec2 outTexCoord;
out vec4 outColor;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out float visibility;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;
uniform mat4 viewMatrix;
uniform mat4 transformationMatrix;

const float density = 0.00030;
const float gradient = 5;


void main()
{

    vec4 worldPosition = transformationMatrix * vec4(position, 1.0);
    vec4 mvPos = modelViewMatrix * vec4(position, 1.0);
    gl_Position = projectionMatrix * mvPos;
    outTexCoord = texCoord;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    mvVertexPos = mvPos.xyz;

    vec4 positionRelativeToCam = viewMatrix * worldPosition;

    outColor = vec4(1.0,1.0,1.0,1.0);

    float distance = length(positionRelativeToCam.xyz);
    visibility = exp(-pow((distance * density),gradient));
    visibility = clamp(visibility, 0.0, 1.0);

}
