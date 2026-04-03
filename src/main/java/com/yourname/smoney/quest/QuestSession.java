package com.yourname.smoney.quest;

import java.util.*;

public class QuestSession {

    private final Map<UUID, Set<String>> cache = new HashMap<>();

    public Set<String> get(UUID uuid) {
        return cache.getOrDefault(uuid, new HashSet<>());
    }

    public void set(UUID uuid, Set<String> quests) {
        cache.put(uuid, quests);
    }

    public void clear(UUID uuid) {
        cache.remove(uuid);
    }
}