package com.dnd.builder.model;

import java.util.List;

public class FlexibleBonus {
    private int count, amount;
    private List<String> excludedStats;
    private String description;

    public FlexibleBonus() {}
    public FlexibleBonus(int count, int amount, List<String> excludedStats, String description) {
        this.count = count; this.amount = amount;
        this.excludedStats = excludedStats; this.description = description;
    }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public List<String> getExcludedStats() { return excludedStats; }
    public void setExcludedStats(List<String> e) { this.excludedStats = e; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
}
