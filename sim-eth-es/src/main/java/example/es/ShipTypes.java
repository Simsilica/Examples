/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package example.es;

import com.simsilica.es.EntityData;

/**
 *
 * @author Asser
 */
public class ShipTypes {

    public static final String WARBIRD = "warbird";
    public static final String JAVELIN = "javeling";
    public static final String SPIDER = "spider";
    public static final String LEVIATHAN = "leviathan";
    public static final String TERRIER = "terrier";
    public static final String WEASEL = "weasel";
    public static final String LANCASTER = "lancaster";
    public static final String SHARK = "shark";

    public static ShipType warbird(EntityData ed) {
        return ShipType.create(WARBIRD, ed);
    }

    public static ShipType javelin(EntityData ed) {
        return ShipType.create(JAVELIN, ed);
    }

    public static ShipType spider(EntityData ed) {
        return ShipType.create(SPIDER, ed);
    }

    public static ShipType leviathan(EntityData ed) {
        return ShipType.create(LEVIATHAN, ed);
    }

    public static ShipType terrier(EntityData ed) {
        return ShipType.create(TERRIER, ed);
    }

    public static ShipType weasel(EntityData ed) {
        return ShipType.create(WEASEL, ed);
    }

    public static ShipType lancaster(EntityData ed) {
        return ShipType.create(LANCASTER, ed);
    }

    public static ShipType shark(EntityData ed) {
        return ShipType.create(SHARK, ed);
    }
}
