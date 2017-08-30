package example.es;

import com.dongbat.walkable.FloatArray;
import com.simsilica.es.EntityComponent;

/**
 *
 * @author Asser
 */
public class MobPath implements EntityComponent {

    private FloatArray path;

    public MobPath(FloatArray path) {
        this.path = path;
    }

    public FloatArray getPath() {
        return path;
    }

}
