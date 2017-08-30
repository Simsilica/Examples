package example.es;

import com.simsilica.es.EntityComponent;

/**
 * Represents a time-to-live for an entity.
 *
 * @author Asser Fahrenholz
 */
public class Decay implements EntityComponent {

    private long start;
    private long delta;

    public Decay(){
        this.start = System.nanoTime();
        this.delta = 1000000 * 10;
    }
    
    public Decay(long deltaMillis) {
        this.start = System.nanoTime();
        this.delta = deltaMillis * 1000000;
    }

    public double getPercent() {
        long time = System.nanoTime();
        return (double) (time - start) / delta;
    }

    @Override
    public String toString() {
        return "Decay[" + (delta / 1000000.0) + " ms]";
    }
}
