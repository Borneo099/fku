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
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
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
    /** 残留活塞清理已过tick数 */
    private int cleanupPistonTicks = 0;

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
    // ★ 第1步：handleStart —— 严格参考 CheatUtils BedrockBreaker.handleStart()
    // ================================================================
    private void handleStart() {
        assert mc.player != null && mc.level != null;

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

        int pistonSlot = ensureInHotbar(Items.PISTON);
        if (pistonSlot < 0) { reset("背包找不到活塞"); return; }

        // ★ 只保留上下方向，侧面放置无法稳定生成无头活塞
        pistonDirection = null;
        for (Direction d : new Direction[]{Direction.UP, Direction.DOWN}) {
            if (canPlacePiston(bedrockPos, d)) {
                pistonDirection = d;
                break;
            }
        }

        if (pistonDirection == null) { reset("找不到放置活塞的位置（仅支持上下方向）"); return; }

        pistonPos = bedrockPos.relative(pistonDirection);

        // 找拉杆位置（含辅助方块策略）
        if (!findLocationForLever()) { reset("找不到放置拉杆的位置"); return; }

        // 使用 BlockPlacer 生成放置计划
        BlockPlacingMethod method = BlockPlacingMethod.facing(pistonDirection);
        BlockPlacer.BlockPlacePlan plan = BlockPlacer.createPacketPlan(pistonPos, method);
        if (plan == null) { reset("无法生成活塞放置计划"); return; }

        // ★ 计算假旋转并发送放置包
        //   plan.apply(fakeYaw, fakePitch) 内部发送4参数假旋转包 + 放置包
        calculateFakeRotation(method);
        mc.player.connection.send(new ServerboundSetCarriedItemPacket(pistonSlot));
        CompletableFuture<Void> future = plan.apply(fakeYaw, fakePitch);
        if (!future.isDone()) {
            reset("Unexpected piston placing plan apply result");
            return;
        }

        // 链式调用：放置拉杆（同一 tick）
        state = State.PLACE_LEVER;
        state.handle(this);
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
    // ★ 第3步：handleBreakPistonStart —— 严格参考 CheatUtils
    // ================================================================
    private void handleBreakPistonStart() {
        assert mc.level != null && mc.player != null;

        tickCount = 0;

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
    //   第一个活塞朝向可能因玩家视角影响而不等于 pistonDirection，
    //   只要活塞被激活（出现活塞头），状态机就继续。
    //   清理时使用实际朝向，但 pistonDirection 保持不变（反向活塞仍需朝向基岩）。
    //
    //   检测到活塞头后，同一 tick 执行：
    //   ① 拉杆 OFF（活塞计划收回）
    //   ② STOP_DESTROY（移除活塞体 → 无头活塞诞生）
    //   ③ 客户端清理
    //   ④ 链式调用 PLACE_REVERSE_PISTON（同一 tick）
    // ================================================================
    private void handleWaitPistonExtend() {
        assert mc.player != null && mc.level != null;

        // ★ 遍历所有方向检测活塞头（不严格要求在 pistonDirection 方向）
        Direction actualPistonDir = null;
        for (Direction d : Direction.values()) {
            if (mc.level.getBlockState(pistonPos.relative(d)).getBlock() == Blocks.PISTON_HEAD) {
                actualPistonDir = d;
                break;
            }
        }

        if (actualPistonDir != null) {
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
    //   朝向 = pistonDirection.getOpposite()（朝向基岩）
    // ================================================================
    private void handlePlaceReversePiston() {
        assert mc.level != null && mc.player != null;

        int pistonSlot = ensureInHotbar(Items.PISTON);
        if (pistonSlot < 0) { reset("背包找不到活塞（反向）"); return; }

        // ★ 反向活塞放置：点击基岩面（bedrockPos），而非活塞头位置
        //   createPacketPlan 默认点击 pistonPos.relative(pistonDirection)（活塞头位置），
        //   但正向活塞激活后该位置变为 MOVING_PISTON/PISTON_HEAD，服务端可能拒绝放置。
        //   改为点击 bedrockPos 的 pistonDirection 面，新方块放置到
        //   bedrockPos.relative(pistonDirection) = pistonPos（底座位置，紧贴基岩）。
        Direction reverseFacing = pistonDirection.getOpposite();
        BlockPos clickPos = pistonPos.relative(reverseFacing); // = bedrockPos
        Direction clickFace = pistonDirection; // 点击基岩的 pistonDirection 面
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
        mc.level.destroyBlock(pistonPos.relative(pistonDirection), false);

        state = State.WAIT_BEDROCK_BREAK;
        tickCount = 0;
    }

    // ================================================================
    // ★ 第6步：handleWaitBedrockBreak —— 严格参考 CheatUtils
    // ================================================================
    private void handleWaitBedrockBreak() {
        assert mc.player != null && mc.level != null;

        if (mc.level.getBlockState(bedrockPos).isAir()
                && !mc.level.getBlockState(pistonPos.relative(pistonDirection)).is(Blocks.MOVING_PISTON)) {
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
    //   策略1：围绕基岩找位置（排除活塞方向）
    //   策略2：围绕活塞找位置（排除活塞方向和反方向，不贴在活塞上）
    // ================================================================
    private boolean findLocationForLever() {
        assert mc.level != null && mc.player != null;

        leverPos = null;

        // 策略1：围绕基岩找
        for (Direction direction : sortByDistance(bedrockPos, Direction.values())) {
            BlockPos possibleLeverPos = bedrockPos.relative(direction);
            if (direction == pistonDirection) continue;
            if (!mc.level.getBlockState(possibleLeverPos).canBeReplaced()) continue;
            if (!isValidY(possibleLeverPos.getY())) continue;

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

        // 策略2：围绕活塞找（不贴在活塞上）
        for (Direction direction : sortByDistance(pistonPos, Direction.values())) {
            if (direction == pistonDirection) continue;
            if (direction == pistonDirection.getOpposite()) continue;

            BlockPos possibleLeverPos = pistonPos.relative(direction);
            if (!mc.level.getBlockState(possibleLeverPos).canBeReplaced()) continue;
            if (!isValidY(possibleLeverPos.getY())) continue;

            for (Direction dir : sortByDistance(possibleLeverPos, Direction.values())) {
                BlockPos possibleSupportPos = possibleLeverPos.relative(dir);
                if (possibleSupportPos.equals(pistonPos)) continue; // 不贴在活塞上
                if (mc.level.getBlockState(possibleSupportPos).canBeReplaced()) continue;

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

        return false;
    }

    // ============ 辅助方法 ============

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
                mc.player.connection.send(new ServerboundContainerClickPacket(
                        0, // 玩家背包容器
                        mc.player.containerMenu.getStateId(),
                        containerSlot,
                        targetSlot, // button = 目标快捷栏编号
                        ClickType.SWAP,
                        ItemStack.EMPTY,
                        new Int2ObjectOpenHashMap<>(Map.of(
                                containerSlot, inventory.getItem(targetSlot).copy(),
                                hotbarContainerSlot, inventory.getItem(i).copy()
                        ))
                ));

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
                mc.player.connection.send(new ServerboundContainerClickPacket(
                        0, mc.player.containerMenu.getStateId(),
                        containerSlot, targetSlot, ClickType.SWAP,
                        ItemStack.EMPTY,
                        new Int2ObjectOpenHashMap<>(Map.of(
                                containerSlot, inventory.getItem(targetSlot).copy(),
                                hotbarContainerSlot, inventory.getItem(i).copy()
                        ))
                ));
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
        pistonPos = null;
        leverPos = null;
        leverPlaceHitResult = null;
        blockDestroyProgress = 0;
        blockDestroySeqNumber = 0;
        state = State.INIT;
        tickCount = 0;
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