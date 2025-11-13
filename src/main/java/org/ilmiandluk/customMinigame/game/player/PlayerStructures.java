package org.ilmiandluk.customMinigame.game.player;

import org.ilmiandluk.customMinigame.game.map.MapSegment;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.game.structures.builds.Mineshaft;
import org.ilmiandluk.customMinigame.game.structures.builds.Sawmill;

import java.util.ArrayList;
import java.util.List;

public class PlayerStructures {
    private int mineshaftCount = 0;
    private int sawmillCount = 0;
    private int militarySchoolCount = 0;
    private int baseCount = 1;
    private final List<MapSegment> schoolMapSegments = new ArrayList<>();

    public void addSchoolSegment(MapSegment schoolMapSegment){
        this.schoolMapSegments.add(schoolMapSegment);
    }
    public void removeSchoolSegment(MapSegment schoolMapSegment){
        this.schoolMapSegments.remove(schoolMapSegment);
    }
    public List<MapSegment> getSchoolMapSegments(){
        return this.schoolMapSegments;
    }

    public void addStructure(AbstractStructure structure){
        if(structure instanceof Sawmill) sawmillCount++;
        if(structure instanceof MilitarySchool) militarySchoolCount++;
        if(structure instanceof Mineshaft) mineshaftCount++;
        if(structure instanceof Base) baseCount++;
        System.out.println("Sawmill count: " + sawmillCount + " militarySchoolCount: " + militarySchoolCount + " baseCount: " + baseCount);
    }
    public void removeStructure(AbstractStructure structure){
        if(structure instanceof Sawmill) sawmillCount--;
        if(structure instanceof MilitarySchool) militarySchoolCount--;
        if(structure instanceof Mineshaft) mineshaftCount--;
        if(structure instanceof Base) baseCount--;
        System.out.println("Sawmill count: " + sawmillCount + " militarySchoolCount: " + militarySchoolCount + " baseCount: " + baseCount);
    }
    public int getBaseCount(){
        return this.baseCount;
    }

    public int getMineshaftCount() {
        return mineshaftCount;
    }

    public void setMineshaftCount(int mineshaftCount) {
        this.mineshaftCount = mineshaftCount;
    }

    public int getSawmillCount() {
        return sawmillCount;
    }

    public void setSawmillCount(int sawmillCount) {
        this.sawmillCount = sawmillCount;
    }

    public int getMilitarySchoolCount() {
        return militarySchoolCount;
    }

    public void setMilitarySchoolCount(int militarySchoolCount) {
        this.militarySchoolCount = militarySchoolCount;
    }
}
