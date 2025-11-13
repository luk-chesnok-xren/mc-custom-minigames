package org.ilmiandluk.customMinigame.game.map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.Battle;
import org.ilmiandluk.customMinigame.game.Game;
import org.ilmiandluk.customMinigame.game.controller.GameController;
import org.ilmiandluk.customMinigame.game.entity.Soldier;
import org.ilmiandluk.customMinigame.game.enums.SoldierRelate;
import org.ilmiandluk.customMinigame.game.enums.SoldierState;
import org.ilmiandluk.customMinigame.game.player.GamePlayer;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.BuildStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.game.structures.builds.Mineshaft;
import org.ilmiandluk.customMinigame.game.structures.builds.Sawmill;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapSegment {
    private AbstractStructure structure;
    private final Location location;
    private Player owner;
    private final int X;
    private final int Z;

    private final List<Soldier> soldiers;

    private static final int TIME_TO_CATCH = CustomMinigame.getInstance().
            getConfigManager().
            getInt("game.timeToCatchSegment");
    private static final int TIME_TO_CATCH_BASE = CustomMinigame.getInstance().
            getConfigManager().
            getInt("game.timeToCatchBase");

    @Nullable
    private BukkitTask catchSegment;

    @Nullable
    private Battle battle;

    public MapSegment(AbstractStructure structure, Location location, Player owner, int currentX, int currentZ) {
        this.structure = structure;
        this.location = location;
        this.owner = owner;
        this.soldiers = new ArrayList<>();
        this.X = currentX;
        this.Z = currentZ;
    }

    public AbstractStructure getStructure() {
        return structure;
    }
    public List<Soldier> getSoldiers(){
        return soldiers;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Player getOwner() {
        return owner;
    }
    public List<Soldier> getFriendlySoldiers() {
        return soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.FRIEND).
                collect(Collectors.toList());
    }
    public List<Soldier> getOwnerSoldiers(){
        return soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.OWNER).
                collect(Collectors.toList());
    }
    public List<Soldier> getFreePlayerSoldiers(GamePlayer owner){
        return soldiers.stream().
                filter(soldier -> (
                        soldier.getGamePlayer() == owner
                && soldier.getSoldierState() == SoldierState.Free)).
                collect(Collectors.toList());
    }
    public List<Soldier> getEnemySoldiers(){
        return soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.ENEMY).
                collect(Collectors.toList());
    }
    public void addSoldier(Soldier soldier){
        soldier.setLinkedMapSegment(this);
        soldiers.add(soldier);
    }
    public void removeSoldier(Soldier soldier){
        soldiers.remove(soldier);
    }
    public void setStructure(AbstractStructure structure) {
        this.structure = structure;
    }
    public boolean checkFight(){
        return soldiers.stream()
                .anyMatch(s -> s.getRelate() == SoldierRelate.ENEMY)
                &&
                soldiers.stream()
                        .anyMatch(s -> s.getRelate() == SoldierRelate.FRIEND
                        || s.getRelate() == SoldierRelate.OWNER);
    }
    public boolean ownerWin(){
        return soldiers.stream()
                .noneMatch(s -> s.getRelate() == SoldierRelate.ENEMY)
                &&
                soldiers.stream()
                        .anyMatch(s -> s.getRelate() == SoldierRelate.FRIEND
                                || s.getRelate() == SoldierRelate.OWNER);
    }
    public boolean isBattle(){
        return battle != null;
    }
    public void endBattle(){
        if(!checkFight()){
            soldiers.forEach(soldier -> {
                soldier.setSoldierState(SoldierState.Free);
                soldier.moveToLocation(location);
            });
            if(battle != null){
                if (ownerWin()) {
                    battle.ownerOrFriendWin();
                }
                else{
                    battle.enemyWin();
                }
            }
            this.battle = null;
        }
    }
    public boolean isCatch(){
        return catchSegment != null && !catchSegment.isCancelled();
    }
    public void catchSegment(){
        this.catchSegment = new BukkitRunnable(){
            int time =  TIME_TO_CATCH;
            {
                if(structure instanceof Base) time = TIME_TO_CATCH_BASE;
            }
            @Override
            public void run() {
                if(soldiers.stream().
                        noneMatch(soldier -> soldier.getRelate() == SoldierRelate.ENEMY))
                    cancel();
                if(isCancelled()) return;
                if(time <= 0){
                    Game game = GameController.getGameWithPlayer(owner);
                    if(game == null) {
                        cancel();
                        return;
                    }
                    GamePlayer enemy = soldiers.stream().
                            filter(soldier -> soldier.getRelate() == SoldierRelate.ENEMY).
                            map(Soldier::getGamePlayer).
                            findFirst().
                            orElse(null);
                    if(enemy == null){
                        cancel();
                        return;
                    }
                    GamePlayer gameOwner = GameController.getGameWithPlayer(owner).getGamePlayer(owner);
                    if(gameOwner == null){
                        cancel();
                        return;
                    }
                    if(structure instanceof BuildStructure){
                        if(structure instanceof Base){
                            if(gameOwner.getPlayerStructures().getBaseCount() <= 1)
                                game.playerLoose(gameOwner, enemy);
                        }
                        else if(structure instanceof MilitarySchool){
                            MapSegment[][] allSegments = gameOwner.getGame().getMap().getSegments();
                            MapSegment nearest = gameOwner.getNearestMilitarySchoolSegment(location);
                            if(nearest != null){
                                    MapSegment segmentXz = allSegments[nearest.getX()][nearest.getZ()+1];
                                    MapSegment segmentxZ = allSegments[nearest.getX()+1][nearest.getZ()];
                                    MapSegment segmentxz = allSegments[nearest.getX()+1][nearest.getZ()+1];

                                    nearest.changeOwner(enemy);
                                    segmentXz.changeOwner(enemy);
                                    segmentxz.changeOwner(enemy);
                                    segmentxZ.changeOwner(enemy);

                                    game.removeSegmentFromPlayer(nearest, gameOwner.getPlayer());
                                    game.removeSegmentFromPlayerFromMap(segmentxz, gameOwner.getPlayer());
                                    game.removeSegmentFromPlayerFromMap(segmentxZ, gameOwner.getPlayer());
                                    game.removeSegmentFromPlayerFromMap(segmentXz, gameOwner.getPlayer());

                                    game.addSegmentToPlayer(nearest, enemy.getPlayer());
                                    game.addSegmentToPlayerFromMap(segmentxz, enemy.getPlayer());
                                    game.addSegmentToPlayerFromMap(segmentxZ, enemy.getPlayer());
                                    game.addSegmentToPlayerFromMap(segmentXz, enemy.getPlayer());

                                    gameOwner.getPlayerStructures().removeSchoolSegment(nearest);
                                    enemy.getPlayerStructures().addSchoolSegment(nearest);

                                    new BukkitRunnable() {
                                        @Override
                                        public void run(){
                                            segmentXz.interruptCatchSegment();
                                            segmentxZ.interruptCatchSegment();
                                            segmentxz.interruptCatchSegment();
                                            nearest.interruptCatchSegment();
                                        }
                                    }.runTaskLater(CustomMinigame.getInstance(), 1);
                                    cancel();
                                    return;
                            }
                        }

                    }

                    if(structure instanceof Base){
                        MapSegment[][] allSegments = gameOwner.getGame().getMap().getSegments();
                        MapSegment mainBaseSegment = gameOwner.getGame().getSegments(owner).getFirst();
                        MapSegment xzBaseSegment = allSegments[mainBaseSegment.getX()+1][mainBaseSegment.getZ()+1];
                        MapSegment xZBaseSegment = allSegments[mainBaseSegment.getX()+1][mainBaseSegment.getZ()];
                        MapSegment XzBaseSegment = allSegments[mainBaseSegment.getX()][mainBaseSegment.getZ()+1];

                        game.removeSegmentFromPlayer(mainBaseSegment, gameOwner.getPlayer());
                        game.removeSegmentFromPlayerFromMap(xzBaseSegment, gameOwner.getPlayer());
                        game.removeSegmentFromPlayerFromMap(xZBaseSegment, gameOwner.getPlayer());
                        game.removeSegmentFromPlayerFromMap(XzBaseSegment, gameOwner.getPlayer());

                        game.addSegmentToPlayer(mainBaseSegment, enemy.getPlayer());
                        game.addSegmentToPlayerFromMap(xzBaseSegment, enemy.getPlayer());
                        game.addSegmentToPlayerFromMap(xZBaseSegment, enemy.getPlayer());
                        game.addSegmentToPlayerFromMap(XzBaseSegment, enemy.getPlayer());

                        mainBaseSegment.changeOwner(enemy);
                        xzBaseSegment.changeOwner(enemy);
                        xZBaseSegment.changeOwner(enemy);
                        XzBaseSegment.changeOwner(enemy);

                        cancel();
                        return;
                    }
                    changeOwner(enemy);
                    game.removeSegmentFromPlayer(MapSegment.this, gameOwner.getPlayer());
                    game.addSegmentToPlayer(MapSegment.this, enemy.getPlayer());

                    cancel();
                }
                time--;
            }
        }.runTaskTimer(CustomMinigame.getInstance(), 0 ,20);
    }
    public void interruptCatchSegment(){
        if(catchSegment != null){
            catchSegment.cancel();
        }
    }
    public void createBattle(){
        GamePlayer owner = soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.OWNER).
                map(Soldier::getGamePlayer).
                findFirst().
                orElse(null);
        GamePlayer friend = soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.FRIEND).
                map(Soldier::getGamePlayer).
                findFirst().
                orElse(null);
        GamePlayer enemy = soldiers.stream().
                filter(soldier -> soldier.getRelate() == SoldierRelate.ENEMY).
                map(Soldier::getGamePlayer).
                findFirst().
                orElse(null);

        interruptCatchSegment();
        this.battle = new Battle(this, owner, friend, enemy);
    }
    public int getX() {
        return X;
    }
    public int getZ() {
        return Z;
    }
    public void setOwner(Player player) {
        this.owner = player;
    }
    public void changeOwner(GamePlayer newOwner){
        List<Soldier> newOwnerSoldiers = soldiers.stream().
                filter(soldier -> (soldier.getRelate() == SoldierRelate.ENEMY) && (soldier.getGamePlayer() == newOwner)).
                toList();
        List<Soldier> otherSoldiers = soldiers.stream().filter(soldier -> !newOwnerSoldiers.contains(soldier)).toList();

        newOwnerSoldiers.forEach(soldier -> soldier.setRelate(SoldierRelate.OWNER));
        otherSoldiers.forEach(other -> other.setRelate(SoldierRelate.ENEMY));
        setOwner(newOwner.getPlayer());
    }
}
