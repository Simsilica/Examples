package example.es;

import com.simsilica.es.EntityComponent;
import java.util.HashSet;

/**
 * Represents a time-to-live for an entity.
 *
 * @author Asser Fahrenholz
 */
public class Delay implements EntityComponent {

    public final static String SET = "set";
    public final static String REMOVE = "remove";
    
    
    private long start;
    private long delta;
    private HashSet<EntityComponent> delayedComponents;
    private String type;
    
    public Delay(long deltaMillis, HashSet<EntityComponent> delayedComponents, String type) {
        this.start = System.nanoTime();
        this.delta = deltaMillis * 1000000;
        this.delayedComponents = delayedComponents;
        this.type = type;
    }

    public double getPercent() {
        long time = System.nanoTime();
        return (double) (time - start) / delta;
    }

    public HashSet<EntityComponent> getDelayedComponents() {
        return delayedComponents;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Delay[" + (delta / 1000000.0) + " ms]";
    }
}
