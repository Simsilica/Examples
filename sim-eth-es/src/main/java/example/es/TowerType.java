package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;

public class TowerType implements EntityComponent {
    
    private int type;
 
    protected TowerType() {
    }
    
    public TowerType( int type ) {
        this.type = type;
    }
    
    public static TowerType create( String typeName, EntityData ed ) {
        return new TowerType(ed.getStrings().getStringId(typeName, true));
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
