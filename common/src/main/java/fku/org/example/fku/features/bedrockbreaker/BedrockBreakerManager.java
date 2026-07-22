package fku.org.example.fku.features.bedrockbreaker;

import fku.org.example.fku.features.bedrockbreaker.BedrockBreakerConfig;
import fku.org.example.fku.features.bedrockbreaker.BlockPlacer;
import fku.org.example.fku.features.bedrockbreaker.BlockPlacingMethod;
import fku.org.example.fku.features.bedrockbreaker.Rotation;
import fku.org.example.fku.mixin.ClientLevelAccessor;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class BedrockBreakerManager {
    private static final BedrockBreakerManager INSTANCE = new BedrockBreakerManager();
    private final Minecraft mc = Minecraft.getInstance();
    private final Queue<BlockPos> queue = new ArrayDeque<BlockPos>();
    private BlockPos bedrockPos;
    private Direction pistonDirection;
    private Direction pistonFacing;
    private BlockPos pistonPos;
    private float blockDestroyProgress;
    private int blockDestroySeqNumber;
    private BlockPos leverPos;
    private BlockHitResult leverPlaceHitResult;
    private State state = State.INIT;
    private int tickCount;
    private float fakeYaw;
    private float fakePitch;
    private BlockPos cleanupPistonPos;
    private int cleanupPistonSeq;
    private boolean reverseRotSent;
    private int cleanupPistonTicks = 0;
    private int predictedContainerStateId = -1;
    private final List<BlockPos> helperBlockPositions = new ArrayList<BlockPos>();

    public static BedrockBreakerManager getInstance() {
        return INSTANCE;
    }

    private BedrockBreakerManager() {
    }

    public void process() {
        if (this.mc.player == null || this.mc.level == null || this.mc.hitResult == null) {
            return;
        }
        if (this.mc.hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = ((BlockHitResult)this.mc.hitResult).getBlockPos();
        if (!this.isValidBlock(pos)) {
            return;
        }
        if (this.bedrockPos != null && this.bedrockPos.equals(pos)) {
            return;
        }
        if (this.state == State.INIT) {
            this.start(pos);
        } else {
            this.queue.add(pos.immutable());
        }
    }

    public void processNearby() {
        int maxY;
        if (this.mc.player == null || this.mc.level == null) {
            return;
        }
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        int range = cfg.autoFindRange > 0 ? cfg.autoFindRange : 5;
        BlockPos playerPos = this.mc.player.blockPosition();
        int minY = Math.max(this.mc.level.getMinBuildHeight(), playerPos.getY() - range);
        for (int y = maxY = Math.min(this.mc.level.getMaxBuildHeight(), playerPos.getY() + range); y >= minY; --y) {
            boolean layerHasTarget = false;
            ArrayList<BlockPos> layerTargets = new ArrayList<BlockPos>();
            for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; ++x) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!this.isValidBlock(pos)) continue;
                    layerHasTarget = true;
                    layerTargets.add(pos.immutable());
                }
            }
            if (!layerHasTarget) continue;
            Vec3 eyePos = this.mc.player.getEyePosition(1.0f);
            layerTargets.sort(Comparator.comparingDouble(p -> p.distToCenterSqr((Position)eyePos)));
            for (BlockPos pos : layerTargets) {
                if (pos.equals(this.bedrockPos) || this.queue.contains(pos)) continue;
                this.queue.add(pos);
            }
        }
    }

    public String getStatus() {
        return switch (this.state) {
            case BREAK_PISTON_PROGRESS -> this.state + " " + Math.round(this.blockDestroyProgress * 100.0f) + "%";
            case BREAK_REMAINING_PISTON_PROGRESS -> this.state + " " + Math.round(this.blockDestroyProgress * 100.0f) + "%";
            default -> this.state.toString();
        };
    }

    public boolean isRunning() {
        return this.state != State.INIT;
    }

    public void stop() {
        this.reset("\u624b\u52a8\u4e2d\u6b62");
    }

    public void tick() {
        if (this.mc.player == null || this.mc.level == null) {
            return;
        }
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        if (!cfg.enabled) {
            if (this.state != State.INIT || !this.queue.isEmpty()) {
                this.reset("\u529f\u80fd\u5df2\u5173\u95ed", false);
            }
            return;
        }
        if (this.cleanupPistonPos != null) {
            ++this.cleanupPistonTicks;
            if (this.cleanupPistonTicks == 1) {
                this.cleanupPistonSeq = this.getSequenceNumber();
                this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.cleanupPistonPos, Direction.DOWN, this.cleanupPistonSeq));
            } else if (this.cleanupPistonTicks >= 3) {
                this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.cleanupPistonPos, Direction.DOWN, this.cleanupPistonSeq));
                this.mc.level.destroyBlock(this.cleanupPistonPos, false);
                this.cleanupPistonPos = null;
                this.cleanupPistonTicks = 0;
            }
            return;
        }
        if (this.state == State.INIT) {
            if (cfg.scanMode) {
                this.processNearby();
            }
            if (!this.queue.isEmpty()) {
                this.start(this.queue.remove());
            }
        }
        if (this.bedrockPos == null) {
            return;
        }
        ++this.tickCount;
        this.state.handle(this);
    }

    private void handleStart() {
        PlacementCandidate best;
        assert (this.mc.player != null && this.mc.level != null);
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        for (Direction face : Direction.values()) {
            BlockPos adjacentPos = this.bedrockPos.relative(face);
            BlockState adjState = this.mc.level.getBlockState(adjacentPos);
            if (adjState.getBlock() != Blocks.LEVER || !((Boolean)adjState.getValue((Property)LeverBlock.POWERED)).booleanValue()) continue;
            BlockHitResult leverHit = new BlockHitResult(Vec3.atCenterOf((Vec3i)adjacentPos).add(Vec3.atLowerCornerOf((Vec3i)face.getOpposite().getNormal()).scale(0.5)), face.getOpposite(), adjacentPos, false);
            this.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, leverHit, this.getSequenceNumber()));
            break;
        }
        if ((best = this.findBestPistonPlacement()) == null) {
            if (cfg.enableHelperBlocks) {
                Direction[] bodyDirs = this.sortByDistance(this.bedrockPos, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
                for (Direction d : bodyDirs = this.sortByPriority(bodyDirs)) {
                    if (!this.canPlacePiston(this.bedrockPos, d)) continue;
                    this.pistonDirection = d;
                    this.pistonFacing = d;
                    this.pistonPos = this.bedrockPos.relative(d);
                    break;
                }
                if (this.pistonDirection != null && this.tryPlaceHelperBlocks()) {
                    best = this.findBestPistonPlacement();
                }
            }
            if (best == null) {
                this.reset("\u627e\u4e0d\u5230\u653e\u7f6e\u6d3b\u585e\u7684\u4f4d\u7f6e");
                return;
            }
        }
        this.pistonDirection = best.bodyDir;
        this.pistonFacing = best.facing;
        this.pistonPos = best.pistonPos;
        this.leverPos = best.leverPos;
        this.leverPlaceHitResult = best.leverHit;
        int pistonSlot = best.facing.getOpposite().getAxis() == Direction.Axis.Y ? this.ensureInHotbar(Items.PISTON) : this.findPistonSlot();
        if (pistonSlot < 0) {
            this.reset("\u80cc\u5305\u627e\u4e0d\u5230\u6d3b\u585e");
            return;
        }
        if (best.facing.getAxis() == Direction.Axis.Y) {
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            this.calculateFakeRotation(method);
            Vec3 clickLoc = Vec3.atCenterOf((Vec3i)this.bedrockPos).add(Vec3.atLowerCornerOf((Vec3i)best.bodyDir.getNormal()).scale(0.5));
            BlockHitResult hit = new BlockHitResult(clickLoc, best.bodyDir, this.bedrockPos, false);
            this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
            this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.onGround()));
            this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
            this.state = State.PLACE_LEVER;
        } else {
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            this.calculateFakeRotation(method);
            this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.onGround()));
            this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
            this.state = State.WAIT_Y_HEAD_ROT_SYNC;
        }
    }

    private void handlePlaceLever() {
        assert (this.mc.player != null && this.mc.level != null);
        int leverSlot = this.ensureInHotbar(Items.LEVER);
        if (leverSlot < 0) {
            this.reset("\u80cc\u5305\u627e\u4e0d\u5230\u62c9\u6746");
            return;
        }
        this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(leverSlot));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, this.leverPlaceHitResult, this.getSequenceNumber());
        this.state = State.BREAK_PISTON_START;
        this.state.handle(this);
    }

    private void handleWaitYHeadRotSync() {
        assert (this.mc.level != null && this.mc.player != null);
        BlockPlacingMethod method = BlockPlacingMethod.facing(this.pistonFacing);
        this.calculateFakeRotation(method);
        Vec3 clickLoc = Vec3.atCenterOf((Vec3i)this.bedrockPos).add(Vec3.atLowerCornerOf((Vec3i)this.pistonDirection.getNormal()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, this.pistonDirection, this.bedrockPos, false);
        this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.onGround()));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
        this.state = State.PLACE_LEVER;
    }

    private void handleBreakPistonStart() {
        int pickaxeSlot;
        Direction reverseFacing;
        BlockPlacingMethod revMethod;
        Rotation revRot;
        assert (this.mc.level != null && this.mc.player != null);
        this.tickCount = 0;
        if (this.pistonDirection.getOpposite().getAxis() != Direction.Axis.Y && (revRot = (revMethod = BlockPlacingMethod.facing(reverseFacing = this.pistonDirection.getOpposite())).getTargetRotation()) != null) {
            float revYaw = Float.isNaN(revRot.yRot()) ? this.mc.player.getYRot() : revRot.yRot();
            float revPitch = Float.isNaN(revRot.xRot()) ? this.mc.player.getXRot() : revRot.xRot();
            this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(revYaw, revPitch, this.mc.player.onGround()));
        }
        if ((pickaxeSlot = this.findPickaxe()) >= 0) {
            this.mc.player.getInventory().selected = pickaxeSlot;
        }
        this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().selected));
        this.blockDestroyProgress = this.getPistonDestroyProgress();
        this.blockDestroySeqNumber = this.getSequenceNumber();
        this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.pistonPos, Direction.UP, this.blockDestroySeqNumber));
        this.state = State.BREAK_PISTON_PROGRESS;
        this.tickCount = 0;
    }

    private void handleBreakPistonProgress() {
        assert (this.mc.level != null && this.mc.player != null);
        this.blockDestroyProgress += this.getPistonDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)this.leverPos), Direction.UP, this.leverPos, false), this.getSequenceNumber()));
            this.state = State.WAIT_PISTON_EXTEND;
            this.tickCount = 0;
        } else if (this.tickCount > 50) {
            this.reset("\u6316\u6398\u6d3b\u585e\u8d85\u65f6\uff08\u9700\u8981\u9550\u5b50 + \u6025\u8feb\u6548\u679c\uff09");
        }
    }

    private void handleWaitPistonExtend() {
        assert (this.mc.player != null && this.mc.level != null);
        Direction actualPistonDir = null;
        for (Direction d : Direction.values()) {
            if (this.mc.level.getBlockState(this.pistonPos.relative(d)).getBlock() != Blocks.PISTON_HEAD) continue;
            actualPistonDir = d;
            break;
        }
        if (actualPistonDir != null) {
            if (!this.reverseRotSent) {
                Direction reverseFacing = this.pistonDirection.getOpposite();
                BlockPlacingMethod revMethod = BlockPlacingMethod.facing(reverseFacing);
                Rotation revRot = revMethod.getTargetRotation();
                if (revRot != null) {
                    float revYaw = Float.isNaN(revRot.yRot()) ? this.mc.player.getYRot() : revRot.yRot();
                    float revPitch = Float.isNaN(revRot.xRot()) ? this.mc.player.getXRot() : revRot.xRot();
                    this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(revYaw, revPitch, this.mc.player.onGround()));
                }
                this.reverseRotSent = true;
                return;
            }
            this.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf((Vec3i)this.leverPos), Direction.UP, this.leverPos, false), this.getSequenceNumber()));
            this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.pistonPos, Direction.UP, this.blockDestroySeqNumber));
            this.state = State.PLACE_REVERSE_PISTON;
            this.state.handle(this);
        } else if (this.tickCount > 10) {
            this.reset("\u7b49\u5f85\u6d3b\u585e\u4f38\u51fa\u8d85\u65f6\uff08\u6d3b\u585e\u5934\u672a\u68c0\u6d4b\u5230\uff09");
        }
    }

    private void handlePlaceReversePiston() {
        assert (this.mc.level != null && this.mc.player != null);
        int pistonSlot = this.pistonDirection.getAxis() == Direction.Axis.Y ? this.ensureInHotbar(Items.PISTON) : this.findPistonSlot();
        if (pistonSlot < 0) {
            this.reset(this.pistonDirection.getAxis() == Direction.Axis.Y ? "\u80cc\u5305\u627e\u4e0d\u5230\u666e\u901a\u6d3b\u585e\uff08\u53cd\u5411\u5782\u76f4\u9700\u8981\u666e\u901a\u6d3b\u585e\uff09" : "\u80cc\u5305\u627e\u4e0d\u5230\u53ef\u7528\u6d3b\u585e");
            return;
        }
        Direction reverseFacing = this.pistonDirection.getOpposite();
        BlockPos clickPos = this.bedrockPos;
        Direction clickFace = this.pistonDirection;
        Vec3 clickLoc = Vec3.atCenterOf((Vec3i)clickPos).add(Vec3.atLowerCornerOf((Vec3i)clickFace.getNormal()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);
        BlockPlacingMethod method = BlockPlacingMethod.facing(reverseFacing);
        this.calculateFakeRotation(method);
        this.mc.player.connection.send((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.onGround()));
        this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
        this.mc.level.destroyBlock(this.pistonPos, false);
        this.mc.level.destroyBlock(this.pistonPos.relative(this.pistonFacing), false);
        this.state = State.WAIT_BEDROCK_BREAK;
        this.tickCount = 0;
    }

    private void handleWaitBedrockBreak() {
        assert (this.mc.player != null && this.mc.level != null);
        if (this.mc.level.getBlockState(this.bedrockPos).isAir() && !this.mc.level.getBlockState(this.pistonPos.relative(this.pistonFacing)).is(Blocks.MOVING_PISTON)) {
            BlockPlacer.BlockPlacePlan repPlan;
            int repSlot;
            Block replaceBlock;
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            if (cfg.replaceBlockId != null && !cfg.replaceBlockId.isEmpty() && (replaceBlock = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(cfg.replaceBlockId))) != null && replaceBlock != Blocks.AIR && (repSlot = this.findItem(replaceBlock.asItem())) >= 0 && (repPlan = BlockPlacer.createPacketPlan(this.bedrockPos, BlockPlacingMethod.FROM_HORIZONTAL)) != null) {
                this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(repSlot));
                repPlan.apply(this.mc.player.getYRot(), this.mc.player.getXRot());
            }
            if (cfg.cleanupHelpers && !this.helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : this.helperBlockPositions) {
                    if (this.mc.level.getBlockState(helperPos).isAir()) continue;
                    this.mineBlock(helperPos);
                }
                this.helperBlockPositions.clear();
            }
            this.state = this.mc.level.getBlockState(this.leverPos).is(Blocks.LEVER) ? State.BREAK_REMAINING_LEVER_START : State.BREAK_REMAINING_PISTON_START;
            this.state.handle(this);
            this.tickCount = 0;
        } else if (this.tickCount > 20) {
            this.mineBlock(this.pistonPos);
            if (this.leverPos != null) {
                this.mineBlock(this.leverPos);
            }
            if (!this.helperBlockPositions.isEmpty()) {
                this.helperBlockPositions.clear();
            }
            this.reset("\u7b49\u5f85\u57fa\u5ca9\u7834\u574f\u8d85\u65f6");
        }
    }

    private void handleBreakRemainingLeverStart() {
        assert (this.mc.player != null);
        this.blockDestroyProgress = this.getLeverDestroyProgress();
        this.blockDestroySeqNumber = this.getSequenceNumber();
        this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.leverPos, Direction.DOWN, this.blockDestroySeqNumber));
        this.state = State.BREAK_REMAINING_LEVER_PROGRESS;
    }

    private void handleBreakRemainingLeverProgress() {
        assert (this.mc.player != null);
        this.blockDestroyProgress += this.getLeverDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.leverPos, Direction.DOWN, this.blockDestroySeqNumber));
            this.state = State.BREAK_REMAINING_PISTON_START;
            this.state.handle(this);
        } else if (this.tickCount > 30) {
            this.state = State.BREAK_REMAINING_PISTON_START;
            this.state.handle(this);
        }
    }

    private void handleBreakRemainingPistonStart() {
        assert (this.mc.player != null);
        int pickaxeSlot = this.findPickaxe();
        if (pickaxeSlot >= 0) {
            this.mc.player.getInventory().selected = pickaxeSlot;
        }
        this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().selected));
        this.blockDestroyProgress = this.getPistonDestroyProgress();
        this.blockDestroySeqNumber = this.getSequenceNumber();
        this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.pistonPos, Direction.DOWN, this.blockDestroySeqNumber));
        this.state = State.BREAK_REMAINING_PISTON_PROGRESS;
        this.tickCount = 0;
    }

    private void handleBreakRemainingPistonProgress() {
        assert (this.mc.player != null && this.mc.level != null);
        this.blockDestroyProgress += this.getPistonDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.pistonPos, Direction.DOWN, this.blockDestroySeqNumber));
            this.mc.level.destroyBlock(this.pistonPos, false);
            BlockPos pistonHeadPos = this.pistonPos.relative(this.pistonFacing);
            if (!this.mc.level.getBlockState(pistonHeadPos).isAir()) {
                this.mc.level.destroyBlock(pistonHeadPos, false);
                this.mineBlock(pistonHeadPos);
            }
            if (this.leverPos != null && this.mc.level.getBlockState(this.leverPos).is(Blocks.LEVER)) {
                this.mineBlock(this.leverPos);
            }
            this.reset(null);
            if (!this.queue.isEmpty()) {
                this.start(this.queue.remove());
            }
        } else if (this.tickCount > 50) {
            this.reset("\u6e05\u7406\u5269\u4f59\u6d3b\u585e\u8d85\u65f6");
        }
    }

    private boolean findLocationForLever() {
        BlockPos possibleLeverPos;
        assert (this.mc.level != null && this.mc.player != null);
        this.leverPos = null;
        BlockPos pistonHeadPos_ = this.pistonPos.relative(this.pistonFacing);
        for (Direction direction : this.sortByDistance(this.bedrockPos, Direction.values())) {
            BlockHitResult hit = null;
            BlockState leverBlockState;
            possibleLeverPos = this.bedrockPos.relative(direction);
            if (direction == this.pistonDirection || possibleLeverPos.equals(pistonHeadPos_) || !this.mc.level.getBlockState(possibleLeverPos).canBeReplaced() || !this.mc.level.getFluidState(possibleLeverPos).isEmpty() || !this.isValidY(possibleLeverPos.getY()) || this.isInvalidLeverSupport(this.bedrockPos) || (leverBlockState = Blocks.LEVER.getStateForPlacement(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.LEVER, 1), hit = new BlockHitResult(Vec3.atCenterOf((Vec3i)this.bedrockPos).add(Vec3.atLowerCornerOf((Vec3i)direction.getNormal()).scale(0.5)), direction, this.bedrockPos, false)))) == null || !this.isLeverStateMatch(leverBlockState, direction)) continue;
            this.leverPos = possibleLeverPos;
            this.leverPlaceHitResult = hit;
            return true;
        }
        for (Direction direction : this.sortByDistance(this.pistonPos, Direction.values())) {
            if (direction == this.pistonDirection || direction == this.pistonDirection.getOpposite() || direction == this.pistonFacing || !this.mc.level.getBlockState(possibleLeverPos = this.pistonPos.relative(direction)).canBeReplaced() || !this.mc.level.getFluidState(possibleLeverPos).isEmpty() || !this.isValidY(possibleLeverPos.getY())) continue;
            for (Direction dir : this.sortByDistance(possibleLeverPos, Direction.values())) {
                BlockHitResult hit;
                BlockState leverBlockState;
                BlockPos possibleSupportPos = possibleLeverPos.relative(dir);
                if (possibleSupportPos.equals(this.pistonPos) || this.mc.level.getBlockState(possibleSupportPos).canBeReplaced() || this.isInvalidLeverSupport(possibleSupportPos) || (leverBlockState = Blocks.LEVER.getStateForPlacement(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.LEVER, 1), hit = new BlockHitResult(Vec3.atCenterOf((Vec3i)possibleSupportPos).add(Vec3.atLowerCornerOf((Vec3i)dir.getOpposite().getNormal()).scale(0.5)), dir.getOpposite(), possibleSupportPos, false)))) == null || !this.isLeverStateMatch(leverBlockState, dir.getOpposite())) continue;
                this.leverPos = possibleLeverPos;
                this.leverPlaceHitResult = hit;
                return true;
            }
        }
        BlockPos pistonHeadPos = this.pistonPos.relative(this.pistonFacing);
        for (Direction lateral : this.getPistonHeadLateralDirections()) {
            BlockPos supportPos;
            BlockPos candidatePos = pistonHeadPos.relative(lateral);
            if (!this.mc.level.getBlockState(candidatePos).canBeReplaced() || !this.mc.level.getFluidState(candidatePos).isEmpty() || !this.isValidY(candidatePos.getY()) || (supportPos = this.pistonPos.relative(lateral)).equals(pistonHeadPos) || this.mc.level.getBlockState(supportPos).canBeReplaced() || this.isInvalidLeverSupport(supportPos)) continue;
            Direction clickFace = this.pistonFacing;
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf((Vec3i)supportPos).add(Vec3.atLowerCornerOf((Vec3i)clickFace.getNormal()).scale(0.5)), clickFace, supportPos, false);
            BlockState leverBlockState = Blocks.LEVER.getStateForPlacement(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.LEVER, 1), hit));
            if (leverBlockState == null || !this.isLeverStateMatch(leverBlockState, clickFace)) continue;
            this.leverPos = candidatePos;
            this.leverPlaceHitResult = hit;
            return true;
        }
        return false;
    }

    private boolean isInvalidLeverSupport(BlockPos supportPos) {
        if (this.mc.level == null) {
            return true;
        }
        BlockState state = this.mc.level.getBlockState(supportPos);
        Block block = state.getBlock();
        return block instanceof PistonBaseBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof StairBlock || block instanceof SlabBlock || block instanceof ComposterBlock;
    }

    private boolean isLeverStateMatch(BlockState state, Direction direction) {
        return switch (direction) {
            case UP -> {
                if (state.getValue(LeverBlock.FACE) == AttachFace.FLOOR) {
                    yield true;
                }
                yield false;
            }
            case DOWN -> {
                if (state.getValue(LeverBlock.FACE) == AttachFace.CEILING) {
                    yield true;
                }
                yield false;
            }
            default -> state.getValue(LeverBlock.FACE) == AttachFace.WALL && state.getValue(LeverBlock.FACING) == direction;
        };
    }

    private float getPistonDestroyProgress() {
        assert (this.mc.level != null && this.mc.player != null);
        return Blocks.PISTON.defaultBlockState().getDestroyProgress((Player)this.mc.player, (BlockGetter)this.mc.level, this.pistonPos);
    }

    private float getLeverDestroyProgress() {
        assert (this.mc.level != null && this.mc.player != null);
        return Blocks.LEVER.defaultBlockState().getDestroyProgress((Player)this.mc.player, (BlockGetter)this.mc.level, this.leverPos);
    }

    private void calculateFakeRotation(BlockPlacingMethod method) {
        Rotation targetRot = method.getTargetRotation();
        if (targetRot != null) {
            this.fakeYaw = Float.isNaN(targetRot.yRot()) ? this.mc.player.getYRot() : targetRot.yRot();
            this.fakePitch = Float.isNaN(targetRot.xRot()) ? this.mc.player.getXRot() : targetRot.xRot();
        }
    }

    private boolean isValidBlock(BlockPos pos) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        if (this.mc.level == null) {
            return false;
        }
        if (cfg.allBlocks) {
            return true;
        }
        Block target = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(cfg.targetBlockId));
        return target != null && this.mc.level.getBlockState(pos).is(target);
    }

    private Direction[] getPistonHeadLateralDirections() {
        if (this.pistonFacing == null) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        if (this.pistonFacing.getAxis() == Direction.Axis.Y) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        Direction.Axis axis = this.pistonFacing.getAxis();
        if (axis == Direction.Axis.X) {
            return new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
        }
        return new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
    }

    private boolean tryPlaceHelperBlocks() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        assert (this.mc.level != null && this.mc.player != null);
        String[] blockIds = cfg.helperBlockList.split(",");
        ArrayList<Direction> searchDirs = new ArrayList<Direction>();
        if (this.pistonDirection.getAxis() == Direction.Axis.Y) {
            searchDirs.addAll(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        } else {
            searchDirs.add(Direction.UP);
            searchDirs.add(Direction.DOWN);
            for (Direction d2 : Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                if (d2.getAxis() == this.pistonDirection.getAxis()) continue;
                searchDirs.add(d2);
            }
        }
        searchDirs.sort(Comparator.comparingDouble(d -> this.mc.player.distanceToSqr(Vec3.atCenterOf((Vec3i)this.bedrockPos.relative(d)))));
        for (Direction dir : searchDirs) {
            BlockPos helperPos = this.bedrockPos.relative(dir);
            if (!this.mc.level.getBlockState(helperPos).canBeReplaced() || !this.isValidY(helperPos.getY())) continue;
            boolean placed = false;
            for (String blockId : blockIds) {
                Item helperItem;
                int hotbarSlot;
                Block helperBlock;
                if ((blockId = blockId.trim()).isEmpty() || (helperBlock = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId))) == null || helperBlock == Blocks.AIR || !helperBlock.defaultBlockState().isSolid() || (hotbarSlot = this.ensureInHotbar(helperItem = helperBlock.asItem())) < 0) continue;
                Vec3 clickLoc = Vec3.atCenterOf((Vec3i)this.bedrockPos).add(Vec3.atLowerCornerOf((Vec3i)dir.getNormal()).scale(0.5));
                BlockHitResult hit = new BlockHitResult(clickLoc, dir, this.bedrockPos, false);
                this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(hotbarSlot));
                this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
                this.mc.level.setBlockAndUpdate(helperPos, helperBlock.defaultBlockState());
                placed = true;
                break;
            }
            if (!placed) continue;
            this.helperBlockPositions.add(helperPos.immutable());
            if (this.findLocationForLever() && this.leverPos != null) {
                return true;
            }
            this.mineBlock(helperPos);
            this.helperBlockPositions.remove(helperPos.immutable());
        }
        return false;
    }

    private void sendUseItemOnSneak(InteractionHand hand, BlockHitResult hitResult, int sequence) {
        this.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        this.mc.player.connection.send((Packet)new ServerboundUseItemOnPacket(hand, hitResult, sequence));
        this.mc.player.connection.send((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private int ensureInHotbar(Item item) {
        int slot = this.findItem(item);
        if (slot >= 0) {
            return slot;
        }
        if (this.mc.player == null) {
            return -1;
        }
        Inventory inventory = this.mc.player.getInventory();
        for (int i = 9; i < 36; ++i) {
            if (!inventory.getItem(i).is(item)) continue;
            int targetSlot = -1;
            for (int j = 0; j < 9; ++j) {
                if (!inventory.getItem(j).isEmpty()) continue;
                targetSlot = j;
                break;
            }
            if (targetSlot < 0) {
                targetSlot = inventory.selected;
            }
            int containerSlot = i;
            int hotbarContainerSlot = 36 + targetSlot;
            int serverStateId = this.mc.player.containerMenu.getStateId();
            int stateId = Math.max(serverStateId, this.predictedContainerStateId);
            this.mc.player.connection.send((Packet)new ServerboundContainerClickPacket(0, stateId, containerSlot, targetSlot, ClickType.SWAP, ItemStack.EMPTY, (Int2ObjectMap)new Int2ObjectOpenHashMap(Map.of(containerSlot, inventory.getItem(targetSlot).copy(), hotbarContainerSlot, inventory.getItem(i).copy()))));
            this.predictedContainerStateId = stateId + 1;
            ItemStack temp = inventory.getItem(targetSlot).copy();
            inventory.items.set(targetSlot, inventory.getItem(i).copy());
            inventory.items.set(i, temp);
            inventory.selected = targetSlot;
            return targetSlot;
        }
        return -1;
    }

    private int findItem(Item item) {
        assert (this.mc.player != null);
        Inventory inventory = this.mc.player.getInventory();
        for (int i = 0; i < 9; ++i) {
            if (!inventory.getItem(i).is(item)) continue;
            return i;
        }
        return -1;
    }

    private int findPistonSlot() {
        int slot = this.ensureInHotbar(Items.PISTON);
        if (slot >= 0) {
            return slot;
        }
        slot = this.ensureInHotbar(Items.STICKY_PISTON);
        return slot;
    }

    private int findPistonSlotPreferSticky() {
        int slot = this.ensureInHotbar(Items.STICKY_PISTON);
        if (slot >= 0) {
            return slot;
        }
        slot = this.ensureInHotbar(Items.PISTON);
        return slot;
    }

    private int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < 36; ++i) {
            if (!this.mc.player.getInventory().getItem(i).is(item)) continue;
            count += this.mc.player.getInventory().getItem(i).getCount();
        }
        return count;
    }

    private int findPickaxe() {
        int i;
        assert (this.mc.player != null);
        Inventory inventory = this.mc.player.getInventory();
        for (i = 0; i < 9; ++i) {
            if (!inventory.getItem(i).getTags().anyMatch(tag -> tag == ItemTags.PICKAXES)) continue;
            return i;
        }
        for (i = 9; i < 36; ++i) {
            if (!inventory.getItem(i).getTags().anyMatch(tag -> tag == ItemTags.PICKAXES)) continue;
            int targetSlot = -1;
            for (int j = 0; j < 9; ++j) {
                if (!inventory.getItem(j).isEmpty()) continue;
                targetSlot = j;
                break;
            }
            if (targetSlot < 0) {
                targetSlot = inventory.selected;
            }
            int containerSlot = i;
            int hotbarContainerSlot = 36 + targetSlot;
            int serverStateId = this.mc.player.containerMenu.getStateId();
            int stateId = Math.max(serverStateId, this.predictedContainerStateId);
            this.mc.player.connection.send((Packet)new ServerboundContainerClickPacket(0, stateId, containerSlot, targetSlot, ClickType.SWAP, ItemStack.EMPTY, (Int2ObjectMap)new Int2ObjectOpenHashMap(Map.of(containerSlot, inventory.getItem(targetSlot).copy(), hotbarContainerSlot, inventory.getItem(i).copy()))));
            this.predictedContainerStateId = stateId + 1;
            ItemStack temp = inventory.getItem(targetSlot).copy();
            inventory.items.set(targetSlot, inventory.getItem(i).copy());
            inventory.items.set(i, temp);
            inventory.selected = targetSlot;
            return targetSlot;
        }
        return -1;
    }

    private void mineBlock(BlockPos pos) {
        if (this.mc.player == null || this.mc.level == null) {
            return;
        }
        if (this.mc.level.getBlockState(pos).isAir()) {
            return;
        }
        int seq = this.getSequenceNumber();
        this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        this.mc.player.connection.send((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        this.mc.level.destroyBlock(pos, false);
    }

    private int getSequenceNumber() {
        assert (this.mc.level != null);
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)this.mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    private boolean isValidY(int y) {
        assert (this.mc.level != null);
        DimensionType dimension = this.mc.level.dimensionType();
        return dimension.minY() <= y && y < dimension.minY() + dimension.height();
    }

    private Direction[] sortByDistance(BlockPos origin, Direction... directions) {
        assert (this.mc.player != null);
        Vec3 eyePos = this.mc.player.getEyePosition(1.0f);
        return (Direction[])Arrays.stream(directions).map(d -> new AbstractMap.SimpleEntry<Direction, Double>((Direction)d, origin.relative(d).distToCenterSqr((Position)eyePos))).sorted(Comparator.comparingDouble(Map.Entry::getValue)).map(Map.Entry::getKey).toArray(Direction[]::new);
    }

    private Direction[] sortByPriority(Direction[] directions) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        String priority = cfg.pistonDirectionPriority;
        if ("DEFAULT".equals(priority)) {
            return directions;
        }
        boolean horizontalFirst = "HORIZONTAL_FIRST".equals(priority);
        return (Direction[])Arrays.stream(directions).sorted(Comparator.comparingInt(d -> {
            boolean isHorizontal = d.getAxis().isHorizontal();
            return horizontalFirst ? (isHorizontal ? 0 : 1) : (isHorizontal ? 1 : 0);
        })).toArray(Direction[]::new);
    }

    private boolean canPlacePiston(BlockPos bedrockPos, Direction d) {
        assert (this.mc.level != null);
        BlockPos p1 = bedrockPos.relative(d);
        if (!this.mc.level.getBlockState(p1).canBeReplaced()) {
            return false;
        }
        BlockPos p2 = p1.relative(d);
        if (!this.mc.level.getBlockState(p2).canBeReplaced()) {
            return false;
        }
        return this.isValidY(p2.getY());
    }

    private void selectSlot(int slot) {
        if (this.mc.player == null) {
            return;
        }
        this.mc.player.getInventory().selected = slot;
        this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(slot));
    }

    private PlacementCandidate findBestPistonPlacement() {
        assert (this.mc.player != null && this.mc.level != null);
        Direction[] bodyDirs = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        bodyDirs = this.sortByDistance(this.bedrockPos, bodyDirs);
        bodyDirs = this.sortByPriority(bodyDirs);
        PlacementCandidate[] candidates = new PlacementCandidate[3];
        for (Direction direction : bodyDirs) {
            BlockPos pPos = this.bedrockPos.relative(direction);
            if (!this.mc.level.getBlockState(pPos).canBeReplaced() || !this.isValidY(pPos.getY())) continue;
            Direction[] facings = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            for (Direction f : facings = this.sortByDistance(pPos, facings)) {
                BlockPos extPos = pPos.relative(f);
                if (!this.mc.level.getBlockState(extPos).canBeReplaced() || !this.isValidY(extPos.getY())) continue;
                Direction savedBodyDir = this.pistonDirection;
                Direction savedFacing = this.pistonFacing;
                BlockPos savedPistonPos = this.pistonPos;
                BlockPos savedLeverPos = this.leverPos;
                BlockHitResult savedLeverHit = this.leverPlaceHitResult;
                this.pistonDirection = direction;
                this.pistonFacing = f;
                this.pistonPos = pPos;
                this.leverPos = null;
                this.leverPlaceHitResult = null;
                boolean leverFound = this.findLocationForLever();
                BlockPos foundLeverPos = this.leverPos;
                BlockHitResult foundLeverHit = this.leverPlaceHitResult;
                this.pistonDirection = savedBodyDir;
                this.pistonFacing = savedFacing;
                this.pistonPos = savedPistonPos;
                this.leverPos = savedLeverPos;
                this.leverPlaceHitResult = savedLeverHit;
                if (!leverFound || foundLeverPos == null) continue;
                AABB pistonBodyBox = new AABB(pPos);
                if (this.mc.player.getBoundingBox().intersects(pistonBodyBox)) continue;
                AABB extendBox = new AABB(extPos);
                int score = this.mc.player.getBoundingBox().intersects(extendBox) ? 2 : 0;
                PlacementCandidate cand = new PlacementCandidate(direction, f, pPos, foundLeverPos, foundLeverHit, score);
                if (score == 0) {
                    return cand;
                }
                if (candidates[score] != null) continue;
                candidates[score] = cand;
            }
        }
        for (PlacementCandidate placementCandidate : candidates) {
            if (placementCandidate == null) continue;
            return placementCandidate;
        }
        return null;
    }

    private void start(BlockPos pos) {
        if (this.mc.level == null) {
            return;
        }
        if (!this.isValidBlock(pos)) {
            return;
        }
        this.bedrockPos = pos;
        this.state = State.START;
        this.tickCount = 0;
        this.blockDestroyProgress = 0.0f;
        this.blockDestroySeqNumber = 0;
        this.leverPos = null;
        this.leverPlaceHitResult = null;
    }

    private void reset(String message) {
        this.reset(message, true);
    }

    private void reset(String message, boolean cleanup) {
        if (cleanup && this.mc.player != null && this.mc.level != null) {
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            if (cfg.cleanupHelpers && !this.helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : this.helperBlockPositions) {
                    if (this.mc.level.getBlockState(helperPos).isAir()) continue;
                    this.mineBlock(helperPos);
                }
                this.helperBlockPositions.clear();
            }
            if (this.pistonPos != null && !this.mc.level.getBlockState(this.pistonPos).isAir()) {
                this.cleanupPistonPos = this.pistonPos.immutable();
                this.cleanupPistonTicks = 0;
            }
            if (this.leverPos != null) {
                this.mineBlock(this.leverPos);
            }
        }
        this.bedrockPos = null;
        this.pistonDirection = null;
        this.pistonFacing = null;
        this.pistonPos = null;
        this.leverPos = null;
        this.leverPlaceHitResult = null;
        this.blockDestroyProgress = 0.0f;
        this.blockDestroySeqNumber = 0;
        this.state = State.INIT;
        this.tickCount = 0;
        this.reverseRotSent = false;
        this.predictedContainerStateId = -1;
        this.queue.clear();
        if (this.mc.player != null) {
            this.mc.player.connection.send((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.getInventory().selected));
        }
        if (message != null && this.mc.player != null) {
            this.mc.player.displayClientMessage(Component.literal((String)("\u00a7c[\u57fa\u5ca9\u7834\u574f\u5668] " + message)), true);
        }
    }

    private static enum State {
        INIT(instance -> {}),
        START(BedrockBreakerManager::handleStart),
        PLACE_LEVER(BedrockBreakerManager::handlePlaceLever),
        WAIT_Y_HEAD_ROT_SYNC(BedrockBreakerManager::handleWaitYHeadRotSync),
        BREAK_PISTON_START(BedrockBreakerManager::handleBreakPistonStart),
        BREAK_PISTON_PROGRESS(BedrockBreakerManager::handleBreakPistonProgress),
        WAIT_PISTON_EXTEND(BedrockBreakerManager::handleWaitPistonExtend),
        PLACE_REVERSE_PISTON(BedrockBreakerManager::handlePlaceReversePiston),
        WAIT_BEDROCK_BREAK(BedrockBreakerManager::handleWaitBedrockBreak),
        BREAK_REMAINING_LEVER_START(BedrockBreakerManager::handleBreakRemainingLeverStart),
        BREAK_REMAINING_LEVER_PROGRESS(BedrockBreakerManager::handleBreakRemainingLeverProgress),
        BREAK_REMAINING_PISTON_START(BedrockBreakerManager::handleBreakRemainingPistonStart),
        BREAK_REMAINING_PISTON_PROGRESS(BedrockBreakerManager::handleBreakRemainingPistonProgress);

        private final Consumer<BedrockBreakerManager> action;

        private State(Consumer<BedrockBreakerManager> action) {
            this.action = action;
        }

        public void handle(BedrockBreakerManager instance) {
            this.action.accept(instance);
        }
    }

    static class PlacementCandidate {
        final Direction bodyDir;
        final Direction facing;
        final BlockPos pistonPos;
        final BlockPos leverPos;
        final BlockHitResult leverHit;
        final int score;

        PlacementCandidate(Direction bodyDir, Direction facing, BlockPos pistonPos, BlockPos leverPos, BlockHitResult leverHit, int score) {
            this.bodyDir = bodyDir;
            this.facing = facing;
            this.pistonPos = pistonPos;
            this.leverPos = leverPos;
            this.leverHit = leverHit;
            this.score = score;
        }
    }
}

