package fku.org.example.fku.features.bedrockbreaker;

import fku.org.example.fku.features.bedrockbreaker.BlockPlacingMethod;
import fku.org.example.fku.mixin.ClientLevelAccessor;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockPlacer {
    private static final Minecraft mc = Minecraft.getInstance();

    public static BlockPlacePlan createPacketPlan(BlockPos pos, BlockPlacingMethod method) {
        BlockHitResult hit;
        if (method == BlockPlacingMethod.FROM_HORIZONTAL) {
            BlockPos clickPos = pos.m_121945_(Direction.UP);
            Vec3 clickLoc = Vec3.m_82512_((Vec3i)clickPos).add(Vec3.m_82528_((Vec3i)Direction.DOWN.m_122436_()).scale(0.5));
            hit = new BlockHitResult(clickLoc, Direction.DOWN, clickPos, false);
        } else {
            Direction clickFace = BlockPlacer.getDirection(method);
            BlockPos clickPos = pos.m_121945_(clickFace.m_122424_());
            if (clickPos.equals(pos)) {
                for (Direction d : Direction.values()) {
                    BlockPos adjacent = pos.m_121945_(d);
                    if (adjacent.equals(pos)) continue;
                    clickPos = adjacent;
                    clickFace = d.m_122424_();
                    break;
                }
            }
            Vec3 clickLoc = Vec3.m_82512_((Vec3i)clickPos).add(Vec3.m_82528_((Vec3i)clickFace.m_122436_()).scale(0.5));
            hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);
        }
        return new BlockPlacePlan(hit, method);
    }

    private static Direction getDirection(BlockPlacingMethod method) {
        return switch (method) {
            case BlockPlacingMethod.FACING_TOP -> Direction.UP;
            case BlockPlacingMethod.FACING_BOTTOM -> Direction.DOWN;
            case BlockPlacingMethod.FACING_NORTH -> Direction.NORTH;
            case BlockPlacingMethod.FACING_SOUTH -> Direction.SOUTH;
            case BlockPlacingMethod.FACING_EAST -> Direction.EAST;
            case BlockPlacingMethod.FACING_WEST -> Direction.WEST;
            default -> Direction.UP;
        };
    }

    public static class BlockPlacePlan {
        private final BlockHitResult hitResult;
        private final BlockPlacingMethod method;

        public BlockPlacePlan(BlockHitResult hitResult, BlockPlacingMethod method) {
            this.hitResult = hitResult;
            this.method = method;
        }

        public BlockHitResult getHitResult() {
            return this.hitResult;
        }

        public CompletableFuture<Void> apply(float fakeYaw, float fakePitch) {
            if (BlockPlacer.mc.player == null) {
                return CompletableFuture.completedFuture(null);
            }
            BlockPlacer.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(fakeYaw, fakePitch, BlockPlacer.mc.player.m_20096_()));
            BlockPlacer.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)BlockPlacer.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            BlockPlacer.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, this.hitResult, this.getSequenceNumber()));
            BlockPlacer.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)BlockPlacer.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            return CompletableFuture.completedFuture(null);
        }

        private int getSequenceNumber() {
            if (BlockPlacer.mc.f_91073_ == null) {
                return 0;
            }
            BlockStatePredictionHandler handler = ((ClientLevelAccessor)BlockPlacer.mc.f_91073_).getBlockStatePredictionHandler_CU();
            handler.m_233855_();
            int num = handler.m_233871_();
            handler.close();
            return num;
        }
    }
}

