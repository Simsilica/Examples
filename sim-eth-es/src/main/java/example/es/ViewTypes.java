package example.es;

import com.simsilica.es.EntityData;

/**
 * Factory methods for the common object types. Because we run the string names
 * through the EntityData's string index we can't just have normal constants.
 *
 * @author Paul Speed
 */
public class ViewTypes {

    public static final String SHIP_SHARK = "ship_shark";
    public static final String SHIP_WARBIRD = "ship_warbird";
    public static final String SHIP_JAVELIN = "ship_javelin";
    public static final String SHIP_SPIDER = "ship_spider";
    public static final String SHIP_LEVI = "ship_leviathan";
    public static final String SHIP_TERRIER = "ship_terrier";
    public static final String SHIP_WEASEL = "ship_weasel";
    public static final String SHIP_LANCASTER = "ship_lancaster";
    public static final String GRAV_SPHERE = "gravSphere";
    public static final String THRUST = "thrust";
    public static final String EXPLOSION = "explosion";
    public static final String BULLET = "bullet";
    public static final String BOMB = "bomb";
    public static final String BOUNTY = "bounty";
    public static final String ARENA = "arena";
    public static final String MAPTILE = "maptile";
    public static final String EXPLOSION2 = "explosion2";
    public static final String OVER1 = "over1";
    public static final String OVER2 = "over2";
    public static final String OVER5 = "over5";
    public static final String WORMHOLE = "wormhole";
    public static final String WARP = "warp";
    public static final String REPEL = "repel";
    public static final String FLAG_OURS = "flag_ours";
    public static final String FLAG_THEIRS = "flag_theirs";
    
    //Will probably need more mob visuals along the way
    public static final String MOB = "mob";
    //Will probably need more tower visuals along the way
    public static final String TOWER = "tower";
    //Will probably need more tower visuals along the way
    public static final String BASE = "base";

    public static ViewType base(EntityData ed) {
        return ViewType.create(BASE, ed);
    }
    public static ViewType tower(EntityData ed) {
        return ViewType.create(TOWER, ed);
    }
    
    public static ViewType mob(EntityData ed) {
        return ViewType.create(MOB, ed);
    }
    /*
    The different ships
     */
    public static ViewType ship_warbird(EntityData ed) {
        return ViewType.create(SHIP_WARBIRD, ed);
    }

    public static ViewType ship_javelin(EntityData ed) {
        return ViewType.create(SHIP_JAVELIN, ed);
    }

    public static ViewType ship_spider(EntityData ed) {
        return ViewType.create(SHIP_SPIDER, ed);
    }

    public static ViewType ship_levi(EntityData ed) {
        return ViewType.create(SHIP_LEVI, ed);
    }

    public static ViewType ship_terrier(EntityData ed) {
        return ViewType.create(SHIP_TERRIER, ed);
    }

    public static ViewType ship_weasel(EntityData ed) {
        return ViewType.create(SHIP_WEASEL, ed);
    }

    public static ViewType ship_lanc(EntityData ed) {
        return ViewType.create(SHIP_LANCASTER, ed);
    }

    public static ViewType ship_shark(EntityData ed) {
        return ViewType.create(SHIP_SHARK, ed);
    }

    public static ViewType gravSphereType(EntityData ed) {
        return ViewType.create(GRAV_SPHERE, ed);
    }

    public static ViewType thrust(EntityData ed) {
        return ViewType.create(THRUST, ed);
    }

    public static ViewType explosion(EntityData ed) {
        return ViewType.create(EXPLOSION, ed);
    }

    public static ViewType bullet(EntityData ed) {
        return ViewType.create(BULLET, ed);
    }

    public static ViewType bounty(EntityData ed) {
        return ViewType.create(BOUNTY, ed);
    }

    public static ViewType bomb(EntityData ed) {
        return ViewType.create(BOMB, ed);
    }

    public static ViewType arena(EntityData ed) {
        return ViewType.create(ARENA, ed);
    }

    public static ViewType mapTile(EntityData ed) {
        return ViewType.create(MAPTILE, ed);
    }

    public static ViewType explosion2(EntityData ed) {
        return ViewType.create(EXPLOSION2, ed);
    }

    public static ViewType wormhole(EntityData ed) {
        return ViewType.create(WORMHOLE, ed);
    }

    public static ViewType over5(EntityData ed) {
        return ViewType.create(OVER5, ed);
    }

    public static ViewType over1(EntityData ed) {
        return ViewType.create(OVER1, ed);
    }

    public static ViewType warp(EntityData ed) {
        return ViewType.create(WARP, ed);
    }

    public static ViewType repel(EntityData ed) {
        return ViewType.create(REPEL, ed);
    }

    public static ViewType over2(EntityData ed) {
        return ViewType.create(OVER2, ed);
    }
    
    public static ViewType flag_ours(EntityData ed){
        return ViewType.create(FLAG_OURS, ed);
    }
    
    public static ViewType flag_theirs(EntityData ed){
        return ViewType.create(FLAG_THEIRS, ed);
    }
}
