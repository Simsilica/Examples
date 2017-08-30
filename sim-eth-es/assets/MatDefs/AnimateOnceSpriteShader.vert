uniform mat4 g_WorldViewProjectionMatrix;
uniform float g_Tpf;
uniform float g_Time;

uniform int m_numTilesX;
uniform int m_numTilesY;
uniform float m_Speed; 
uniform int m_numTilesOffsetX;
uniform int m_numTilesOffsetY;

attribute vec3 inPosition; //The view
attribute vec2 inTexCoord;

varying vec2 texCoordAni; //parameter to calculate the correct texture coordinates and pass it to the frag shader
varying float completed;

void main(){
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    
    float tileDistance = float(g_Time*m_Speed);
    int selectedTileX = int(mod(float(tileDistance), m_numTilesX));
    int selectedTileY = int(mod(float(tileDistance), m_numTilesY));

    completed = (tileDistance / m_numTilesX ;

    texCoordAni.x = (float(float(inTexCoord.x/m_numTilesX) + float(selectedTileX)/float(m_numTilesX)));
    texCoordAni.y = (float(float(inTexCoord.y/m_numTilesY) + float(selectedTileY)/float(m_numTilesY)));
}