package org.ilmiandluk.customMinigame.game.enums;


import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.ChatColor;

public enum GameWoolColors {
    RED_WOOL("§c", BlockTypes.RED_WOOL, ChatColor.RED),
    ORANGE_WOOL("§6",BlockTypes.ORANGE_WOOL, ChatColor.GOLD),
    YELLOW_WOOL("§e",BlockTypes.YELLOW_WOOL, ChatColor.YELLOW),
    LIME_WOOL("§a",BlockTypes.LIME_WOOL, ChatColor.GREEN),
    GREEN_WOOL("§2",BlockTypes.GREEN_WOOL, ChatColor.DARK_GREEN),
    CYAN_WOOL("§3",BlockTypes.CYAN_WOOL, ChatColor.BLUE),
    BLUE_WOOL("§9",BlockTypes.BLUE_WOOL, ChatColor.DARK_BLUE),
    PURPLE_WOOL("§5",BlockTypes.PURPLE_WOOL, ChatColor.DARK_PURPLE),
    PINK_WOOL("§d",BlockTypes.PINK_WOOL, ChatColor.LIGHT_PURPLE);
    private final String colorString;
    private final BlockType blockType;
    private final ChatColor color;
    GameWoolColors(String colorString, BlockType blockType, ChatColor color) {
        this.colorString = colorString;
        this.blockType = blockType;
        this.color = color;
    }
    public String getColorString() {
        return colorString;
    }
    public BlockType getBlockType() {
        return blockType;
    }
    public ChatColor getChatColor(){
        return color;
    }
}
