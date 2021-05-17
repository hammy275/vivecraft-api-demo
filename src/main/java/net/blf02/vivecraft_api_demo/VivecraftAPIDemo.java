package net.blf02.vivecraft_api_demo;

import net.blf02.vivecraft_api_demo.event.MainSubscriber;
import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.item.Wand;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
import net.blf02.vivecraftapi.api.events.VRPlayerTickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod(VivecraftAPIDemo.MOD_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class VivecraftAPIDemo {

    public static final String MOD_ID = "vivecraft_api_demo";

    /**
     * Main Mod Constructor<br>
     *<br>
     * This mod isn't perfect, and probably shouldn't be used on an actual server (not in its current state,
     * at least). However, it serves to demonstrate that Vivecraft can be expanded upon by mods
     * (see {@link MainSubscriber#vrPlayerTick(VRPlayerTickEvent)}'s immersive furnace code), while at the
     * same time, mods can add VR integrations for their pre-existing items to give VR players a
     * more immersive experience (see {@link net.blf02.vivecraft_api_demo.item.LaserHands}).<br>
     *<br>
     * This mod should always, 100% work in singleplayer, however may not work in multiplayer perfectly in terms
     * of more cosmetic things (such as particles for lasers). All VR-specific things should work in both singleplayer
     * and multiplayer, and this mod shouldn't crash in either setting.
     */
    public VivecraftAPIDemo() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new MainSubscriber());

        ItemInit.ITEMS.register(bus);

    }

    public void setup(final FMLCommonSetupEvent event) {
        SimpleChannel main = PacketHandler.INSTANCE;

        int index = 1;
        main.registerMessage(index++, PlayerAction.class, PlayerAction::encode, PlayerAction::decode, PlayerAction::handle);

        // Registers wand spells.
        Wand.registerSpells();
    }
}
