package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;

public class MobType implements EntityComponent {
    
    private int type;
 
    protected MobType() {
    }
    
    public MobType( int type ) {
        this.type = type;
    }
    
    public static MobType create( String typeName, EntityData ed ) {
        return new MobType(ed.getStrings().getStringId(typeName, true));
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
