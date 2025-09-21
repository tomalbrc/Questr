package de.tomalbrc.questr.api.quest;

public record QuestLifecycle(
    boolean repeatable,
    int cooldownSeconds,
    int maxCompletionsPerPeriod
) {
}