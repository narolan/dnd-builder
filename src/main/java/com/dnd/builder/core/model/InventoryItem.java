package com.dnd.builder.core.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.Map;

/**
 * Represents an item in the character's inventory (campaign loot, purchased items, etc.)
 */
public class InventoryItem {

    private String id;
    @Size(max = 100)
    private String name;
    @Size(max = 50)
    private String category;    // weapon, armor, wondrous, potion, ring, etc.
    @Min(0) @Max(9999)
    private int quantity;
    private boolean equipped;
    private boolean attuned;
    private boolean requiresAttunement;
    @Size(max = 20)
    private String rarity;      // common, uncommon, rare, very rare, legendary, artifact
    @Size(max = 500)
    private String description;

    // Stat modifiers when equipped
    private int acBonus;
    private int attackBonus;
    private int damageBonus;
    private Map<String, Integer> abilityBonuses;  // e.g., {"STR": 2}
    private int saveDcBonus;
    private int speedBonus;

    // Weapon/armor specific
    @Size(max = 30)
    private String damage;      // e.g., "1d8+1 slashing"
    private int baseAc;         // for armor, e.g., 14
    private int maxDexBonus;    // -1 for no limit, 0 for no dex, 2 for medium armor
    private boolean finesse;    // can use STR or DEX for attack/damage (PHB p. 147)
    private boolean ranged;     // ranged weapon — uses DEX unless thrown with STR

    public InventoryItem() {}

    public InventoryItem(String name, String category) {
        this.id = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.category = category;
        this.quantity = 1;
        this.rarity = "common";
    }

    // Fluent setters for building items
    public InventoryItem withRarity(String rarity) { this.rarity = rarity; return this; }
    public InventoryItem withDescription(String desc) { this.description = desc; return this; }
    public InventoryItem withAcBonus(int bonus) { this.acBonus = bonus; return this; }
    public InventoryItem withAttackBonus(int bonus) { this.attackBonus = bonus; return this; }
    public InventoryItem withDamageBonus(int bonus) { this.damageBonus = bonus; return this; }
    public InventoryItem withAbilityBonuses(Map<String, Integer> bonuses) { this.abilityBonuses = bonuses; return this; }
    public InventoryItem withRequiresAttunement(boolean req) { this.requiresAttunement = req; return this; }
    public InventoryItem withBaseAc(int ac) { this.baseAc = ac; return this; }
    public InventoryItem withMaxDexBonus(int max) { this.maxDexBonus = max; return this; }
    public InventoryItem withDamage(String dmg) { this.damage = dmg; return this; }
    public InventoryItem withFinesse(boolean f) { this.finesse = f; return this; }
    public InventoryItem withRanged(boolean r) { this.ranged = r; return this; }

    // Standard getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isEquipped() { return equipped; }
    public void setEquipped(boolean equipped) { this.equipped = equipped; }
    public boolean isAttuned() { return attuned; }
    public void setAttuned(boolean attuned) { this.attuned = attuned; }
    public boolean isRequiresAttunement() { return requiresAttunement; }
    public void setRequiresAttunement(boolean requiresAttunement) { this.requiresAttunement = requiresAttunement; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getAcBonus() { return acBonus; }
    public void setAcBonus(int acBonus) { this.acBonus = acBonus; }
    public int getAttackBonus() { return attackBonus; }
    public void setAttackBonus(int attackBonus) { this.attackBonus = attackBonus; }
    public int getDamageBonus() { return damageBonus; }
    public void setDamageBonus(int damageBonus) { this.damageBonus = damageBonus; }
    public Map<String, Integer> getAbilityBonuses() { return abilityBonuses; }
    public void setAbilityBonuses(Map<String, Integer> abilityBonuses) { this.abilityBonuses = abilityBonuses; }
    public int getSaveDcBonus() { return saveDcBonus; }
    public void setSaveDcBonus(int saveDcBonus) { this.saveDcBonus = saveDcBonus; }
    public int getSpeedBonus() { return speedBonus; }
    public void setSpeedBonus(int speedBonus) { this.speedBonus = speedBonus; }
    public String getDamage() { return damage; }
    public void setDamage(String damage) { this.damage = damage; }
    public int getBaseAc() { return baseAc; }
    public void setBaseAc(int baseAc) { this.baseAc = baseAc; }
    public int getMaxDexBonus() { return maxDexBonus; }
    public void setMaxDexBonus(int maxDexBonus) { this.maxDexBonus = maxDexBonus; }
    public boolean isFinesse() { return finesse; }
    public void setFinesse(boolean finesse) { this.finesse = finesse; }
    public boolean isRanged() { return ranged; }
    public void setRanged(boolean ranged) { this.ranged = ranged; }
}
