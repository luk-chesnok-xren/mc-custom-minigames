package org.ilmiandluk.customMinigame.game.player;

import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;
import org.ilmiandluk.customMinigame.game.structures.builds.Mineshaft;
import org.ilmiandluk.customMinigame.game.structures.builds.Sawmill;

public class PlayerStructures {
    private int mineshaftCount = 0;
    private int sawmillCount = 0;
    private int militarySchoolCount = 0;

    public void addStructure(AbstractStructure structure){
        if(structure instanceof Sawmill) sawmillCount++;
        if(structure instanceof MilitarySchool) militarySchoolCount++;
        if(structure instanceof Mineshaft) mineshaftCount++;
    }
    public void removeStructure(AbstractStructure structure){
        if(structure instanceof Sawmill) sawmillCount--;
        if(structure instanceof MilitarySchool) militarySchoolCount--;
        if(structure instanceof Mineshaft) mineshaftCount--;
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
