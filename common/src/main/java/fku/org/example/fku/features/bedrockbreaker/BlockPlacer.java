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
            BlockPos clickPos = pos.relative(Direction.UP);
            Vec3 clickLoc = Vec3.atCenterOf((Vec3i)clickPos).add(Vec3.atLowerCornerOf((Vec3i)Direction.DOWN.getNormal()).scale(0.5));
            hit = new BlockHitResult(clickLoc, Direction.DOWN, clickPos, false);
        } else {
            Direction clickFace = BlockPlacer.getDirection(method);
            BlockPos clickPos = pos.relative(clickFace.getOpposite());
            if (clickPos.equals(pos)) {
                for (Direction d : Direction.values()) {
                    BlockPos adjacent = pos.relative(d);
                    if (adjacent.equals(pos)) continue;
                    clickPos = adjacent;
                    clickFace = d.getOpposite();
                    break;
                }
            }
            Vec3 clickLoc = Vec3.atCenterOf((Vec3i)clickPos).add(Vec3.atLowerCornerOf((Vec3i)clickFace.getNormal()).scale(0.5));
            hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);
        }
        return new BlockPlacePlan(hit, method);
    }

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
            BlockPlacer.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(fakeYaw, fakePitch, BlockPlacer.mc.player.onGround()));
            BlockPlacer.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)BlockPlacer.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            BlockPlacer.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, this.hitResult, this.getSequenceNumber()));
            BlockPlacer.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)BlockPlacer.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            return CompletableFuture.completedFuture(null);
        }

        private int getSequenceNumber() {
            if (BlockPlacer.mc.level == null) {
                return 0;
            }
            BlockStatePredictionHandler handler = ((ClientLevelAccessor)BlockPlacer.mc.level).getBlockStatePredictionHandler_CU();
            handler.startPredicting();
            int num = handler.currentSequence();
            handler.close();
            return num;
        }
    }
}

