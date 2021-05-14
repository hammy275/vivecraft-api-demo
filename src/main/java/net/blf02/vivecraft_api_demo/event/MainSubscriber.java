package net.blf02.vivecraft_api_demo.event;

import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
import net.blf02.vivecraftapi.api.events.VRPlayerTickEvent;
import net.blf02.vivecraftapi.dataclass.ImmutableVRObjectInfo;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MainSubscriber {

    @SubscribeEvent
    public void emptyRightClick(PlayerInteractEvent.RightClickEmpty event) {
        NonNullList<ItemStack> armor = (NonNullList<ItemStack>) event.getPlayer().getArmorInventoryList();
        if (armor.get(3).getItem() == ItemInit.laserHelmet.get()) {
            PacketHandler.INSTANCE.sendToServer(new PlayerAction(PlayerAction.LASER_HELMET));
        }
    }

    @SubscribeEvent
    public void vrPlayerTick(VRPlayerTickEvent event) {
        ImmutableVRObjectInfo activeController = event.data.getActiveController();
        BlockPos pos = new BlockPos(activeController.getPos());
        ItemStack stack = event.player.getHeldItemMainhand();

        if (event.player.world.getBlockState(pos).getBlock() == Blocks.FURNACE) { // Immersive Furnace
            FurnaceTileEntity tileEnt = (FurnaceTileEntity) event.player.world.getTileEntity(pos);
            if (event.player.getHeldItemMainhand() == ItemStack.EMPTY) {
                if (!tileEnt.getStackInSlot(2).getStack().isEmpty()) {
                    event.player.inventory.mainInventory.set(event.player.inventory.currentItem, tileEnt.getStackInSlot(2).getStack());
                    tileEnt.setInventorySlotContents(2, ItemStack.EMPTY);
                }
            } else if (tileEnt.getStackInSlot(0).getStack().isEmpty()){
                tileEnt.setInventorySlotContents(0, stack);
                event.player.inventory.mainInventory.set(event.player.inventory.currentItem, ItemStack.EMPTY);
            }
        }
    }

}
