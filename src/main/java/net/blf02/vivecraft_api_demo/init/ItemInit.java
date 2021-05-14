package net.blf02.vivecraft_api_demo.init;

import net.blf02.vivecraft_api_demo.VivecraftAPIDemo;
import net.blf02.vivecraft_api_demo.item.LaserHands;
import net.blf02.vivecraft_api_demo.material.ModArmorMaterial;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemInit {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VivecraftAPIDemo.MOD_ID);

    public static final RegistryObject<Item> laserHands = ITEMS.register("laser_hands", () ->
            new LaserHands(new Item.Properties().group(ItemGroup.COMBAT)));

    public static final ModArmorMaterial laserHelmMaterial = new ModArmorMaterial("laser", 66,
            new int[]{0,0,0,1}, 0, SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            0, 0, null);
    public static final RegistryObject<Item> laserHelmet = ITEMS.register("laser_helmet", () ->
            new ArmorItem(laserHelmMaterial, EquipmentSlotType.HEAD, new Item.Properties().group(ItemGroup.COMBAT)));

}
