package com.sistr.realmoving.mixin;

import com.sistr.realmoving.RealMovingMod;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class Connector implements IMixinConnector {

    @Override
    public void connect() {
        RealMovingMod.LOGGER.info("Invoking Mixin Connector");
        Mixins.addConfiguration("assets/realmoving/realmoving.mixins.json");
    }

}