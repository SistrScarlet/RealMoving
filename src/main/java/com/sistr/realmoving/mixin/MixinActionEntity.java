package com.sistr.realmoving.mixin;

import com.sistr.realmoving.network.Networking;
import com.sistr.realmoving.network.PacketAction;
import com.sistr.realmoving.util.ClimbBlockData;
import com.sistr.realmoving.util.IActionable;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

@Mixin(PlayerEntity.class)
public abstract class MixinActionEntity extends LivingEntity implements IActionable {
    private final ClimbBlockData[] climbBlockData = new ClimbBlockData[4];
    private boolean actioning;
    private boolean crawling;
    private boolean climbing;

    @Nonnull
    @Shadow
    public abstract EntitySize getSize(@Nonnull Pose poseIn);

    private int slideTime;
    private float climbHeightCache;

    protected MixinActionEntity(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("HEAD"), method = "updatePose", cancellable = true)
    public void onUpdatePose(CallbackInfo ci) {
        updateClimbing();
        updateCrawling();
        if (((IActionable) this).isCrawling() && this.isPoseClear(Pose.SWIMMING)) {
            setPose(Pose.SWIMMING);
            ci.cancel();
        }
    }

    @Inject(at = @At("RETURN"), method = "tick")
    public void postTick(CallbackInfo ci) {
        if (isSliding()) {
            Vector3d motion = getMotion();
            motion = motion.subtract(0, motion.getY(), 0).normalize()
                    .scale((1F - (slideTime / 20F) * (slideTime / 20F)) * 0.2F);
            setMotion(getMotion().mul(0.6D, 1D, 0.6D).add(motion));
            if (20 < ++slideTime) {
                setSprinting(false);
            }
        } else {
            slideTime = 0;
        }
        if (isClimbing() && !isSliding()) {
            fallDistance = 0;
            setMotion(getMotion().mul(1D, 0, 1D));
            float climbHeight = getClimbHeight();
            if (collidedHorizontally) {
                if (0.1 < climbHeight) {
                    setMotion(getMotion().add(0, 0.1D, 0));
                } else if (isCrawling() &&
                        (canHook(climbBlockData[0], climbBlockData[1], getHeight())
                                || canHook(climbBlockData[1], climbBlockData[2], getHeight()))
                        || (canStand(climbBlockData[0], climbBlockData[1], climbBlockData[2], getHeight())
                        || canStand(climbBlockData[1], climbBlockData[2], climbBlockData[3], getHeight()))) {
                    setMotion(getMotion().add(0, 0.2, 0));
                    setClimbing(false);
                }
                //サーバー側ではうまく発動しない(原因不明)
                if (world.isRemote && isSneaking() && climbHeight + getSize(Pose.SWIMMING).height < getHeight()) {
                    setCrawling(true);
                    Networking.INSTANCE.sendToServer(new PacketAction(PacketAction.ActionType.CRAWLING_TRUE));
                }
            } else if (climbHeight < 2 - 0.2) {
                setMotion(getMotion().subtract(0, 0.1D, 0));
            }
        }
    }

    public void updateCrawling() {
        if (isCrawling()) {
            setCrawling((!isInWater() && !isPassenger() && (isSneaking() || isActioning())));
        } else {
            setCrawling(onGround && !isInWater() && !isPassenger() && isSneaking() && isActioning() && !isClimbing());
        }
    }

    public void updateClimbing() {
        climbHeightCache = -1;
        //伏せを優先するため、着地かつスニーク中は発動しない
        if (!(onGround && isSneaking() && !isCrawling()) && !isPassenger() && !isInWater() && isActioning()) {
            float climbHeight = getClimbHeight();
            boolean climbing = stepHeight < climbHeight || (isClimbing() && 0 < climbHeight);
            setClimbing(climbing);
            if (climbing) {
                this.climbHeightCache = climbHeight;
            }
        } else {
            setClimbing(false);
        }
    }

    //登れるブロックの上面がエンティティの底面よりどれだけ高いかを返す
    //-1 < x < 2
    public float getClimbHeight() {
        if (this.climbHeightCache != -1) {
            return climbHeightCache;
        }
        BlockPos base = getBaseClimbBlock();
        ClimbBlockData downData = climbBlockData[0] = getClimbBlockData(base.down());
        ClimbBlockData standData = climbBlockData[1] = getClimbBlockData(base);
        ClimbBlockData upData = climbBlockData[2] = getClimbBlockData(base.up());
        ClimbBlockData toData = climbBlockData[3] = getClimbBlockData(base.up(2));

        //登れるブロックの選択
        EntitySize swimSize = getSize(Pose.SWIMMING);
        float swimHeight = swimSize.height;
        if (canHook(standData, upData, swimHeight)) {
            return standData.getUp() - (float) getPosY();
        } else if (!isCrawling() && canHook(upData, toData, swimHeight)) {
            return upData.getUp() - (float) getPosY();
        } else if (canHook(downData, standData, swimHeight)) {
            return downData.getUp() - (float) getPosY();
        }
        return -1;
    }

    public boolean canHook(ClimbBlockData climb, ClimbBlockData up, float height) {
        return !climb.isEmpty() //床アリ
                && (up.isEmpty() || height < up.getDown() - climb.getUp());//下スキマ
    }

    public boolean canStand(ClimbBlockData climb, ClimbBlockData up, ClimbBlockData to, float height) {
        return !climb.isEmpty() //床アリ
                && up.isEmpty() && (to.isEmpty() || height < to.getDown() - climb.getUp());//下空上空またはスキマ
    }

    public ClimbBlockData getClimbBlockData(BlockPos pos) {
        VoxelShape shape = world.getBlockState(pos).getCollisionShape(world, pos);
        return shape.isEmpty() ? ClimbBlockData.DUMMY :
                new ClimbBlockData(pos.getY(), (float) shape.getEnd(Direction.Axis.Y), (float) shape.getStart(Direction.Axis.Y));
    }

    public BlockPos getBaseClimbBlock() {
        Vector3d look = this.getVectorForRotation(0, this.rotationYaw);
        return new BlockPos(getPositionVec().add(look.scale(0.5D)));
    }

    @Override
    public void setActioning(boolean actioning) {
        this.actioning = actioning;
    }

    @Override
    public boolean isActioning() {
        return this.actioning;
    }

    @Override
    public void setCrawling(boolean crawling) {
        this.crawling = crawling;
    }

    @Override
    public boolean isCrawling() {
        return this.crawling;
    }

    @Override
    public boolean isSliding() {
        return isSprinting() && isCrawling();
    }

    @Override
    public void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    public boolean isClimbing() {
        return this.climbing;
    }

}
