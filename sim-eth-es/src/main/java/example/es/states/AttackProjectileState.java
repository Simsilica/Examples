package example.es.states;

import com.simsilica.es.Entity;
import com.simsilica.es.EntityComponent;
import com.simsilica.es.EntityData;
import com.simsilica.es.EntityId;
import com.simsilica.es.EntitySet;
import com.simsilica.mathd.Vec3d;
import com.simsilica.sim.AbstractGameSystem;
import com.simsilica.sim.SimTime;
import example.GameConstants;
import example.PhysicsConstants;
import example.es.Attack;
import example.es.ProjectileType;
import example.es.ProjectileTypes;
import example.es.BodyPosition;
import example.es.Buff;
import example.es.GravityWell;
import example.es.HealthChange;
import example.es.HitPoints;
import example.es.PhysicsVelocity;
import example.sim.SimpleBody;
import example.sim.GameEntities;
import example.sim.SimplePhysics;
import java.util.HashSet;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

/**
 * General app state that watches entities with a AttackType component and
 * HitPoints. It performs the attack if there is enough hitpoints and then
 * removes the attacktype component.
 *
 * The attacker must have a physical body (BodyPosition)
 *
 * @author Asser Fahrenholz
 */
public class AttackProjectileState extends AbstractGameSystem {

    private EntityData ed;
    private EntitySet entities;
    private EntitySet attacks;
    private Object gameSystems;
    private SimplePhysics physics;
    private SimTime time;
    private HealthState health;

    @Override
    public void update(SimTime tpf) {

        if (attacks.applyChanges()) {
            for (Entity e : attacks.getAddedEntities()) {
                //TODO: Check entities able to attack (cooldown as well as energi and get type of attack)
                Attack a = e.get(Attack.class);
                ProjectileType at = e.get(ProjectileType.class);
                this.attack(a, at);

                ed.removeEntity(e.getId()); //Attack has been processed, now remove it
            }
        }

        time = tpf;
    }

    @Override
    protected void initialize() {
        this.ed = getSystem(EntityData.class);

        // Get the physics system... it's not available yet when onInitialize() is called.
        physics = getSystem(SimplePhysics.class);
        if (physics == null) {
            throw new RuntimeException("GameSessionHostedService requires a SimplePhysics system.");
        }

        health = getSystem(HealthState.class);

        entities = ed.getEntities(BodyPosition.class, HitPoints.class); //This filters the entities that are allowed to perform attacks
        attacks = ed.getEntities(Attack.class, ProjectileType.class);
    }

    @Override
    protected void terminate() {
        // Release the entity set we grabbed previously
        entities.release();
        entities = null;

        attacks.release();
        attacks = null;
    }

    private void attack(Attack a, ProjectileType at) {
        EntityId owner = a.getOwner();

        this.attack(owner, at);
    }

    private void attack(EntityId eId, ProjectileType type) {
        Entity e = ed.getEntity(eId, HitPoints.class);
        SimTime localTime = time; //Copy incase someone else is writing to it
        HitPoints hp = e.get(HitPoints.class); //The current health of the attacker

        //Derive how much health it will take to perform the given attack
        //Perform the attack
        // Attacking body:
        SimpleBody shipBody = physics.getBody(e.getId());

        Transform shipTransform = shipBody.getTransform();

        //Bomb velocity:
        Vector2 attackVel = this.getAttackVelocity(shipTransform.getRotation(), type, shipBody.getLinearVelocity());

        //Position
        Vector2 attackPos = this.getAttackPosition(shipTransform.getRotation(), shipTransform.getTranslation());

        //Convert to Vec3d because that's what the rest of sim-eth-es uses
        Vec3d attackPosVec3d = new Vec3d(attackPos.x, attackPos.y, 0); //TODO: missing arena as z

        switch (type.getTypeName(ed)) {
            case ProjectileTypes.BOMB:
                GameEntities.createBomb(attackPosVec3d, shipBody.orientation, shipTransform.getRotation(), attackVel, GameConstants.BULLETDECAY, ed);
                break;
            case ProjectileTypes.BULLET:
                GameEntities.createBullet(attackPosVec3d, shipBody.orientation, shipTransform.getRotation(), attackVel, GameConstants.BULLETDECAY, ed);
                break;
            case ProjectileTypes.GRAVITYBOMB:
                HashSet<EntityComponent> delayedComponents = new HashSet<>();
                delayedComponents.add(new GravityWell(5, GameConstants.GRAVBOMBWORMHOLEFORCE, GravityWell.PULL));             //Suck everything in
                delayedComponents.add(new PhysicsVelocity(new Vector2(0, 0))); //Freeze the bomb
                GameEntities.createDelayedBomb(attackPosVec3d, shipBody.orientation, shipTransform.getRotation(), attackVel, GameConstants.GRAVBOMBDECAY, GameConstants.GRAVBOMBDELAY, delayedComponents, ed);
                break;
        }

        //Create a buff/healthchange component because attacking takes health
        EntityId buffEntity = ed.createEntity();
        ed.setComponents(buffEntity, new Buff(e.getId(), localTime.getTime()), new HealthChange(0)); //TODO: change in health not set
    }

    private Vector2 getAttackVelocity(double rotation, ProjectileType type, Vector2 linearVelocity) {
        Vector2 attackVel = new Vector2(0, 1);

        attackVel.rotate(rotation);

        attackVel.multiply(GameConstants.BASEPROJECTILESPEED);

        if (type.getTypeName(ed).equals(ProjectileTypes.BOMB)) {
            attackVel.multiply(GameConstants.BOMBPROJECTILESPEED);
            attackVel.add(linearVelocity); //Add ships velocity to account for direction of ship and rotation

        } else if (type.getTypeName(ed).equals(ProjectileTypes.BULLET)) {
            attackVel.multiply(GameConstants.BULLETPROJECTILESPEED);
            attackVel.add(linearVelocity); //Add ships velocity to account for direction of ship and rotation

        } else if (type.getTypeName(ed).equals(ProjectileTypes.GRAVITYBOMB)) {
            attackVel.multiply(GameConstants.GRAVBOMBPROJECTILESPEED);
            attackVel.add(linearVelocity); //Add ships velocity to account for direction of ship and rotation

        } else if (type.getTypeName(ed).equals(ProjectileTypes.MINE)) {
            attackVel.multiply(0); //A mine stands still
        }

        return attackVel;
    }

    private Vector2 getAttackPosition(double rotation, Vector2 translation) {
        Vector2 attackPos = new Vector2(0, 1);
        attackPos.rotate(rotation);
        attackPos.multiply(PhysicsConstants.PROJECTILEOFFSET);
        attackPos.add(translation.x, translation.y);
        return attackPos;
    }

}
