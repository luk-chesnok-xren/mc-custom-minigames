package org.ilmiandluk.customMinigame.game.entity.pathfinder;

import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.EntityInsentient;

public class PathFinderMoveToLocation extends PathfinderGoal {
    private final EntityInsentient entity;
    private final double x, y, z;
    private final double distance;
    private int timeToRecalcPath;
    private boolean reached = false;

    public PathFinderMoveToLocation(EntityInsentient entity, double x, double y, double z, double distance) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.distance = distance;
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

        //createPath или findPathTo(double x, double y, double z, int distance)
        this.entity.S().a(this.x, this.y, this.z, this.distance);

        // Проверяем расстояние до цели
        double distanceSquared = entity.h(x, y, z);

        // Если ближе чем 1.73 блок (√3). ИМХО, регистрация странная
        // поэтому так много. Лучше пусть он не дойдет на 1-2 блока, но
        // pathfinder выполнится, чем он остановиться и не дойдет.
        if (distanceSquared < 2) {
            reached = true;
            // Какая-то особенная логика

        } else if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            this.entity.S().a(this.x, this.y, this.z, this.distance);
        }
    }
}