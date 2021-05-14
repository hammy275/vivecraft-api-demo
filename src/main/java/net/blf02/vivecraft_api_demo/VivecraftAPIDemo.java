package net.blf02.vivecraft_api_demo;

import net.blf02.vivecraft_api_demo.event.MainSubscriber;
import net.blf02.vivecraft_api_demo.init.ItemInit;
import net.blf02.vivecraft_api_demo.network.PacketHandler;
import net.blf02.vivecraft_api_demo.network.packet.PlayerAction;
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
     * Constructor
     *
     * Yes, the code for this mod definitely isn't the best, but this isn't supposed to be much more
     * than a tech demo to show that, yes, the Vivecraft API can do neat things.
     *
     * Why is the code so bad? Partially because I wanna make cool things fast, and partially because
     * some of the code is heavily based off of a currently closed-source mod that I'm working on.
     *
     * Maybe I'll expand this into a full mod that takes advantage of it someday.
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
    }
}
