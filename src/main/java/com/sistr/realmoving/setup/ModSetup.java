package com.sistr.realmoving.setup;

import com.sistr.realmoving.RealMovingMod;
import com.sistr.realmoving.network.Networking;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = RealMovingMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        Networking.registerMessages();
    }

}
