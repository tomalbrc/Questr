package de.tomalbrc.questr.impl.sidebar;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.FontUtil;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.api.task.Task;
import de.tomalbrc.questr.impl.storage.ProgressList;
import de.tomalbrc.questr.impl.util.SmallCapsConverter;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.lines.SidebarLine;
import eu.pb4.sidebars.api.lines.SimpleSidebarLine;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidebarManager {
    private final Map<ServerGamePacketListenerImpl, Sidebar> playerSidebars = new ConcurrentHashMap<>();

    public void add(ServerGamePacketListenerImpl player) {
        Sidebar sidebar = new Sidebar(TextUtil.parse(String.format("<font:%s>%s</font>", FontUtil.FONT.id(), SmallCapsConverter.toSmallCaps("objectives"))), Sidebar.Priority.LOW);
        sidebar.setUpdateRate(10);
        sidebar.addPlayer(player);
        sidebar.show();
        playerSidebars.put(player, sidebar);
    }

    public void setVisible(ServerGamePacketListenerImpl player, boolean vis) {
        Sidebar bar = this.playerSidebars.get(player);
        if (bar == null)
            return;

        if (vis) bar.show();
        else bar.hide();
    }

    public void remove(ServerGamePacketListenerImpl player) {
        var removed = this.playerSidebars.remove(player);
        if (removed != null) {
            removed.removePlayer(player);
            removed.hide();
        }
    }

    public void playerJoined(ServerGamePacketListenerImpl player) {
        try {
            this.add(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playerLeft(ServerGamePacketListenerImpl player, MinecraftServer server) {
        remove(player);
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 2 == 0) { // every .1 secs
            for (Map.Entry<ServerGamePacketListenerImpl, Sidebar> entry : this.playerSidebars.entrySet()) {
                var player = entry.getKey();
                var sidebar = entry.getValue();

                List<SidebarLine> lines = new ArrayList<>();
                for (QuestProgress activeQuest : ProgressList.getProgress(player.player.getUUID())) {
                    if (!activeQuest.isActive()) {
                        //continue;
                    }

                    var title = activeQuest.quest().title;
                    lines.add(new SimpleSidebarLine(
                            0,
                            ComponentAligner.defaultFont(TextUtil.parse("<white>•</white> " + title)),
                            BlankFormat.INSTANCE
                    ));

                    var tasks = activeQuest.quest().tasks;
                    for (Task task : tasks) {
                        if (task.showProgress()) lines.add(new SimpleSidebarLine(
                                0,
                                ComponentAligner.defaultFont(TextUtil.parse(String.format("  <font:%s>%s</font> %s <gray>%d/%d</gray>", QuestrMod.ICON_FONT.id(), (activeQuest.isCompleted(task) ? "\uE10D" : "\uE10C"), task.description(), activeQuest.getProgress(task), task.target()))),
                                BlankFormat.INSTANCE
                        ));
                    }

                    lines.add(new SimpleSidebarLine(
                            0,
                            Component.empty(),
                            BlankFormat.INSTANCE
                    ));
                }

                sidebar.replaceLines(lines.toArray(new SidebarLine[0]));
            }
        }
    }
}
