package net.blf02.vivecraft_api_demo.item;

import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
import net.blf02.vivecraftapi.api.utils.APIUtils;
import net.blf02.vivecraftapi.dataclass.ImmutableEventVRPlayerData;
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
        if (APIUtils.isVRPlayer(playerIn)) {
            ImmutableEventVRPlayerData data = APIUtils.getVRPlayerData(playerIn);
            fireLaser(worldIn, playerIn, 5f, data.getController0().getPos(),
                    data.getController0().getRotation().asLookVector());
            fireLaser(worldIn, playerIn, 5f, data.getController1().getPos(),
                    data.getController1().getRotation().asLookVector());
            NonNullList<ItemStack> armor = (NonNullList<ItemStack>) playerIn.getArmorInventoryList();
            if (armor.get(3).getItem() == ItemInit.laserHelmet.get()) {
                PacketHandler.INSTANCE.sendToServer(new PlayerAction(PlayerAction.LASER_HELMET));
            }
            return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
        }
        return ActionResult.resultFail(playerIn.getHeldItem(handIn));
    }

    public static void fireLaser(World worldIn, PlayerEntity playerIn, float damageBase,
                                 Vector3d posVec, Vector3d lookVec) {
        int STEPS = 100;

        Vector3d newVec;
        BlockPos newPos;
        AxisAlignedBB hitVec;
        for (int i = 0; i <= STEPS; i++) {
            newVec = new Vector3d(posVec.getX() + lookVec.getX() * i,
                    posVec.getY() + lookVec.getY() * i,
                    posVec.getZ() + lookVec.getZ() * i);
            newPos = new BlockPos(Math.floor(newVec.getX()), Math.floor(newVec.getY()), Math.floor(newVec.getZ()));
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
            hitVec = new AxisAlignedBB(newVec.add(-0.5, -0.5, -0.5), newVec.add(0.5, 0.5, 0.5));
            ArrayList<Entity> hitEnts = (ArrayList<Entity>) worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, hitVec);
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
