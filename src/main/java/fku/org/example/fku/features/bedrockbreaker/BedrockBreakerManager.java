package fku.org.example.fku.features.bedrockbreaker; /* water */

import fku.org.example.fku.mixin.ClientLevelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 基岩破坏器核心状态机 —— 严格参考 CheatUtils BedrockBreaker.java
 * (https://github.com/Zergatul/cheatutils/blob/1.21.11/common/java/com/zergatul/cheatutils/modules/hacks/BedrockBreaker.java)
 *
 * ★ 矛盾定性：
 *   核心技术难点在于"无头活塞"时序控制：
 *   在活塞伸出状态下（刚激活），STOP_DESTROY 移除活塞体，
 *   同一 tick 内放置反向活塞，利用服务端 BE 事件排队机制产生无头活塞破除基岩。
 *
 * ★ 信标急迫效果（Haste）：
 *   本实现依赖玩家持有镐子 + 信标急迫效果加速挖掘。
 *   getDestroyProgress() 自动计算玩家当前手持物品 + 状态效果（含急迫）的挖掘速度。
 *   无急迫效果时可能需要多次 tick 累积进度（默认 timeout=50 tick）。
 *
 * ★ 状态流程（11 状态，与 CheatUtils 一致）：
 *   INIT → START(放活塞) → PLACE_LEVER(同tick)
 *   → BREAK_PISTON_START(切镐+START_DESTROY)
 *   → BREAK_PISTON_PROGRESS(累积进度→拉杆ON)
 *   → WAIT_PISTON_EXTEND(等待活塞头→拉杆OFF+STOP_DESTROY)
 *   → PLACE_REVERSE_PISTON(放反向活塞, 同tick链式调用)
 *   → WAIT_BEDROCK_BREAK(等待基岩破除)
 *   → BREAK_REMAINING_LEVER_START/PROGRESS(挖拉杆)
 *   → BREAK_REMAINING_PISTON_START/PROGRESS(挖剩余活塞)
 *   → INIT(空闲, 处理队列下一个)
 */
public class BedrockBreakerManager {

    private static final BedrockBreakerManager INSTANCE = new BedrockBreakerManager();
    public static BedrockBreakerManager getInstance() { return INSTANCE; }

    private final Minecraft mc = Minecraft.getInstance();
    private final Queue<BlockPos> queue = new ArrayDeque<>();

    private BlockPos bedrockPos;
    private Direction pistonDirection;
    /** ★ 活塞朝向（v2.7 新增）：独立于活塞位置方向，可任意朝向 */
    private Direction pistonFacing;
    private BlockPos pistonPos;
    private float blockDestroyProgress;
    private int blockDestroySeqNumber;
    private BlockPos leverPos;
    private BlockHitResult leverPlaceHitResult;
    private State state = State.INIT;
    private int tickCount;
    /** 假旋转Yaw（服务端朝向，客户端不受影响） */
    private float fakeYaw;
    /** 假旋转Pitch（服务端朝向，客户端不受影响） */
    private float fakePitch;
    /** 残留活塞清理位置（非即时挖掘方块需多tick处理） */
    private BlockPos cleanupPistonPos;
    /** 残留活塞清理序列号 */
    private int cleanupPistonSeq;
    /** ★ 反向活塞旋转是否已预发（v2.6 新增） */
    private boolean reverseRotSent;
    /** 残留活塞清理已过tick数 */
    private int cleanupPistonTicks = 0;

    /** ★ v2.10 本地预测的容器 stateId（修复跨 tick SWAP 被拒问题）
     *   ServerboundContainerClickPacket(SWAP) 发送后，服务端处理且 stateId 自增 1，
     *   但客户端需等服务端回复才更新 containerMenu.stateId。
     *   若下一 tick 再次发送 SWAP，客户端 stateId 可能仍是旧值，导致服务端拒绝。
     *   本字段跟踪当前预期的 stateId，初始为 -1（从 containerMenu.getStateId() 读取）。
     *
     *   同步策略：
     *   每次读取时取 Math.max(containerMenu.getStateId(), predictedContainerStateId)，
     *   既覆盖服务端已回复的增量，又弥补未回复时的缺失。 */
    private int predictedContainerStateId = -1;

    /** ★ 辅助方块位置列表（v2.2 新增）
     *  当无法找到拉杆位置时，在目标基岩周围放置辅助方块提供拉杆附着面。
     *  功能完成后按优先级清理。 */
    private final List<BlockPos> helperBlockPositions = new ArrayList<>();

    private BedrockBreakerManager() {}

    // ============ 外部接口（与 CheatUtils 一致）============

    /**
     * 处理对准的基岩
     */
    public void process() {
        if (mc.player == null || mc.level == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
        if (!isValidBlock(pos)) return;

        if (bedrockPos != null && bedrockPos.equals(pos)) return;

        if (state == State.INIT) {
            start(pos);
        } else {
            queue.add(pos.immutable());
        }
    }

    /**
     * ★ 扫描模式：自动扫描周围目标方块并加入队列
     *   严格参考 CheatUtils BedrockBreaker.processNearby()
     *
     *   扫描策略：
     *   1. 以玩家眼部位置为中心，autoFindRange 为半径
     *   2. 按 Y 层从上到下分组
     *   3. 每层找到目标方块后，将该层所有目标方块加入队列
     *   4. 从最高层开始处理，逐层向下
     */
    public void processNearby() {
        if (mc.player == null || mc.level == null) return;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        int range = cfg.autoFindRange;
        if (range <= 0) return;

        BlockPos playerPos = mc.player.blockPosition();
        int minY = Math.max(mc.level.getMinBuildHeight(), playerPos.getY() - range);
        int maxY = Math.min(mc.level.getMaxBuildHeight(), playerPos.getY() + range);

        // 从最高层向下扫描
        for (int y = maxY; y >= minY; y--) {
            boolean layerHasTarget = false;
            java.util.List<BlockPos> layerTargets = new java.util.ArrayList<>();

            for (int x = playerPos.getX() - range; x <= playerPos.getX() + range; x++) {
                for (int z = playerPos.getZ() - range; z <= playerPos.getZ() + range; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isValidBlock(pos)) {
                        layerHasTarget = true;
                        layerTargets.add(pos.immutable());
                    }
                }
            }

            if (layerHasTarget) {
                // 将该层所有目标方块加入队列（按距离玩家排序，由近到远）
                Vec3 eyePos = mc.player.getEyePosition(1.0f);
                layerTargets.sort(java.util.Comparator.comparingDouble(
                        p -> p.distToCenterSqr(eyePos)));
                for (BlockPos pos : layerTargets) {
                    // 避免重复添加已在队列或正在处理的目标
                    if (!pos.equals(bedrockPos) && !queue.contains(pos)) {
                        queue.add(pos);
                    }
                }
                break; // 只处理最高层，下一tick继续
            }
        }
    }

    /**
     * 获取当前状态文本
     */
    public String getStatus() {
        return switch (state) {
            case BREAK_PISTON_PROGRESS -> state + " " + Math.round(blockDestroyProgress * 100) + "%";
            case BREAK_REMAINING_PISTON_PROGRESS -> state + " " + Math.round(blockDestroyProgress * 100) + "%";
            default -> state.toString();
        };
    }

    public boolean isRunning() { return state != State.INIT; }
    public void stop() { reset("手动中止"); }

    // ============ Tick 入口 ============

    public void tick() {
        if (mc.player == null || mc.level == null) return;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        // ★ 功能关闭时清空队列和状态，避免残留序列继续执行
        if (!cfg.enabled) {
            if (state != State.INIT || !queue.isEmpty()) {
                reset("功能已关闭", false);
            }
            return;
        }

        // ★ 残留活塞清理：非即时挖掘方块（如活塞）需要多tick处理
        //   第1tick发送START_DESTROY，第3tick发送STOP_DESTROY完成挖掘。
        //   清理期间阻塞状态机，避免干扰。
        if (cleanupPistonPos != null) {
            cleanupPistonTicks++;
            if (cleanupPistonTicks == 1) {
                cleanupPistonSeq = getSequenceNumber();
                mc.player.connection.send(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                        cleanupPistonPos, Direction.DOWN, cleanupPistonSeq));
            } else if (cleanupPistonTicks >= 3) {
                mc.player.connection.send(new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                        cleanupPistonPos, Direction.DOWN, cleanupPistonSeq));
                mc.level.destroyBlock(cleanupPistonPos, false);
                cleanupPistonPos = null;
                cleanupPistonTicks = 0;
            }
            return; // 清理期间不处理其他逻辑
        }

        if (state == State.INIT) {
            // ★ 扫描模式：自动扫描周围目标方块并加入队列
            if (cfg.scanMode && cfg.autoFindRange > 0) {
                processNearby();
            }
            if (!queue.isEmpty()) {
                start(queue.remove());
            }
        }

        if (bedrockPos == null) return;

        tickCount++;
        state.handle(this);
    }

    // ================================================================
    // ★ 第1步：handleStart —— 基于启发式搜索 + 评分系统
    //   参考 ApproachBase.findBest() 策略 + CheatUtils BedrockBreaker.handleStart()
    //
    //   ★ v2.8 改造（抓主要矛盾——粘性活塞垂直方向失败）：
    //     旧实现：首匹配 6 方向 + 耦合（体方向=伸出方向）+ 拉杆搜索三策略分步执行
    //     旧问题：首匹配找到的方向组合可能拉杆不可行，直接跳过该方向，粘性活塞垂直时易失败
    //     新实现：统一搜索所有 (体方向×伸出方向) 组合，集成拉杆检查 + 碰撞评分，
    //             返回质量最佳方案（score=0 > score=2），全部不可行才触发 reset
    // ================================================================
    private void handleStart() {
        assert mc.player != null && mc.level != null;
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();

        // ★ 检测并关闭目标方块上已激活的拉杆
        //   如果目标方块上有被激活的拉杆，会导致破基岩失败。
        //   遍历目标方块所有面，检查是否有激活的拉杆附着，若有则发送交互包关闭。
        for (Direction face : Direction.values()) {
            BlockPos adjacentPos = bedrockPos.relative(face);
            BlockState adjState = mc.level.getBlockState(adjacentPos);
            if (adjState.getBlock() == Blocks.LEVER && adjState.getValue(LeverBlock.POWERED)) {
                // 发送交互包关闭拉杆
                BlockHitResult leverHit = new BlockHitResult(
                        Vec3.atCenterOf(adjacentPos).add(Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(0.5)),
                        face.getOpposite(),
                        adjacentPos,
                        false);
                mc.player.connection.send(new ServerboundUseItemOnPacket(
                        InteractionHand.MAIN_HAND, leverHit, getSequenceNumber()));
                break; // 只处理第一个找到的激活拉杆
            }
        }

        // ★ v2.8 启发式搜索：统一找最佳方向组合（体方向+伸出方向+拉杆）
        PlacementCandidate best = findBestPistonPlacement();
        if (best == null) {
            // 尝试辅助方块补救（旧逻辑保留，确保不破坏现有功能）
            if (cfg.enableHelperBlocks) {
                // 临时设置一个默认方向供 tryPlaceHelperBlocks 使用
                Direction[] bodyDirs = sortByDistance(bedrockPos,
                        Direction.UP, Direction.DOWN,
                        Direction.NORTH, Direction.SOUTH,
                        Direction.EAST, Direction.WEST);
                bodyDirs = sortByPriority(bodyDirs);
                for (Direction d : bodyDirs) {
                    if (canPlacePiston(bedrockPos, d)) {
                        pistonDirection = d;
                        pistonFacing = d;
                        pistonPos = bedrockPos.relative(d);
                        break;
                    }
                }
                if (pistonDirection != null && tryPlaceHelperBlocks()) {
                    // 辅助方块放置后重新搜索
                    best = findBestPistonPlacement();
                }
            }
            if (best == null) {
                reset("找不到放置活塞的位置");
                return;
            }
        }

        // ★ 将最佳候选赋值到类字段
        pistonDirection = best.bodyDir;
        pistonFacing = best.facing;
        pistonPos = best.pistonPos;
        leverPos = best.leverPos;
        leverPlaceHitResult = best.leverHit;

        int pistonSlot = findPistonSlot();
        if (pistonSlot < 0) { reset("背包找不到活塞"); return; }

        // ★ v2.8 质量评分日志（调试场景用）
        //   若 pistonFacing 与 pistonDirection 不同（未来解耦扩展），下方代码自动适配

        // ★ v2.7 水平方向正向活塞需等待1tick同步yHeadRot
        //   竖直方向（UP/DOWN）不受玩家视角影响，可直接放置
        if (best.facing.getAxis() == Direction.Axis.Y) {
            // 竖直：同tick放置，不等待yHeadRot同步
            // ★ v2.9 正向活塞解耦：点击 bedrockPos（始终固体）
            //   参考 BlockPlacer.vanillaPistonPlacement2() 思路
            //   借鉴点：点击 bedrockPos 的 bodyDir 面 → 新方块位置 = pistonPos ✓
            //   旋转至 pistonFacing → 活塞伸出方向 = pistonFacing ✓
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            calculateFakeRotation(method);
            Vec3 clickLoc = Vec3.atCenterOf(bedrockPos)
                    .add(Vec3.atLowerCornerOf(best.bodyDir.getNormal()).scale(0.5));
            BlockHitResult hit = new BlockHitResult(clickLoc, best.bodyDir, bedrockPos, false);
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(pistonSlot));
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                    fakeYaw, fakePitch, mc.player.onGround()));
            sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, getSequenceNumber());
            // ★ 不在此tick链式调用 handlePlaceLever（v2.9 stateId 冲突修复）
            //   ensureInHotbar 在同一tick发送第二个 ServerboundContainerClickPacket(SWAP)
            //   时 stateId 与服务端不匹配（服务端处理第一个 SWAP 后 stateId 已 +1），
            //   导致 lever SWAP 被拒，目标槽位仍为活塞，误在 leverPos 放置活塞。
            //   改为下一 tick 由 onClientTick() 进入 PLACE_LEVER 状态处理。
            state = State.PLACE_LEVER;
            // state.handle(this);  ← 不再直接调用
        } else {
            // 水平：先发旋转包同步yHeadRot，下一tick再放置
            BlockPlacingMethod method = BlockPlacingMethod.facing(best.facing);
            calculateFakeRotation(method);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                    fakeYaw, fakePitch, mc.player.onGround()));
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(pistonSlot));
            state = State.WAIT_Y_HEAD_ROT_SYNC;
        }
    }

    // ================================================================
    // ★ 第2步：handlePlaceLever —— 严格参考 CheatUtils BedrockBreaker.handlePlaceLever()
    // ================================================================
    private void handlePlaceLever() {
        assert mc.player != null && mc.level != null;

        int leverSlot = ensureInHotbar(Items.LEVER);
        if (leverSlot < 0) { reset("背包找不到拉杆"); return; }

        mc.player.connection.send(new ServerboundSetCarriedItemPacket(leverSlot));
        // ★ 下蹲+右键放置拉杆，避免打开容器 GUI
        sendUseItemOnSneak(InteractionHand.MAIN_HAND, leverPlaceHitResult, getSequenceNumber());

        // 链式调用：开始挖掘活塞
        state = State.BREAK_PISTON_START;
        state.handle(this);
    }

    // ================================================================
    // ★ 第3步：handleWaitYHeadRotSync —— 等待 yHeadRot 同步后放置正向活塞
    //
    //   上一 tick 已通过 fake 旋转包设置 yRot，
    //   此 tick 服务端 yHeadRot 已同步至 yRot，
    //   此时再发包放置活塞，确保活塞朝向正确。
    // ================================================================
    private void handleWaitYHeadRotSync() {
        assert mc.level != null && mc.player != null;

        // 上一tick已发送旋转包，这一tick yHeadRot已同步
        // ★ v2.9 正向活塞解耦：点击 bedrockPos（始终固体）
        //   参考 BlockPlacer.vanillaPistonPlacement2() 思路 + 反向活塞模式
        BlockPlacingMethod method = BlockPlacingMethod.facing(pistonFacing);
        calculateFakeRotation(method);
        Vec3 clickLoc = Vec3.atCenterOf(bedrockPos)
                .add(Vec3.atLowerCornerOf(pistonDirection.getNormal()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, pistonDirection, bedrockPos, false);
        mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                fakeYaw, fakePitch, mc.player.onGround()));
        sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, getSequenceNumber());

        // ★ 不在此tick链式调用 handlePlaceLever（v2.9 stateId 冲突修复）
        //   见 handleStart() 中竖直方向的注释。
        state = State.PLACE_LEVER;
        // state.handle(this);  ← 不再直接调用
    }

    // ================================================================
    // ★ 第4步：handleBreakPistonStart —— 严格参考 CheatUtils
    // ================================================================
    private void handleBreakPistonStart() {
        assert mc.level != null && mc.player != null;

        tickCount = 0;

        // ★ 预发送反向活塞旋转包（v2.5 侧向破基岩专用）
        //   矛盾定性：反向活塞放置与活塞破坏需同 tick 完成，无法加入等待状态。
        //   实践路线：在破坏活塞的第 1 tick 就预发送反向活塞的旋转包，
        //   此时距实际破坏活塞还有 N 个 tick（挖掘进度累计阶段），
        //   服务端有足够时间将 yRot 同步到 yHeadRot。
        //   当最终执行 PLACE_REVERSE_PISTON 时，yHeadRot 已正确同步。
        // ★ v2.8 解耦：反向活塞朝向 = pistonDirection.getOpposite()（朝向基岩）
        //   旧代码使用 pistonFacing.getOpposite()，当体方向≠伸出方向时朝向错误。
        if (pistonDirection.getOpposite().getAxis() != Direction.Axis.Y) {
            Direction reverseFacing = pistonDirection.getOpposite();
            BlockPlacingMethod revMethod = BlockPlacingMethod.facing(reverseFacing);
            Rotation revRot = revMethod.getTargetRotation();
            if (revRot != null) {
                float revYaw = Float.isNaN(revRot.yRot()) ? mc.player.getYRot() : revRot.yRot();
                float revPitch = Float.isNaN(revRot.xRot()) ? mc.player.getXRot() : revRot.xRot();
                mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                        revYaw, revPitch, mc.player.onGround()));
            }
        }

        // 切换镐子（严格参考 CheatUtils：使用 setSelectedSlot）
        int pickaxeSlot = findPickaxe();
        if (pickaxeSlot >= 0) {
            mc.player.getInventory().selected = pickaxeSlot;
        }
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(
                mc.player.getInventory().selected));

        blockDestroyProgress = getPistonDestroyProgress();
        blockDestroySeqNumber = getSequenceNumber();

        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                pistonPos,
                Direction.UP,
                blockDestroySeqNumber));

        state = State.BREAK_PISTON_PROGRESS;
        tickCount = 0;
    }

    // ================================================================
    // ★ 第4步：handleBreakPistonProgress —— 严格参考 CheatUtils
    //
    //   每 tick 累加 getPistonDestroyProgress()
    //   进度 >= 1 → 激活拉杆（活塞伸出）
    //
    //   ★ 信标急迫效果（Haste）：
    //     getDestroyProgress() 是 Minecraft 原版方法，自动考虑：
    //     - 手持物品的挖掘速度
    //     - 玩家状态效果（急迫、潮涌能量等）
    //     - 方块硬度
    //     因此无需额外处理，持有镐子 + 信标急迫效果即可大幅加速。
    // ================================================================
    private void handleBreakPistonProgress() {
        assert mc.level != null && mc.player != null;

        blockDestroyProgress += getPistonDestroyProgress();
        if (blockDestroyProgress >= 1) {
            // 激活拉杆 → 活塞伸出
            mc.player.connection.send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(leverPos), Direction.UP, leverPos, false),
                    getSequenceNumber()));

            state = State.WAIT_PISTON_EXTEND;
            tickCount = 0;
        } else {
            if (tickCount > 50) {
                reset("挖掘活塞超时（需要镐子 + 急迫效果）");
            }
        }
    }

    // ================================================================
    // ★★★ 第5步：handleWaitPistonExtend —— 核心！无头活塞时序控制 ★★★
    //   严格参考 CheatUtils BedrockBreaker.handleWaitPistonExtend()
    //
    // ★ 容错改进：遍历所有方向检测活塞头。
    //   第一个活塞朝向可能因玩家视角影响而不等于预期的方向，
    //   只要活塞被激活（出现活塞头），状态机就继续。
    //   pistonDirection = 活塞位置方向（基岩→活塞体），
    //   pistonFacing = 活塞伸出朝向，两者解耦。
    //   清理时使用实际朝向，但 pistonDirection/pistonFacing 保持不变。
    //
    //   检测到活塞头后，同一 tick 执行：
    //   ① 拉杆 OFF（活塞计划收回）
    //   ② STOP_DESTROY（移除活塞体 → 无头活塞诞生）
    //   ③ 客户端清理
    //   ④ 链式调用 PLACE_REVERSE_PISTON（同一 tick）
    // ================================================================
    private void handleWaitPistonExtend() {
        assert mc.player != null && mc.level != null;

        // ★ 遍历所有方向检测活塞头（不限制在 pistonFacing 方向）
        Direction actualPistonDir = null;
        for (Direction d : Direction.values()) {
            if (mc.level.getBlockState(pistonPos.relative(d)).getBlock() == Blocks.PISTON_HEAD) {
                actualPistonDir = d;
                break;
            }
        }

        if (actualPistonDir != null) {
            // ★ v2.7 侧向破基岩优化：检测到活塞头后，先预发反向活塞旋转包
            //   矛盾定性：反向活塞与 STOP_DESTROY 必须同 tick（否则无头活塞失效），
            //   但 yHeadRot 需要 1 tick 从 yRot 同步。
            //   实践路线：此 tick 先发旋转包设 yRot，下一 tick 再执行破坏+放置，
            //   此时 yHeadRot 已正确同步，且 STOP_DESTROY 与 PLACE_REVERSE_PISTON 仍同 tick。
            //   ★ v2.7 使用 pistonFacing 计算反向活塞朝向，不再依赖活塞位置方向。
            //   ★ v2.8 解耦：反向活塞朝向 = pistonDirection.getOpposite()（朝向基岩）
            if (pistonDirection.getOpposite().getAxis() != Direction.Axis.Y && !reverseRotSent) {
                Direction reverseFacing = pistonDirection.getOpposite();
                BlockPlacingMethod revMethod = BlockPlacingMethod.facing(reverseFacing);
                Rotation revRot = revMethod.getTargetRotation();
                if (revRot != null) {
                    float revYaw = Float.isNaN(revRot.yRot()) ? mc.player.getYRot() : revRot.yRot();
                    float revPitch = Float.isNaN(revRot.xRot()) ? mc.player.getXRot() : revRot.xRot();
                    mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                            revYaw, revPitch, mc.player.onGround()));
                }
                reverseRotSent = true;
                return; // 等待下一 tick 让 yHeadRot 同步
            }

            // ① 拉杆 OFF（收杆）
            mc.player.connection.send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(leverPos), Direction.UP, leverPos, false),
                    getSequenceNumber()));

            // ② STOP_DESTROY（移除活塞体 → 无头活塞诞生）
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    pistonPos,
                    Direction.UP,
                    blockDestroySeqNumber));

            // ★ 不在此处客户端销毁活塞头！
            //   反向活塞需要点击活塞头所在位置来放置。
            //   如果客户端先销毁了活塞头，点击位置变成空气，
            //   服务端可能将反向活塞放置在活塞头位置而非底座位置。
            //   客户端销毁延后到 handlePlaceReversePiston 执行。

            // ③ 链式调用：放置反向活塞（同一 tick）
            state = State.PLACE_REVERSE_PISTON;
            state.handle(this);
        } else {
            if (tickCount > 10) {
                reset("等待活塞伸出超时（活塞头未检测到）");
            }
        }
    }

    // ================================================================
    // ★★★ 第6步：handlePlaceReversePiston（同一 tick）★★★
    //   严格参考 CheatUtils BedrockBreaker.handlePlaceReversePiston()
    //
    //   使用 BlockPlacer.createPacketPlan() + plan.apply() 模式，
    //   与正向活塞放置保持一致，由 BlockPlacePlan.apply() 内部处理旋转。
    //
    //   反向活塞放在 pistonPos（同一位置）
    //   朝向 = pistonFacing.getOpposite()（朝向基岩）
    // ================================================================
    private void handlePlaceReversePiston() {
        assert mc.level != null && mc.player != null;

        int pistonSlot = findPistonSlot();
        if (pistonSlot < 0) { reset("背包找不到活塞（反向）"); return; }

        // ★ 反向活塞放置：点击基岩面（bedrockPos），而非活塞头位置
        //   createPacketPlan 默认点击 pistonPos.relative(pistonFacing)（活塞头位置），
        //   但正向活塞激活后该位置变为 MOVING_PISTON/PISTON_HEAD，服务端可能拒绝放置。
        //   改为点击 bedrockPos 的 pistonDirection 面，新方块放置到
        //   bedrockPos.relative(pistonDirection) = pistonPos（底座位置，紧贴基岩）。
        //   pistonDirection 是活塞位置方向（基岩→活塞），与朝向无关。
        // ★ v2.8 解耦修复：使用 pistonDirection 而非 pistonFacing
        //   旧实现：reverseFacing = pistonFacing.getOpposite()
        //           假设 pistonFacing == pistonDirection（耦合），
        //           粘性活塞垂直方向引入其他朝向组合时反向活塞朝向错误。
        //   修复：reverseFacing = pistonDirection.getOpposite()
        //         clickPos = bedrockPos（不论方向组合如何，点击位置固定为基岩）
        Direction reverseFacing = pistonDirection.getOpposite();
        BlockPos clickPos = bedrockPos;
        Direction clickFace = pistonDirection; // 点击基岩的 pistonDirection 面（活塞位置方向）
        Vec3 clickLoc = Vec3.atCenterOf(clickPos)
                .add(Vec3.atLowerCornerOf(clickFace.getNormal()).scale(0.5));
        BlockHitResult hit = new BlockHitResult(clickLoc, clickFace, clickPos, false);

        // 旋转：使用假旋转包，客户端视角不受影响
        //   活塞朝向 = getNearestLookingDirection().getOpposite() = reverseFacing
        BlockPlacingMethod method = BlockPlacingMethod.facing(reverseFacing);
        calculateFakeRotation(method);
        mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                fakeYaw, fakePitch, mc.player.onGround()));

        // 下蹲+右键放置反向活塞，避免打开容器 GUI
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(pistonSlot));
        sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, getSequenceNumber());

        // ★ 反向活塞放置成功后，客户端清理正向活塞头和底座
        mc.level.destroyBlock(pistonPos, false);
        mc.level.destroyBlock(pistonPos.relative(pistonFacing), false);

        state = State.WAIT_BEDROCK_BREAK;
        tickCount = 0;
    }

    // ================================================================
    // ★ 第6步：handleWaitBedrockBreak —— 严格参考 CheatUtils
    // ================================================================
    private void handleWaitBedrockBreak() {
        assert mc.player != null && mc.level != null;

        if (mc.level.getBlockState(bedrockPos).isAir()
                && !mc.level.getBlockState(pistonPos.relative(pistonFacing)).is(Blocks.MOVING_PISTON)) {
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();

            // 替换方块（如配置）- 使用 plan.apply() 模式
            if (cfg.replaceBlockId != null && !cfg.replaceBlockId.isEmpty()) {
                Block replaceBlock = ForgeRegistries.BLOCKS.getValue(
                        new net.minecraft.resources.ResourceLocation(cfg.replaceBlockId));
                if (replaceBlock != null && replaceBlock != Blocks.AIR) {
                    int repSlot = findItem(replaceBlock.asItem());
                    if (repSlot >= 0) {
                        BlockPlacer.BlockPlacePlan repPlan = BlockPlacer.createPacketPlan(
                                bedrockPos, BlockPlacingMethod.FROM_HORIZONTAL);
                        if (repPlan != null) {
                            mc.player.connection.send(new ServerboundSetCarriedItemPacket(repSlot));
                            repPlan.apply(mc.player.getYRot(), mc.player.getXRot());
                        }
                    }
                }
            }

            // ★ 清理辅助方块（v2.2 新增）：在清理拉杆/活塞前优先清理辅助方块
            //   清理顺序调整为：辅助方块 → 拉杆 → 活塞 → 活塞头，确保无残留
            if (cfg.cleanupHelpers && !helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : helperBlockPositions) {
                    if (!mc.level.getBlockState(helperPos).isAir()) {
                        mineBlock(helperPos);
                    }
                }
                helperBlockPositions.clear();
            }

            // 判断是否先清理拉杆
            if (mc.level.getBlockState(leverPos).is(Blocks.LEVER)) {
                state = State.BREAK_REMAINING_LEVER_START;
            } else {
                state = State.BREAK_REMAINING_PISTON_START;
            }
            state.handle(this);
            tickCount = 0;
        } else {
            if (tickCount > 20) {
                mineBlock(pistonPos);
                if (leverPos != null) mineBlock(leverPos);
                if (!helperBlockPositions.isEmpty()) {
                    helperBlockPositions.clear();
                }
                reset("等待基岩破坏超时");
            }
        }
    }

    // ================================================================
    // ★ 第7步：破坏剩余拉杆 —— 严格参考 CheatUtils
    // ================================================================
    private void handleBreakRemainingLeverStart() {
        assert mc.player != null;

        blockDestroyProgress = getLeverDestroyProgress();
        blockDestroySeqNumber = getSequenceNumber();

        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                leverPos,
                Direction.DOWN,
                blockDestroySeqNumber));

        state = State.BREAK_REMAINING_LEVER_PROGRESS;
    }

    private void handleBreakRemainingLeverProgress() {
        assert mc.player != null;

        blockDestroyProgress += getLeverDestroyProgress();
        if (blockDestroyProgress >= 1) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    leverPos,
                    Direction.DOWN,
                    blockDestroySeqNumber));

            state = State.BREAK_REMAINING_PISTON_START;
            state.handle(this);
        } else {
            if (tickCount > 30) {
                // 拉杆没破坏成功也继续到下一个状态（CheatUtils 原版行为）
                state = State.BREAK_REMAINING_PISTON_START;
                state.handle(this);
            }
        }
    }

    // ================================================================
    // ★ 第8步：破坏剩余活塞 —— 严格参考 CheatUtils
    // ================================================================
    private void handleBreakRemainingPistonStart() {
        assert mc.player != null;

        int pickaxeSlot = findPickaxe();
        if (pickaxeSlot >= 0) {
            mc.player.getInventory().selected = pickaxeSlot;
        }

        mc.player.connection.send(new ServerboundSetCarriedItemPacket(
                mc.player.getInventory().selected));

        blockDestroyProgress = getPistonDestroyProgress();
        blockDestroySeqNumber = getSequenceNumber();

        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                pistonPos,
                Direction.DOWN,
                blockDestroySeqNumber));

        state = State.BREAK_REMAINING_PISTON_PROGRESS;
        tickCount = 0;
    }

    private void handleBreakRemainingPistonProgress() {
        assert mc.player != null && mc.level != null;

        blockDestroyProgress += getPistonDestroyProgress();
        if (blockDestroyProgress >= 1) {
            mc.player.connection.send(new ServerboundPlayerActionPacket(
                    ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                    pistonPos,
                    Direction.DOWN,
                    blockDestroySeqNumber));

            mc.level.destroyBlock(pistonPos, false);

            // ★ 清理活塞头（v2.2 新增）：确保无头活塞的活塞头也被清理
            //   活塞头位于 pistonPos.relative(pistonFacing)
            //   使用 destroyBlock 客户端销毁 + mineBlock 服务端包发送
            BlockPos pistonHeadPos = pistonPos.relative(pistonFacing);
            if (!mc.level.getBlockState(pistonHeadPos).isAir()) {
                mc.level.destroyBlock(pistonHeadPos, false);
                mineBlock(pistonHeadPos);
            }

            // ★ 二次检测拉杆（v2.3 新增兜底）：
            //   若拉杆在 BREAK_REMAINING_LEVER 阶段超时跳过，此处兜底清理，确保无残留。
            if (leverPos != null && mc.level.getBlockState(leverPos).is(Blocks.LEVER)) {
                mineBlock(leverPos);
            }

            reset(null);

            // 处理下一个队列目标
            if (!queue.isEmpty()) start(queue.remove());
        } else {
            if (tickCount > 50) {
                reset("清理剩余活塞超时");
            }
        }
    }

    // ================================================================
    // ★ 寻找拉杆位置 —— 严格参考 CheatUtils BedrockBreaker.findLocationForLever()
    //
    //   策略1：围绕基岩找位置（排除活塞方向和伸出方向）
    //   策略2：围绕活塞找位置（排除活塞方向和反方向、伸出方向，不贴在活塞上）
    //   策略3：围绕活塞头四个侧向方向找（v2.2 新增）
    //          活塞头位于 pistonPos.relative(pistonFacing)，
    //          拉杆放置在活塞头相邻方块上，通过充能该方块激活活塞。
    //          → 参考来源：Minecraft Wiki 活塞激活机制（准链接）
    // ================================================================
    private boolean findLocationForLever() {
        assert mc.level != null && mc.player != null;

        leverPos = null;

        // 策略1：围绕基岩找
        BlockPos pistonHeadPos_ = pistonPos.relative(pistonFacing);
        for (Direction direction : sortByDistance(bedrockPos, Direction.values())) {
            BlockPos possibleLeverPos = bedrockPos.relative(direction);
            if (direction == pistonDirection) continue;
            // ★ v2.7 排除活塞伸出方向位置（防止拉杆与活塞头重叠）
            if (possibleLeverPos.equals(pistonHeadPos_)) continue;
            if (!mc.level.getBlockState(possibleLeverPos).canBeReplaced()) continue;
            // ★ v2.8 排除流体位置（拉杆放置在流体中会掉落）
            if (!mc.level.getFluidState(possibleLeverPos).isEmpty()) continue;
            if (!isValidY(possibleLeverPos.getY())) continue;
            if (isInvalidLeverSupport(bedrockPos)) continue;

            BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(bedrockPos).add(Vec3.atLowerCornerOf(direction.getNormal()).scale(0.5)),
                    direction,
                    bedrockPos,
                    false);
            BlockState leverBlockState = Blocks.LEVER.getStateForPlacement(
                    new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND,
                            new ItemStack(Items.LEVER, 1), hit));
            if (leverBlockState == null) continue;
            if (isLeverStateMatch(leverBlockState, direction)) {
                leverPos = possibleLeverPos;
                leverPlaceHitResult = hit;
                return true;
            }
        }

        // 策略2：围绕活塞找（不贴在活塞上，不在活塞伸出方向）
        for (Direction direction : sortByDistance(pistonPos, Direction.values())) {
            if (direction == pistonDirection) continue;
            if (direction == pistonDirection.getOpposite()) continue;
            // ★ v2.7 排除活塞伸出方向（防止拉杆与活塞头重叠）
            if (direction == pistonFacing) continue;

            BlockPos possibleLeverPos = pistonPos.relative(direction);
            if (!mc.level.getBlockState(possibleLeverPos).canBeReplaced()) continue;
            // ★ v2.8 排除流体位置（拉杆放置在流体中会掉落）
            if (!mc.level.getFluidState(possibleLeverPos).isEmpty()) continue;
            if (!isValidY(possibleLeverPos.getY())) continue;

            for (Direction dir : sortByDistance(possibleLeverPos, Direction.values())) {
                BlockPos possibleSupportPos = possibleLeverPos.relative(dir);
                if (possibleSupportPos.equals(pistonPos)) continue; // 不贴在活塞上
                if (mc.level.getBlockState(possibleSupportPos).canBeReplaced()) continue;
                if (isInvalidLeverSupport(possibleSupportPos)) continue; // 无效附着方块跳过

                BlockHitResult hit = new BlockHitResult(
                        Vec3.atCenterOf(possibleSupportPos)
                                .add(Vec3.atLowerCornerOf(dir.getOpposite().getNormal()).scale(0.5)),
                        dir.getOpposite(),
                        possibleSupportPos,
                        false);
                BlockState leverBlockState = Blocks.LEVER.getStateForPlacement(
                        new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND,
                                new ItemStack(Items.LEVER, 1), hit));
                if (leverBlockState == null) continue;
                if (isLeverStateMatch(leverBlockState, dir.getOpposite())) {
                    leverPos = possibleLeverPos;
                    leverPlaceHitResult = hit;
                    return true;
                }
            }
        }

        // ★ 策略3：围绕活塞头四个侧向方向找（v2.4 重构，修复对角方块 bug）
        //   活塞头位于 pistonPos.relative(pistonFacing)，
        //   拉杆放在活塞头侧向（candidatePos，需空气），附着在活塞水平相邻方块（supportPos，需固体）。
        //
        //   ★ 红石激活原理：
        //     拉杆强充能其附着的方块（supportPos）。
        //     被强充能的 supportPos 激活其相邻的机械元件（活塞）。
        //     因此 supportPos 必须与 pistonPos 相邻（6面相邻）。
        //
        //   ★ v2.4 修复对角方块 bug：
        //     旧实现遍历 candidatePos 的所有6方向找 supportPos，会找到对角位置的固体方块。
        //     对角方块与活塞不相邻，充能后无法激活活塞。
        //     修复：supportPos 固定为 pistonPos.relative(lateral)（活塞水平相邻，与活塞相邻）。
        //     几何关系：candidatePos = pistonHeadPos.relative(lateral)
        //                       = pistonPos.relative(pistonFacing).relative(lateral)
        //     supportPos = pistonPos.relative(lateral)
        //     candidatePos = supportPos.relative(pistonFacing)（supportPos 沿活塞朝向再走一格）
        //     → clickFace = pistonFacing（从 supportPos 指向 candidatePos）
        //     → 拉杆放在 candidatePos，附着在 supportPos 的 pistonFacing 面
        //     → 拉杆充能 supportPos，supportPos 与 pistonPos 相邻 ✓，激活活塞 ✓
        BlockPos pistonHeadPos = pistonPos.relative(pistonFacing);
        for (Direction lateral : getPistonHeadLateralDirections()) {
            BlockPos candidatePos = pistonHeadPos.relative(lateral);
            if (!mc.level.getBlockState(candidatePos).canBeReplaced()) continue;
            // ★ v2.8 排除流体位置（拉杆放置在流体中会掉落）
            if (!mc.level.getFluidState(candidatePos).isEmpty()) continue;
            if (!isValidY(candidatePos.getY())) continue;

            // ★ supportPos 固定为活塞水平相邻方块（不遍历对角方向）
            BlockPos supportPos = pistonPos.relative(lateral);
            if (supportPos.equals(pistonHeadPos)) continue; // 防御性检查
            if (mc.level.getBlockState(supportPos).canBeReplaced()) continue; // 需固体方块
            if (isInvalidLeverSupport(supportPos)) continue; // 无效附着方块跳过

            // 点击 supportPos 的 pistonFacing 面（朝向 candidatePos）
            // 新方块位置 = supportPos.relative(pistonFacing) = candidatePos ✓
            Direction clickFace = pistonFacing;
            BlockHitResult hit = new BlockHitResult(
                    Vec3.atCenterOf(supportPos)
                            .add(Vec3.atLowerCornerOf(clickFace.getNormal()).scale(0.5)),
                    clickFace,
                    supportPos,
                    false);
            BlockState leverBlockState = Blocks.LEVER.getStateForPlacement(
                    new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND,
                            new ItemStack(Items.LEVER, 1), hit));
            if (leverBlockState == null) continue;
            if (isLeverStateMatch(leverBlockState, clickFace)) {
                leverPos = candidatePos;
                leverPlaceHitResult = hit;
                return true;
            }
        }

        return false;
    }

    // ============ 辅助方法 ============

    /**
     * 校验拉杆附着方块是否为无效类型
     *
     * 无效类型分为两类：
     *   1. 激活后拉杆会掉落的方块：活塞、粘液活塞、门、活板门
     *      这些方块被活塞推动或开关状态变化时，附着的拉杆会掉落为物品。
     *   2. 能放置拉杆但无法激活活塞的方块：半砖、楼梯
     *      这些方块不是完整固体方块，拉杆放置后无法强充能方块激活活塞。
     *
     * @param supportPos 附着方块位置
     * @return true 如果该方块不能直接放置拉杆（需使用辅助方块方案）
     */
    private boolean isInvalidLeverSupport(BlockPos supportPos) {
        if (mc.level == null) return true;
        BlockState state = mc.level.getBlockState(supportPos);
        Block block = state.getBlock();
        return block instanceof PistonBaseBlock
                || block instanceof DoorBlock
                || block instanceof TrapDoorBlock
                || block instanceof StairBlock
                || block instanceof SlabBlock
                // ★ v2.8 堆肥桶等方块无法传递红石信号，附着拉杆后无法激活活塞
                || block instanceof ComposterBlock;
    }

    /**
     * 校验拉杆放置方向是否匹配指定方向
     */
    private boolean isLeverStateMatch(BlockState state, Direction direction) {
        return switch (direction) {
            case UP -> state.getValue(LeverBlock.FACE) == AttachFace.FLOOR;
            case DOWN -> state.getValue(LeverBlock.FACE) == AttachFace.CEILING;
            default -> state.getValue(LeverBlock.FACE) == AttachFace.WALL
                    && state.getValue(LeverBlock.FACING) == direction;
        };
    }

    /**
     * ★ 挖掘活塞的进度（含急迫效果）
     *
     *   getDestroyProgress() 是 Minecraft 原版方法，自动计算：
     *   - 手持物品对当前方块的挖掘速度（镐子远快于空手）
     *   - 玩家状态效果：急迫（Haste）加速，挖掘疲劳（Mining Fatigue）减速
     *   - 方块硬度
     *   - 水下挖掘惩罚
     *
     *   因此，仅需持有镐子 + 信标急迫效果即可大幅加速挖掘。
     *   每 tick 返回的进度值会被累加到 blockDestroyProgress。
     */
    private float getPistonDestroyProgress() {
        assert mc.level != null && mc.player != null;
        return Blocks.PISTON.defaultBlockState().getDestroyProgress(mc.player, mc.level, pistonPos);
    }

    private float getLeverDestroyProgress() {
        assert mc.level != null && mc.player != null;
        return Blocks.LEVER.defaultBlockState().getDestroyProgress(mc.player, mc.level, leverPos);
    }

    /**
     * ★ 计算假旋转（fakeYaw / fakePitch）
     *   根据 BlockPlacingMethod 的目标旋转值，计算服务端朝向。
     *   NaN 分量保持玩家当前真实值，客户端视角完全不受影响。
     */
    private void calculateFakeRotation(BlockPlacingMethod method) {
        Rotation targetRot = method.getTargetRotation();
        if (targetRot != null) {
            fakeYaw = Float.isNaN(targetRot.yRot()) ? mc.player.getYRot() : targetRot.yRot();
            fakePitch = Float.isNaN(targetRot.xRot()) ? mc.player.getXRot() : targetRot.xRot();
        }
    }

    /**
     * 校验目标方块是否合法（支持 allBlocks 模式）
     */
    private boolean isValidBlock(BlockPos pos) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        if (mc.level == null) return false;
        if (cfg.allBlocks) return true;  // allBlocks 模式：对所有硬方块生效
        Block target = ForgeRegistries.BLOCKS.getValue(
                new net.minecraft.resources.ResourceLocation(cfg.targetBlockId));
        return target != null && mc.level.getBlockState(pos).is(target);
    }

    /**
     * ★ 获取活塞头的侧向方向（回退单方向耦合，仍保留 pistonFacing 字段）
     *
     *   对于垂直方向活塞（UP/DOWN），侧向为四个水平方向：
     *     活塞头在上/下方，拉杆可以放在四个水平侧面相邻方块的底面/顶面。
     *   对于水平方向活塞（N/S/E/W），侧向包括 UP/DOWN 及另两个水平方向：
     *     活塞头在水平方向，拉杆可以放在上下或侧面相邻方块的侧面。
     *
     *   → 注：此处返回的是与活塞朝向（pistonFacing）垂直的所有方向，
     *     用于在活塞头周围找拉杆放置位置（策略3）。
     */
    private Direction[] getPistonHeadLateralDirections() {
        if (pistonFacing == null) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        // 垂直活塞：侧向为四个水平方向
        if (pistonFacing.getAxis() == Direction.Axis.Y) {
            return new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        }
        // 水平活塞：侧向为 UP/DOWN + 除朝向外的另一水平轴向
        Direction.Axis axis = pistonFacing.getAxis();
        if (axis == Direction.Axis.X) {
            return new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH};
        } else { // Z axis（NORTH/SOUTH）
            return new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST};
        }
    }

    /**
     * ★ 尝试放置辅助方块（v2.3 重构）
     *
     *   ★ 矛盾定性：辅助方块放置位置不准确 + 放置数量失控
     *     旧实现使用 BlockPlacer.FROM_HORIZONTAL（点击上方方块下表面），
     *     若辅助方块上方为空气则放置失败或位置偏移；且 isAdjacent 检查失败时
     *     仅从列表移除但不挖掘已放置方块，导致循环放置多个。
     *
     *   ★ 实践路线：
     *     1. 直接点击基岩的 dir 面放置辅助方块到 bedrockPos.relative(dir)，
     *        基岩一定是固体方块，点击面可靠。
     *     2. 每个方向只放一个辅助方块，放置后立即检测拉杆位置。
     *     3. 成功则返回（只放一个）；失败则挖掘掉辅助方块再试下一个方向。
     *
     *   @return true 若放置后成功找到拉杆位置
     */
    private boolean tryPlaceHelperBlocks() {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        assert mc.level != null && mc.player != null;

        String[] blockIds = cfg.helperBlockList.split(",");

        // ★ v2.6 适配侧向破基岩：根据活塞朝向动态选择辅助方块放置方向
        //   垂直活塞（UP/DOWN）：辅助方块放在基岩前后左右四个水平方向
        //   水平活塞（N/S/E/W）：辅助方块放在基岩上下 + 与活塞垂直的两个水平方向
        //   → 跳过活塞方向和反方向，优先保证辅助方块紧贴活塞提供拉杆附着面
        List<Direction> searchDirs = new ArrayList<>();
        if (pistonDirection.getAxis() == Direction.Axis.Y) {
            searchDirs.addAll(Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        } else {
            searchDirs.add(Direction.UP);
            searchDirs.add(Direction.DOWN);
            for (Direction d : Arrays.asList(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                if (d.getAxis() != pistonDirection.getAxis()) {
                    searchDirs.add(d);
                }
            }
        }

        // ★ 按距离玩家由近到远排序，优先放置最近的辅助方块
        searchDirs.sort(Comparator.comparingDouble(d ->
                mc.player.distanceToSqr(Vec3.atCenterOf(bedrockPos.relative(d)))));

        for (Direction dir : searchDirs) {
            BlockPos helperPos = bedrockPos.relative(dir);
            // 该位置必须可替换（空气/水/草等）
            if (!mc.level.getBlockState(helperPos).canBeReplaced()) continue;
            if (!isValidY(helperPos.getY())) continue;

            // 按配置列表优先级尝试每种方块
            boolean placed = false;
            for (String blockId : blockIds) {
                blockId = blockId.trim();
                if (blockId.isEmpty()) continue;

                Block helperBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                if (helperBlock == null || helperBlock == Blocks.AIR) continue;
                // 必须是固体方块才能提供拉杆附着面
                if (!helperBlock.defaultBlockState().isSolid()) continue;

                Item helperItem = helperBlock.asItem();
                int hotbarSlot = ensureInHotbar(helperItem);
                if (hotbarSlot < 0) continue; // 背包没有该方块，尝试下一个

                // ★ 放置辅助方块：点击基岩的 dir 面
                //   新方块位置 = bedrockPos.relative(dir) = helperPos ✓
                //   基岩一定是固体方块，点击面可靠（不依赖上方方块）
                Vec3 clickLoc = Vec3.atCenterOf(bedrockPos)
                        .add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
                BlockHitResult hit = new BlockHitResult(clickLoc, dir, bedrockPos, false);

                mc.player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
                sendUseItemOnSneak(InteractionHand.MAIN_HAND, hit, getSequenceNumber());

                // ★ 客户端预测性更新方块状态（v2.4 关键修复）
                //   放置包发送后服务端需1tick确认，同tick内 mc.level.getBlockState(helperPos)
                //   仍是空气，导致 findLocationForLever() 检测不到辅助方块（策略2跳过）。
                //   手动 setBlockAndUpdate 使同tick检测能识别辅助方块为固体，拉杆可附着。
                //   若服务端最终拒绝放置，后续 reset 会清理，状态不一致风险可控。
                mc.level.setBlockAndUpdate(helperPos, helperBlock.defaultBlockState());

                placed = true;
                break; // 成功放置一种方块即可
            }

            if (!placed) continue; // 这个方向放不下，试下一个

            // 记录辅助方块位置
            helperBlockPositions.add(helperPos.immutable());

            // ★ 放置后重新检测拉杆位置
            if (findLocationForLever() && leverPos != null) {
                // 成功找到拉杆位置，只放了一个辅助方块，立即返回
                return true;
            }

            // ★ 没找到拉杆位置：挖掘掉刚放的辅助方块，试下一个方向
            //   旧实现仅从列表移除不挖掘，导致多个辅助方块残留（"放了四个"的根因）
            mineBlock(helperPos);
            helperBlockPositions.remove(helperPos.immutable());
        }

        return false;
    }

    /**
     * ★ 下蹲状态下发送放置包，避免打开容器 GUI
     *   ServerboundPlayerCommandPacket.PRESS_SHIFT_KEY 使服务端认为玩家在潜行，
     *   潜行时右击容器不会打开 GUI，而是正常放置方块。
     *   发包顺序：PRESS_SHIFT → UseItemOn → RELEASE_SHIFT，在同 tick 完成。
     */
    private void sendUseItemOnSneak(InteractionHand hand, BlockHitResult hitResult, int sequence) {
        mc.player.connection.send(new ServerboundPlayerCommandPacket(
                mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        mc.player.connection.send(new ServerboundUseItemOnPacket(hand, hitResult, sequence));
        mc.player.connection.send(new ServerboundPlayerCommandPacket(
                mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
    }

    /**
     * ★ 确保物品在快捷栏，若不在则从背包自动移至快捷栏。
     *   优先使用空槽位，无空位则覆盖当前选中的槽位（快捷栏物品会被换到背包原位置）。
     *
     * @param item 目标物品
     * @return 快捷栏槽位（0-8），-1 表示全背包未找到
     */
    private int ensureInHotbar(Item item) {
        // 1. 快捷栏搜索
        int slot = findItem(item);
        if (slot >= 0) return slot;

        if (mc.player == null) return -1;

        var inventory = mc.player.getInventory();
        // 2. 背包搜索（9-35）
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).is(item)) {
                // 2a. 优先找空的快捷栏槽位
                int targetSlot = -1;
                for (int j = 0; j < 9; j++) {
                    if (inventory.getItem(j).isEmpty()) {
                        targetSlot = j;
                        break;
                    }
                }
                if (targetSlot < 0) {
                    targetSlot = inventory.selected; // 无空位则用当前槽
                }

                // 2b. 发送 SWAP 包，将背包物品与快捷栏交换
                int containerSlot = i;
                int hotbarContainerSlot = 36 + targetSlot;
                // ★ v2.10 使用本地预测的 stateId（取 max 与服务端同步）
                int serverStateId = mc.player.containerMenu.getStateId();
                int stateId = Math.max(serverStateId, predictedContainerStateId);
                mc.player.connection.send(new ServerboundContainerClickPacket(
                        0, // 玩家背包容器
                        stateId,
                        containerSlot,
                        targetSlot, // button = 目标快捷栏编号
                        ClickType.SWAP,
                        ItemStack.EMPTY,
                        new Int2ObjectOpenHashMap<>(Map.of(
                                containerSlot, inventory.getItem(targetSlot).copy(),
                                hotbarContainerSlot, inventory.getItem(i).copy()
                        ))
                ));
                // ★ v2.10 本地预测 stateId 递增（核心修复：解决跨 tick 第二个 SWAP 被拒）
                //   矛盾定性：服务端处理 SWAP 后 stateId 自增 1，但客户端 stateId
                //   需等服务端回复（ClientboundContainerSetSlot）才更新。
                //   若下一 tick 再次调用 ensureInHotbar（如 handlePlaceLever 中放拉杆），
                //   此时客户端 stateId 可能仍是旧值（服务端回复尚未到达），
                //   导致第二个 SWAP 被服务端拒绝，快捷栏实际仍为活塞，
                //   后续放置操作误放第二个活塞。
                //   实践路线：本地预测 stateId 增量，无需等待服务端回复。
                //   predictedContainerStateId 取 max 可同时覆盖服务端已回复的增量。
                predictedContainerStateId = stateId + 1;

                // 2c. 客户端同步
                ItemStack temp = inventory.getItem(targetSlot).copy();
                inventory.items.set(targetSlot, inventory.getItem(i).copy());
                inventory.items.set(i, temp);
                inventory.selected = targetSlot;

                return targetSlot;
            }
        }
        return -1;
    }

    /**
     * 在热栏搜索物品（仅热栏 0-8，与 CheatUtils 一致）
     */
    private int findItem(Item item) {
        assert mc.player != null;
        var inventory = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(item)) return i;
        }
        return -1;
    }

    /**
     * ★ 找活塞物品（v2.7 新增）：优先普通活塞，后备粘液活塞
     *
     *   与 ensureInHotbar 不同，此方法在背包中搜索两种活塞：
     *   1. 优先找普通活塞（Items.PISTON）
     *   2. 未找到则找粘液活塞（Items.STICKY_PISTON）
     *   3. 两种都找不到返回 -1
     *
     *   @return 快捷栏槽位（0-8），-1 表示全背包未找到
     */
    @SuppressWarnings("deprecation")
    private int findPistonSlot() {
        int slot = ensureInHotbar(Items.PISTON);
        if (slot >= 0) return slot;
        slot = ensureInHotbar(Items.STICKY_PISTON);
        return slot;
    }

    /**
     * 在热栏或背包搜索镐子（与 CheatUtils 一致）
     */
    private int findPickaxe() {
        assert mc.player != null;
        var inventory = mc.player.getInventory();
        // 搜快捷栏
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).getTags().anyMatch(tag -> tag == ItemTags.PICKAXES)) {
                return i;
            }
        }
        // 搜背包（9-35），找到后直接换到快捷栏空位或当前槽
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).getTags().anyMatch(tag -> tag == ItemTags.PICKAXES)) {
                int targetSlot = -1;
                for (int j = 0; j < 9; j++) {
                    if (inventory.getItem(j).isEmpty()) { targetSlot = j; break; }
                }
                if (targetSlot < 0) targetSlot = inventory.selected;

                int containerSlot = i;
                int hotbarContainerSlot = 36 + targetSlot;
                // ★ v2.10 使用本地预测的 stateId（取 max 与服务端同步）
                int serverStateId = mc.player.containerMenu.getStateId();
                int stateId = Math.max(serverStateId, predictedContainerStateId);
                mc.player.connection.send(new ServerboundContainerClickPacket(
                        0, stateId,
                        containerSlot, targetSlot, ClickType.SWAP,
                        ItemStack.EMPTY,
                        new Int2ObjectOpenHashMap<>(Map.of(
                                containerSlot, inventory.getItem(targetSlot).copy(),
                                hotbarContainerSlot, inventory.getItem(i).copy()
                        ))
                ));
                // ★ v2.10 本地预测 stateId 递增（同 ensureInHotbar 修复）
                predictedContainerStateId = stateId + 1;
                ItemStack temp = inventory.getItem(targetSlot).copy();
                inventory.items.set(targetSlot, inventory.getItem(i).copy());
                inventory.items.set(i, temp);
                inventory.selected = targetSlot;
                return targetSlot;
            }
        }
        return -1;
    }

    /**
     * ★ 快速挖掘方块（发送 START_DESTROY + STOP_DESTROY）
     *   用于清理残留的拉杆等即时挖掘方块。
     */
    private void mineBlock(BlockPos pos) {
        if (mc.player == null || mc.level == null) return;
        if (mc.level.getBlockState(pos).isAir()) return;

        // ★ 使用相同序列号：START_DESTROY 和 STOP_DESTROY 必须使用相同 seq，
        //   否则服务端无法匹配，挖掘操作不会生效。
        int seq = getSequenceNumber();
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                pos, Direction.DOWN, seq));
        mc.player.connection.send(new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                pos, Direction.DOWN, seq));
        mc.level.destroyBlock(pos, false);
    }

    /**
     * ★ 获取预测序列号 —— 严格参考 CheatUtils
     *   使用 BlockStatePredictionHandler.currentSequence()，
     *   确保服务端能正确匹配客户端预测。此序列号用于：
     *   - ServerboundPlayerActionPacket（START_DESTROY_BLOCK / STOP_DESTROY_BLOCK）
     *   - ServerboundUseItemOnPacket（放置方块、交互拉杆）
     *
     *   不使用 BlockStatePredictionHandler 的序列号将导致
     *   服务端拒绝所有数据包，功能完全失效。
     */
    private int getSequenceNumber() {
        assert mc.level != null;
        BlockStatePredictionHandler handler = ((ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    /**
     * 校验 Y 坐标是否在当前维度有效范围内
     */
    private boolean isValidY(int y) {
        assert mc.level != null;
        DimensionType dimension = mc.level.dimensionType();
        return dimension.minY() <= y && y < dimension.minY() + dimension.height();
    }

    /**
     * 方向按玩家距离排序（与 CheatUtils 一致）
     */
    private Direction[] sortByDistance(BlockPos origin, Direction... directions) {
        assert mc.player != null;
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        return Arrays.stream(directions)
                .map(d -> new AbstractMap.SimpleEntry<>(d,
                        origin.relative(d).distToCenterSqr(eyePos)))
                .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .toArray(Direction[]::new);
    }

    /**
     * ★ v2.8 按配置优先级排序方向
     *   HORIZONTAL_FIRST：水平方向（N/S/E/W）排在前，垂直方向（UP/DOWN）排在后
     *   VERTICAL_FIRST：垂直方向排在前，水平方向排在后
     *   DEFAULT：保持原顺序（距离排序优先）
     */
    private Direction[] sortByPriority(Direction[] directions) {
        BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
        String priority = cfg.pistonDirectionPriority;
        if ("DEFAULT".equals(priority)) {
            return directions;
        }
        boolean horizontalFirst = "HORIZONTAL_FIRST".equals(priority);
        return Arrays.stream(directions)
                .sorted(Comparator.comparingInt(d -> {
                    boolean isHorizontal = d.getAxis().isHorizontal();
                    return horizontalFirst ? (isHorizontal ? 0 : 1) : (isHorizontal ? 1 : 0);
                }))
                .toArray(Direction[]::new);
    }

    /**
     * 检查目标方块在指定方向是否可放置活塞（底座+头部两格空间）
     * 用于方向选择时的快速判断
     */
    private boolean canPlacePiston(BlockPos bedrockPos, Direction d) {
        assert mc.level != null;
        BlockPos p1 = bedrockPos.relative(d);
        if (!mc.level.getBlockState(p1).canBeReplaced()) return false;
        BlockPos p2 = p1.relative(d);
        if (!mc.level.getBlockState(p2).canBeReplaced()) return false;
        return isValidY(p2.getY());
    }

    /** 切换快捷栏槽位 */
    @SuppressWarnings("unused")
    private void selectSlot(int slot) {
        if (mc.player == null) return;
        mc.player.getInventory().selected = slot;
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
    }

    // ================================================================
    // ★ v2.8 启发式搜索：最佳活塞放置方案
    //   参考 ApproachBase.findBest() 的搜索+评分策略
    //   (https://github.com/lxyan2333/bedrockminer)
    //
    //   ★ 核心思想（抓主要矛盾）：
    //     旧实现在 handleStart() 中首匹配方向 + findLocationForLever() 三策略顺序查找，
    //     遇到垂直粘性活塞时方向组合+拉杆放置可能不可行就直接跳过，导致失败。
    //     新实现统一搜索所有方向组合，按质量评分缓存最优解，确保粘性活塞各方向（含上下）都有候选。
    //
    //   ★ 评分规则（实践论——量化评价）：
    //     score=0（完美）：活塞臂不与玩家碰撞，无需辅助支撑方块
    //     score=1（需支撑）：需要在 leverPos 放置辅助方块提供附着面（保留，未来扩展）
    //     score=2（碰撞）：活塞臂与玩家碰撞箱相交（方案可用但玩家需移动一小步）
    //     null：该组合不可行（空间被占用 / 拉杆无有效附着面）
    //
    //   ★ 回退机制：
    //     搜索命中 score=0 时立即返回（最优解），否则缓存在 candidate[] 中。
    //     全部搜索完成后按 score 升序返回缓存中的最佳方案。
    //     所有组合均不可行时返回 null，由 handleStart 触发 reset。
    // ================================================================

    /**
     * 活塞放置候选 —— 包含活塞体方向/朝向、拉杆位置/点击、评分
     */
    static class PlacementCandidate {
        final Direction bodyDir;         // 活塞体位置方向（基岩→活塞体）
        final Direction facing;          // 活塞伸出方向
        final BlockPos pistonPos;        // 活塞体位置
        final BlockPos leverPos;         // 拉杆放置位置
        final BlockHitResult leverHit;   // 拉杆放置点击结果
        final int score;                 // 0=完美，1=需支撑，2=碰撞

        PlacementCandidate(Direction bodyDir, Direction facing, BlockPos pistonPos,
                           BlockPos leverPos, BlockHitResult leverHit, int score) {
            this.bodyDir = bodyDir;
            this.facing = facing;
            this.pistonPos = pistonPos;
            this.leverPos = leverPos;
            this.leverHit = leverHit;
            this.score = score;
        }
    }

    /**
     * ★ 启发式搜索：在所有方向组合中找最佳活塞+拉杆放置方案
     *
     *   搜索空间（6×6=36 组合，参考 VanillaAllDirectionApproach）：
     *     体方向（6）× 伸出方向（6）× 拉杆位置（多策略）
     *     = 最多约 36×N 种组合，由 quality 评分+缓存回退保证最优选择
     *
     *   ★ v2.9 正向活塞解耦（参考 VanillaAllDirectionApproach.findBest）：
     *     正向活塞放置改为点击 bedrockPos（始终固体），详见 handleStart() 和
     *     handleWaitYHeadRotSync() 中的自定义 BlockHitResult。
     *     因此解除 facing 与 bodyDir 的耦合限制，支持所有 6×6=36 种方向组合。
     *
     *   每个组合的效验过程：
     *     ① pistonPos（基岩附近）必须可替换
     *     ② extendPos（活塞头位置）必须可替换
     *     ③ findLocationForLever() 搜索拉杆位置（多策略）
     *     ④ 对有效组合调用质量评分
     *
     *   评分机制（与 ApproachBase.quality() 一致）：
     *     0 = 完美：无碰撞 → 立即返回
     *     2 = 碰撞：活塞臂与玩家碰撞箱相交 → 缓存，继续搜索
     *     null = 不可行 → 跳过
     *
     *   调用约束：
     *     必须在 handleStart 中调用，调用前 bedrockPos 已确定。
     *     返回后需将结果赋值给类字段（pistonDirection, pistonFacing 等）。
     *
     * @return 最佳候选，或 null（全部不可行）
     */
    private PlacementCandidate findBestPistonPlacement() {
        assert mc.player != null && mc.level != null;

        // 1. 构建所有体方向列表（6方向），按距离排序 + 优先级重排
        Direction[] bodyDirs = {
                Direction.UP, Direction.DOWN,
                Direction.NORTH, Direction.SOUTH,
                Direction.EAST, Direction.WEST
        };
        bodyDirs = sortByDistance(bedrockPos, bodyDirs);
        bodyDirs = sortByPriority(bodyDirs);

        // ★ 候选缓存：candidate[score] = 该评分首次遇到的方案
        //   长度 3，下标 0,1,2 对应评分 0,1,2
        PlacementCandidate[] candidates = new PlacementCandidate[3];

        // 2. 遍历所有体方向（作为活塞位置方向）
        for (Direction bd : bodyDirs) {
            BlockPos pPos = bedrockPos.relative(bd);
            if (!mc.level.getBlockState(pPos).canBeReplaced()) continue;
            if (!isValidY(pPos.getY())) continue;

            // 3. ★ v2.9 正向活塞解耦：体方向×伸出方向=36组合
            //   参考 VanillaAllDirectionApproach.findBest() 策略
            //   (https://github.com/lxyan2333/bedrock-miner)
            //   借鉴点：正向活塞放置改为点击 bedrockPos（始终固体），
            //   彻底消除 facing 与 bodyDir 的耦合限制。
            //   见 handleStart() / handleWaitYHeadRotSync() 中的自定义 BlockHitResult。
            //   只要 extendPos 可替换，任何 facing 组合均可行——无需担心 clickPos 为空气。
            Direction[] facings = {
                    Direction.UP, Direction.DOWN,
                    Direction.NORTH, Direction.SOUTH,
                    Direction.EAST, Direction.WEST
            };
            facings = sortByDistance(pPos, facings);

            for (Direction f : facings) {
                BlockPos extPos = pPos.relative(f);
                if (!mc.level.getBlockState(extPos).canBeReplaced()) continue;
                if (!isValidY(extPos.getY())) continue;

                // 4. ★ 保存当前状态 & 临时赋值，供 findLocationForLever 读取
                //    使用 save/restore 模式避免副作用
                Direction savedBodyDir = pistonDirection;
                Direction savedFacing = pistonFacing;
                BlockPos savedPistonPos = this.pistonPos;
                BlockPos savedLeverPos = leverPos;
                BlockHitResult savedLeverHit = leverPlaceHitResult;

                pistonDirection = bd;
                pistonFacing = f;
                this.pistonPos = pPos;
                leverPos = null;
                leverPlaceHitResult = null;

                boolean leverFound = findLocationForLever();
                BlockPos foundLeverPos = leverPos;
                BlockHitResult foundLeverHit = leverPlaceHitResult;

                // ★ 恢复状态（消除 findLocationForLever 的副作用）
                pistonDirection = savedBodyDir;
                pistonFacing = savedFacing;
                this.pistonPos = savedPistonPos;
                leverPos = savedLeverPos;
                leverPlaceHitResult = savedLeverHit;

                if (!leverFound || foundLeverPos == null) {
                    continue; // 不可行
                }

                // 5. ★ 质量评分（v2.10 新增活塞体位碰撞检测）
                //   矛盾定性：活塞体位置（pPos）与玩家碰撞箱重叠时，玩家卡入方块，
                //   轻则遮挡视野，重则触发窒息伤害，必须排除。
                //   实践路线：先检活塞体碰撞（致命），再检伸缩方向碰撞（可降级容忍）。
                AABB pistonBodyBox = new AABB(pPos);
                if (mc.player.getBoundingBox().intersects(pistonBodyBox)) {
                    continue; // 活塞体位与玩家碰撞 → 不可行，换下一个组合
                }
                int score;
                AABB extendBox = new AABB(extPos);
                if (mc.player.getBoundingBox().intersects(extendBox)) {
                    score = 2; // 伸缩位碰撞：方案可用但玩家需移动
                } else {
                    score = 0; // 完美
                }

                PlacementCandidate cand = new PlacementCandidate(
                        bd, f, pPos, foundLeverPos, foundLeverHit, score);

                if (score == 0) {
                    return cand; // 最优解，立即返回
                }
                if (candidates[score] == null) {
                    candidates[score] = cand; // 缓存首次出现的低分方案
                }
            }
        }

        // 6. 回退：按评分升序返回缓存中的最佳方案
        for (PlacementCandidate c : candidates) {
            if (c != null) return c;
        }

        return null; // 全部不可行
    }

    // ============ 生命周期管理 ============

    private void start(BlockPos pos) {
        if (mc.level == null) return;
        if (!isValidBlock(pos)) return;

        bedrockPos = pos;
        state = State.START;
        tickCount = 0;
        blockDestroyProgress = 0;
        blockDestroySeqNumber = 0;
        leverPos = null;
        leverPlaceHitResult = null;
    }

    private void reset(String message) {
        reset(message, true);
    }

    private void reset(String message, boolean cleanup) {
        // ★ 用户主动关闭时不清理残留（cleanup=false），避免重新开启后被清理阻塞
        //   故障 reset 时清理残留（cleanup=true），避免对下一次操作造成影响
        if (cleanup && mc.player != null && mc.level != null) {
            // ★ 清理辅助方块（v2.2 新增）：比拉杆优先清理，确保无残留
            BedrockBreakerConfig cfg = BedrockBreakerConfig.getInstance();
            if (cfg.cleanupHelpers && !helperBlockPositions.isEmpty()) {
                for (BlockPos helperPos : helperBlockPositions) {
                    if (!mc.level.getBlockState(helperPos).isAir()) {
                        mineBlock(helperPos);
                    }
                }
                helperBlockPositions.clear();
            }

            if (pistonPos != null && !mc.level.getBlockState(pistonPos).isAir()) {
                cleanupPistonPos = pistonPos.immutable();
                cleanupPistonTicks = 0;
            }
            if (leverPos != null) {
                mineBlock(leverPos);
            }
        }

        bedrockPos = null;
        pistonDirection = null;
        pistonFacing = null;
        pistonPos = null;
        leverPos = null;
        leverPlaceHitResult = null;
        blockDestroyProgress = 0;
        blockDestroySeqNumber = 0;
        state = State.INIT;
        tickCount = 0;
        reverseRotSent = false;
        predictedContainerStateId = -1;
        queue.clear(); // ★ 清空序列队列，避免关闭功能后残留

        // 恢复快捷栏
        if (mc.player != null) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(
                    mc.player.getInventory().selected));
        }

        if (message != null && mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("§c[基岩破坏器] " + message), true);
        }
    }

    // ================================================================
    // ★ 状态枚举（严格匹配 CheatUtils 12 状态）
    // ================================================================

    private enum State {
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

        State(Consumer<BedrockBreakerManager> action) {
            this.action = action;
        }

        public void handle(BedrockBreakerManager instance) {
            action.accept(instance);
        }
    }
}