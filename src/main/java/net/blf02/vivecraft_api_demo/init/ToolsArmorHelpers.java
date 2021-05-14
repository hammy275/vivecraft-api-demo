package net.blf02.vivecraft_api_demo.init;

import net.blf02.vivecraft_api_demo.material.ModArmorMaterial;
import net.blf02.vivecraft_api_demo.material.ModItemTier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.SoundEvent;

import java.util.function.Supplier;

public class ToolsArmorHelpers {

    public static class ModToolsArmor {


        public static boolean registerTools(String materialName, int harvestLevel, int maxUses, float miningSpeed,
                                            float attackDamageExtraFromWood, int enchantability, float axeAttackSpeed, Supplier<Item> repairMaterial) {

            ModItemTier itemTier = new ModItemTier(harvestLevel, maxUses, miningSpeed, attackDamageExtraFromWood, enchantability,
                    repairMaterial);

            ItemInit.ITEMS.register(materialName + "_sword", () ->
                    new SwordItem(itemTier, 3, -2.4F, new Item.Properties().group(ItemGroup.COMBAT))
            );
            ItemInit.ITEMS.register(materialName + "_pickaxe", () ->
                    new PickaxeItem(itemTier, 1, -2.8F, new Item.Properties().group(ItemGroup.COMBAT))
            );
            ItemInit.ITEMS.register(materialName + "_axe", () ->
                    new AxeItem(itemTier, 5, axeAttackSpeed, new Item.Properties().group(ItemGroup.COMBAT))
            );
            ItemInit.ITEMS.register(materialName + "_shovel", () ->
                    new ShovelItem(itemTier, 1.5F, -3.0F, new Item.Properties().group(ItemGroup.COMBAT))
            );
            ItemInit.ITEMS.register(materialName + "_hoe", () ->
                    new HoeItem(itemTier, -3, 0.0F, new Item.Properties().group(ItemGroup.COMBAT))
            );

            return true;

        }

        public static boolean registerArmor(String materialName, int durabilityFactor, int[] armorValuesFromBootsToHelm, int enchantability,
                                            SoundEvent equipSound, float toughness, float knockbackResistance, Supplier<Item> repairMaterial) {
            ModArmorMaterial armorMaterial = new ModArmorMaterial(materialName, durabilityFactor, armorValuesFromBootsToHelm, enchantability, equipSound,
                    toughness, knockbackResistance, repairMaterial);

            ItemInit.ITEMS.register(materialName + "_helmet", () ->
                    new ArmorItem(armorMaterial, EquipmentSlotType.HEAD, new Item.Properties().group(ItemGroup.COMBAT)));
            ItemInit.ITEMS.register(materialName + "_chestplate", () ->
                    new ArmorItem(armorMaterial, EquipmentSlotType.CHEST, new Item.Properties().group(ItemGroup.COMBAT)));
            ItemInit.ITEMS.register(materialName + "_leggings", () ->
                    new ArmorItem(armorMaterial, EquipmentSlotType.LEGS, new Item.Properties().group(ItemGroup.COMBAT)));
            ItemInit.ITEMS.register(materialName + "_boots", () ->
                    new ArmorItem(armorMaterial, EquipmentSlotType.FEET, new Item.Properties().group(ItemGroup.COMBAT)));


            return true;
        }
    }
}
