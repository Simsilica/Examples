package example.es;


import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import java.awt.Color;


/**
 *  For visible game objects, this is the type of object.
 *
 *  @author    Paul Speed
 */
public class ViewType implements EntityComponent {
    
    //Translates into the texture
    private int type;
    //Translates into the color overlay
    private Color color;
 
    protected ViewType() {
    }
    
    public ViewType( int type ) {
        this.type = type;
    }
    
    public static ViewType create( String typeName, EntityData ed ) {
        return new ViewType(ed.getStrings().getStringId(typeName, true));
    }
    
    public int getType() {
        return type;
    }
    
    public String getTypeName( EntityData ed ) {
        return ed.getStrings().getString(type);                 
    }
 
    @Override   
    public String toString() {
        return getClass().getSimpleName() + "[type=" + type + "]";
    }     
}
