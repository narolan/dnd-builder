package com.dnd.builder.core.model;

/**
 * Represents an active condition affecting the character.
 */
public class ActiveCondition {

    private String id;
    private String name;
    private String description;
    private int remainingRounds;  // -1 = indefinite (until removed)
    private String source;        // What caused the condition (optional)

    public ActiveCondition() {}

    public ActiveCondition(String name) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.remainingRounds = -1;
        this.description = getDefaultDescription(name);
    }

    public ActiveCondition(String name, int rounds) {
        this(name);
        this.remainingRounds = rounds;
    }

    public ActiveCondition withSource(String source) {
        this.source = source;
        return this;
    }

    public ActiveCondition withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Decrements the remaining rounds. Returns true if condition should be removed.
     */
    public boolean tick() {
        if (remainingRounds > 0) {
            remainingRounds--;
            return remainingRounds == 0;
        }
        return false;
    }

    private String getDefaultDescription(String conditionName) {
        return switch (conditionName.toLowerCase()) {
            case "blinded" -> "Can't see. Auto-fail sight checks. Attacks have disadvantage, attacks against have advantage.";
            case "charmed" -> "Can't attack charmer. Charmer has advantage on social checks.";
            case "deafened" -> "Can't hear. Auto-fail hearing checks.";
            case "frightened" -> "Disadvantage on ability checks and attacks while source is in sight. Can't willingly move closer.";
            case "grappled" -> "Speed becomes 0. Ends if grappler is incapacitated or forced apart.";
            case "incapacitated" -> "Can't take actions or reactions.";
            case "invisible" -> "Impossible to see without special sense. Attacks have advantage, attacks against have disadvantage.";
            case "paralyzed" -> "Incapacitated, can't move or speak. Auto-fail STR/DEX saves. Attacks have advantage, hits within 5ft are crits.";
            case "petrified" -> "Transformed to stone. Weight x10. Incapacitated, unaware. Resistance to all damage. Immune to poison/disease.";
            case "poisoned" -> "Disadvantage on attack rolls and ability checks.";
            case "prone" -> "Can only crawl. Disadvantage on attacks. Melee attacks have advantage, ranged have disadvantage.";
            case "restrained" -> "Speed 0. Attacks have disadvantage. Attacks against have advantage. Disadvantage on DEX saves.";
            case "stunned" -> "Incapacitated, can't move, speak falteringly. Auto-fail STR/DEX saves. Attacks have advantage.";
            case "unconscious" -> "Incapacitated, can't move or speak, unaware. Drop held items, fall prone. Auto-fail STR/DEX saves. Attacks have advantage, hits within 5ft are crits.";
            case "exhaustion" -> "Cumulative levels: 1=Disadv ability checks, 2=Speed halved, 3=Disadv attacks/saves, 4=HP max halved, 5=Speed 0, 6=Death.";
            default -> "";
        };
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getRemainingRounds() { return remainingRounds; }
    public void setRemainingRounds(int remainingRounds) { this.remainingRounds = remainingRounds; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
