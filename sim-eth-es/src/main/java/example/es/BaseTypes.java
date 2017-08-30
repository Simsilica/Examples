package example.es;

import com.simsilica.es.EntityData;

public class BaseTypes {

    
    public static final String BASE1 = "base1";

    public static BaseType base1(EntityData ed) {
        return BaseType.create(BASE1, ed);
    }
}
