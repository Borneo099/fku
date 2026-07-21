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
        if (this.mc.player == null || this.mc.f_91073_ == null || this.mc.f_91077_ == null) {
            return;
        }
        if (this.mc.f_91077_.m_6662_() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = ((BlockHitResult)this.mc.f_91077_).m_82425_();
        if (!this.isValidBlock(pos)) {
            return;
        }
        if (this.bedrockPos != null && this.bedrockPos.equals(pos)) {
            return;
        }
        if (this.state == State.INIT) {
            this.start(pos);
        } else {
            this.queue.add(pos.m_7949_());
        }
    }

    public void processNearby() {
        int maxY;
        if (this.mc.player == null || this.mc.f_91073_ == null) {
            return;
        }
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        int range = cfg.autoFindRange > 0 ? cfg.autoFindRange : 5;
        BlockPos playerPos = this.mc.player.m_20183_();
        int minY = Math.max(this.mc.f_91073_.m_141937_(), playerPos.m_123342_() - range);
        for (int y = maxY = Math.min(this.mc.f_91073_.m_151558_(), playerPos.m_123342_() + range); y >= minY; --y) {
            boolean layerHasTarget = false;
            ArrayList<BlockPos> layerTargets = new ArrayList<BlockPos>();
            for (int x = playerPos.m_123341_() - range; x <= playerPos.m_123341_() + range; ++x) {
                for (int z = playerPos.m_123343_() - range; z <= playerPos.m_123343_() + range; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!this.isValidBlock(pos)) continue;
                    layerHasTarget = true;
                    layerTargets.add(pos.m_7949_());
                }
            }
            if (!layerHasTarget) continue;
            Vec3 eyePos = this.mc.player.m_20299_(1.0f);
            layerTargets.sort(Comparator.comparingDouble(p -> p.m_203193_((Position)eyePos)));
            for (BlockPos pos : layerTargets) {
                if (pos.equals(this.bedrockPos) || this.queue.contains(pos)) continue;
                this.queue.add(pos);
            }
        }
    }

    public String getStatus() {
        return switch (this.state) {
            case State.BREAK_PISTON_PROGRESS -> this.state + " " + Math.round(this.blockDestroyProgress * 100.0f) + "%";
            case State.BREAK_REMAINING_PISTON_PROGRESS -> this.state + " " + Math.round(this.blockDestroyProgress * 100.0f) + "%";
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
        if (this.mc.player == null || this.mc.f_91073_ == null) {
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
                this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.cleanupPistonPos, Direction.DOWN, this.cleanupPistonSeq));
            } else if (this.cleanupPistonTicks >= 3) {
                this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.cleanupPistonPos, Direction.DOWN, this.cleanupPistonSeq));
                this.mc.f_91073_.m_46961_(this.cleanupPistonPos, false);
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
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        for (Direction face : Direction.values()) {
            BlockPos adjacentPos = this.bedrockPos.m_121945_(face);
            BlockState adjState = this.mc.f_91073_.m_8055_(adjacentPos);
            if (adjState.m_60734_() != Blocks.f_50164_ || !((Boolean)adjState.m_61143_((Property)LeverBlock.f_54622_)).booleanValue()) continue;
            BlockHitResult leverHit = new BlockHitResult(Vec3.m_82512_((Vec3i)adjacentPos).add(Vec3.m_82528_((Vec3i)face.m_122424_().m_122436_()).scale(0.5)), face.m_122424_(), adjacentPos, false);
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, leverHit, this.getSequenceNumber()));
            break;
        }
        if ((best = this.findBestPistonPlacement()) == null) {
            if (cfg.enableHelperBlocks) {
                Direction[] bodyDirs = this.sortByDistance(this.bedrockPos, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
                for (Direction d : bodyDirs = this.sortByPriority(bodyDirs)) {
                    if (!this.canPlacePiston(this.bedrockPos, d)) continue;
                    this.pistonDirection = d;
                    this.pistonFacing = d;
                    this.pistonPos = this.bedrockPos.m_121945_(d);
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
        int pistonSlot = best.facing.m_122424_().m_122434_() == Direction.Axis.Y ? this.ensureInHotbar(Items.f_41869_) : this.findPistonSlot();
        if (pistonSlot < 0) {
            this.reset("\u80cc\u5305\u627e\u4e0d\u5230\u6d3b\u585e");
            return;
        }
        if (best.facing.m_122434_() == Direction.Axis.Y) {
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            this.calculateFakeRotation(method);
            Vec3 clickLoc = Vec3.m_82512_((Vec3i)this.bedrockPos).add(Vec3.m_82528_((Vec3i)best.bodyDir.m_122436_()).scale(0.5));
            BlockHitResult hit = new BlockHitResult(clickLoc, best.bodyDir, this.bedrockPos, false);
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.m_20096_()));
            this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
            this.state = State.PLACE_LEVER;
        } else {
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            this.calculateFakeRotation(method);
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.m_20096_()));
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
            this.state = State.WAIT_Y_HEAD_ROT_SYNC;
        }
    }

    private void handlePlaceLever() {
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        int leverSlot = this.ensureInHotbar(Items.f_41966_);
        if (leverSlot < 0) {
            this.reset("\u80cc\u5305\u627e\u4e0d\u5230\u62c9\u6746");
            return;
        }
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(leverSlot));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, this.leverPlaceHitResult, this.getSequenceNumber());
        this.state = State.BREAK_PISTON_START;
        this.state.handle(this);
    }

    private void handleWaitYHeadRotSync() {
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        BlockPlacingMethod method = BlockPlacingMethod.facing(this.pistonFacing);
        this.calculateFakeRotation(method);
        Vec3 clickLoc = Vec3.m_82512_((Vec3i)this.bedrockPos).add(Vec3.m_82528_((Vec3i)this.pistonDirection.m_122436_()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, this.pistonDirection, this.bedrockPos, false);
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.m_20096_()));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
        this.state = State.PLACE_LEVER;
    }

    private void handleBreakPistonStart() {
        int pickaxeSlot;
        Direction reverseFacing;
        BlockPlacingMethod revMethod;
        Rotation revRot;
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        this.tickCount = 0;
        if (this.pistonDirection.m_122424_().m_122434_() != Direction.Axis.Y && (revRot = (revMethod = BlockPlacingMethod.facing(reverseFacing = this.pistonDirection.m_122424_())).getTargetRotation()) != null) {
            float revYaw = Float.isNaN(revRot.yRot()) ? this.mc.player.m_146908_() : revRot.yRot();
            float revPitch = Float.isNaN(revRot.xRot()) ? this.mc.player.m_146909_() : revRot.xRot();
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(revYaw, revPitch, this.mc.player.m_20096_()));
        }
        if ((pickaxeSlot = this.findPickaxe()) >= 0) {
            this.mc.player.m_150109_().f_35977_ = pickaxeSlot;
        }
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.m_150109_().f_35977_));
        this.blockDestroyProgress = this.getPistonDestroyProgress();
        this.blockDestroySeqNumber = this.getSequenceNumber();
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.pistonPos, Direction.UP, this.blockDestroySeqNumber));
        this.state = State.BREAK_PISTON_PROGRESS;
        this.tickCount = 0;
    }

    private void handleBreakPistonProgress() {
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        this.blockDestroyProgress += this.getPistonDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82512_((Vec3i)this.leverPos), Direction.UP, this.leverPos, false), this.getSequenceNumber()));
            this.state = State.WAIT_PISTON_EXTEND;
            this.tickCount = 0;
        } else if (this.tickCount > 50) {
            this.reset("\u6316\u6398\u6d3b\u585e\u8d85\u65f6\uff08\u9700\u8981\u9550\u5b50 + \u6025\u8feb\u6548\u679c\uff09");
        }
    }

    private void handleWaitPistonExtend() {
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        Direction actualPistonDir = null;
        for (Direction d : Direction.values()) {
            if (this.mc.f_91073_.m_8055_(this.pistonPos.m_121945_(d)).m_60734_() != Blocks.f_50040_) continue;
            actualPistonDir = d;
            break;
        }
        if (actualPistonDir != null) {
            if (!this.reverseRotSent) {
                Direction reverseFacing = this.pistonDirection.m_122424_();
                BlockPlacingMethod revMethod = BlockPlacingMethod.facing(reverseFacing);
                Rotation revRot = revMethod.getTargetRotation();
                if (revRot != null) {
                    float revYaw = Float.isNaN(revRot.yRot()) ? this.mc.player.m_146908_() : revRot.yRot();
                    float revPitch = Float.isNaN(revRot.xRot()) ? this.mc.player.m_146909_() : revRot.xRot();
                    this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(revYaw, revPitch, this.mc.player.m_20096_()));
                }
                this.reverseRotSent = true;
                return;
            }
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.m_82512_((Vec3i)this.leverPos), Direction.UP, this.leverPos, false), this.getSequenceNumber()));
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.pistonPos, Direction.UP, this.blockDestroySeqNumber));
            this.state = State.PLACE_REVERSE_PISTON;
            this.state.handle(this);
        } else if (this.tickCount > 10) {
            this.reset("\u7b49\u5f85\u6d3b\u585e\u4f38\u51fa\u8d85\u65f6\uff08\u6d3b\u585e\u5934\u672a\u68c0\u6d4b\u5230\uff09");
        }
    }

    private void handlePlaceReversePiston() {
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        int pistonSlot = this.pistonDirection.m_122434_() == Direction.Axis.Y ? this.ensureInHotbar(Items.f_41869_) : this.findPistonSlot();
        if (pistonSlot < 0) {
            this.reset(this.pistonDirection.m_122434_() == Direction.Axis.Y ? "\u80cc\u5305\u627e\u4e0d\u5230\u666e\u901a\u6d3b\u585e\uff08\u53cd\u5411\u5782\u76f4\u9700\u8981\u666e\u901a\u6d3b\u585e\uff09" : "\u80cc\u5305\u627e\u4e0d\u5230\u53ef\u7528\u6d3b\u585e");
            return;
        }
        Direction reverseFacing = this.pistonDirection.m_122424_();
        BlockPos clickPos = this.bedrockPos;
        Direction clickFace = this.pistonDirection;
        Vec3 clickLoc = Vec3.m_82512_((Vec3i)clickPos).add(Vec3.m_82528_((Vec3i)clickFace.m_122436_()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);
        BlockPlacingMethod method = BlockPlacingMethod.facing(reverseFacing);
        this.calculateFakeRotation(method);
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundMovePlayerPacket.Rot(this.fakeYaw, this.fakePitch, this.mc.player.m_20096_()));
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(pistonSlot));
        this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
        this.mc.f_91073_.m_46961_(this.pistonPos, false);
        this.mc.f_91073_.m_46961_(this.pistonPos.m_121945_(this.pistonFacing), false);
        this.state = State.WAIT_BEDROCK_BREAK;
        this.tickCount = 0;
    }

    private void handleWaitBedrockBreak() {
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        if (this.mc.f_91073_.m_8055_(this.bedrockPos).m_60795_() && !this.mc.f_91073_.m_8055_(this.pistonPos.m_121945_(this.pistonFacing)).m_60713_(Blocks.f_50110_)) {
            BlockPlacer.BlockPlacePlan repPlan;
            int repSlot;
            Block replaceBlock;
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            if (cfg.replaceBlockId != null && !cfg.replaceBlockId.isEmpty() && (replaceBlock = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(cfg.replaceBlockId))) != null && replaceBlock != Blocks.f_50016_ && (repSlot = this.findItem(replaceBlock.m_5456_())) >= 0 && (repPlan = BlockPlacer.createPacketPlan(this.bedrockPos, BlockPlacingMethod.FROM_HORIZONTAL)) != null) {
                this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(repSlot));
                repPlan.apply(this.mc.player.m_146908_(), this.mc.player.m_146909_());
            }
            if (cfg.cleanupHelpers && !this.helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : this.helperBlockPositions) {
                    if (this.mc.f_91073_.m_8055_(helperPos).m_60795_()) continue;
                    this.mineBlock(helperPos);
                }
                this.helperBlockPositions.clear();
            }
            this.state = this.mc.f_91073_.m_8055_(this.leverPos).m_60713_(Blocks.f_50164_) ? State.BREAK_REMAINING_LEVER_START : State.BREAK_REMAINING_PISTON_START;
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
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.leverPos, Direction.DOWN, this.blockDestroySeqNumber));
        this.state = State.BREAK_REMAINING_LEVER_PROGRESS;
    }

    private void handleBreakRemainingLeverProgress() {
        assert (this.mc.player != null);
        this.blockDestroyProgress += this.getLeverDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.leverPos, Direction.DOWN, this.blockDestroySeqNumber));
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
            this.mc.player.m_150109_().f_35977_ = pickaxeSlot;
        }
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.m_150109_().f_35977_));
        this.blockDestroyProgress = this.getPistonDestroyProgress();
        this.blockDestroySeqNumber = this.getSequenceNumber();
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.pistonPos, Direction.DOWN, this.blockDestroySeqNumber));
        this.state = State.BREAK_REMAINING_PISTON_PROGRESS;
        this.tickCount = 0;
    }

    private void handleBreakRemainingPistonProgress() {
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        this.blockDestroyProgress += this.getPistonDestroyProgress();
        if (this.blockDestroyProgress >= 1.0f) {
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.pistonPos, Direction.DOWN, this.blockDestroySeqNumber));
            this.mc.f_91073_.m_46961_(this.pistonPos, false);
            BlockPos pistonHeadPos = this.pistonPos.m_121945_(this.pistonFacing);
            if (!this.mc.f_91073_.m_8055_(pistonHeadPos).m_60795_()) {
                this.mc.f_91073_.m_46961_(pistonHeadPos, false);
                this.mineBlock(pistonHeadPos);
            }
            if (this.leverPos != null && this.mc.f_91073_.m_8055_(this.leverPos).m_60713_(Blocks.f_50164_)) {
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
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        this.leverPos = null;
        BlockPos pistonHeadPos_ = this.pistonPos.m_121945_(this.pistonFacing);
        for (Direction direction : this.sortByDistance(this.bedrockPos, Direction.values())) {
            Direction[] hit;
            BlockState leverBlockState;
            possibleLeverPos = this.bedrockPos.m_121945_(direction);
            if (direction == this.pistonDirection || possibleLeverPos.equals(pistonHeadPos_) || !this.mc.f_91073_.m_8055_(possibleLeverPos).m_247087_() || !this.mc.f_91073_.m_6425_(possibleLeverPos).m_76178_() || !this.isValidY(possibleLeverPos.m_123342_()) || this.isInvalidLeverSupport(this.bedrockPos) || (leverBlockState = Blocks.f_50164_.m_5573_(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.f_41966_, 1), (BlockHitResult)(hit = new BlockHitResult(Vec3.m_82512_((Vec3i)this.bedrockPos).add(Vec3.m_82528_((Vec3i)direction.m_122436_()).scale(0.5)), direction, this.bedrockPos, false))))) == null || !this.isLeverStateMatch(leverBlockState, direction)) continue;
            this.leverPos = possibleLeverPos;
            this.leverPlaceHitResult = hit;
            return true;
        }
        for (Direction direction : this.sortByDistance(this.pistonPos, Direction.values())) {
            if (direction == this.pistonDirection || direction == this.pistonDirection.m_122424_() || direction == this.pistonFacing || !this.mc.f_91073_.m_8055_(possibleLeverPos = this.pistonPos.m_121945_(direction)).m_247087_() || !this.mc.f_91073_.m_6425_(possibleLeverPos).m_76178_() || !this.isValidY(possibleLeverPos.m_123342_())) continue;
            for (Direction dir : this.sortByDistance(possibleLeverPos, Direction.values())) {
                BlockHitResult hit;
                BlockState leverBlockState;
                BlockPos possibleSupportPos = possibleLeverPos.m_121945_(dir);
                if (possibleSupportPos.equals(this.pistonPos) || this.mc.f_91073_.m_8055_(possibleSupportPos).m_247087_() || this.isInvalidLeverSupport(possibleSupportPos) || (leverBlockState = Blocks.f_50164_.m_5573_(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.f_41966_, 1), hit = new BlockHitResult(Vec3.m_82512_((Vec3i)possibleSupportPos).add(Vec3.m_82528_((Vec3i)dir.m_122424_().m_122436_()).scale(0.5)), dir.m_122424_(), possibleSupportPos, false)))) == null || !this.isLeverStateMatch(leverBlockState, dir.m_122424_())) continue;
                this.leverPos = possibleLeverPos;
                this.leverPlaceHitResult = hit;
                return true;
            }
        }
        BlockPos pistonHeadPos = this.pistonPos.m_121945_(this.pistonFacing);
        for (Direction lateral : this.getPistonHeadLateralDirections()) {
            BlockPos supportPos;
            BlockPos candidatePos = pistonHeadPos.m_121945_(lateral);
            if (!this.mc.f_91073_.m_8055_(candidatePos).m_247087_() || !this.mc.f_91073_.m_6425_(candidatePos).m_76178_() || !this.isValidY(candidatePos.m_123342_()) || (supportPos = this.pistonPos.m_121945_(lateral)).equals(pistonHeadPos) || this.mc.f_91073_.m_8055_(supportPos).m_247087_() || this.isInvalidLeverSupport(supportPos)) continue;
            Direction clickFace = this.pistonFacing;
            BlockHitResult hit = new BlockHitResult(Vec3.m_82512_((Vec3i)supportPos).add(Vec3.m_82528_((Vec3i)clickFace.m_122436_()).scale(0.5)), clickFace, supportPos, false);
            BlockState leverBlockState = Blocks.f_50164_.m_5573_(new BlockPlaceContext((Player)this.mc.player, InteractionHand.MAIN_HAND, new ItemStack((ItemLike)Items.f_41966_, 1), hit));
            if (leverBlockState == null || !this.isLeverStateMatch(leverBlockState, clickFace)) continue;
            this.leverPos = candidatePos;
            this.leverPlaceHitResult = hit;
            return true;
        }
        return false;
    }

    private boolean isInvalidLeverSupport(BlockPos supportPos) {
        if (this.mc.f_91073_ == null) {
            return true;
        }
        BlockState state = this.mc.f_91073_.m_8055_(supportPos);
        Block block = state.m_60734_();
        return block instanceof PistonBaseBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof StairBlock || block instanceof SlabBlock || block instanceof ComposterBlock;
    }

    private boolean isLeverStateMatch(BlockState state, Direction direction) {
        return switch (direction) {
            case Direction.UP -> {
                if (state.m_61143_((Property)LeverBlock.f_53179_) == AttachFace.FLOOR) {
                    yield true;
                }
                yield false;
            }
            case Direction.DOWN -> {
                if (state.m_61143_((Property)LeverBlock.f_53179_) == AttachFace.CEILING) {
                    yield true;
                }
                yield false;
            }
            default -> state.m_61143_((Property)LeverBlock.f_53179_) == AttachFace.WALL && state.m_61143_((Property)LeverBlock.f_54117_) == direction;
        };
    }

    private float getPistonDestroyProgress() {
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        return Blocks.f_50039_.m_49966_().m_60625_((Player)this.mc.player, (BlockGetter)this.mc.f_91073_, this.pistonPos);
    }

    private float getLeverDestroyProgress() {
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        return Blocks.f_50164_.m_49966_().m_60625_((Player)this.mc.player, (BlockGetter)this.mc.f_91073_, this.leverPos);
    }

    private void calculateFakeRotation(BlockPlacingMethod method) {
        Rotation targetRot = method.getTargetRotation();
        if (targetRot != null) {
            this.fakeYaw = Float.isNaN(targetRot.yRot()) ? this.mc.player.m_146908_() : targetRot.yRot();
            this.fakePitch = Float.isNaN(targetRot.xRot()) ? this.mc.player.m_146909_() : targetRot.xRot();
        }
    }

    private boolean isValidBlock(BlockPos pos) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        if (this.mc.f_91073_ == null) {
            return false;
        }
        if (cfg.allBlocks) {
            return true;
        }
        Block target = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(cfg.targetBlockId));
        return target != null && this.mc.f_91073_.m_8055_(pos).m_60713_(target);
    }

    private Direction[] getPistonHeadLateralDirections() {
        if (this.pistonFacing == null) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        if (this.pistonFacing.m_122434_() == Direction.Axis.Y) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        Direction.Axis axis = this.pistonFacing.m_122434_();
        if (axis == Direction.Axis.X) {
            return new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
        }
        return new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
    }

    private boolean tryPlaceHelperBlocks() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        assert (this.mc.f_91073_ != null && this.mc.player != null);
        String[] blockIds = cfg.helperBlockList.split(",");
        ArrayList<Direction> searchDirs = new ArrayList<Direction>();
        if (this.pistonDirection.m_122434_() == Direction.Axis.Y) {
            searchDirs.addAll(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        } else {
            searchDirs.add(Direction.UP);
            searchDirs.add(Direction.DOWN);
            for (Direction d2 : Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                if (d2.m_122434_() == this.pistonDirection.m_122434_()) continue;
                searchDirs.add(d2);
            }
        }
        searchDirs.sort(Comparator.comparingDouble(d -> this.mc.player.m_20238_(Vec3.m_82512_((Vec3i)this.bedrockPos.m_121945_(d)))));
        for (Direction dir : searchDirs) {
            BlockPos helperPos = this.bedrockPos.m_121945_(dir);
            if (!this.mc.f_91073_.m_8055_(helperPos).m_247087_() || !this.isValidY(helperPos.m_123342_())) continue;
            boolean placed = false;
            for (String blockId : blockIds) {
                Item helperItem;
                int hotbarSlot;
                Block helperBlock;
                if ((blockId = blockId.trim()).isEmpty() || (helperBlock = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId))) == null || helperBlock == Blocks.f_50016_ || !helperBlock.m_49966_().m_280296_() || (hotbarSlot = this.ensureInHotbar(helperItem = helperBlock.m_5456_())) < 0) continue;
                Vec3 clickLoc = Vec3.m_82512_((Vec3i)this.bedrockPos).add(Vec3.m_82528_((Vec3i)dir.m_122436_()).scale(0.5));
                BlockHitResult hit = new BlockHitResult(clickLoc, dir, this.bedrockPos, false);
                this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(hotbarSlot));
                this.sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, this.getSequenceNumber());
                this.mc.f_91073_.m_46597_(helperPos, helperBlock.m_49966_());
                placed = true;
                break;
            }
            if (!placed) continue;
            this.helperBlockPositions.add(helperPos.m_7949_());
            if (this.findLocationForLever() && this.leverPos != null) {
                return true;
            }
            this.mineBlock(helperPos);
            this.helperBlockPositions.remove(helperPos.m_7949_());
        }
        return false;
    }

    private void sendUseItemOnSneak(InteractionHand hand, BlockHitResult hitResult, int sequence) {
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(hand, hitResult, sequence));
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerCommandPacket((Entity)this.mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    private int ensureInHotbar(Item item) {
        int slot = this.findItem(item);
        if (slot >= 0) {
            return slot;
        }
        if (this.mc.player == null) {
            return -1;
        }
        Inventory inventory = this.mc.player.m_150109_();
        for (int i = 9; i < 36; ++i) {
            if (!inventory.m_8020_(i).m_150930_(item)) continue;
            int targetSlot = -1;
            for (int j = 0; j < 9; ++j) {
                if (!inventory.m_8020_(j).m_41619_()) continue;
                targetSlot = j;
                break;
            }
            if (targetSlot < 0) {
                targetSlot = inventory.f_35977_;
            }
            int containerSlot = i;
            int hotbarContainerSlot = 36 + targetSlot;
            int serverStateId = this.mc.player.f_36096_.m_182424_();
            int stateId = Math.max(serverStateId, this.predictedContainerStateId);
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(0, stateId, containerSlot, targetSlot, ClickType.SWAP, ItemStack.f_41583_, (Int2ObjectMap)new Int2ObjectOpenHashMap(Map.of(containerSlot, inventory.m_8020_(targetSlot).m_41777_(), hotbarContainerSlot, inventory.m_8020_(i).m_41777_()))));
            this.predictedContainerStateId = stateId + 1;
            ItemStack temp = inventory.m_8020_(targetSlot).m_41777_();
            inventory.f_35974_.set(targetSlot, inventory.m_8020_(i).m_41777_());
            inventory.f_35974_.set(i, temp);
            inventory.f_35977_ = targetSlot;
            return targetSlot;
        }
        return -1;
    }

    private int findItem(Item item) {
        assert (this.mc.player != null);
        Inventory inventory = this.mc.player.m_150109_();
        for (int i = 0; i < 9; ++i) {
            if (!inventory.m_8020_(i).m_150930_(item)) continue;
            return i;
        }
        return -1;
    }

    private int findPistonSlot() {
        int slot = this.ensureInHotbar(Items.f_41869_);
        if (slot >= 0) {
            return slot;
        }
        slot = this.ensureInHotbar(Items.f_41862_);
        return slot;
    }

    private int findPistonSlotPreferSticky() {
        int slot = this.ensureInHotbar(Items.f_41862_);
        if (slot >= 0) {
            return slot;
        }
        slot = this.ensureInHotbar(Items.f_41869_);
        return slot;
    }

    private int countItem(Item item) {
        int count = 0;
        for (int i = 0; i < 36; ++i) {
            if (!this.mc.player.m_150109_().m_8020_(i).m_150930_(item)) continue;
            count += this.mc.player.m_150109_().m_8020_(i).m_41613_();
        }
        return count;
    }

    private int findPickaxe() {
        int i;
        assert (this.mc.player != null);
        Inventory inventory = this.mc.player.m_150109_();
        for (i = 0; i < 9; ++i) {
            if (!inventory.m_8020_(i).m_204131_().anyMatch(tag -> tag == ItemTags.f_271360_)) continue;
            return i;
        }
        for (i = 9; i < 36; ++i) {
            if (!inventory.m_8020_(i).m_204131_().anyMatch(tag -> tag == ItemTags.f_271360_)) continue;
            int targetSlot = -1;
            for (int j = 0; j < 9; ++j) {
                if (!inventory.m_8020_(j).m_41619_()) continue;
                targetSlot = j;
                break;
            }
            if (targetSlot < 0) {
                targetSlot = inventory.f_35977_;
            }
            int containerSlot = i;
            int hotbarContainerSlot = 36 + targetSlot;
            int serverStateId = this.mc.player.f_36096_.m_182424_();
            int stateId = Math.max(serverStateId, this.predictedContainerStateId);
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundContainerClickPacket(0, stateId, containerSlot, targetSlot, ClickType.SWAP, ItemStack.f_41583_, (Int2ObjectMap)new Int2ObjectOpenHashMap(Map.of(containerSlot, inventory.m_8020_(targetSlot).m_41777_(), hotbarContainerSlot, inventory.m_8020_(i).m_41777_()))));
            this.predictedContainerStateId = stateId + 1;
            ItemStack temp = inventory.m_8020_(targetSlot).m_41777_();
            inventory.f_35974_.set(targetSlot, inventory.m_8020_(i).m_41777_());
            inventory.f_35974_.set(i, temp);
            inventory.f_35977_ = targetSlot;
            return targetSlot;
        }
        return -1;
    }

    private void mineBlock(BlockPos pos) {
        if (this.mc.player == null || this.mc.f_91073_ == null) {
            return;
        }
        if (this.mc.f_91073_.m_8055_(pos).m_60795_()) {
            return;
        }
        int seq = this.getSequenceNumber();
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        this.mc.f_91073_.m_46961_(pos, false);
    }

    private int getSequenceNumber() {
        assert (this.mc.f_91073_ != null);
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)this.mc.f_91073_).getBlockStatePredictionHandler_CU();
        handler.m_233855_();
        int num = handler.m_233871_();
        handler.close();
        return num;
    }

    private boolean isValidY(int y) {
        assert (this.mc.f_91073_ != null);
        DimensionType dimension = this.mc.f_91073_.m_6042_();
        return dimension.f_156647_() <= y && y < dimension.f_156647_() + dimension.f_156648_();
    }

    private Direction[] sortByDistance(BlockPos origin, Direction . directions) {
        assert (this.mc.player != null);
        Vec3 eyePos = this.mc.player.m_20299_(1.0f);
        return (Direction[])Arrays.stream(directions).map(d -> new AbstractMap.SimpleEntry<Direction, Double>((Direction)d, origin.m_121945_(d).m_203193_((Position)eyePos))).sorted(Comparator.comparingDouble(Map.Entry::getValue)).map(Map.Entry::getKey).toArray(Direction[]::new);
    }

    private Direction[] sortByPriority(Direction[] directions) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        String priority = cfg.pistonDirectionPriority;
        if ("DEFAULT".equals(priority)) {
            return directions;
        }
        boolean horizontalFirst = "HORIZONTAL_FIRST".equals(priority);
        return (Direction[])Arrays.stream(directions).sorted(Comparator.comparingInt(d -> {
            boolean isHorizontal = d.m_122434_().m_122479_();
            return horizontalFirst ? (isHorizontal ? 0 : 1) : (isHorizontal ? 1 : 0);
        })).toArray(Direction[]::new);
    }

    private boolean canPlacePiston(BlockPos bedrockPos, Direction d) {
        assert (this.mc.f_91073_ != null);
        BlockPos p1 = bedrockPos.m_121945_(d);
        if (!this.mc.f_91073_.m_8055_(p1).m_247087_()) {
            return false;
        }
        BlockPos p2 = p1.m_121945_(d);
        if (!this.mc.f_91073_.m_8055_(p2).m_247087_()) {
            return false;
        }
        return this.isValidY(p2.m_123342_());
    }

    private void selectSlot(int slot) {
        if (this.mc.player == null) {
            return;
        }
        this.mc.player.m_150109_().f_35977_ = slot;
        this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(slot));
    }

    private PlacementCandidate findBestPistonPlacement() {
        assert (this.mc.player != null && this.mc.f_91073_ != null);
        Direction[] bodyDirs = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        bodyDirs = this.sortByDistance(this.bedrockPos, bodyDirs);
        bodyDirs = this.sortByPriority(bodyDirs);
        PlacementCandidate[] candidates = new PlacementCandidate[3];
        for (Direction direction : bodyDirs) {
            BlockPos pPos = this.bedrockPos.m_121945_(direction);
            if (!this.mc.f_91073_.m_8055_(pPos).m_247087_() || !this.isValidY(pPos.m_123342_())) continue;
            Direction[] facings = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            for (Direction f : facings = this.sortByDistance(pPos, facings)) {
                BlockPos extPos = pPos.m_121945_(f);
                if (!this.mc.f_91073_.m_8055_(extPos).m_247087_() || !this.isValidY(extPos.m_123342_())) continue;
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
                if (this.mc.player.m_20191_().m_82381_(pistonBodyBox)) continue;
                AABB extendBox = new AABB(extPos);
                int score = this.mc.player.m_20191_().m_82381_(extendBox) ? 2 : 0;
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
        if (this.mc.f_91073_ == null) {
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
        if (cleanup && this.mc.player != null && this.mc.f_91073_ != null) {
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            if (cfg.cleanupHelpers && !this.helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : this.helperBlockPositions) {
                    if (this.mc.f_91073_.m_8055_(helperPos).m_60795_()) continue;
                    this.mineBlock(helperPos);
                }
                this.helperBlockPositions.clear();
            }
            if (this.pistonPos != null && !this.mc.f_91073_.m_8055_(this.pistonPos).m_60795_()) {
                this.cleanupPistonPos = this.pistonPos.m_7949_();
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
            this.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(this.mc.player.m_150109_().f_35977_));
        }
        if (message != null && this.mc.player != null) {
            this.mc.player.m_5661_(Component.literal((String)("\u00a7c[\u57fa\u5ca9\u7834\u574f\u5668] " + message)), true);
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

