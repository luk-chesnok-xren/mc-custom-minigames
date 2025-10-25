package org.ilmiandluk.customMinigame.game.map;

import com.fastasyncworldedit.core.Fawe;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.ilmiandluk.customMinigame.CustomMinigame;
import org.ilmiandluk.customMinigame.game.structures.AbstractStructure;
import org.ilmiandluk.customMinigame.game.structures.builds.Base;
import org.ilmiandluk.customMinigame.game.structures.builds.MilitarySchool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class SegmentBuilder {
    private CustomMinigame plugin;
    private final static List<CompletableFuture<Boolean>> activeFutures = Collections.synchronizedList(new ArrayList<>());
    private final static HashMap<String, Clipboard> clipboardMap = new HashMap<>();

    public SegmentBuilder(CustomMinigame plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Boolean> buildSegment(MapSegment mapSegment) {
        AbstractStructure structure = mapSegment.getStructure();
        Location loc = mapSegment.getLocation().clone();
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            String path = plugin.getDataFolder().getPath() + File.separator + plugin.getConfigManager().getStructurePath(structure);
            if(!clipboardMap.containsKey(path)) {
                File file = new File(path);

                if (!file.exists()) {
                    plugin.getLogger().log(Level.SEVERE, plugin.getMessagesManager().getString("scheme.notfound") + file.getAbsolutePath());
                    return false;
                }

                ClipboardFormat format = ClipboardFormats.findByFile(file);
                if (format == null) {
                    plugin.getLogger().log(Level.SEVERE, plugin.getMessagesManager().getString("scheme.unformatted") + file.getAbsolutePath());
                    return false;
                }

                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    Clipboard clipboard = reader.read();
                    clipboardMap.put(path, clipboard);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            Clipboard clipboard = clipboardMap.get(path);

            EditSession editSession = Fawe.instance().getWorldEdit().newEditSessionBuilder()
                    .world(BukkitAdapter.adapt(loc.getWorld()))
                    .fastMode(true)
                    .allowedRegionsEverywhere()
                    .build();
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
                    .ignoreAirBlocks(false)
                    .build();

            Operations.complete(operation);

            editSession.close();

            return true;
        });

        activeFutures.add(future);
        future.whenComplete((result, throwable) -> {
            activeFutures.remove(future);
        });

        return future;
    }

    public CompletableFuture<Void> waitForAllCompletions() {
        synchronized (activeFutures) {
            if (activeFutures.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            return CompletableFuture.allOf(
                    activeFutures.toArray(new CompletableFuture[0])
            );
        }
    }

    public static boolean haveTasks() {
        synchronized (activeFutures) {
            return !activeFutures.isEmpty();
        }
    }
}

