package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;


/**
 *  For visible game objects, this is the type of object.
 *
 *  @author    Paul Speed
 */
public class PhysicsMassType implements EntityComponent {
    
    private int type;
 
    protected PhysicsMassType() {
    }
    
    public PhysicsMassType( int type ) {
        this.type = type;
    }
    
    public static PhysicsMassType create( String typeName, EntityData ed ) {
        return new PhysicsMassType(ed.getStrings().getStringId(typeName, true));
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
