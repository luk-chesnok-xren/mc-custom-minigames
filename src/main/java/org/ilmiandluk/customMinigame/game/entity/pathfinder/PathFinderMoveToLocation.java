package org.ilmiandluk.customMinigame.game.entity.pathfinder;

import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.EntityInsentient;
import org.bukkit.Location;
import org.bukkit.World;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.enums.SoldierState;
import org.ilmiandluk.customMinigame.util.ConfigurationManager;

public class PathFinderMoveToLocation extends PathfinderGoal {
    private static final ConfigurationManager configLoader = CustomMinigame.getInstance().getConfigManager();
    private static final ConfigurationManager messageLoader = CustomMinigame.getInstance().getMessagesManager();

    private final EntityInsentient entity;
    private final double x, y, z;
    private final World world;
    private final double speed;
    private int timeToRecalcPath;
    private boolean reached = false;

    public PathFinderMoveToLocation(EntityInsentient entity, double x, double y, double z, double speed, World world) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.speed = speed;
        this.world = world;
    }
    public void cancel(){
        reached = true;
    }
    public Location getTargetLocation(){
        return new Location(world, x, y, z);
    }
    @Override
    public boolean c() // shouldContinue() или canContinueToUse() - Mojang
    {
        return !reached;
    }

    @Override
    public boolean b() // canStart() можно ли начать двигаться?
    {
        return !reached;
    }

    @Override
    public void d() // start
    {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void a() // tick()
    {
        if (reached) return;

        //createPath или findPathTo(double x, double y, double z, double speed)
        this.entity.S().a(this.x, this.y, this.z, this.speed);

        // Проверяем расстояние до цели
        double distanceSquared = entity.h(x, y, z);

        // Если ближе чем 1.73 блок (√5). ИМХО, регистрация странная
        // поэтому так много. Лучше пусть он не дойдет на 1-2 блока, но
        // pathfinder выполнится, чем он остановиться и не дойдет.
        if (distanceSquared < 5) {
            reached = true;
            if(entity instanceof Soldier soldier){
                soldier.setSoldierState(SoldierState.Free);
                if(soldier.getRelate() == SoldierRelate.ENEMY) {
                    if(!soldier.getLinkedMapSegment().checkFight()) {
                        if(!soldier.getLinkedMapSegment().isCatch()) {
                            soldier.getGamePlayer().getPlayer().sendMessage(messageLoader.getString("game.catchSegment", configLoader.getInt("game.timeToCatchSegment")));
                            soldier.getLinkedMapSegment().catchSegment();
                        }
                    }
                }
            }

        } else if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.entity.S().a(this.x, this.y, this.z, this.speed);
        }
    }
}