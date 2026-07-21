package fku.org.example.fku.features.pearlphase;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.pearlphase.PearlPhaseConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class PearlPhaseFeature {
    private static final Minecraft mc = Minecraft.getInstance();
    private static PhaseState state = PhaseState.IDLE;
    private static int stateTick = 0;
    private static int waitTicks = 0;
    private static float targetYaw = 0.0f;
    private static float targetPitch = 0.0f;
    private static float smoothYaw = Float.NaN;
    private static float smoothPitch = Float.NaN;
    private static boolean savedUp = false;
    private static boolean savedDown = false;
    private static boolean savedLeft = false;
    private static boolean savedRight = false;
    private static boolean wasInsideBlock = false;

    public static void init() {
        PearlPhaseConfig.getInstance();
        Fku.LOGGER.info("[PearlPhase] \u529f\u80fd\u5df2\u521d\u59cb\u5316");
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!PearlPhaseConfig.getInstance().enabled) {
            return;
        }
        int keyW = PearlPhaseFeature.mc.f_91066_.f_92085_.getKey().getValue();
        int keyS = PearlPhaseFeature.mc.f_91066_.f_92087_.getKey().getValue();
        int keyA = PearlPhaseFeature.mc.f_91066_.f_92086_.getKey().getValue();
        int keyD = PearlPhaseFeature.mc.f_91066_.f_92088_.getKey().getValue();
        if (event.getKey() == keyW) {
            boolean bl = savedUp = event.getAction() != 0;
        }
        if (event.getKey() == keyS) {
            boolean bl = savedDown = event.getAction() != 0;
        }
        if (event.getKey() == keyA) {
            boolean bl = savedLeft = event.getAction() != 0;
        }
        if (event.getKey() == keyD) {
            savedRight = event.getAction() != 0;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (PearlPhaseFeature.mc.player == null || PearlPhaseFeature.mc.f_91073_ == null) {
            return;
        }
        PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();
        if (!cfg.enabled) {
            if (state != PhaseState.IDLE) {
                PearlPhaseFeature.resetState();
            }
            if (PearlPhaseFeature.mc.player.f_19794_) {
                PearlPhaseFeature.mc.player.f_19794_ = false;
            }
            wasInsideBlock = false;
            return;
        }
        ++stateTick;
        boolean inside = PearlPhaseFeature.isInsideBlock(PearlPhaseFeature.mc.player);
        if (inside) {
            PearlPhaseFeature.handleInsideBlock(PearlPhaseFeature.mc.player, cfg);
            wasInsideBlock = true;
            return;
        }
        if (wasInsideBlock && !inside) {
            PearlPhaseFeature.mc.player.f_19794_ = false;
            wasInsideBlock = false;
        }
        if (cfg.autoThrow) {
            PearlPhaseFeature.handleAutoThrow(cfg);
        } else if (state != PhaseState.IDLE) {
            PearlPhaseFeature.resetState();
        }
        if (cfg.removeOverlay) {
            PearlPhaseFeature.mc.player.m_20242_(false);
        }
        if (cfg.noFront && PearlPhaseFeature.mc.f_91066_.m_92176_() == CameraType.THIRD_PERSON_FRONT) {
            PearlPhaseFeature.mc.f_91066_.m_92157_(CameraType.FIRST_PERSON);
        }
    }

    private static void handleInsideBlock(LocalPlayer player, PearlPhaseConfig cfg) {
        player.f_19794_ = cfg.noClipEnabled;
        player.f_19789_ = 0.0f;
        player.m_6853_(true);
        double baseSpeed = cfg.baseSpeed;
        double finalSpeed = baseSpeed * cfg.speed;
        double forward = savedUp ? 1.0 : 0.0;
        double backward = savedDown ? 1.0 : 0.0;
        double left = savedLeft ? 1.0 : 0.0;
        double right = savedRight ? 1.0 : 0.0;
        double fwd = forward - backward;
        double strafe = left - right;
        if (fwd == 0.0 && strafe == 0.0) {
            player.m_20334_(0.0, 0.0, 0.0);
            return;
        }
        if (fwd != 0.0 && strafe != 0.0) {
            fwd *= Math.sin(0.7853981633974483);
            strafe *= Math.cos(0.7853981633974483);
        }
        float yaw = player.m_146908_();
        double motionX = fwd * finalSpeed * -Math.sin(Math.toRadians(yaw)) + strafe * finalSpeed * Math.cos(Math.toRadians(yaw));
        double motionZ = fwd * finalSpeed * Math.cos(Math.toRadians(yaw)) - strafe * finalSpeed * -Math.sin(Math.toRadians(yaw));
        player.m_20334_(motionX, forward > 0.0 ? finalSpeed * 0.5 : (backward > 0.0 ? -finalSpeed * 0.5 : 0.0), motionZ);
        state = PhaseState.INSIDE;
    }

    private static void handleAutoThrow(PearlPhaseConfig cfg) {
        if (PearlPhaseFeature.mc.player == null) {
            return;
        }
        switch (state) {
            case IDLE: {
                if (PearlPhaseFeature.mc.f_91077_ == null || PearlPhaseFeature.mc.f_91077_.m_6662_() != HitResult.Type.BLOCK) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                if (!PearlPhaseFeature.hasPearl()) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                BlockHitResult blockHit = (BlockHitResult)PearlPhaseFeature.mc.f_91077_;
                BlockPos targetPos = blockHit.m_82425_();
                BlockState targetState = PearlPhaseFeature.mc.f_91073_.m_8055_(targetPos);
                if (targetState.m_60795_() || !targetState.m_280296_()) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                float[] angles = PearlPhaseFeature.calculateTargetAngle(targetPos, cfg.edgeOffset);
                if (angles == null) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                targetYaw = angles[0];
                targetPitch = angles[1];
                smoothYaw = Float.NaN;
                smoothPitch = Float.NaN;
                stateTick = 0;
                state = PhaseState.AIMING;
                break;
            }
            case AIMING: {
                if (cfg.aimTime > 0 && !PearlPhaseFeature.smoothRotate(cfg.aimTime)) break;
                if (!PearlPhaseFeature.selectPearl()) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                stateTick = 0;
                state = PhaseState.THROWING;
                break;
            }
            case THROWING: {
                if (stateTick < 1) break;
                PearlPhaseFeature.throwPearl();
                waitTicks = cfg.maxWaitTicks;
                stateTick = 0;
                state = PhaseState.WAITING;
                break;
            }
            case WAITING: {
                if (waitTicks-- <= 0) {
                    PearlPhaseFeature.resetState();
                    return;
                }
                if (!PearlPhaseFeature.isInsideBlock(PearlPhaseFeature.mc.player)) break;
                PearlPhaseFeature.handleInsideBlock(PearlPhaseFeature.mc.player, cfg);
                break;
            }
        }
    }

    private static boolean isInsideBlock(LocalPlayer player) {
        if (PearlPhaseFeature.mc.f_91073_ == null) {
            return false;
        }
        return PearlPhaseFeature.mc.f_91073_.m_186434_((Entity)player, player.m_20191_().m_82310_(0.001, 0.001, 0.001)).iterator().hasNext();
    }

    private static float[] calculateTargetAngle(BlockPos blockPos, double edgeOffset) {
        if (PearlPhaseFeature.mc.player == null || PearlPhaseFeature.mc.f_91073_ == null) {
            return null;
        }
        BlockState state = PearlPhaseFeature.mc.f_91073_.m_8055_(blockPos);
        VoxelShape shape = state.m_60812_((BlockGetter)PearlPhaseFeature.mc.f_91073_, blockPos);
        if (shape.m_83281_()) {
            return null;
        }
        AABB bounds = shape.m_83215_();
        double minX = blockPos.m_123341_() + bounds.f_82288_;
        double maxX = blockPos.m_123341_() + bounds.f_82291_;
        double minY = blockPos.m_123342_() + bounds.f_82289_;
        double maxY = blockPos.m_123342_() + bounds.f_82292_;
        double minZ = blockPos.m_123343_() + bounds.f_82290_;
        double maxZ = blockPos.m_123343_() + bounds.f_82293_;
        double eyeX = PearlPhaseFeature.mc.player.getX();
        double eyeY = PearlPhaseFeature.mc.player.m_20188_();
        double eyeZ = PearlPhaseFeature.mc.player.getZ();
        double lookX = PearlPhaseFeature.mc.player.getLookAngle().x;
        double lookY = PearlPhaseFeature.mc.player.getLookAngle().y;
        double lookZ = PearlPhaseFeature.mc.player.getLookAngle().z;
        double[][] candidates = new double[][]{{minX + edgeOffset, minY + edgeOffset, minZ + edgeOffset}, {minX + edgeOffset, minY + edgeOffset, maxZ - edgeOffset}, {minX + edgeOffset, maxY - edgeOffset, minZ + edgeOffset}, {minX + edgeOffset, maxY - edgeOffset, maxZ - edgeOffset}, {maxX - edgeOffset, minY + edgeOffset, minZ + edgeOffset}, {maxX - edgeOffset, minY + edgeOffset, maxZ - edgeOffset}, {maxX - edgeOffset, maxY - edgeOffset, minZ + edgeOffset}, {maxX - edgeOffset, maxY - edgeOffset, maxZ - edgeOffset}};
        double bestDot = -1.7976931348623157E308;
        double bestX = 0.0;
        double bestY = 0.0;
        double bestZ = 0.0;
        for (double[] c : candidates) {
            double dot;
            double dx = c[0] - eyeX;
            double dy = c[1] - eyeY;
            double dz = c[2] - eyeZ;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 0.001 || !((dot = dx / len * lookX + dy / len * lookY + dz / len * lookZ) > bestDot)) continue;
            bestDot = dot;
            bestX = c[0];
            bestY = c[1];
            bestZ = c[2];
        }
        double dx = bestX - eyeX;
        double dy = bestY - eyeY;
        double dz = bestZ - eyeZ;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        float yaw = Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (-Math.toDegrees(Math.atan2(dy, horizontalDist)));
        return new float[]{yaw, pitch};
    }

    private static boolean smoothRotate(int aimTimeMs) {
        if (PearlPhaseFeature.mc.player == null) {
            return true;
        }
        if (Float.isNaN(smoothYaw)) {
            smoothYaw = PearlPhaseFeature.mc.player.m_146908_();
            smoothPitch = PearlPhaseFeature.mc.player.m_146909_();
        }
        int totalTicks = Math.max(1, aimTimeMs / 50);
        float progress = Math.min(1.0f, stateTick / totalTicks);
        float factor = 0.3f;
        smoothYaw += (targetYaw - smoothYaw) * factor;
        smoothPitch += (targetPitch - smoothPitch) * factor;
        PearlPhaseFeature.mc.player.m_146922_(smoothYaw);
        PearlPhaseFeature.mc.player.m_146926_(smoothPitch);
        return progress >= 1.0f || Math.abs(smoothYaw - targetYaw) < 0.1f;
    }

    private static boolean hasPearl() {
        if (PearlPhaseFeature.mc.player == null) {
            return false;
        }
        for (int i = 0; i < PearlPhaseFeature.mc.player.m_150109_().m_6643_(); ++i) {
            ItemStack stack = PearlPhaseFeature.mc.player.m_150109_().m_8020_(i);
            if (stack.m_41619_() || stack.m_41720_() != Items.f_42584_) continue;
            return true;
        }
        return false;
    }

    private static boolean selectPearl() {
        ItemStack stack;
        int i;
        if (PearlPhaseFeature.mc.player == null) {
            return false;
        }
        if (PearlPhaseFeature.mc.player.m_21205_().m_41720_() == Items.f_42584_) {
            return true;
        }
        for (i = 0; i < 9; ++i) {
            stack = PearlPhaseFeature.mc.player.m_150109_().m_8020_(i);
            if (stack.m_41619_() || stack.m_41720_() != Items.f_42584_) continue;
            PearlPhaseFeature.mc.player.m_150109_().f_35977_ = i;
            return true;
        }
        for (i = 9; i < PearlPhaseFeature.mc.player.m_150109_().m_6643_(); ++i) {
            stack = PearlPhaseFeature.mc.player.m_150109_().m_8020_(i);
            if (stack.m_41619_() || stack.m_41720_() != Items.f_42584_) continue;
            PearlPhaseFeature.mc.player.m_150109_().f_35977_ = 0;
            PearlPhaseFeature.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(PearlPhaseFeature.mc.player.m_146908_(), PearlPhaseFeature.mc.player.m_146909_(), PearlPhaseFeature.mc.player.m_20096_()));
            return false;
        }
        return false;
    }

    private static void throwPearl() {
        if (PearlPhaseFeature.mc.player == null || PearlPhaseFeature.mc.f_91072_ == null) {
            return;
        }
        PearlPhaseFeature.mc.f_91072_.m_233721_((Player)PearlPhaseFeature.mc.player, InteractionHand.MAIN_HAND);
        PearlPhaseFeature.mc.player.m_6674_(InteractionHand.MAIN_HAND);
    }

    private static void resetState() {
        state = PhaseState.IDLE;
        stateTick = 0;
        waitTicks = 0;
        smoothYaw = Float.NaN;
        smoothPitch = Float.NaN;
    }

    public static PhaseState getState() {
        return state;
    }

    public static boolean isInside() {
        return state == PhaseState.INSIDE;
    }

    public static void toggle() {
        PearlPhaseConfig cfg = PearlPhaseConfig.getInstance();
        cfg.setEnabled(!cfg.enabled);
        if (!cfg.enabled) {
            PearlPhaseFeature.resetState();
            if (PearlPhaseFeature.mc.player != null) {
                PearlPhaseFeature.mc.player.f_19794_ = false;
            }
            wasInsideBlock = false;
        }
    }

    public static enum PhaseState {
        IDLE,
        AIMING,
        THROWING,
        WAITING,
        INSIDE;

    }
}

