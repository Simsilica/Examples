package example.es;

import com.simsilica.es.EntityData;

public class TowerTypes {

    
    public static final String TOWER1 = "tower1";

    public static TowerType tower1(EntityData ed) {
        return TowerType.create(TOWER1, ed);
    }
}
