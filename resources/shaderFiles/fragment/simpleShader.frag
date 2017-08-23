#version 330

in vec2 outTexCoord;
in vec4 outColor;
out vec4 fragColor;

uniform sampler2D texture_sampler;
uniform bool hasTexture;

void main()
{
    if(hasTexture){
        fragColor = texture(texture_sampler, outTexCoord);
    }
    else{
        fragColor = vec4(1.0, 1.0, 1.0, .25);
    }
}
