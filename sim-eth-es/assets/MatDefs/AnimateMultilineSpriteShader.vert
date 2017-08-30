uniform mat4 g_WorldViewProjectionMatrix;
uniform float g_Tpf;
uniform float g_Time;

uniform int m_numTilesX;
uniform int m_numTilesY;
uniform float m_Speed; 
uniform int m_numTilesOffsetX;
uniform int m_numTilesOffsetY;
uniform float m_StartTime;

attribute vec3 inPosition; //The view
attribute vec2 inTexCoord;

varying vec2 texCoordAni; //parameter to calculate the correct texture coordinates and pass it to the frag shader

void main(){
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    
    float tileDistance = float((g_Time-m_StartTime)*m_Speed);
    int selectedTileX = int(mod(float(tileDistance), m_numTilesX))+m_numTilesOffsetX;
    int selectedTileY = (m_numTilesY-1) - (int(mod(float(tileDistance / m_numTilesX), m_numTilesY))+m_numTilesOffsetY);
    //int selectedTileY = (m_numTilesY-1)- m_numTilesOffsetY;

    texCoordAni.x = (float(float(inTexCoord.x/m_numTilesX) + float(selectedTileX)/float(m_numTilesX)));
    texCoordAni.y = (float(float(inTexCoord.y/m_numTilesY) + float(selectedTileY)/float(m_numTilesY)));
}