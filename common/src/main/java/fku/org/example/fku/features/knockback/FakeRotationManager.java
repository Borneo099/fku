package fku.org.example.fku.features.knockback;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FakeRotationManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final double POS_OFFSET = 0.3;
    private static boolean active = false;
    private static double origX;
    private static double origY;
    private static double origZ;
    private static float origYaw;
    private static float origPitch;
    private static int restoreTimer;
    private static final int RESTORE_DELAY_TICKS = 2;
    private static boolean pending;
    private static double pendingX;
    private static double pendingY;
    private static double pendingZ;
    private static float pendingYaw;
    private static float pendingPitch;
    private static boolean pendingOnGround;

    public static void setPending(LivingEntity target, float targetYaw) {
        LocalPlayer player = FakeRotationManager.mc.player;
        if (player == null) {
            return;
        }
        if (!active) {
            origX = player.getX();
            origY = player.getY();
            origZ = player.getZ();
            origYaw = player.m_146908_();
            origPitch = player.m_146909_();
            active = true;
        }
        float yawRad = Math.toRadians(targetYaw);
        pendingX = target.getX() + 0.3 * Math.sin(yawRad);
        pendingZ = target.getZ() - 0.3 * Math.cos(yawRad);
        pendingY = player.getY();
        pendingYaw = targetYaw;
        pendingPitch = player.m_146909_();
        pendingOnGround = player.m_20096_();
        pending = true;
    }

    public static boolean hasPending() {
        return pending;
    }

    public static void clearPending() {
        pending = false;
    }

    public static ServerboundMovePlayerPacket.PosRot createPendingPacket() {
        return new ServerboundMovePlayerPacket.PosRot(pendingX, pendingY, pendingZ, pendingYaw, pendingPitch, pendingOnGround);
    }

    public static void enable(LivingEntity target, float targetYaw) {
        LocalPlayer player = FakeRotationManager.mc.player;
        if (player == null) {
            return;
        }
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        if (!active) {
            origX = playerPos.x;
            origY = playerPos.y;
            origZ = playerPos.z;
            origYaw = player.m_146908_();
            origPitch = player.m_146909_();
            active = true;
        }
        float yawRad = Math.toRadians(targetYaw);
        double fakedX = targetPos.x + 0.3 * Math.sin(yawRad);
        double fakedZ = targetPos.z - 0.3 * Math.cos(yawRad);
        double fakedY = playerPos.y;
        player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(fakedX, fakedY, fakedZ, targetYaw, player.m_146909_(), player.m_20096_()));
        restoreTimer = 2;
    }

    public static void disable() {
        if (!active) {
            return;
        }
        active = false;
        restoreTimer = 0;
        LocalPlayer player = FakeRotationManager.mc.player;
        if (player == null) {
            return;
        }
        player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.PosRot(origX, origY, origZ, origYaw, origPitch, player.m_20096_()));
    }

    public static void tick() {
        if (restoreTimer > 0 && --restoreTimer == 0) {
            FakeRotationManager.disable();
        }
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isRestoring() {
        return restoreTimer > 0;
    }

    static {
        restoreTimer = 0;
        pending = false;
    }
}

