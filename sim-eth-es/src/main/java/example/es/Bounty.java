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
public class Bounty implements EntityComponent { 
    
    private int bounty;

    public Bounty(){
    }
    
    public Bounty(int bounty) {
        this.bounty = bounty;
    }

    public int getBounty() {
        return bounty;
    }

    public void setBounty(int bounty) {
        this.bounty = bounty;
    }
}
