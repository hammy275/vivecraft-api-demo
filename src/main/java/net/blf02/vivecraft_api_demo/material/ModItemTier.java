package net.blf02.vivecraft_api_demo.material;

import net.minecraft.item.IItemTier;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;

import java.util.function.Supplier;

public class ModItemTier implements IItemTier {

    private final int harvestLevel;
    private final int maxUses;
    private final float miningSpeed;
    private final float attackDamageExtraFromWood;
    private final int enchantability;
    private final LazyValue<Item> repairMaterial;

    public ModItemTier(int harvestLevel, int maxUses, float miningSpeed, float attackDamageExtraFromWood, int enchantability, Supplier<Item> repairMaterial) {
        this.harvestLevel = harvestLevel;
        this.maxUses = maxUses;
        this.miningSpeed = miningSpeed;
        this.attackDamageExtraFromWood = attackDamageExtraFromWood;
        this.repairMaterial = new LazyValue<>(repairMaterial);
        this.enchantability = enchantability;

    }

    @Override
    public int getMaxUses() {
        return this.maxUses;
    }

    @Override
    public float getEfficiency() {
        return this.miningSpeed;
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamageExtraFromWood;
    }

    @Override
    public int getHarvestLevel() {
        return this.harvestLevel;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(this.repairMaterial.getValue());
    }
}
