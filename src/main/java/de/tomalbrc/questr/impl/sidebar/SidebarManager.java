package de.tomalbrc.questr.impl.sidebar;

import de.tomalbrc.dialogutils.util.TextAligner;
import de.tomalbrc.questr.api.quest.QuestProgress;
import de.tomalbrc.questr.impl.util.TextUtil;
import eu.pb4.sidebars.api.Sidebar;
import eu.pb4.sidebars.api.lines.SidebarLine;
import eu.pb4.sidebars.api.lines.SimpleSidebarLine;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidebarManager {
    private final Map<ServerPlayer, Sidebar> playerSidebars = new ConcurrentHashMap<>();

    public void add(ServerPlayer player) {
        Sidebar sidebar = new Sidebar(TextUtil.format("<font:questr:boxy>   QUESTS   </font>"), Sidebar.Priority.LOW);
        sidebar.setUpdateRate(10);
        sidebar.addPlayer(player);
        sidebar.show();
        playerSidebars.put(player, sidebar);
    }

    public void setVisible(ServerPlayer player, boolean vis) {
        Sidebar bar = this.playerSidebars.get(player);
        if (bar == null)
            return;

        if (vis) bar.show();
        else bar.hide();
    }

    public void remove(ServerPlayer player) {
        var removed = this.playerSidebars.remove(player);
        if (removed != null) {
            removed.removePlayer(player);
            removed.hide();
        }
    }

    public void playerJoined(ServerPlayer player) {
        try {
            this.add(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playerLeft(ServerPlayer player, MinecraftServer server) {
        remove(player);
    }

    public void tick(MinecraftServer server) {
        if (server.getTickCount() % 2 == 0) { // every .1 secs
            for (Map.Entry<ServerPlayer, Sidebar> entry : this.playerSidebars.entrySet()) {
                var player = entry.getKey();
                var sidebar = entry.getValue();

                List<SidebarLine> lines = new ArrayList<>();
                for (QuestProgress activeQuest : player.getActiveQuests()) {
                    if (!activeQuest.isActive()) {
                        continue;
                    }

                    var title = activeQuest.quest().title;
                    // Quest title with vertical bar
                    lines.add(new SimpleSidebarLine(
                            0,
                            TextUtil.format(TextAligner.wrapDefaultFont("<dark_gray>│</dark_gray> " + title)),
                            BlankFormat.INSTANCE
                    ));

                    // Quest title with vertical bar
                    lines.add(new SimpleSidebarLine(
                            0,
                            TextUtil.format(TextAligner.wrapDefaultFont("<dark_gray>│</dark_gray> <gray>ᴏʙᴊᴇᴄᴛɪᴠᴇs</gray>")),
                            BlankFormat.INSTANCE
                    ));

//                    var desc = activeQuest.quest().description;
//                    // Quest title with vertical bar
//                    if (desc != null) lines.add(new SimpleSidebarLine(
//                            0,
//                            TextUtil.format(TextAligner.wrapDefaultFont("<dark_gray>│</dark_gray> " + desc)),
//                            BlankFormat.INSTANCE
//                    ));

                    var tasks = activeQuest.quest().tasks;
                    for (int i = 0; i < tasks.size(); i++) {
                        var task = tasks.get(i);
                        boolean last = (i == tasks.size() - 1);

                        String branch = last ? "└─" : "├─";
                        lines.add(new SimpleSidebarLine(
                                0,
                                TextUtil.format(TextAligner.wrapDefaultFont("<dark_gray>├─</dark_gray> " + task.getDescription())),
                                BlankFormat.INSTANCE
                        ));

                        lines.add(new SimpleSidebarLine(
                                0,
                                TextUtil.format(TextAligner.wrapDefaultFont(String.format("<dark_gray>%s─</dark_gray> <gray>Progress:</gray> %d / %d", branch, activeQuest.getProgress(task), task.getTarget()))),
                                BlankFormat.INSTANCE
                        ));
                    }

                    lines.add(new SimpleSidebarLine(
                            0,
                            TextUtil.format(" "),
                            BlankFormat.INSTANCE
                    ));
                }

                if (lines.size() > 1) lines.removeLast();
                sidebar.replaceLines(lines.toArray(new SidebarLine[0]));
            }
        }
    }
}
