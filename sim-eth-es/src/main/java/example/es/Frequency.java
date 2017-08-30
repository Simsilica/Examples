package example.es;

import com.simsilica.es.EntityComponent;

/**
 *
 * @author Asser
 */
public class Frequency implements EntityComponent {

    private int freq;

    public Frequency() {

    }

    public Frequency(int freq) {
        this.freq = freq;
    }

    public int getFreq() {
        return freq;
    }
}
