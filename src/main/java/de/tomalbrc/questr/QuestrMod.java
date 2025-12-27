package de.tomalbrc.questr;

import com.mojang.logging.LogUtils;
import de.tomalbrc.dialogutils.util.FontUtil;
import de.tomalbrc.questr.api.quest.Quest;
import de.tomalbrc.questr.api.quest.QuestCategories;
import de.tomalbrc.questr.api.quest.QuestCategory;
import de.tomalbrc.questr.api.quest.Quests;
import de.tomalbrc.questr.api.task.TaskTypes;
import de.tomalbrc.questr.impl.DialogManager;
import de.tomalbrc.questr.impl.command.NavigationBarCommand;
import de.tomalbrc.questr.impl.json.Config;
import de.tomalbrc.questr.impl.json.Loader;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarManager;
import de.tomalbrc.questr.impl.sidebar.SidebarManager;
import de.tomalbrc.questr.impl.storage.ProgressList;
import de.tomalbrc.questr.impl.task.*;
import de.tomalbrc.shaderfx.api.ShaderEffects;
import de.tomalbrc.shaderfx.api.ShaderUtil;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PackResource;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuestrMod implements ModInitializer {
    public static final String MODID = "questr";
    public static ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer SERVER;
    public static NavigationBarManager NAVIGATION = new NavigationBarManager();
    public static SidebarManager SIDEBAR = new SidebarManager();
    public static DialogManager DIALOG = new DialogManager();
    public static Config config = new Config();

    public static final FontDescription.Resource ICON_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "mini-icons"));
    public static final FontDescription.Resource ICON_FONT_NAV = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "mini-icons-nav"));
    public static final FontDescription.Resource ICON_FONT_NAV2 = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "mini-icons-nav2"));
    public static final FontDescription.Resource NAV_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "nav"));
    public static final FontDescription.Resource NAV_FONT2 = new FontDescription.Resource(Identifier.fromNamespaceAndPath("questr", "nav2"));
    public static final FontDescription.Resource BOXY_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "boxy"));
    public static final FontDescription.Resource BOXY_NAV_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "boxy_nav"));
    public static final FontDescription.Resource DIALOG_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "dialog"));
    public static final FontDescription.Resource LINE1_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line1"));
    public static final FontDescription.Resource LINE2_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line2"));
    public static final FontDescription.Resource LINE3_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line3"));
    public static final FontDescription.Resource LINE4_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line4"));
    public static final FontDescription.Resource LINE4_JIGGLE_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line4_jiggle"));
    public static final FontDescription.Resource LINE5_FONT = new FontDescription.Resource(Identifier.fromNamespaceAndPath(QuestrMod.MODID, "line5"));
    public static final List<FontDescription.Resource> LINE_FONTS = List.of(LINE1_FONT, LINE2_FONT, LINE3_FONT, LINE4_FONT, LINE5_FONT, LINE4_JIGGLE_FONT);

    private void addBuiltinTaskTypes() {
        TaskTypes.register(new KillTaskType());
        TaskTypes.register(new BreakBlockTaskType());
        TaskTypes.register(new SendChatMessageTaskType());
        TaskTypes.register(new ShearEntityTaskType());
        TaskTypes.register(new OnTickTaskType());
    }

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets("questr");
        PolymerResourcePackUtils.markAsRequired();

        ShaderUtil.enableAssets();
        ShaderUtil.enableAnimojiConversion();

        ServerLifecycleEvents.SERVER_STOPPING.register(x -> EXECUTOR.shutdownNow());
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(builder -> builder.addResourceConverter((path, resource) -> {
            if (path.equals("assets/questr/textures/font/frame.png")) {
                try {
                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(resource.readAllBytes()));
                    ShaderUtil.tintEdges(image, ShaderEffects.APERTURE.asFullscreenColor());
                    var out = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", out);
                    return PackResource.of(out.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return resource;
        }));

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(x -> {
            var voices = List.of("male", "female");
            var soundBuilder = SoundsAsset.builder();
            for (String voice : voices) {
                for (int i = 1; i < 5; i++) {
                    for (char c = 'a'; c <= 'z'; c++) {
                        soundBuilder.add(voice + ".voice_" + i + "." + c, SoundEntry.builder().sound(SoundDefinition.builder(Identifier.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + c))));
                    }

                    soundBuilder.add(voice + ".voice_" + i + ".deska", SoundEntry.builder().sound(SoundDefinition.builder(Identifier.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "deska"))));
                    soundBuilder.add(voice + ".voice_" + i + ".gwah", SoundEntry.builder().sound(SoundDefinition.builder(Identifier.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "gwah"))));
                    soundBuilder.add(voice + ".voice_" + i + ".ok", SoundEntry.builder().sound(SoundDefinition.builder(Identifier.fromNamespaceAndPath(QuestrMod.MODID, voice + "/voice_" + i + "/" + "ok"))));
                }
            }
            x.addData(AssetPaths.soundsAsset(QuestrMod.MODID), soundBuilder.build().toBytes());

            FontUtil.registerDefaultFonts(x);
            var defFont = FontUtil.FONTS.get(FontUtil.FONT.id());
            for (var font : LINE_FONTS) {
                FontUtil.FONTS.put(font.id(), defFont);
            }

            FontUtil.loadFont(x, ICON_FONT.id());
            FontUtil.FONTS.put(ICON_FONT_NAV2.id(), FontUtil.loadFont(x, ICON_FONT_NAV.id()));
            FontUtil.FONTS.put(NAV_FONT2.id(), FontUtil.loadFont(x, NAV_FONT.id()));
            FontUtil.loadFont(x, BOXY_FONT.id());
            FontUtil.loadFont(x, BOXY_NAV_FONT.id());
            FontUtil.loadFont(x, DIALOG_FONT.id());
            FontUtil.loadFont(x, Identifier.fromNamespaceAndPath("avatar-renderer", "pixel"));
        });

        addBuiltinTaskTypes();
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
                if (quest.lifecycle.automaticSelection()) {
                    if (!ProgressList.has(serverGamePacketListener.player.getUUID(), quest.id))
                        serverGamePacketListener.startQuest(quest);
                }
            }

            NAVIGATION.playerJoined(serverGamePacketListener, server);
            SIDEBAR.playerJoined(serverGamePacketListener);

            var p1 = new ClientboundSetTitlesAnimationPacket(0, 20, 10);
            var p2 = new ClientboundSetTitleTextPacket(ShaderEffects.effectComponent(ShaderEffects.DIRECTIONAL_GRID.location(), 0x0));
            serverGamePacketListener.send(new ClientboundBundlePacket(List.of(p1, p2)));
        });

        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener, server) -> {
            NAVIGATION.playerLeft(serverGamePacketListener, server);
            SIDEBAR.playerLeft(serverGamePacketListener, server);
            DIALOG.playerLeft(serverGamePacketListener);
            ProgressList.remove(serverGamePacketListener.player.getUUID());
        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            List<ServerPlayer> list = new ArrayList<>(server.getPlayerList().getPlayers());
            EXECUTOR.execute(() -> {
                try {
                    list.forEach(x -> x.connection.tickQuests());
                    for (ServerPlayer serverPlayer : list) {
                        if (!serverPlayer.isRemoved()) {
                            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()));
                        }
                    }
                } catch (Exception e) {
                    QuestrMod.LOGGER.error("Error ticking player quests: ", e);
                }

                NAVIGATION.tick(server);
                SIDEBAR.tick(server);
                DIALOG.tick(server);
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
