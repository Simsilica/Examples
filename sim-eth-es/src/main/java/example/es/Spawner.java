/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.simsilica.es.EntityComponent;

/**
 *
 * @author Asser
 */
public class Spawner implements EntityComponent{
    
    public enum SpawnType { Players, Bounties }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public Spawner(int maxCount, SpawnType type) {
        this.maxCount = maxCount;
        this.type = type;
    }
    
    
    private int maxCount;
    private SpawnType type;
    
    public Spawner(){
        
    }

    public SpawnType getType() {
        return type;
    }

    public void setType(SpawnType type) {
        this.type = type;
    }

    public Spawner(SpawnType type) {
        this.type = type;
    }
}
