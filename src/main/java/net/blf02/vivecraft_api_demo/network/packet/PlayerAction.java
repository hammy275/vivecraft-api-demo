package net.blf02.vivecraft_api_demo.network.packet;

import net.blf02.vivecraft_api_demo.item.LaserHands;
import net.blf02.vivecraftapi.api.utils.VRAPI;
import net.blf02.vivecraftapi.dataclass.VRPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerAction {

    public static final int LASER_HELMET = 1;


    public final int action;

    public PlayerAction(int action) {
        this.action = action;
    }

    public static void encode(PlayerAction pa, PacketBuffer pb) {
        pb.writeInt(pa.action);
    }

    public static PlayerAction decode(PacketBuffer pb) {
        return new PlayerAction(pb.readInt());
    }

    public static void handle(final PlayerAction message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            VRPlayerData data = VRAPI.getVRPlayerData(player);

            if (message.action == LASER_HELMET) {
                LaserHands.fireLaser(player.world, player, 10, data.getHead().getPos(),
                        data.getHead().getRotation().asLookVector());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
