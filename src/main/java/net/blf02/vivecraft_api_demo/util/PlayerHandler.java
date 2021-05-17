package net.blf02.vivecraft_api_demo.util;

import net.blf02.vivecraft_api_demo.item.Wand;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;

public class PlayerHandler {

    public static final Set<PlayerEntity> handInFurnacePlayers = new HashSet<>();

    /* Using UUIDs for these, since they need to be accessed by functions that
     * carry different forms of PlayerEntity objects. As a result, we use UUIDs instead. */
    public static final Set<UUID> wandCastCooldown = new HashSet<>();
    public static final Map<UUID, ArrayList<Wand.Direction>> wandCastingPositions = new HashMap<>();
    public static final Map<UUID, Wand.ActiveSpell> wandActiveSpells = new HashMap<>();


}
