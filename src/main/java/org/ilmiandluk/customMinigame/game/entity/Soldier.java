package org.ilmiandluk.customMinigame.game.entity;

import net.minecraft.core.BlockPosition;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalMeleeAttack;
import net.minecraft.world.entity.ai.goal.PathfinderGoalZombieAttack;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntityZombie;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.controller.ChunkController;
import org.ilmiandluk.customMinigame.game.entity.pathfinder.PathFinderMoveToLocation;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.enums.SoldierState;
import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;

import javax.annotation.Nullable;

public class Soldier extends EntityZombie {
    private Entity entity;
    private final Location spawnLocation;
    private GamePlayer gamePlayer;
    private final ChunkController chunkController;
    private PathFinderMoveToLocation currentGoal;

    private MapSegment linkedMapSegment;
    private SoldierRelate relate;

    private SoldierState soldierState;

    private static final double attackDamage = 15;
    private static final double attackSpeed = 2.5;
    private static final double moveSpeed = 0.3;
    private static final double maxHealth = 300;

    private int lastChunkX;
    private int lastChunkZ;
    private boolean isInitialize = true;

    @Nullable
    public Entity getEntity(){
        return entity;
    }

    public void changeOwner(GamePlayer gamePlayer){
        this.gamePlayer = gamePlayer;
    }

    public Soldier(Location location, GamePlayer gamePlayer, ChunkController chunkController, MapSegment mapSegment) {
        super(EntityTypes.bQ, ((CraftWorld) location.getWorld()).getHandle());
        this.o(location.getX(), location.getY(), location.getZ()); //setPosRaw
        this.spawnLocation = location;
        this.gamePlayer = gamePlayer;
        this.chunkController = chunkController;
        this.soldierState = SoldierState.Free;
        this.linkedMapSegment = mapSegment;
        // ci - targetSelector
        // ch - goalSelector
        // S() - getNavigation

        this.h(GenericAttributes.w).a(moveSpeed); //get and change speed
        this.h(GenericAttributes.t).a(maxHealth); //get and change max health
        this.h(GenericAttributes.c).a(attackDamage); // attack damage
        this.gK().a(GenericAttributes.e, attackSpeed); // attack speed
        this.x((float) maxHealth); // set health
    }
    public void doHeal(){
        this.x((float) maxHealth);
        entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 5);
    }
    public void doTeleport(){
        if(currentGoal != null)
            entity.teleport(currentGoal.getTargetLocation());
    }
    public void doStrength(){
        PotionEffect effect = new PotionEffect(PotionEffectType.STRENGTH, 120*20, 2);
        ((LivingEntity) entity).addPotionEffect(effect);
    }
    public void doSpeed(){
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, 120*20, 2);
        ((LivingEntity) entity).addPotionEffect(effect);
    }
    public void doRocket(){
        entity.getWorld().spawnParticle(Particle.EXPLOSION, entity.getLocation(), 1);
        c(((CraftWorld) entity.getWorld()).getHandle());
    }
    public void doNuclear(){
        entity.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, entity.getLocation(), 3);
        c(((CraftWorld) entity.getWorld()).getHandle());
    }
    public boolean spawnMob(){
        try {
            ((CraftWorld) spawnLocation.getWorld()).getHandle().addFreshEntity(this, CreatureSpawnEvent.SpawnReason.COMMAND);
            // Создаем сущность не из NMS. Чтобы затем не работать с обфусцированными методами
            Zombie zombie = (Zombie) this.getBukkitEntity(); // cast Entity to Bukkit Zombie?

            // Чтобы потом фиксировать, что умер именно наш зомби
            zombie.setMetadata("mobType", new FixedMetadataValue(CustomMinigame.getInstance(), "soldier"));

            this.entity = zombie;
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public void moveToLocation(Location targetLocation){
        // Удаляем старую цель
        if(currentGoal != null){
            currentGoal.cancel();
        }
        currentGoal = new PathFinderMoveToLocation(
                this,
                targetLocation.getX(),
                targetLocation.getY(),
                targetLocation.getZ(),
                1,
                entity.getWorld()
        );
        // Добавляем новую цель - движение к координатам
        this.ch.a(1, currentGoal);
        this.soldierState = SoldierState.Walking;
    }
    public SoldierState getSoldierState(){
        return soldierState;
    }
    public void setSoldierState(SoldierState soldierState){
        this.soldierState = soldierState;
    }
    public SoldierRelate getRelate(){
        return relate;
    }
    public Soldier setRelate(SoldierRelate relate){
        this.relate = relate;
        return this;
    }
    public GamePlayer getGamePlayer(){
        return gamePlayer;
    }
    public MapSegment getLinkedMapSegment(){
        return linkedMapSegment;
    }
    public void setLinkedMapSegment(MapSegment linkedMapSegment){
        this.linkedMapSegment = linkedMapSegment;
    }

    //tryAttack(ServerWorld arg0, Entity arg1)
    @Override
    public boolean c(WorldServer worldserver, net.minecraft.world.entity.Entity entity) {
        if(entity instanceof Soldier soldier) {
            if (linkedMapSegment.getSoldiers().contains(soldier)) {
                setSoldierState(SoldierState.Fighting);
                if(!linkedMapSegment.isBattle()){
                    linkedMapSegment.createBattle();
                }
            }
        }
        return super.c(worldserver, entity);
    }
    @Override
    public void g(){
        super.g();
        int cx = (int) (this.dC()+0.5) >> 4;
        int cz = (int) (this.dI()+0.5) >> 4;
        if (isInitialize) {
            chunkController.ensureChunkLoaded(cx, cz);
            lastChunkX = cx;
            lastChunkZ = cz;
            isInitialize = false;
        } else if (cx != lastChunkX || cz != lastChunkZ) {
            chunkController.ensureChunkLoaded(cx, cz);
            chunkController.releaseChunk(lastChunkX, lastChunkZ);
            lastChunkX = cx;
            lastChunkZ = cz;
        }
    }
    /// Mojang removeWhenFarAway делаем, чтобы не деспавнился никогда
    @Override
    public boolean h(double d) {
        return false;
    }

    /// Mojang isSunSensitive() должен гореть днем?
    @Override
    protected boolean af_(){
        return false;
    }
    /// Mojang registerGoals() in EntityZombie
    @Override
    protected void H() {
        this.m();
    }

    /// Mojang addBehaviourGoals() in EntityZombie
    @Override
    protected void m() {
        // ci - targetSelector
        // ch - goalSelector
        // S() - getNavigation

        // Очистка целей существующего EntityZombie
        // Predicate<Goal> (i -> true), т.е. очищаем любую Goal
        // После этого наш зомби становиться овощем и ему нужно добавить новое поведение
        this.ci.a(i -> true);
        this.ch.a(i -> true);
        // Добавляем новую цель - движение к координатам

        // a(int, Pathfinder) - > Mojang addGoal(int arg0, Goal arg1)
        this.ch.a(2, new PathfinderGoalMeleeAttack(this, (double)1.0F, false));
        // Этот pathfinder будет работать, если цели для атаки еще нет.
        // Будем искать ближайшего Zombie, которого можно атаковать.
        // Нельзя атаковать, если зомби в нашей команде...
        this.ci.a(2, new PathfinderGoalNearestAttackableTarget<>(
                this,
                Soldier.class,
                8,
                true,
                false,
                (target, worldServer) -> {
                    if (!(target instanceof Soldier soldierTarget)) return false;

                    MapSegment segment = this.getLinkedMapSegment();
                    if (!soldierTarget.getLinkedMapSegment().equals(segment)) return false;

                    boolean thisIsEnemy = segment.getEnemySoldiers().contains(this);
                    boolean targetIsEnemy = segment.getEnemySoldiers().contains(soldierTarget);
                    boolean thisIsOwner = segment.getOwnerSoldiers().contains(this);
                    boolean targetIsOwner = segment.getOwnerSoldiers().contains(soldierTarget);
                    boolean thisIsFriendly = segment.getFriendlySoldiers().contains(this);
                    boolean targetIsFriendly = segment.getFriendlySoldiers().contains(soldierTarget);

                    return (thisIsEnemy && (targetIsFriendly || targetIsOwner))
                            || (targetIsEnemy && (thisIsFriendly || thisIsOwner));
                }
        ));
    }
    // die()
    @Override
    public void a(DamageSource damagesource){
        linkedMapSegment.removeSoldier(this);

        // Забираем одного солдата, отправляем об этом сообщение внутри.
        gamePlayer.getPlayerResources().removeSoldier(this);

        // Нужно проверить, закончилось ли сражение.
        linkedMapSegment.endBattle();
        super.a(damagesource);
    }

    ///Hit sound
    @Override
    protected SoundEffect e(DamageSource damagesource) {
        return SoundEffects.wY;
    }

    ///Death sound to break
    @Override
    protected SoundEffect f_() {
        return SoundEffects.wW;
    }

    ///Ambient sound to step
    @Override
    protected SoundEffect p() {
        return SoundEffects.xa;
    }
    @Override
    protected boolean eu(){
        return false;
    }
}
