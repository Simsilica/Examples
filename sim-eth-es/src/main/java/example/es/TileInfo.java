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
public class TileInfo implements EntityComponent{
    
    String tileSet;
    short tileIndex;
    
    //Empty constructor for serialization
    public TileInfo(){
        
    }

    public TileInfo(String tileSet, short tileIndex) {
        this.tileSet = tileSet;
        this.tileIndex = tileIndex;
    }

    public String getTileSet() {
        return tileSet;
    }

    public short getTileIndex() {
        return tileIndex;
    }

    @Override
    public String toString() {
        return "TileInfo{" + "tileSet=" + tileSet + ", tileIndex=" + tileIndex + '}';
    }
}
