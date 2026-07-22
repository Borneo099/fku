package fku.org.example.fku.features.knockback;

import fku.org.example.fku.features.knockback.KnockbackConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnockbackDirectionCalculator {
    private static final Minecraft mc = Minecraft.getInstance();

    public static float calculateYaw(LivingEntity player, LivingEntity target, String mode) {
        KnockbackConfig config = KnockbackConfig.getInstance();
        switch (mode) {
            case "PULLBACK": {
                return KnockbackDirectionCalculator.calculatePullbackYaw(player, target);
            }
            case "PUSHBACK": {
                return KnockbackDirectionCalculator.calculatePushbackYaw(player, target);
            }
            case "CLIFF": {
                return KnockbackDirectionCalculator.calculateCliffYaw(player, target, config.cliffSearchRadius);
            }
            case "CUSTOM": {
                return (player.getYRot() + config.customYaw) % 360.0f;
            }
        }
        return player.getYRot();
    }

    private static float calculatePullbackYaw(LivingEntity player, LivingEntity target) {
        double wx = player.getX() - target.getX();
        double wz = player.getZ() - target.getZ();
        return (float)Math.toDegrees(Math.atan2(-wx, wz));
    }

    private static float calculatePushbackYaw(LivingEntity player, LivingEntity target) {
        double wx = target.getX() - player.getX();
        double wz = target.getZ() - player.getZ();
        return (float)Math.toDegrees(Math.atan2(-wx, wz));
    }

    private static float calculateCliffYaw(LivingEntity player, LivingEntity target, int radius) {
        ClientLevel level = KnockbackDirectionCalculator.mc.level;
        if (level == null) {
            return player.getYRot();
        }
        int tx = (int)Math.floor(target.getX());
        int tz = (int)Math.floor(target.getZ());
        int ty = (int)Math.floor(target.getY());
        int minY = level.getMinBuildHeight();
        double bestDist = Double.MAX_VALUE;
        float bestAngle = player.getYRot();
        for (int dz = -radius; dz <= radius; ++dz) {
            for (int dx = -radius; dx <= radius; ++dx) {
                double dist;
                if (dx == 0 && dz == 0) continue;
                int bx = tx + dx;
                int bz = tz + dz;
                int lastSolidY = Integer.MIN_VALUE;
                boolean hasCliff = false;
                for (int y = ty; y >= minY; --y) {
                    BlockPos checkPos = new BlockPos(bx, y, bz);
                    BlockState state = level.getBlockState(checkPos);
                    if (y == minY && state.isAir()) {
                        hasCliff = true;
                        break;
                    }
                    if (state.isAir()) {
                        int gap;
                        if (lastSolidY == Integer.MIN_VALUE || (gap = lastSolidY - y) < 2) continue;
                        hasCliff = true;
                        break;
                    }
                    lastSolidY = y;
                }
                if (!hasCliff || !((dist = Math.sqrt(dx * dx + dz * dz)) < bestDist)) continue;
                bestDist = dist;
                double angleDeg = Math.toDegrees(Math.atan2(-dx, dz));
                if (angleDeg < 0.0) {
                    angleDeg += 360.0;
                }
                bestAngle = (float)angleDeg;
            }
        }
        return bestAngle;
    }

    public static float calculatePitch(LivingEntity player) {
        return player.getXRot();
    }
}

