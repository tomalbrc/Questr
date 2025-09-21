package de.tomalbrc.questr.impl.navigationbar;

import com.google.gson.*;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Json;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class NavigationBarManager {
    private final Object2ObjectOpenHashMap<UUID, NavigationBarList> playerNavigationBars = new Object2ObjectOpenHashMap<>();

    public void add(MinecraftServer server, UUID playerUUID, String message, BlockPos targetPos) {
        NavigationBarList list = this.playerNavigationBars.computeIfAbsent(playerUUID, (object) -> new NavigationBarList());
        NavigationBar newData = new NavigationBar(server, playerUUID, message, targetPos);
        newData.setVisible(true);
        list.add(newData);
    }

    public void setVisible(UUID playerUUID, int index) {
        NavigationBarList list = this.playerNavigationBars.get(playerUUID);
        if (list.isEmpty() || index >= list.size() || index < 0)
            return;

        list.get(index).setVisible(true);
    }

    public void removeOldestNavigationBar(UUID playerUUID) {
        NavigationBar rm = null;
        NavigationBarList list = this.playerNavigationBars.get(playerUUID);
        if (!list.isEmpty()) {
            list.getFirst().setActive(false);
            rm = list.getFirst();
        }

        if (rm != null) {
            list.remove(rm);
        }
    }

    public void remove(UUID playerUUID, int index) {
        NavigationBarList list = this.playerNavigationBars.get(playerUUID);
        if (list.isEmpty() || index >= list.size()  || index < 0)
            return;

        list.get(index).setActive(false);
        list.remove(index);
    }

    public Component list(UUID playerUUID) {
        MutableComponent out = MutableComponent.create(PlainTextContents.EMPTY);
        NavigationBarList get = this.playerNavigationBars.get(playerUUID);

        out.append(get.size() + " Navigation Bars (quests):");
        for (int i = 0; i < get.size(); i++) {
            out.append("\n");
            NavigationBar data = get.get(i);
            out.append(" ["+ i + "] " + data.getTargetPos().toShortString() + ": " + data.getMessage());
        }

        return out;
    }

    public void playerJoined(ServerPlayer player, MinecraftServer server) {
        this.load(server, player.getUUID());
    }
    public void playerLeft(ServerPlayer player, MinecraftServer server) {
        this.save(server, player.getUUID());
        this.playerNavigationBars.remove(player.getUUID());
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 2 == 0) { // every .1 secs
            this.playerNavigationBars.forEach((player,list) -> {
                if (list.hasVisible()) {
                    for (NavigationBar data: list) {
                        data.update();

                        if (server.getTickCount() % 40 == 0) {
                            data.trail();
                        }
                    }
                }
            });
        }
    }

    public void save(MinecraftServer server, UUID playerUUID) {
        this.saveListForPlayer(server, playerUUID, this.playerNavigationBars.get(playerUUID));
        this.playerNavigationBars.get(playerUUID).dirty = false;
    }

    public void load(MinecraftServer server, UUID playerUUID) {
        this.loadListForPlayer(server, playerUUID, this.playerNavigationBars.computeIfAbsent(playerUUID, x -> new NavigationBarList()));
    }

    private void loadListForPlayer(MinecraftServer server, UUID playerUUID, NavigationBarList navigationBarList) {
        QuestrMod.LOGGER.debug("Reading player navigation data for player with UUID {}", playerUUID);
        File dir = new File(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile(), "/navigation/");

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File file = new File(dir, playerUUID.toString() + ".json");
        if (!file.exists()) {
            return;
        }

        JsonArray listArray;
        try (FileReader reader = new FileReader(file)) {
            listArray = Json.GSON.fromJson(reader, JsonArray.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        navigationBarList.clear();

        for (JsonElement element : listArray) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                String message = obj.has("message") ? obj.get("message").getAsString() : null;
                JsonArray posArray = obj.getAsJsonArray("pos");

                if (posArray != null && posArray.size() == 3 && obj.has("uuid") && obj.has("visible")) {
                    int posX = posArray.get(0).getAsInt();
                    int posY = posArray.get(1).getAsInt();
                    int posZ = posArray.get(2).getAsInt();
                    BlockPos targetPos = new BlockPos(posX, posY, posZ);

                    String uuidString = obj.get("uuid").getAsString();
                    UUID uuid = UUID.fromString(uuidString);

                    boolean visible = obj.get("visible").getAsBoolean();

                    NavigationBar data = new NavigationBar(server, uuid, message, targetPos);
                    data.setVisible(visible);
                    navigationBarList.add(data);
                }
            }
        }
    }

    private void saveListForPlayer(MinecraftServer server, UUID playerUUID, NavigationBarList list) {
        if (!list.dirty)
            return;

        try {
            QuestrMod.LOGGER.debug("Saving player navigation data for player with UUID {}", playerUUID);
            File dir = new File(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile(), "/navigation/");

            if (!dir.exists() && !dir.mkdirs())
                return;

            File file = new File(dir, playerUUID.toString() + ".json");
            if (!file.exists() && !file.createNewFile())
                return;

            JsonArray listArray = getArray(list);
            FileWriter writer = new FileWriter(file);
            Json.GSON.toJson(listArray, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static @NotNull JsonArray getArray(NavigationBarList list) {
        JsonArray listArray = new JsonArray();
        for (NavigationBar data : list) {
            JsonObject obj = new JsonObject();
            obj.addProperty("message", data.getMessage());

            JsonArray posArray = new JsonArray();
            posArray.add(data.getTargetPos().getX());
            posArray.add(data.getTargetPos().getY());
            posArray.add(data.getTargetPos().getZ());
            obj.add("pos", posArray);

            obj.addProperty("uuid", data.getPlayerUUID().toString());

            obj.addProperty("visible", data.isVisible());
            listArray.add(obj);
        }
        return listArray;
    }
}