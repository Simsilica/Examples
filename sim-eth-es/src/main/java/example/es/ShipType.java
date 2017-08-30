package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;


/**
 *  For players, this is the type of ship they currently are playing
 */
public class ShipType implements EntityComponent {
    
    private int type;
 
    protected ShipType() {
    }
    
    public ShipType( int type ) {
        this.type = type;
    }
    
    public static ShipType create( String typeName, EntityData ed ) {
        return new ShipType(ed.getStrings().getStringId(typeName, true));
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
