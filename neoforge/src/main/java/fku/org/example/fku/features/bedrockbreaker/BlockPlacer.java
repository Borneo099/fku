package fku.org.example.fku.features.bedrockbreaker; /* water */

import fku.org.example.fku.mixin.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

/**
 * 方块放置器 —— 严格参考 CheatUtils BlockPlacer / BlockPlacePlan
 * (https://github.com/Zergatul/cheatutils)
 *
 * 职责：
 *   根据目标位置和放置方式，构造 BlockHitResult。
 *   apply() 实际发送 ServerboundUseItemOnPacket 并返回 Future。
 *
 * 关键逻辑：
 *   要使方块放置在 pos 处且朝向 dir，需要：
 *   点击 pos.relative(dir.getOpposite()) 的 dir 面
 *   → 新方块位置 = clickedPos.relative(clickedFace) = pos
 */
public class BlockPlacer {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * 创建放置计划
     * @param pos 目标方块位置
     * @param method 放置方式（决定朝向）
     * @return 放置计划，或 null（如果无法计算）
     */
    public static BlockPlacePlan createPacketPlan(BlockPos pos, BlockPlacingMethod method) {
        BlockHitResult hit;

        if (method == BlockPlacingMethod.FROM_HORIZONTAL) {
            // 从上方放置：点击目标位置上方方块的下表面
            BlockPos clickPos = pos.relative(Direction.UP);
            Vec3 clickLoc = Vec3.atCenterOf(clickPos)
                    .add(Vec3.atLowerCornerOf(Direction.DOWN.getNormal()).scale(0.5));
            hit = new BlockHitResult(clickLoc, Direction.DOWN, clickPos, false);
        } else {
            // 朝向放置：点击目标位置反方向方块的目标面
            Direction clickFace = getDirection(method);
            BlockPos clickPos = pos.relative(clickFace.getOpposite());

            // 保护：防止点击位置与目标位置重叠
            if (clickPos.equals(pos)) {
                for (Direction d : Direction.values()) {
                    BlockPos adjacent = pos.relative(d);
                    if (!adjacent.equals(pos)) {
                        clickPos = adjacent;
                        clickFace = d.getOpposite();
                        break;
                    }
                }
            }

            Vec3 clickLoc = Vec3.atCenterOf(clickPos)
                    .add(Vec3.atLowerCornerOf(clickFace.getNormal()).scale(0.5));
            hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);
        }

        return new BlockPlacePlan(hit, method);
    }

    /**
     * 将 BlockPlacingMethod 枚举值映射为 Direction
     */
    private static Direction getDirection(BlockPlacingMethod method) {
        return switch (method) {
            case FACING_TOP -> Direction.UP;
            case FACING_BOTTOM -> Direction.DOWN;
            case FACING_NORTH -> Direction.NORTH;
            case FACING_SOUTH -> Direction.SOUTH;
            case FACING_EAST -> Direction.EAST;
            case FACING_WEST -> Direction.WEST;
            default -> Direction.UP;
        };
    }

    /**
     * 放置计划 —— 封装 BlockHitResult 和 BlockPlacingMethod，
     *   apply() 先发送旋转包再发送放置包
     *
     * 严格参考 CheatUtils BlockPlacePlan：
     *   apply() 发送旋转包 + ServerboundUseItemOnPacket 并返回 CompletableFuture
     *   （同步放置模式，Future 在发包完成后立即完成）
     */
    public static class BlockPlacePlan {
        private final BlockHitResult hitResult;
        private final BlockPlacingMethod method;

        public BlockPlacePlan(BlockHitResult hitResult, BlockPlacingMethod method) {
            this.hitResult = hitResult;
            this.method = method;
        }

        public BlockHitResult getHitResult() {
            return hitResult;
        }

        /**
         * ★ 发送假旋转包，然后发送放置包。
         *   客户端视角不受影响，仅服务端朝向改变。其他玩家看到的是假旋转朝向。
         *
         * @param fakeYaw  假Yaw（服务端朝向）
         * @param fakePitch 假Pitch（服务端朝向）
         */
        public CompletableFuture<Void> apply(float fakeYaw, float fakePitch) {
            if (mc.player == null) return CompletableFuture.completedFuture(null);

            // ★ 发送假旋转包：客户端视角不受影响，仅服务端朝向改变
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                    fakeYaw, fakePitch, mc.player.onGround()));

            // ★ 下蹲+右键放置方块，避免打开容器 GUI
            mc.player.connection.send(new ServerboundPlayerCommandPacket(
                    mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            mc.player.connection.send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND,
                    hitResult,
                    getSequenceNumber()));
            mc.player.connection.send(new ServerboundPlayerCommandPacket(
                    mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            return CompletableFuture.completedFuture(null);
        }

        /**
         * ★ 获取预测序列号 —— 严格参考 CheatUtils
         *   使用 BlockStatePredictionHandler.currentSequence()，
         *   确保服务端能正确匹配客户端预测，否则数据包会被服务端拒绝。
         */
        private int getSequenceNumber() {
            if (mc.level == null) return 0;
            BlockStatePredictionHandler handler = ((ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
            handler.startPredicting();
            int num = handler.currentSequence();
            handler.close();
            return num;
        }
    }
}