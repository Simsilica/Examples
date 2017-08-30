package example.es;

import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;

public class BaseType implements EntityComponent {
    
    private int type;
 
    protected BaseType() {
    }
    
    public BaseType( int type ) {
        this.type = type;
    }
    
    public static BaseType create( String typeName, EntityData ed ) {
        return new BaseType(ed.getStrings().getStringId(typeName, true));
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
