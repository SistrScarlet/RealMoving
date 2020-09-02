package com.sistr.realmoving.mixin;

import com.sistr.realmoving.network.Networking;
import com.sistr.realmoving.network.PacketAction;
import com.sistr.realmoving.setup.ClientSetup;
import com.sistr.realmoving.util.IActionable;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class MixinClientActionEntity extends MixinActionEntity {
    private boolean actioning;
    private boolean actioningSend;

    protected MixinClientActionEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("RETURN"), method = "livingTick")
    public void onLivingTick(CallbackInfo ci) {
        actioning = ClientSetup.action.isKeyDown();
    }

    @Inject(at = @At("RETURN"), method = "tick")
    public void postTick(CallbackInfo ci) {
        if (!this.isPassenger()) {
            boolean isActioning = ((IActionable) this).isActioning();
            if (isActioning != this.actioningSend) {
                Networking.INSTANCE.sendToServer(new PacketAction(isActioning ?
                        PacketAction.ActionType.ACTION_TRUE : PacketAction.ActionType.ACTION_FALSE));
                this.actioningSend = isActioning;
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "isCrouching", cancellable = true)
    public void onIsCrouching(CallbackInfoReturnable<Boolean> cir) {
        if (isCrawling()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public boolean isActioning() {
        return actioning;
    }

}
