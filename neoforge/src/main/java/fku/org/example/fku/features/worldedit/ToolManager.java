package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 工具管理器 — 绑定便捷工具到物品
 *
 * 功能：
 * - 木斧（Wooden Axe）：选区工具（左键Pos1，右键Pos2）
 * - 树木生成工具（树苗）：在选区或目标位置生成树木
 * - 清除工具（木剑）：清除目标方块
 * - 替换工具（木镐）：替换目标方块
 * - 信息查询工具（木棍）：显示方块信息
 */
public class ToolManager {

    private static final Minecraft mc = Minecraft.getInstance();
    private static final ToolManager INSTANCE = new ToolManager();

    private boolean wandMode = false;
    private String currentTool = ""; // "wand", "tree", "remover", "replacer", "info"

    public static ToolManager getInstance() { return INSTANCE; }

    private ToolManager() {}

    /**
     * 处理鼠标点击事件 — 支持超远距离选区
     * @return true 如果点击已被工具处理
     */
    public boolean handleClick(int button, InteractionHand hand) {
        if (mc.player == null || mc.level == null) return false;
        if (!wandMode) return false;

        // 检查手持物品
        var heldItem = mc.player.getItemInHand(hand != null ? hand : InteractionHand.MAIN_HAND);
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(heldItem.getItem()).toString();

        if (itemId.equals(WorldEditConfig.getInstance().toolItem) && currentTool.equals("wand")) {
            // ★ 使用自定义射线追踪（超远距离）
            BlockHitResult hitResult = customRayTrace();
            if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) return false;
            BlockPos targetPos = hitResult.getBlockPos();

            if (button == 0) {
                // 左键 → Pos1
                SelectionManager.getInstance().setPos1(targetPos);
                return true;
            } else if (button == 1) {
                // 右键 → Pos2
                SelectionManager.getInstance().setPos2(targetPos);
                return true;
            }
        }

        return handleToolAction(button, hand);
    }

    /**
     * 自定义射线追踪 — 支持超远距离（使用 BLOCK_REACH 属性值）
     */
    private BlockHitResult customRayTrace() {
        if (mc.player == null || mc.level == null) return null;
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 lookVec = mc.player.getLookAngle();

        // 使用配置的距离倍率
        double range = WorldEditConfig.getInstance().rangeMultiplier;
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        return mc.level.clip(new net.minecraft.world.level.ClipContext(
                eyePos, endPos,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                mc.player));
    }

    /**
     * 处理工具动作
     */
    private boolean handleToolAction(int button, InteractionHand hand) {
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return false;
        BlockPos targetPos = ((BlockHitResult) mc.hitResult).getBlockPos();
        BlockState targetState = mc.level.getBlockState(targetPos);

        switch (currentTool) {
            case "tree":
                if (button == 1) {
                    generateTree(targetPos);
                    return true;
                }
                break;
            case "remover":
                if (button == 0) {
                    removeTree(targetPos);
                    return true;
                }
                break;
            case "replacer":
                if (button == 1) {
                    // 第一点击选择要替换的方块，第二次点击使用手持物品替换
                    return true;
                }
                break;
            case "info":
                if (button == 1) {
                    showBlockInfo(targetPos, targetState);
                    return true;
                }
                break;
        }
        return false;
    }

    /**
     * 生成树木
     */
    private void generateTree(BlockPos pos) {
        if (mc.player == null) return;
        // 简单的橡树生成：在目标位置放置橡木原木和树叶
        // 实际可以用 Feature 系统，这里简化实现
        int height = 5 + mc.level.random.nextInt(3);

        // 树干
        for (int i = 0; i < height; i++) {
            BlockPos trunkPos = pos.above(i);
            if (mc.level.getBlockState(trunkPos).canBeReplaced()) {
                sendBlockPacket(trunkPos, Blocks.OAK_LOG.defaultBlockState());
            }
        }

        // 树叶（球形树冠）
        int leafRadius = 2;
        BlockPos leafCenter = pos.above(height - 2);
        Set<BlockPos> leafPositions = new HashSet<>();
        leafPositions.addAll(ShapeGenerator.sphere(leafCenter, leafRadius, false));

        for (BlockPos leafPos : leafPositions) {
            if (mc.level.getBlockState(leafPos).canBeReplaced()) {
                sendBlockPacket(leafPos, Blocks.OAK_LEAVES.defaultBlockState());
            }
        }

        Fku.LOGGER.debug("[WorldEdit] 树木已生成在 {}", pos);
    }

    /**
     * 清除指定位置的树木
     */
    private void removeTree(BlockPos pos) {
        if (mc.level == null) return;

        // 扫描周围的原木和树叶
        int range = 10;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos scanPos = pos.offset(x, y, z);
                    BlockState state = mc.level.getBlockState(scanPos);
                    Block block = state.getBlock();
                    if (block == Blocks.OAK_LOG || block == Blocks.OAK_LEAVES
                            || block == Blocks.BIRCH_LOG || block == Blocks.BIRCH_LEAVES
                            || block == Blocks.SPRUCE_LOG || block == Blocks.SPRUCE_LEAVES
                            || block == Blocks.JUNGLE_LOG || block == Blocks.JUNGLE_LEAVES
                            || block == Blocks.ACACIA_LOG || block == Blocks.ACACIA_LEAVES
                            || block == Blocks.DARK_OAK_LOG || block == Blocks.DARK_OAK_LEAVES
                            || block == Blocks.MANGROVE_LOG || block == Blocks.MANGROVE_LEAVES) {
                        breakBlockPacket(scanPos);
                    }
                }
            }
        }
    }

    /**
     * 显示方块信息
     */
    private void showBlockInfo(BlockPos pos, BlockState state) {
        if (mc.player == null) return;
        Block block = state.getBlock();
        String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).toString();
        String hardness = String.format("%.2f", state.getDestroySpeed(mc.level, pos));

        mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "§7[WorldEdit] §e方块信息:"), true);
        mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        " §7ID: §f" + blockId), true);
        mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        " §7硬度: §f" + hardness), true);
        mc.player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                                " §7位置: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), true);

        // 显示可替换的方块
        mc.player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        " §7可替换: §f" + state.canBeReplaced()), true);
    }

    private void sendBlockPacket(BlockPos pos, BlockState state) {
        if (mc.player == null || mc.player.connection == null) return;

        // 简单发包放置
        var itemStack = new net.minecraft.world.item.ItemStack(state.getBlock().asItem(), 1);
        if (itemStack.isEmpty()) return;

        // 找物品
        for (int i = 0; i < 9; i++) {
            var invStack = mc.player.getInventory().getItem(i);
            if (invStack.getItem() == itemStack.getItem()) {
                mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(i));
                break;
            }
        }

        Vec3 blockCenter = Vec3.atCenterOf(pos);
        Vec3 lookVec = mc.player.getLookAngle();
        net.minecraft.core.Direction face = net.minecraft.core.Direction.getApproximateNearest(lookVec.x, lookVec.y, lookVec.z).getOpposite();
        Vec3 clickPos = blockCenter.add(Vec3.atLowerCornerOf(face.getUnitVec3i()).scale(-0.5));

        mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundUseItemOnPacket(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                new BlockHitResult(clickPos, face, pos, false),
                getSequence()));
    }

    private void breakBlockPacket(BlockPos pos) {
        if (mc.player == null || mc.player.connection == null) return;
        int seq = getSequence();
        mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                pos, net.minecraft.core.Direction.DOWN, seq));
        mc.player.connection.send(new net.minecraft.network.protocol.game.ServerboundPlayerActionPacket(
                net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                pos, net.minecraft.core.Direction.DOWN, seq));
    }

    private int getSequence() {
        if (mc.level == null) return 0;
        var handler = ((fku.org.example.fku.mixin.ClientLevelAccessor) mc.level).getBlockStatePredictionHandler_CU();
        handler.startPredicting();
        int num = handler.currentSequence();
        handler.close();
        return num;
    }

    // ════════════ 工具模式切换 ════════════

    public void setTool(String tool) {
        this.currentTool = tool;
        this.wandMode = !tool.isEmpty();
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§7[WorldEdit] " + getToolDisplayName(tool) + " §a已激活"), true);
        }
    }

    public void enableWand() {
        setTool("wand");
    }

    public void disableAll() {
        this.wandMode = false;
        this.currentTool = "";
    }

    private String getToolDisplayName(String tool) {
        return switch (tool) {
            case "wand" -> "选区工具 (木斧)";
            case "tree" -> "树木生成 (树苗右键)";
            case "remover" -> "树木清除 (木剑左键)";
            case "replacer" -> "替换工具 (木镐)";
            case "info" -> "信息查询 (木棍右键)";
            default -> "未知工具";
        };
    }

    public boolean isWandMode() { return wandMode; }
    public String getCurrentTool() { return currentTool; }
}
