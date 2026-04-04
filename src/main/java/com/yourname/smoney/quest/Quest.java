package com.yourname.smoney.quest;

public class Quest {

    private final String id;
    private final String description;
    private final String actionType;
    private final QuestType type;
    private final String targetType;
    private final int target;
    private final double reward;

    public Quest(String id, String description, String actionType, QuestType type, String targetType, int target, double reward) {
        this.id = id;
        this.description = description != null ? description : id;
        this.actionType = actionType != null ? actionType.toUpperCase() : "KILL";
        this.type = type;
        this.targetType = targetType != null ? targetType.toUpperCase() : "NONE";
        this.target = target;
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getActionType() {
        return actionType;
    }

    public QuestType getType() {
        return type;
    }

    public String getTargetType() {
        return targetType;
    }

    public int getTarget() {
        return target;
    }

    public double getReward() {
        return reward;
    }
}