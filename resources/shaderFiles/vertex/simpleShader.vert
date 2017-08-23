#version 330
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec4 color;


out vec2 outTexCoord;
out vec4 outColor;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat4 worldMatrix;

void main()
{
    gl_Position =  projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    outTexCoord = texCoord;
    outColor = color;
}
