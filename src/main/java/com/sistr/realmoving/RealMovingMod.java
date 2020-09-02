package com.sistr.realmoving;

import com.sistr.realmoving.setup.ClientSetup;
import com.sistr.realmoving.setup.ModSetup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("realmoving")
public class RealMovingMod
{
    public static final String MODID = "realmoving";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public RealMovingMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ModSetup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
    }

}
