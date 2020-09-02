package com.sistr.realmoving.setup;

import com.sistr.realmoving.RealMovingMod;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RealMovingMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static final KeyBinding action = new KeyBinding(RealMovingMod.MODID + ".key.action",
            86, "key.categories.gameplay");

    public static void init(final FMLClientSetupEvent event) {
        //キーの登録
        ClientRegistry.registerKeyBinding(action);
    }

}
