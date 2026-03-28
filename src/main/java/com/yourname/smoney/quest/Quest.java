package com.yourname.smoney.quest;

public class Quest {

    private final String id;
    private final QuestType type;
    private final int target;
    private final double reward;

    public Quest(String id, QuestType type, int target, double reward) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public QuestType getType() {
        return type;
    }

    public int getTarget() {
        return target;
    }

    public double getReward() {
        return reward;
    }
}