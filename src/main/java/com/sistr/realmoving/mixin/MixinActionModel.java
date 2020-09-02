package com.sistr.realmoving.mixin;

import com.sistr.realmoving.util.IActionable;
import net.minecraft.client.renderer.entity.model.AgeableModel;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
@Mixin(BipedModel.class)
public abstract class MixinActionModel<T extends LivingEntity> extends AgeableModel<T> {

    @Shadow
    public float swimAnimation;

    @Shadow
    public ModelRenderer bipedLeftArm;
    private boolean isCrawling;

    @Shadow
    public ModelRenderer bipedRightArm;

    @Shadow
    public ModelRenderer bipedLeftLeg;

    @Shadow
    public ModelRenderer bipedRightLeg;

    @Shadow
    public abstract void setLivingAnimations(@Nonnull T entityIn, float limbSwing, float limbSwingAmount, float partialTick);

    @Inject(at = @At("RETURN"), method = "setLivingAnimations")
    public void onSetLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick, CallbackInfo ci) {
        this.isCrawling = 0 < entityIn.getSwimAnimation(partialTick) && !entityIn.isInWater();
    }

    @Inject(at = @At("RETURN"), method = "setRotationAngles")
    public void onSetRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        float rad = (float) Math.PI / 180F;

        if (isCrawling) {
            float limbRange = 5;
            //0 < x < limbRange
            float limb = limbSwing % limbRange;
            //0 < x <= 1
            float swim = this.swingProgress > 0.0F ? 0.0F : this.swimAnimation;

            float armD = 165F;
            float armU = 180F;
            float armI = 0F;
            float armO = 20F;
            float legD = 15F;
            float legU = 0F;
            float legI = 0F;
            float legO = 20F;

            //PlayerModelをMixinしてやろうと思ったけど別にPlayerModelはプレイヤー限定ではないのだ…
            if (entityIn instanceof IActionable && ((IActionable) entityIn).isSliding()) {
                this.bipedLeftArm.rotateAngleX = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleX, -armU * rad);
                this.bipedLeftArm.rotateAngleY = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleY, 0F * rad);
                this.bipedLeftArm.rotateAngleZ = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleZ, armO * rad);
                this.bipedRightArm.rotateAngleX = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleX, -armU * rad);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleY, 0F * rad);
                this.bipedRightArm.rotateAngleZ = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleZ, -armO * rad);
                this.bipedLeftLeg.rotateAngleX = MathHelper.lerp(swim, this.bipedLeftLeg.rotateAngleX, -legU * rad);
                this.bipedLeftLeg.rotateAngleZ = MathHelper.lerp(swim, this.bipedLeftLeg.rotateAngleZ, -legO * rad);
                this.bipedRightLeg.rotateAngleX = MathHelper.lerp(swim, this.bipedRightLeg.rotateAngleX, -legU * rad);
                this.bipedRightLeg.rotateAngleZ = MathHelper.lerp(swim, this.bipedRightLeg.rotateAngleZ, legO * rad);
            }//接地している手足はzが増す
            // 上げる手足はzを減らしxを上げ下げ
            else if (limb < limbRange / 2) {
                float pct = limb / (limbRange / 2);
                float half = 1 - Math.abs((pct - 0.5F) * 2);

                //下げ
                this.bipedLeftArm.rotateAngleX = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleX, -armD * rad);
                this.bipedLeftArm.rotateAngleY = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleY, 0F * rad);
                this.bipedLeftArm.rotateAngleZ = lerp(swim, this.bipedLeftArm.rotateAngleZ, pct, armI, armO);
                //上げ
                this.bipedRightArm.rotateAngleX = lerp(swim, this.bipedRightArm.rotateAngleX, half, -armD, -armU);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleY, 0F * rad);
                this.bipedRightArm.rotateAngleZ = lerp(swim, this.bipedRightArm.rotateAngleZ, pct, -armO, -armI);
                //上げ
                this.bipedLeftLeg.rotateAngleX = lerp(swim, this.bipedLeftLeg.rotateAngleX, half, -legD, -legU);
                this.bipedLeftLeg.rotateAngleZ = lerp(swim, this.bipedLeftLeg.rotateAngleZ, pct, -legO, -legI);
                //下げ
                this.bipedRightLeg.rotateAngleX = MathHelper.lerp(swim, this.bipedRightLeg.rotateAngleX, -legD * rad);
                this.bipedRightLeg.rotateAngleZ = lerp(swim, this.bipedRightLeg.rotateAngleZ, pct, legI, legO);
            } else {
                float pct = (limb - limbRange / 2) / (limbRange / 2);
                float half = 1 - Math.abs((pct - 0.5F) * 2);
                //上げ
                this.bipedLeftArm.rotateAngleX = lerp(swim, this.bipedLeftArm.rotateAngleX, half, -armD, -armU);
                this.bipedLeftArm.rotateAngleY = MathHelper.lerp(swim, this.bipedLeftArm.rotateAngleY, 0F * rad);
                this.bipedLeftArm.rotateAngleZ = lerp(swim, this.bipedLeftArm.rotateAngleZ, pct, armO, armI);
                //下げ
                this.bipedRightArm.rotateAngleX = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleX, -armD * rad);
                this.bipedRightArm.rotateAngleY = MathHelper.lerp(swim, this.bipedRightArm.rotateAngleY, 0F * rad);
                this.bipedRightArm.rotateAngleZ = lerp(swim, this.bipedRightArm.rotateAngleZ, pct, -armI, -armO);
                //下げ
                this.bipedLeftLeg.rotateAngleX = MathHelper.lerp(swim, this.bipedLeftLeg.rotateAngleX, -legD * rad);
                this.bipedLeftLeg.rotateAngleZ = lerp(swim, this.bipedLeftLeg.rotateAngleZ, pct, -legI, -legO);
                //上げ
                this.bipedRightLeg.rotateAngleX = lerp(swim, this.bipedRightLeg.rotateAngleX, half, -legD, -legU);
                this.bipedRightLeg.rotateAngleZ = lerp(swim, this.bipedRightLeg.rotateAngleZ, pct, legO, legI);
            }
        } else if (entityIn instanceof IActionable && ((IActionable) entityIn).isClimbing()) {
            float pct = climbProgress((IActionable) entityIn);
            float armC = 5F;
            float armU = 160F;
            float armO = 5F;
            this.bipedLeftArm.rotateAngleX = MathHelper.lerp(pct, -armU * rad, -armC * rad);
            this.bipedLeftArm.rotateAngleY = MathHelper.lerp(pct, 0 * rad, 0F * rad);
            this.bipedLeftArm.rotateAngleZ = MathHelper.lerp(pct, armO * rad, -armO * rad);
            this.bipedRightArm.rotateAngleX = MathHelper.lerp(pct, -armU * rad, -armC * rad);
            this.bipedRightArm.rotateAngleY = MathHelper.lerp(pct, 0 * rad, 0F * rad);
            this.bipedRightArm.rotateAngleZ = MathHelper.lerp(pct, -armO * rad, armO * rad);
        }

    }

    private static float lerp(float swim, float armLeg, float pct, float start, float end) {
        return MathHelper.lerp(swim, armLeg,
                MathHelper.lerp(pct, start * (float) Math.PI / 180F, end * (float) Math.PI / 180F));
    }

    private static float climbProgress(IActionable actionable) {
        float climbHeight = actionable.getClimbHeight();
        return 1F - MathHelper.clamp(climbHeight / 2F, 0F, 1F);
    }

}
