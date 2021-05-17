package net.blf02.vivecraft_api_demo.item;

import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
import net.blf02.vivecraftapi.api.utils.VRAPI;
import net.blf02.vivecraftapi.dataclass.VRPlayerData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

public class LaserHands extends Item {
    public LaserHands(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (VRAPI.isVRPlayer(playerIn)) { // If they are a VR player
            VRPlayerData data = VRAPI.getVRPlayerData(playerIn); // Get the data of the VR player.
            fireLaser(worldIn, playerIn, 5f, data.getController0().getPos(),
                    data.getController0().getRotation().asLookVector()); // Fire the laser for left controller
            fireLaser(worldIn, playerIn, 5f, data.getController1().getPos(),
                    data.getController1().getRotation().asLookVector()); // Fire the laser for the right controller
            NonNullList<ItemStack> armor = (NonNullList<ItemStack>) playerIn.getArmorInventoryList();
            if (armor.get(3).getItem() == ItemInit.laserHelmet.get()) {
                // Fire the laser for the helmet.
                PacketHandler.INSTANCE.sendToServer(new PlayerAction(PlayerAction.LASER_HELMET));
            }
            return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
        } else {
            // Fire laser for non-VR player.
            fireLaser(worldIn, playerIn, 5f, playerIn.getPositionVec(), playerIn.getLookVec());
        }
        return ActionResult.resultFail(playerIn.getHeldItem(handIn));
    }

    /**
     * Fires a laser that destroys a block or damages a mob.<br>
     *<br>
     * @param worldIn The world object to shoot the laser in.
     * @param playerIn The player shooting the laser.
     * @param damageBase The amount of damage the laser should do if it hits a mob.
     * @param posVec The position the laser should originate from.
     * @param lookVec The direction the laser should travel.
     */
    public static void fireLaser(World worldIn, PlayerEntity playerIn, float damageBase,
                                 Vector3d posVec, Vector3d lookVec) {
        int STEPS = 100;

        Vector3d newVec;
        BlockPos newPos;
        AxisAlignedBB hitVec;
        for (int i = 0; i <= STEPS; i++) {
            // Get position of current "step" for laser.
            newVec = new Vector3d(posVec.getX() + lookVec.getX() * i,
                    posVec.getY() + lookVec.getY() * i,
                    posVec.getZ() + lookVec.getZ() * i);
            newPos = new BlockPos(Math.floor(newVec.getX()), Math.floor(newVec.getY()), Math.floor(newVec.getZ()));

            // Add particles into the world
            if (worldIn.isRemote && i >= 2) {
                worldIn.addParticle(new RedstoneParticleData(1, 0, 0, 1), newVec.getX(), newVec.getY(),
                        newVec.getZ(), 0, 0, 0);
            } else if (i >= 2) {
                try {
                    ServerWorld world = playerIn.getServer().getWorld(playerIn.world.getDimensionKey());
                    world.spawnParticle(new RedstoneParticleData(1, 0, 0, 1), newVec.getX(), newVec.getY(),
                            newVec.getZ(), 1, 0, 0, 0, 0);
                } catch (NullPointerException ignored) {}

            }

            // End loop and break block at the current position of the laser if it is a "weaker" block
            if (!worldIn.getBlockState(newPos).equals(Blocks.AIR.getDefaultState())) {
                if (worldIn.getBlockState(newPos).getBlock().getHarvestLevel(worldIn.getBlockState(newPos)) > 2 ||
                        worldIn.getBlockState(newPos).getBlockHardness(worldIn, newPos) < 0) {
                    break;
                }
                if (!worldIn.isRemote) {
                    worldIn.destroyBlock(newPos, true, playerIn);
                    break;
                }
            }

            // Get all entities in laser
            hitVec = new AxisAlignedBB(newVec.add(-0.5, -0.5, -0.5), newVec.add(0.5, 0.5, 0.5));
            ArrayList<Entity> hitEnts = (ArrayList<Entity>) worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, hitVec);

            // Attempt to damage an entity hit by the laser, and break the loop.
            if (!hitEnts.isEmpty()) {
                if (!(hitEnts.get(0) instanceof LivingEntity) && !(hitEnts.get(0) instanceof ItemEntity)) {
                    break;
                } else if (hitEnts.get(0) instanceof ItemEntity) {
                    continue;
                }
                hitEnts.get(0).attackEntityFrom(DamageSource.causePlayerDamage(playerIn), damageBase);
                hitEnts.get(0).setFire(3);
                break;
            }

        }
    }
}
