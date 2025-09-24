package de.tomalbrc.questr.api.quest;

public record QuestLifecycle(
        boolean automaticCompletion,
        boolean automaticSelection,
        boolean repeatable,
        int cooldownSeconds) {
}