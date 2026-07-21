package fku.org.example.fku.features.worldedit;

import fku.org.example.fku.Fku;
import fku.org.example.fku.features.worldedit.SelectionManager;
import fku.org.example.fku.features.worldedit.ShapeGenerator;
import fku.org.example.fku.features.worldedit.WorldEditConfig;
import fku.org.example.fku.mixin.ClientLevelAccessor;
import java.util.HashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class ToolManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final ToolManager INSTANCE = new ToolManager();
    private boolean wandMode = false;
    private String currentTool = "";

    public static ToolManager getInstance() {
        return INSTANCE;
    }

    private ToolManager() {
    }

    public boolean handleClick(int button, InteractionHand hand) {
        if (ToolManager.mc.player == null || ToolManager.mc.f_91073_ == null) {
            return false;
        }
        if (!this.wandMode) {
            return false;
        }
        ItemStack heldItem = ToolManager.mc.player.m_21120_(hand != null ? hand : InteractionHand.MAIN_HAND);
        String itemId = ForgeRegistries.ITEMS.getKey(heldItem.m_41720_()).toString();
        if (itemId.equals(WorldEditConfig.getInstance().toolItem) && this.currentTool.equals("wand")) {
            BlockHitResult hitResult = this.customRayTrace();
            if (hitResult == null || hitResult.m_6662_() != HitResult.Type.BLOCK) {
                return false;
            }
            BlockPos targetPos = hitResult.m_82425_();
            if (button == 0) {
                SelectionManager.getInstance().setPos1(targetPos);
                return true;
            }
            if (button == 1) {
                SelectionManager.getInstance().setPos2(targetPos);
                return true;
            }
        }
        return this.handleToolAction(button, hand);
    }

    private BlockHitResult customRayTrace() {
        if (ToolManager.mc.player == null || ToolManager.mc.f_91073_ == null) {
            return null;
        }
        Vec3 eyePos = ToolManager.mc.player.m_20299_(1.0f);
        Vec3 lookVec = ToolManager.mc.player.getLookAngle();
        double range = WorldEditConfig.getInstance().rangeMultiplier;
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        return ToolManager.mc.f_91073_.m_45547_(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)ToolManager.mc.player));
    }

    private boolean handleToolAction(int button, InteractionHand hand) {
        if (ToolManager.mc.f_91077_ == null || ToolManager.mc.f_91077_.m_6662_() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockPos targetPos = ((BlockHitResult)ToolManager.mc.f_91077_).m_82425_();
        BlockState targetState = ToolManager.mc.f_91073_.m_8055_(targetPos);
        switch (this.currentTool) {
            case "tree": {
                if (button != 1) break;
                this.generateTree(targetPos);
                return true;
            }
            case "remover": {
                if (button != 0) break;
                this.removeTree(targetPos);
                return true;
            }
            case "replacer": {
                if (button != 1) break;
                return true;
            }
            case "info": {
                if (button != 1) break;
                this.showBlockInfo(targetPos, targetState);
                return true;
            }
        }
        return false;
    }

    private void generateTree(BlockPos pos) {
        if (ToolManager.mc.player == null) {
            return;
        }
        int height = 5 + ToolManager.mc.f_91073_.f_46441_.m_188503_(3);
        for (int i = 0; i < height; ++i) {
            BlockPos trunkPos = pos.m_6630_(i);
            if (!ToolManager.mc.f_91073_.m_8055_(trunkPos).m_247087_()) continue;
            this.sendBlockPacket(trunkPos, Blocks.f_49999_.m_49966_());
        }
        int leafRadius = 2;
        BlockPos leafCenter = pos.m_6630_(height - 2);
        HashSet<BlockPos> leafPositions = new HashSet<BlockPos>();
        leafPositions.addAll(ShapeGenerator.sphere(leafCenter, leafRadius, false));
        for (BlockPos leafPos : leafPositions) {
            if (!ToolManager.mc.f_91073_.m_8055_(leafPos).m_247087_()) continue;
            this.sendBlockPacket(leafPos, Blocks.f_50050_.m_49966_());
        }
        Fku.LOGGER.debug("[WorldEdit] \u6811\u6728\u5df2\u751f\u6210\u5728 {}", pos);
    }

    private void removeTree(BlockPos pos) {
        if (ToolManager.mc.f_91073_ == null) {
            return;
        }
        int range = 10;
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    BlockPos scanPos = pos.m_7918_(x, y, z);
                    BlockState state = ToolManager.mc.f_91073_.m_8055_(scanPos);
                    Block block = state.m_60734_();
                    if (block != Blocks.f_49999_ && block != Blocks.f_50050_ && block != Blocks.f_50001_ && block != Blocks.f_50052_ && block != Blocks.f_50000_ && block != Blocks.f_50051_ && block != Blocks.f_50002_ && block != Blocks.f_50053_ && block != Blocks.f_50003_ && block != Blocks.f_50054_ && block != Blocks.f_50004_ && block != Blocks.f_50055_ && block != Blocks.f_220832_ && block != Blocks.f_220838_) continue;
                    this.breakBlockPacket(scanPos);
                }
            }
        }
    }

    private void showBlockInfo(BlockPos pos, BlockState state) {
        if (ToolManager.mc.player == null) {
            return;
        }
        Block block = state.m_60734_();
        String blockId = ForgeRegistries.BLOCKS.getKey(block).toString();
        String hardness = String.format("%.2f", state.m_60800_((BlockGetter)ToolManager.mc.f_91073_, pos)));
        ToolManager.mc.player.m_5661_(Component.literal((String)"\u00a77[WorldEdit] \u00a7e\u65b9\u5757\u4fe1\u606f:"), true);
        ToolManager.mc.player.m_5661_(Component.literal((String)(" \u00a77ID: \u00a7f" + blockId)), true);
        ToolManager.mc.player.m_5661_(Component.literal((String)(" \u00a77\u786c\u5ea6: \u00a7f" + hardness)), true);
        ToolManager.mc.player.m_5661_(Component.literal((String)(" \u00a77\u4f4d\u7f6e: \u00a7f" + pos.m_123341_() + ", " + pos.m_123342_() + ", " + pos.m_123343_())), true);
        ToolManager.mc.player.m_5661_(Component.literal((String)(" \u00a77\u53ef\u66ff\u6362: \u00a7f" + state.m_247087_())), true);
    }

    private void sendBlockPacket(BlockPos pos, BlockState state) {
        if (ToolManager.mc.player == null || ToolManager.mc.player.f_108617_ == null) {
            return;
        }
        ItemStack itemStack = new ItemStack((ItemLike)state.m_60734_().m_5456_(), 1);
        if (itemStack.m_41619_()) {
            return;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack invStack = ToolManager.mc.player.m_150109_().m_8020_(i);
            if (invStack.m_41720_() != itemStack.m_41720_()) continue;
            ToolManager.mc.player.f_108617_.m_104955_((Packet)new ServerboundSetCarriedItemPacket(i));
            break;
        }
        Vec3 blockCenter = Vec3.m_82512_((Vec3i)pos);
        Vec3 lookVec = ToolManager.mc.player.getLookAngle();
        Direction face = Direction.m_122366_(lookVec.x, lookVec.y, lookVec.z).m_122424_();
        Vec3 clickPos = blockCenter.add(Vec3.m_82528_((Vec3i)face.m_122436_()).scale(-0.5));
        ToolManager.mc.player.f_108617_.m_104955_((Packet)new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(clickPos, face, pos, false), this.getSequence()));
    }

    private void breakBlockPacket(BlockPos pos) {
        if (ToolManager.mc.player == null || ToolManager.mc.player.f_108617_ == null) {
            return;
        }
        int seq = this.getSequence();
        ToolManager.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, Direction.DOWN, seq));
        ToolManager.mc.player.f_108617_.m_104955_((Packet)new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.DOWN, seq));
    }

    private int getSequence() {
        if (ToolManager.mc.f_91073_ == null) {
            return 0;
        }
        BlockStatePredictionHandler handler = ((ClientLevelAccessor)ToolManager.mc.f_91073_).getBlockStatePredictionHandler_CU();
        handler.m_233855_();
        int num = handler.m_233871_();
        handler.close();
        return num;
    }

    public void setTool(String tool) {
        this.currentTool = tool;
        boolean bl = this.wandMode = !tool.isEmpty();
        if (ToolManager.mc.player != null) {
            ToolManager.mc.player.m_5661_(Component.literal((String)("\u00a77[WorldEdit] " + this.getToolDisplayName(tool) + " \u00a7a\u5df2\u6fc0\u6d3b")), true);
        }
    }

    public void enableWand() {
        this.setTool("wand");
    }

    public void disableAll() {
        this.wandMode = false;
        this.currentTool = "";
    }

    private String getToolDisplayName(String tool) {
        return switch (tool) {
            case "wand" -> "\u9009\u533a\u5de5\u5177 (\u6728\u65a7)";
            case "tree" -> "\u6811\u6728\u751f\u6210 (\u6811\u82d7\u53f3\u952e)";
            case "remover" -> "\u6811\u6728\u6e05\u9664 (\u6728\u5251\u5de6\u952e)";
            case "replacer" -> "\u66ff\u6362\u5de5\u5177 (\u6728\u9550)";
            case "info" -> "\u4fe1\u606f\u67e5\u8be2 (\u6728\u68cd\u53f3\u952e)";
            default -> "\u672a\u77e5\u5de5\u5177";
        };
    }

    public boolean isWandMode() {
        return this.wandMode;
    }

    public String getCurrentTool() {
        return this.currentTool;
    }
}

