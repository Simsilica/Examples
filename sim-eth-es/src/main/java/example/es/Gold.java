/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.simsilica.es.EntityComponent;

/**
 *
 * @author ss
 */
public class Gold implements EntityComponent{
 
    private int gold;
    
    
    public Gold() {
    }

    public Gold(int gold) {
        this.gold = gold;
                
    }

    public int getGold() {
        return this.gold;
    }
    
}
