varying vec2 texCoordAni;
varying float completed;
uniform sampler2D m_AniTexMap;

void main(){
    vec4 AniTex = texture2D(m_AniTexMap, vec2(texCoordAni));

    if(completed > 1 || AniTex.rgb == (0.0, 0.0, 0.0)) 
        discard;

    gl_FragColor = AniTex;
}