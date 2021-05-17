package net.blf02.vivecraft_api_demo.event;

import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
import net.blf02.vivecraft_api_demo.util.PlayerTracker;
import net.blf02.vivecraftapi.api.events.VRPlayerTickEvent;
import net.blf02.vivecraftapi.dataclass.VRObjectInfo;
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
            // If wearing a laser helmet and right-clicking an empty hand, tell the server to shoot a laser
            PacketHandler.INSTANCE.sendToServer(new PlayerAction(PlayerAction.LASER_HELMET));
        }
    }

    @SubscribeEvent
    public void vrPlayerTick(VRPlayerTickEvent event) {
        VRObjectInfo activeController = event.data.getActiveController(); // Get info about main controller position
        BlockPos pos = new BlockPos(activeController.getPos()); // Get block position from controller position
        ItemStack stack = event.player.getHeldItemMainhand(); // Get held item

        // Immersive furnace code
        if (event.player.world.getBlockState(pos).getBlock() == Blocks.FURNACE &&
                !PlayerTracker.handInFurnacePlayers.contains(event.player)) {
            // Get tile entity for furnace
            FurnaceTileEntity tileEnt = (FurnaceTileEntity) event.player.world.getTileEntity(pos);
            /* Mark player as currently using furnace (prevents a player from using the furnace multiple times
               without taking their hand out first) */
            PlayerTracker.handInFurnacePlayers.add(event.player);

            if (event.player.getHeldItemMainhand() == ItemStack.EMPTY) {
                /* If the player's hand is empty, and there is something in the output slot, we can
                   put it in the player's hand. */
                if (!tileEnt.getStackInSlot(2).getStack().isEmpty()) {
                    event.player.inventory.mainInventory.set(event.player.inventory.currentItem, tileEnt.getStackInSlot(2).getStack());
                    tileEnt.setInventorySlotContents(2, ItemStack.EMPTY);
                }
            } else if (tileEnt.getStackInSlot(1).getStack().isEmpty()
                    && tileEnt.isItemValidForSlot(1, stack)) {
                // If the held item by the player is valid fuel, and the fuel slot is empty, put it in the fuel slot
                tileEnt.setInventorySlotContents(1, stack);
                event.player.inventory.mainInventory.set(event.player.inventory.currentItem, ItemStack.EMPTY);
            }
            else if (tileEnt.getStackInSlot(0).getStack().isEmpty()) {
                // If the item to be smelted slot is empty and the player is holding something, put that something there
                tileEnt.setInventorySlotContents(0, stack);
                event.player.inventory.mainInventory.set(event.player.inventory.currentItem, ItemStack.EMPTY);
            }
        } else if (event.player.world.getBlockState(pos).getBlock() != Blocks.FURNACE) {
            // Mark player as not using furnace if their hand isn't in a furnace anymore
            PlayerTracker.handInFurnacePlayers.remove(event.player);
        }
    }

}
