package example.es;

import com.simsilica.es.EntityData;

public class MobTypes {

    
    public static final String MOB1 = "mob1";

    public static MobType mob1(EntityData ed) {
        return MobType.create(MOB1, ed);
    }
}
