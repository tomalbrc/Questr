package de.tomalbrc.questr.impl;

import de.tomalbrc.dialogutils.util.ComponentAligner;
import de.tomalbrc.dialogutils.util.TextUtil;
import de.tomalbrc.questr.QuestrMod;
import de.tomalbrc.questr.impl.util.Animalese;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MiniDialog {
    private final AtomicInteger progress = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private final ServerPlayer player;
    private final List<Component> lines;
    private final List<Integer> lengths;
    private final int maxWidth;
    private final int totalChars;
    private final long startTime;
    private final String gender;
    private final String voice;
    private final Runnable onFinish;

    private final Component postDialogSuffix = Component.literal("Next").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withFont(QuestrMod.LINE5_FONT).withShadowColor(0xFF_00_00_00));

    public MiniDialog(ServerPlayer player,
                      List<Component> lines,
                      int maxWidth,
                      String gender,
                      String voice,
                      Runnable onFinish) {
        this.player = player;
        this.lines = lines;
        this.lengths = this.lines.stream().map(x -> {
            int count = 0;
            for (Component component : x.toFlatList()) {
                count += component.visit(compTxt -> Optional.of(compTxt.length())).orElseThrow();
            }
            return count;
        }).toList();
        this.maxWidth = maxWidth;
        this.startTime = player.level().getGameTime() % 2;

        int chars = 0;
        for (Component line : lines) {
            for (Component component : line.toFlatList()) {
                chars += component.visit((txt) -> Optional.of(txt.length())).orElseThrow();
            }
        }
        this.totalChars = chars;
        this.gender = gender;
        this.voice = voice;
        this.onFinish = onFinish;
    }

    public void tick(MinecraftServer server) {
        if (closed.get()) {
            return;
        }

        if ((player.level().getGameTime() + startTime) % 2 == 1) {
            return;
        }

        int globalCharIndex = Math.min(progress.get(), totalChars);
        int charsInPreviousLines = 0;

        var carriageReturn = ComponentAligner.spacer(-maxWidth);

        MutableComponent text = Component.literal("\uE100").withStyle(Style.EMPTY.withFont(QuestrMod.DIALOG_FONT).withColor(ChatFormatting.WHITE).withShadowColor(0));
        text = ComponentAligner.align(text, TextUtil.Alignment.CENTER, maxWidth).withStyle(Style.EMPTY.withFont(QuestrMod.DIALOG_FONT).withColor(ChatFormatting.WHITE).withShadowColor(0));
        text.append(carriageReturn);

        int shiftBy = 25;
        text.append(ComponentAligner.spacer(shiftBy));

        for (int i = 0; i < lines.size(); i++) {
            Component lineComponent = lines.get(i);
            int lineLength = lengths.get(i);

            // Check if the typing cursor is past this entire line
            if (globalCharIndex >= charsInPreviousLines + lineLength) {
                // This line is fully displayed. Append it directly.
                MutableComponent fullLine = lineComponent.copy();
                // Apply the overall line font and color, preserving existing styles where possible
                fullLine.setStyle(fullLine.getStyle().withFont(QuestrMod.LINE_FONTS.get(i)).withColor(ChatFormatting.BLACK));
                text.append(ComponentAligner.align(fullLine, TextUtil.Alignment.LEFT, maxWidth));
            } else {
                // The typing cursor is somewhere within this line. We need to build it part-by-part.
                MutableComponent currentLineText = Component.empty();
                int charsWithinLineSoFar = 0;

                // Iterate through the styled parts of the line
                for (Component part : lineComponent.toFlatList()) {
                    String partText = part.getString();
                    int partLength = partText.length();

                    // Check if the typing cursor is completely past this part
                    if (globalCharIndex >= charsInPreviousLines + charsWithinLineSoFar + partLength) {
                        currentLineText.append(part);
                        charsWithinLineSoFar += partLength;
                    } else {
                        // The cursor is inside this part. Find the exact character.
                        int charIndexInPart = globalCharIndex - (charsInPreviousLines + charsWithinLineSoFar);
                        String visiblePartText = partText.substring(0, charIndexInPart);

                        // Append the visible portion of this part, preserving its original style
                        if (!visiblePartText.isEmpty()) {
                            currentLineText.append(Component.literal(visiblePartText).withStyle(part.getStyle()));
                        }

                        // Play the sound for the character being "typed"
                        if (globalCharIndex < totalChars) {
                            char characterToPlay = partText.charAt(charIndexInPart);
                            Animalese.playLetter(
                                    player,
                                    characterToPlay,
                                    0.6f,
                                    player.getRandom().nextIntBetweenInclusive(60, 75) / 100f,
                                    gender,
                                    voice
                            );
                        }
                        // We've found the typing point, so we can stop processing more parts for this line
                        break;
                    }
                }

                // Apply the overall line font and color to the constructed line
                currentLineText.setStyle(currentLineText.getStyle().withFont(QuestrMod.LINE_FONTS.get(i)).withColor(ChatFormatting.BLACK));

                text.append(ComponentAligner.align(currentLineText, TextUtil.Alignment.LEFT, maxWidth));

                // Increment progress and break from the main loop over all lines
                progress.incrementAndGet();
                break;
            }

            charsInPreviousLines += lineLength;

            if (i != lines.size() - 1) {
                text.append(carriageReturn);
            }
        }

        text.append(ComponentAligner.spacer(-shiftBy));

        // if dialog is fully written, append suffix and shift back to preserve alignment
        if (progress.get() >= totalChars) {
            text.append(postDialogSuffix).append(ComponentAligner.spacer(-ComponentAligner.getWidth(postDialogSuffix)));
        }

        if (!player.isRemoved()) {
            player.connection.send(new ClientboundSetActionBarTextPacket(text));
        }
    }

    public boolean textFinished() {
        return progress.get() >= totalChars;
    }

    public void close() {
        player.connection.send(new ClientboundSetActionBarTextPacket(Component.empty()));

        if (closed.compareAndSet(false, true) && onFinish != null) {
            onFinish.run();
        }
    }

    public void skip() {
        progress.set(totalChars);
    }

    public boolean isClosed() {
        return closed.get();
    }
}
