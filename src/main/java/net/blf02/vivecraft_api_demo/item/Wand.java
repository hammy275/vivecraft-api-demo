package net.blf02.vivecraft_api_demo.item;

import net.blf02.vivecraft_api_demo.util.PlayerHandler;
import net.blf02.vivecraftapi.api.utils.VRAPI;
import net.blf02.vivecraftapi.dataclass.VRObjectInfo;
import net.blf02.vivecraftapi.dataclass.VRPlayerData;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wand extends Item {

    protected static final Map<ArrayList<Direction>, ActiveSpell> spellList = new HashMap<>();

    public Wand(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            if (getActiveSpell(playerIn) != ActiveSpell.NONE) { // If we have a spell to cast.
                VRPlayerData data = VRAPI.getVRPlayerData(playerIn);
                if (data == null) { // VR players only!
                    playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                            SoundCategory.PLAYERS, 1, 0.5f);
                    return ActionResult.resultFail(playerIn.getHeldItemMainhand());
                }

                // Attempt to get a position for the spell, failing if we don't get one.
                BlockPos spellPos = getSpellPosition(worldIn, playerIn, data.getActiveController().getPos(),
                        data.getActiveController().asLookVec());
                if (spellPos == null) {
                    clearWandInfo(playerIn);
                    playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                            SoundCategory.PLAYERS, 1, 0.5f);
                    return ActionResult.resultFail(playerIn.getHeldItemMainhand());
                }

                // Cast the spell, and reset the player for another spell.
                doSpell(getActiveSpell(playerIn), spellPos, worldIn);
                clearWandInfo(playerIn);
                return ActionResult.resultSuccess(playerIn.getHeldItemMainhand());
            }
            // If the player doesn't have a spell, reset them.
            clearWandInfo(playerIn);
            playerIn.world.playSound(null, playerIn.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                    SoundCategory.PLAYERS, 1, 1);
            return ActionResult.resultPass(playerIn.getHeldItemMainhand());
        } else {
            return ActionResult.resultPass(playerIn.getHeldItemMainhand());
        }

    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true; // It's a magical wand, let's let it always glow.
    }

    /**
     * Resets a player to perform another spell.<br>
     * <br>
     * @param player Player to reset in terms of spell casting.
     */
    public static void clearWandInfo(@Nonnull PlayerEntity player) {
        PlayerHandler.wandCastingPositions.remove(player.getUniqueID());
        PlayerHandler.wandCastCooldown.remove(player.getUniqueID());
        PlayerHandler.wandActiveSpells.remove(player.getUniqueID());
    }

    /**
     * Function to run every tick for every VR player. Handles reading wand positions.<br>
     * <br>
     * @param player Player to tick. Must be a VR player!
     */
    public static void vrPlayerTick(@Nonnull PlayerEntity player) {
        // Don't tick if they aren't holding the wand.
        if (!(player.getHeldItemMainhand().getItem() instanceof Wand)) {
            return;
        }

        // Don't tick if they already have an active spell.
        if (getActiveSpell(player) != ActiveSpell.NONE) {
            return;
        }

        VRPlayerData data = VRAPI.getVRPlayerData(player);
        VRObjectInfo hand = data.getActiveController();

        // Get Direction enum for which direction their hand is pointing
        Direction direction = getDirection(hand, data.getHead());

        if (direction == Direction.NONE) {
            PlayerHandler.wandCastCooldown.remove(player.getUniqueID()); // Remove from cooldown if they're ready to go to a new pose
        } else if (!PlayerHandler.wandCastCooldown.contains(player.getUniqueID())) {
            PlayerHandler.wandCastCooldown.add(player.getUniqueID()); // Cooldown so player can get to a different pose
            addSpellDirection(player, direction); // Add the direction to the internal list of directions so-far (if any)
        }
    }

    /**
     * Gets the direction the hand is in.
     * @param hand The VR info about the hand (relative).
     * @return The direction enum for where the hand is.
     */
    protected static Direction getDirection(VRObjectInfo hand, VRObjectInfo head) {
        final double THRESHOLD = 0.85;
        Vector3d lookVec = hand.asLookVec(); // Gets direction hand is pointing
        if (lookVec.y > THRESHOLD) {
            return Direction.UP; // Up if pointing up
        } else if (lookVec.y < -THRESHOLD) {
            return Direction.DOWN; // Down if pointing down
        } else if (
                (Math.abs(lookVec.x) > THRESHOLD && Math.abs(head.asLookVec().x) < Math.abs(head.asLookVec().z)) ||
                (Math.abs(lookVec.z) > THRESHOLD && Math.abs(head.asLookVec().z) < Math.abs(head.asLookVec().x))
                ) {
            return Direction.SIDE; // Side if pointing to the side (this code could definitely be improved)
        }
        return Direction.NONE;

    }

    /**
     * Add a new spell direction to a player's list. Will attempt to set the spell if it hits the spell length of 4.<br>
     * <br>
     * @param player Player to set the spell of.
     * @param direction The direction to add to the spell list.
     */
    protected static void addSpellDirection(PlayerEntity player, Direction direction) {
        if (!PlayerHandler.wandCastingPositions.containsKey(player.getUniqueID())) {
            /* If the player has 0 directions in their list so far, create a new list, assign it to them, and add
               the passed in direction to the list. */
            ArrayList<Direction> directions = new ArrayList<>();
            directions.add(direction);
            player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                    SoundCategory.PLAYERS, 1, 1);
            PlayerHandler.wandCastingPositions.put(player.getUniqueID(), directions);
        } else {
            // Get the player's direction list, and add it.
            ArrayList<Direction> directionList = PlayerHandler.wandCastingPositions.get(player.getUniqueID());
            directionList.add(direction);

            // 4 is the size of a direction list ready for a spell. Get ready to set the player's spell!
            if (directionList.size() == 4) {
                ArrayList<Direction> directions = PlayerHandler.wandCastingPositions.remove(player.getUniqueID());
                if (attemptSetSpell(player, directions)) { // Set the spell, and play a sound if it was successful or not
                    player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                            SoundCategory.PLAYERS, 1, 2f);
                } else {
                    player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                            SoundCategory.PLAYERS, 1, 0.5f);
                }
            } else { // Neutral sound to tell player that a direction was registered
                player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_BASS,
                        SoundCategory.PLAYERS, 1, 1);
            }
        }
    }

    /**
     * Get the supplied player's active spell.<br>
     * <br>
     * @param player The player.
     * @return The spell they have, or ActiveSpell.NONE if they have no spell active.
     */
    protected static ActiveSpell getActiveSpell(PlayerEntity player) {
        if (PlayerHandler.wandActiveSpells.containsKey(player.getUniqueID())) {
            return PlayerHandler.wandActiveSpells.get(player.getUniqueID());
        }
        return ActiveSpell.NONE;
    }

    /**
     * Register the spells into the list of possible spells. Called in VivecraftAPIDemo.setup().
     */
    public static void registerSpells() {
        ArrayList<Direction> lightning = new ArrayList<>();
        lightning.add(Direction.SIDE);
        lightning.add(Direction.UP);
        lightning.add(Direction.DOWN);
        lightning.add(Direction.SIDE);
        spellList.put(lightning, ActiveSpell.LIGHTNING);
    }

    /**
     * Attempt to set the player's active spell based on the supplied direction list.<br>
     * <br>
     * @param player The player to set the spell of.
     * @param directionList The list of directions that possibly correspond to a spell.
     * @return Whether or not a spell was successfully set.
     */
    public static boolean attemptSetSpell(PlayerEntity player, ArrayList<Direction> directionList) {
        ActiveSpell spell = spellList.get(directionList);
        if (spell != null) {
            PlayerHandler.wandActiveSpells.put(player.getUniqueID(), spell);
        }
        return spell != null;
    }

    /**
     * Get the position to perform the spell. Based heavily off of LaserHands' fireLaser() function.<br>
     * <br>
     * @param worldIn The world to get the position in.
     * @param playerIn The player that will cast the spell.
     * @param posVec The position vector the spell should be cast from.
     * @param lookVec The look vector the spell should be cast from.
     * @return The location the spell should be cast, or null for any reaosn for it to fail.
     */
    public static BlockPos getSpellPosition(World worldIn, PlayerEntity playerIn, Vector3d posVec, Vector3d lookVec) {
        int STEPS = 100;

        Vector3d newVec;
        BlockPos newPos;
        AxisAlignedBB hitVec;
        for (int i = 0; i <= STEPS; i++) {
            // Get position of current "step" for spell.
            newVec = new Vector3d(posVec.getX() + lookVec.getX() * i,
                    posVec.getY() + lookVec.getY() * i,
                    posVec.getZ() + lookVec.getZ() * i);
            newPos = new BlockPos(Math.floor(newVec.getX()), Math.floor(newVec.getY()), Math.floor(newVec.getZ()));

            // Return block position for spell if finding non-solid
            if (!worldIn.getBlockState(newPos).equals(Blocks.AIR.getDefaultState())) {
                return newPos.add(0, 1, 0);
            }

            // Get all entities in spell direction
            hitVec = new AxisAlignedBB(newVec.add(-0.5, -0.5, -0.5), newVec.add(0.5, 0.5, 0.5));
            ArrayList<Entity> hitEnts = (ArrayList<Entity>) worldIn.getEntitiesWithinAABBExcludingEntity(playerIn, hitVec);

            // Attempt to damage an entity hit by the laser, and break the loop.
            if (!hitEnts.isEmpty()) {
                if (!(hitEnts.get(0) instanceof LivingEntity) && !(hitEnts.get(0) instanceof ItemEntity)) {
                    return newPos.add(0, 1, 0);
                } else if (hitEnts.get(0) instanceof ItemEntity) {
                    continue;
                }
                return hitEnts.get(0).getPosition();
            }

        }
        return null;
    }

    /**
     * Performs the specified spell at the specified location.<br>
     * <br>
     * @param spell The spell to perform.
     * @param pos The location to perform the spell.
     * @param world The world to perform the spell in.
     */
    public static void doSpell(ActiveSpell spell, BlockPos pos, World world) {
        if (spell == ActiveSpell.LIGHTNING) {
            LightningBoltEntity ent = new LightningBoltEntity(EntityType.LIGHTNING_BOLT, world);
            ent.setPosition(pos.getX(), pos.getY(), pos.getZ());
            world.addEntity(ent);
        }
    }

    /**
     * Enum for which direction relative to the player the wand is in.
     */
    public enum Direction {
        UP,
        DOWN,
        SIDE,
        NONE
    }

    /**
     * Enum for spell that the player has and to cast.
     */
    public enum ActiveSpell {
        NONE,
        LIGHTNING
    }

}
