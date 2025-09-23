package de.tomalbrc.questr.api.quest;

public record QuestLifecycle(
        boolean automaticSelection,
        boolean repeatable,
        int cooldownSeconds) {
}