uniform mat4 g_WorldViewProjectionMatrix;

uniform int m_numTilesX;
uniform int m_numTilesY;
uniform int m_numTilesOffsetX;
uniform int m_numTilesOffsetY;

attribute vec3 inPosition; //The view
attribute vec2 inTexCoord;

varying vec2 texCoordAni; //parameter to calculate the correct texture coordinates and pass it to the frag shader

void main(){
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    
    int selectedTileX = m_numTilesOffsetX;
    int selectedTileY = m_numTilesOffsetY;

    texCoordAni.x = (float(float(inTexCoord.x/m_numTilesX) + float(selectedTileX)/float(m_numTilesX)));
    texCoordAni.y = (float(float(inTexCoord.y/m_numTilesY) + float(selectedTileY)/float(m_numTilesY)));
}