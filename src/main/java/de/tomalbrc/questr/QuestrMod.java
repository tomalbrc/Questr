package de.tomalbrc.questr;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import de.tomalbrc.dialogutils.util.FontUtil;
import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestCategories;
import de.tomalbrc.questr.api.quest.QuestCategory;
import de.tomalbrc.questr.api.quest.Quests;
import de.tomalbrc.questr.api.task.TaskTypes;
import de.tomalbrc.questr.impl.command.NavigationBarCommand;
import de.tomalbrc.questr.impl.json.Config;
import de.tomalbrc.questr.impl.json.Loader;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarManager;
import de.tomalbrc.questr.impl.sidebar.SidebarManager;
import de.tomalbrc.questr.impl.task.*;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundDefinition;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundEntry;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestrMod implements ModInitializer {
    public static final String MODID = "questr";
    public static ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("questr-worker-%d").build());

    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer SERVER;
    public static NavigationBarManager NAVIGATION = new NavigationBarManager();
    public static SidebarManager SIDEBAR = new SidebarManager();
    public static Config config = new Config();

    public static final ResourceLocation ICON_FONT = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "mini-icons");
    public static final ResourceLocation ICON_FONT_NAV = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "mini-icons-nav");
    public static final ResourceLocation NAV_FONT = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "nav");
    public static final ResourceLocation NAV_FONT2 = ResourceLocation.fromNamespaceAndPath("questr", "nav2");
    public static final ResourceLocation BOXY_FONT = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "boxy");
    public static final ResourceLocation BOXY_NAV_FONT = ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, "boxy_nav");

    private void addBuiltinTypes() {
        TaskTypes.register(new KillTaskType());
        TaskTypes.register(new BreakBlockTaskType());
        TaskTypes.register(new SendChatMessageTaskType());
        TaskTypes.register(new ShearEntityTaskType());
        TaskTypes.register(new OnTickTaskType());

        //RequirementTypes.register(new DefeatCobblemonRequirementType());
    }

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets("questr");
        PolymerResourcePackUtils.markAsRequired();

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(x -> {
            var voices = List.of("male", "female");
            var soundBuilder = SoundsAsset.builder();
            for (String voice : voices) {
                for (int i = 0; i < 4; i++) {
                    for (char c = 'a'; c <= 'z'; c++) {
                        soundBuilder.add(voice + ".voice_" + i + "." + c, SoundEntry.builder().sound(SoundDefinition.builder(ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + c))));
                    }

                    soundBuilder.add(voice + ".voice_" + i + ".deska", SoundEntry.builder().sound(SoundDefinition.builder(ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "deska"))));
                    soundBuilder.add(voice + ".voice_" + i + ".gwah", SoundEntry.builder().sound(SoundDefinition.builder(ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "gwah"))));
                    soundBuilder.add(voice + ".voice_" + i + ".ok", SoundEntry.builder().sound(SoundDefinition.builder(ResourceLocation.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "ok"))));
                }
            }
            x.addData(AssetPaths.soundsAsset(QuestrMod.MODID), soundBuilder.build().toBytes());

            FontUtil.registerDefaultFonts(x);
            FontUtil.loadFont(x, ICON_FONT);
            FontUtil.loadFont(x, ICON_FONT_NAV);
            FontUtil.loadFont(x, NAV_FONT);
            FontUtil.loadFont(x, NAV_FONT2);
            FontUtil.loadFont(x, BOXY_FONT);
            FontUtil.loadFont(x, BOXY_NAV_FONT);
        });
        addBuiltinTypes();
        addEvents();
        addConfigEvents();

        addCommands();
    }

    private void addCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            NavigationBarCommand.register(dispatcher);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            //FilamentCommand.register(dispatcher)
        });
    }

    private void addConfigEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            SERVER = minecraftServer;
            loadConfig();
        });

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager) -> {
            loadConfig();
        });
    }

    private void addEvents() {
        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, server) -> {
            for (Quest quest : Quests.all()) {
                if (quest.lifecycle.automaticSelection() && !serverGamePacketListener.player.hasQuest(quest))
                    serverGamePacketListener.player.startQuest(quest);
            }

            NAVIGATION.playerJoined(serverGamePacketListener.player, server);
            SIDEBAR.playerJoined(serverGamePacketListener.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, packetSender) -> {
            NAVIGATION.playerLeft(serverGamePacketListener.player, serverGamePacketListener.player.getServer());
            SIDEBAR.playerLeft(serverGamePacketListener.player, serverGamePacketListener.player.getServer());
        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            List<ServerPlayer> list = new ArrayList<>(server.getPlayerList().getPlayers());
            EXECUTOR.execute(() -> {
                try {
                    list.forEach(ServerPlayer::tickQuests);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                NAVIGATION.tick(server);
                SIDEBAR.tick(server);
            });
        });
    }

    private void loadCategories() {
        try {
            Loader.loadAll(FabricLoader.getInstance().getConfigDir().resolve("questr/categories"), QuestCategory.class).forEach(QuestCategories::register);
        } catch (IOException e) {
            QuestrMod.LOGGER.error("Could not load categories");
        }
    }

    private void loadQuests() {
        try {
            Loader.loadAll(FabricLoader.getInstance().getConfigDir().resolve("questr/quests"), Quest.class).forEach(Quests::register);
        } catch (IOException e) {
            QuestrMod.LOGGER.error("Could not load quests");
        }
    }

    private void loadConfig() {
        loadCategories();
        loadQuests();
    }
}
