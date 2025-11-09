package org.ilmiandluk.customMinigame.game.entity;

import net.minecraft.server.level.WorldServer;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.entity.ai.goal.PathfinderGoalZombieAttack;
import net.minecraft.world.entity.ai.goal.target.PathfinderGoalNearestAttackableTarget;
import net.minecraft.world.entity.animal.EntityIronGolem;
import net.minecraft.world.entity.monster.EntityZombie;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R5.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.controller.ChunkController;
import org.ilmiandluk.customMinigame.game.entity.pathfinder.PathFinderMoveToLocation;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;

import javax.annotation.Nullable;

public class Soldier extends EntityZombie {
    private Entity entity;
    private final Location spawnLocation;
    private final Location targetLocation;
    private final GamePlayer gamePlayer;
    private final ChunkController chunkController;

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

    public Soldier(Location location, Location targetLoc, GamePlayer gamePlayer, ChunkController chunkController) {
        super(EntityTypes.bQ, ((CraftWorld) location.getWorld()).getHandle());
        this.o(location.getX(), location.getY(), location.getZ()); //setPosRaw
        this.spawnLocation = location;
        this.targetLocation = targetLoc;
        this.gamePlayer = gamePlayer;
        this.chunkController = chunkController;
        // ci - targetSelector
        // ch - goalSelector
        // S() - getNavigation

        this.h(GenericAttributes.w).a(moveSpeed); //get and change speed
        this.h(GenericAttributes.t).a(maxHealth); //get and change max health
        this.h(GenericAttributes.c).a(attackDamage); // attack damage
        this.gK().a(GenericAttributes.e, attackSpeed); // attack speed
        this.x((float) maxHealth); // set health

        // Добавляем новую цель - движение к координатам
        this.ch.a(1, new PathFinderMoveToLocation(
                this,
                targetLocation.getX(),
                targetLocation.getY(),
                targetLocation.getZ(),
                1
        ));
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
    @Override
    public void g(){
        super.g();
        int cx = (int) (this.dC()+0.5) >> 4;
        int cz = (int) (this.dI()+0.5) >> 4;
        if(isInitialize){
            chunkController.loadChunk(cx, cz);
            isInitialize = false;
            lastChunkX = cx;
            lastChunkZ = cz;
        }
        else if(lastChunkX != cx && lastChunkZ != cz) {
            chunkController.loadChunk(cx, cz);
            chunkController.unloadChunk(lastChunkX, lastChunkZ);
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

        // Делаем зомби атаку зомби (когда зомби атакует цель, она может двигаться, отталкиваться
        // и т.д.). Атака зомби хранит информацию о хитбоксе самого зомби и расстоянии, на которое
        // наш зомби должен будет подойти для атаки цели.
        // Приоритет ставим на 1 (приоритеты идут от 0 до n, где, чем меньше число,
        // тем выше приоритет)
        this.ch.a(2, new PathfinderGoalZombieAttack(this, 1.0, false));

        // Этот pathfinder будет работать, если цели для атаки еще нет.
        // Будем искать ближайшего Zombie, которого можно атаковать.
        // Нельзя атаковать, если зомби в нашей команде...
        this.ci.a(2, new PathfinderGoalNearestAttackableTarget<>(
                this,
                EntityZombie.class,
                8,
                true,
                false,
                (target, worldServer) -> {
                    if (target instanceof Soldier soldierTarget) {
                        if(soldierTarget.gamePlayer != null && this.gamePlayer != null) {
                            return !soldierTarget.gamePlayer.equals(this.gamePlayer);
                        }
                    }
                    return true;
                }));
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
