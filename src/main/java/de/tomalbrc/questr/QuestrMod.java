package de.tomalbrc.questr;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import de.tomalbrc.questr.api.requirement.RequirementTypes;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarCommand;
import de.tomalbrc.questr.impl.navigationbar.NavigationBarManager;
import de.tomalbrc.questr.impl.requirement.BreakBlockRequirementType;
import de.tomalbrc.questr.impl.requirement.KillRequirementType;
import de.tomalbrc.questr.impl.util.Config;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sidebars.api.ScrollableSidebar;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.lines.SidebarLine;
import eu.pb4.sidebars.impl.SidebarHolder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.minecraft.commands.Commands.literal;

public class QuestrMod implements ModInitializer {
    private static ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    public static final Logger LOGGER = LogUtils.getLogger();
    public static MinecraftServer SERVER;
    public static NavigationBarManager NAVIGATION = new NavigationBarManager();
    public static Config config = new Config();

    public Map<ServerGamePacketListener, Sidebar> BARS = new Reference2ObjectOpenHashMap<>();

    private void addBuiltinTypes() {
        RequirementTypes.register(new KillRequirementType());
        RequirementTypes.register(new BreakBlockRequirementType());
        //RequirementTypes.register("cobblemon:defeat_pokemon", new CobblemonDefeatMatcher());
    }

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.addModAssets("questr");
        PolymerResourcePackUtils.markAsRequired();

        addBuiltinTypes();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            SERVER = minecraftServer;
            loadConfig();
        });

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((minecraftServer, closeableResourceManager) -> {
            loadConfig();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            NavigationBarCommand.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register(((serverGamePacketListener, packetSender, minecraftServer) -> {
            Sidebar sidebar = BARS.computeIfAbsent(serverGamePacketListener, x -> new Sidebar(Component.literal(config.messages.sidebarTitle), Sidebar.Priority.LOW));
            sidebar.setUpdateRate(100);
            sidebar.setLine(1, Component.literal("test"));

            sidebar.addPlayer(serverGamePacketListener);
            sidebar.show();
        }));
        ServerPlayConnectionEvents.DISCONNECT.register(((serverGamePacketListener, minecraftServer) -> {
            BARS.remove(serverGamePacketListener).removePlayer(serverGamePacketListener);
        }));


        ServerPlayConnectionEvents.JOIN.register((serverGamePacketListener, packetSender, server) -> {
            NAVIGATION.playerJoined(serverGamePacketListener.player, server);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((serverGamePacketListener,packetSender) -> NAVIGATION.playerLeft(serverGamePacketListener.player, serverGamePacketListener.player.getServer()));
        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            NAVIGATION.tick(server);

            List<ServerPlayer> list = new ArrayList<>(server.getPlayerList().getPlayers());
            CompletableFuture.runAsync(() -> list.forEach(ServerPlayer::tickQuests), EXECUTOR);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) -> {
            //FilamentCommand.register(dispatcher)
        });


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("test").executes(QuestrMod::test)
            );
            dispatcher.register(
                    literal("test2").executes(QuestrMod::test2)
            );
            dispatcher.register(
                    literal("test3").executes(QuestrMod::test3)
            );
            dispatcher.register(
                    literal("test4").executes(QuestrMod::test4)
            );
        });
    }

    private void loadCategories() {

    }

    private void loadQuests() {

    }

    private void loadConfig() {
        loadCategories();
        loadQuests();
    }








    private static int test(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            Sidebar sidebar = new ScrollableSidebar(Sidebar.Priority.MEDIUM, 10);
            boolean bool = Math.random() > 0.5;
            System.out.println(bool);
            sidebar.setTitle(Component.literal("Test Sidebar").setStyle(Style.EMPTY.withColor(bool ? ChatFormatting.GOLD : ChatFormatting.AQUA)));

            int value = (int) (30 + Math.random() * 4);
            System.out.println(value);
            for (int x = 0; x < value; x++) {
                sidebar.setLine(x, Component.literal("Hello World! " + (int) (Math.random() * 1000)).setStyle(randomStyle()), new StyledFormat(randomStyle()));
            }

            sidebar.show();
            sidebar.addPlayer(player);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Style randomStyle() {
        return Style.EMPTY
                .withColor(TextColor.fromRgb((int) (Math.random() * 0xFFFFFF)))
                .withBold(Math.random() > 0.5)
                .withItalic(Math.random() > 0.5)
                .withUnderlined(Math.random() > 0.5)
                .withStrikethrough(Math.random() > 0.5)
                .withObfuscated(Math.random() > 0.5)
                .withFont(Math.random() > 0.6 ? ResourceLocation.withDefaultNamespace("default") : Math.random() > 0.3 ? ResourceLocation.withDefaultNamespace("uniform") : ResourceLocation.withDefaultNamespace("alt"));
    }

    private static int test2(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            boolean bool = Math.random() > 0.5;
            System.out.println(bool);
            Sidebar sidebar = new Sidebar(Sidebar.Priority.HIGH);
            sidebar.setTitle(Component.literal("Should Override").setStyle(Style.EMPTY.withColor(bool ? ChatFormatting.GOLD : ChatFormatting.AQUA)));

            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < 40; x++) {
                builder.append("Hello World! ");
            }

            sidebar.setDefaultNumberFormat(BlankFormat.INSTANCE);

            sidebar.setLine(0, Component.literal(builder.toString()).setStyle(randomStyle()));

            sidebar.addPlayer(player);
            sidebar.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test3(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            Sidebar sidebar = new Sidebar(Sidebar.Priority.LOW);
            boolean bool = Math.random() > 0.5;
            System.out.println(bool);
            sidebar.setTitle(Component.literal("LOW").setStyle(Style.EMPTY.withColor(bool ? ChatFormatting.GOLD : ChatFormatting.AQUA)));

            sidebar.setLine(0, Component.literal("Hello World! " + (int) (Math.random() * 1000)).setStyle(randomStyle()));
            int speed = (int) (Math.random() * 20);
            sidebar.setUpdateRate(speed);

            sidebar.addLines(SidebarLine.create(2, Component.literal("" + speed), new FixedFormat(Component.literal("<- Speed"))));

            sidebar.addLines(SidebarLine.create(2, (p) -> {
                System.out.println(p.tickCount);
                return Component.literal("" + p.tickCount);
            }));

            sidebar.addPlayer(player);
            sidebar.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test4(CommandContext<CommandSourceStack> objectCommandContext) {
        try {
            ServerPlayer player = objectCommandContext.getSource().getPlayer();

            ((SidebarHolder) player.connection).sidebarApi$clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
