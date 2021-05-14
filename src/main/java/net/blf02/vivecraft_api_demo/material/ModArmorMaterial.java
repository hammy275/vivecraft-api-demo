package net.blf02.vivecraft_api_demo.material;

import net.blf02.vivecraft_api_demo.VivecraftAPIDemo;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.LazyValue;
import net.minecraft.util.SoundEvent;

import java.util.function.Supplier;

public class ModArmorMaterial implements IArmorMaterial {

    private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11}; // Values from MC's ArmorMaterial.java
    private final String name;
    private final int durabilityFactor;
    private final int[] armorValues;
    private final int enchantability;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final LazyValue<Item> repairMaterial;

    public ModArmorMaterial(String name, int durabilityFactor, int[] armorValuesFromBootsToHelm, int enchantability,
                            SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Item> repairMaterial) {
        this.name = VivecraftAPIDemo.MOD_ID + ":" + name;
        this.durabilityFactor = durabilityFactor;
        this.armorValues = armorValuesFromBootsToHelm;
        this.enchantability = enchantability;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairMaterial = new LazyValue<>(repairMaterial);
    }

    @Override
    public int getDurability(EquipmentSlotType slotIn) {
        return MAX_DAMAGE_ARRAY[slotIn.getIndex()] * this.durabilityFactor;
    }

    @Override
    public int getDamageReductionAmount(EquipmentSlotType slotIn) {
        return this.armorValues[slotIn.getIndex()];
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return this.equipSound;
    }

    @Override
    public Ingredient getRepairMaterial() {
        return Ingredient.fromItems(this.repairMaterial.getValue());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public float getToughness() {
        return this.toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return this.knockbackResistance;
    }
}
