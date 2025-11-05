package org.ilmiandluk.customMinigame.game.enums;


import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

public enum GameWoolColors {
    RED_WOOL("§c", BlockTypes.RED_WOOL),
    ORANGE_WOOL("§6",BlockTypes.ORANGE_WOOL),
    YELLOW_WOOL("§e",BlockTypes.YELLOW_WOOL),
    LIME_WOOL("§a",BlockTypes.LIME_WOOL),
    GREEN_WOOL("§2",BlockTypes.GREEN_WOOL),
    CYAN_WOOL("§3",BlockTypes.CYAN_WOOL),
    BLUE_WOOL("§9",BlockTypes.BLUE_WOOL),
    PURPLE_WOOL("§5",BlockTypes.PURPLE_WOOL),
    PINK_WOOL("§d",BlockTypes.PINK_WOOL);
    private final String colorString;
    private final BlockType blockType;
    GameWoolColors(String colorString, BlockType blockType) {
        this.colorString = colorString;
        this.blockType = blockType;
    }
    public String getColorString() {
        return colorString;
    }
    public BlockType getBlockType() {
        return blockType;
    }
}
