package com.sistr.realmoving.network;

import com.sistr.realmoving.util.IActionable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

//C2S
public class PacketAction {
    private final ActionType type;

    public PacketAction(PacketBuffer buf) {
        type = buf.readEnumValue(ActionType.class);
    }

    public PacketAction(ActionType type) {
        this.type = type;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeEnumValue(type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            switch (type) {
                case ACTION_TRUE:
                    ((IActionable)player).setActioning(true);
                    break;
                case ACTION_FALSE:
                    ((IActionable)player).setActioning(false);
                    break;
                case CRAWLING_TRUE:
                    ((IActionable)player).setCrawling(true);
                    break;
                case CRAWLING_FALSE:
                    ((IActionable)player).setCrawling(false);
                    break;
                case CLIMBING_TRUE:
                    ((IActionable)player).setClimbing(true);
                    break;
                case CLIMBING_FALSE:
                    ((IActionable)player).setClimbing(false);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum ActionType {
        ACTION_TRUE,
        ACTION_FALSE,
        CRAWLING_TRUE,
        CRAWLING_FALSE,
        CLIMBING_TRUE,
        CLIMBING_FALSE
    }
}
