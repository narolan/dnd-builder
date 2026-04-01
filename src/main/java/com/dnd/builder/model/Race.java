package com.dnd.builder.model;

import java.util.List;
import java.util.Map;

public class Race {
    private String id, name, source;
    private Map<String, Integer> fixedBonuses;
    private List<FlexibleBonus> flexibleBonuses;

    public Race() {}
    public Race(String id, String name, String source,
                Map<String, Integer> fixedBonuses, List<FlexibleBonus> flexibleBonuses) {
        this.id = id; this.name = name; this.source = source;
        this.fixedBonuses = fixedBonuses; this.flexibleBonuses = flexibleBonuses;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Map<String, Integer> getFixedBonuses() { return fixedBonuses; }
    public void setFixedBonuses(Map<String, Integer> f) { this.fixedBonuses = f; }
    public List<FlexibleBonus> getFlexibleBonuses() { return flexibleBonuses; }
    public void setFlexibleBonuses(List<FlexibleBonus> f) { this.flexibleBonuses = f; }
}
