package fku.org.example.fku.features.worldedit; /* water */

import fku.org.example.fku.Fku;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命令注册器 — 解析并执行 // 命令
 *
 * 支持的命令：
 *   //wand         — 获取选区工具
 *   //set <block>  — 填充选区
 *   //replace [from] <to> — 替换方块
 *   //sphere <block> <radius> [hollow] — 生成球体
 *   //cyl <block> <radius> <height> [hollow] — 生成圆柱
 *   //pyramid <block> <size> [hollow] — 生成金字塔
 *   //wall <block> — 选区围墙
 *   //roof <block> — 选区屋顶
 *   //copy         — 复制选区
 *   //paste        — 粘贴剪贴板
 *   //undo         — 撤销
 *   //redo         — 重做
 *   //expand <amount> <dir> — 扩展选区
 *   //contract <amount> <dir> — 收缩选区
 *   //inset <amount> — 向内缩进
 *   //outset <amount> — 向外扩展
 *   //schematic save/load <name> — 结构文件管理
 *   //pos1         — 设置Pos1为当前位置
 *   //pos2         — 设置Pos2为当前位置
 *   //help         — 显示帮助
 *   //sel         — 显示选区信息
 *   //desel       — 清除选区
 *   //tool tree/remover/info — 工具绑定
 */
public class CommandRegistry {

    private static final CommandRegistry INSTANCE = new CommandRegistry();

    private final Map<String, CommandHandler> commands = new HashMap<>();

    // 上一次替换的源方块
    private String lastReplaceFrom = "";

    public static CommandRegistry getInstance() { return INSTANCE; }

    private CommandRegistry() {
        registerCommands();
    }

    private void registerCommands() {
        commands.put("wand", this::cmdWand);
        commands.put("set", this::cmdSet);
        commands.put("replace", this::cmdReplace);
        commands.put("sphere", this::cmdSphere);
        commands.put("cyl", this::cmdCylinder);
        commands.put("pyramid", this::cmdPyramid);
        commands.put("wall", this::cmdWall);
        commands.put("roof", this::cmdRoof);
        commands.put("copy", this::cmdCopy);
        commands.put("paste", this::cmdPaste);
        commands.put("undo", this::cmdUndo);
        commands.put("redo", this::cmdRedo);
        commands.put("expand", this::cmdExpand);
        commands.put("contract", this::cmdContract);
        commands.put("inset", this::cmdInset);
        commands.put("outset", this::cmdOutset);
        commands.put("schematic", this::cmdSchematic);
        commands.put("pos1", this::cmdPos1);
        commands.put("pos2", this::cmdPos2);
        commands.put("help", this::cmdHelp);
        commands.put("sel", this::cmdSel);
        commands.put("desel", this::cmdDesel);
        commands.put("tool", this::cmdTool);
        commands.put("cancel", this::cmdCancel);
        commands.put("pause", this::cmdPause);
        commands.put("resume", this::cmdResume);
    }

    /**
     * 解析并执行命令 (从聊天/指令输入)
     */
    public boolean execute(String input) {
        if (!input.startsWith("//")) return false;

        // 去掉 //
        String cmdLine = input.substring(2).trim();
        String[] parts = cmdLine.split("\\s+");
        if (parts.length == 0) return false;

        String cmdName = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        if (parts.length > 1) {
            System.arraycopy(parts, 1, args, 0, parts.length - 1);
        }

        CommandHandler handler = commands.get(cmdName);
        if (handler == null) {
            sendMsg("§c未知命令: //" + cmdName + " (输入 //help 查看帮助)");
            return true;
        }

        WorldEditConfig cfg = WorldEditConfig.getInstance();
        if (!cfg.enabled && !cmdName.equals("wand") && !cmdName.equals("help") && !cmdName.equals("sel") && !cmdName.equals("desel")) {
            sendMsg("§cWorldEdit 未激活！请先打开GUI开关或使用 //wand 激活");
            return true;
        }

        try {
            handler.handle(args);
        } catch (Exception e) {
            sendMsg("§c执行命令时出错: " + e.getMessage());
            Fku.LOGGER.error("[WorldEdit] 命令执行失败: //" + cmdName, e);
        }
        return true;
    }

    // ═══════════════ 命令实现 ═══════════════

    private void cmdWand(String[] args) {
        if (!WorldEditConfig.getInstance().enabled) {
            WorldEditConfig.getInstance().setEnabled(true);
        }
        ToolManager.getInstance().enableWand();
        // 给玩家木斧（如果快捷栏没有则从背包拿）
        giveToolItem();
        sendMsg("§a左键选择Pos1，右键选择Pos2");
    }

    private void giveToolItem() {
        if (Minecraft.getInstance().player == null) return;
        String toolId = WorldEditConfig.getInstance().toolItem;
        Block toolBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(toolId));
        if (toolBlock == null) {
            // fallback to wooden axe
            toolId = "minecraft:wooden_axe";
        }
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(ResourceLocation.tryParse(toolId));
        if (item == null) return;

        // 检查快捷栏是否有
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getInstance().player.getInventory().getItem(i).getItem() == item) return;
        }
        // 检查背包
        for (int i = 9; i < 36; i++) {
            if (Minecraft.getInstance().player.getInventory().getItem(i).getItem() == item) {
                // 移到快捷栏
                var targetStack = Minecraft.getInstance().player.getInventory().getItem(i).copy();
                Minecraft.getInstance().player.getInventory().setItem(Minecraft.getInstance().player.getInventory().getSelectedSlot(), targetStack);
                Minecraft.getInstance().player.getInventory().setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
                return;
            }
        }
        // 创造模式直接给
        if (Minecraft.getInstance().player.getAbilities().instabuild) {
            Minecraft.getInstance().player.getInventory().add(new net.minecraft.world.item.ItemStack(item, 1));
        }
    }

    private void cmdSet(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //set <方块名>"); return; }
        BlockState target = parseBlockState(args[0]);
        if (target == null) return;

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        List<BlockPos> positions = ShapeGenerator.cuboid(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, target, "//set " + args[0]);
    }

    private void cmdReplace(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //replace [from] <to>"); return; }

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        BlockState targetState;
        BlockState fromState = null;

        if (args.length >= 2) {
            fromState = parseBlockState(args[0]);
            targetState = parseBlockState(args[1]);
            lastReplaceFrom = args[0];
        } else {
            targetState = parseBlockState(args[0]);
        }

        if (targetState == null) return;

        List<BlockPos> positions = ShapeGenerator.cuboid(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitReplace(positions, targetState, fromState, "//replace");
    }

    private void cmdSphere(String[] args) {
        if (args.length < 2) { sendMsg("§c用法: //sphere <方块> <半径> [hollow]"); return; }

        BlockState state = parseBlockState(args[0]);
        if (state == null) return;

        int radius = parseInt(args[1], 1);
        if (radius <= 0) { sendMsg("§c半径必须大于0"); return; }

        boolean hollow = args.length >= 3 && args[2].equalsIgnoreCase("hollow");
        BlockPos center = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.blockPosition() : BlockPos.ZERO;

        List<BlockPos> positions = ShapeGenerator.sphere(center, radius, hollow);
        TaskQueue.getInstance().submitSet(positions, state, hollow ? "//sphere hollow" : "//sphere");
    }

    private void cmdCylinder(String[] args) {
        if (args.length < 3) { sendMsg("§c用法: //cyl <方块> <半径> <高度> [hollow]"); return; }

        BlockState state = parseBlockState(args[0]);
        if (state == null) return;

        int radius = parseInt(args[1], 1);
        int height = parseInt(args[2], 1);
        if (radius <= 0 || height <= 0) { sendMsg("§c半径和高度必须大于0"); return; }

        boolean hollow = args.length >= 4 && args[3].equalsIgnoreCase("hollow");
        BlockPos center = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.blockPosition() : BlockPos.ZERO;

        List<BlockPos> positions = ShapeGenerator.cylinder(center, radius, height, hollow);
        TaskQueue.getInstance().submitSet(positions, state, "//cyl");
    }

    private void cmdPyramid(String[] args) {
        if (args.length < 2) { sendMsg("§c用法: //pyramid <方块> <大小> [hollow]"); return; }

        BlockState state = parseBlockState(args[0]);
        if (state == null) return;

        int size = parseInt(args[1], 1);
        if (size <= 0) { sendMsg("§c大小必须大于0"); return; }

        boolean hollow = args.length >= 3 && args[2].equalsIgnoreCase("hollow");
        BlockPos baseCenter = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.blockPosition() : BlockPos.ZERO;

        List<BlockPos> positions = ShapeGenerator.pyramid(baseCenter, size, hollow);
        TaskQueue.getInstance().submitSet(positions, state, "//pyramid");
    }

    private void cmdWall(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //wall <方块>"); return; }

        BlockState state = parseBlockState(args[0]);
        if (state == null) return;

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        List<BlockPos> positions = ShapeGenerator.wall(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, state, "//wall");
    }

    private void cmdRoof(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //roof <方块>"); return; }

        BlockState state = parseBlockState(args[0]);
        if (state == null) return;

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        List<BlockPos> positions = ShapeGenerator.roof(sel.getPos1(), sel.getPos2());
        TaskQueue.getInstance().submitSet(positions, state, "//roof");
    }

    private void cmdCopy(String[] args) {
        ClipboardManager.getInstance().copySelection();
    }

    private void cmdPaste(String[] args) {
        SelectionManager sel = SelectionManager.getInstance();
        BlockPos target = sel.hasPos1() ? sel.getPos1() :
                (Minecraft.getInstance().player != null ? Minecraft.getInstance().player.blockPosition() : BlockPos.ZERO);
        ClipboardManager.getInstance().paste(target);
    }

    private void cmdUndo(String[] args) {
        HistoryManager.getInstance().undo();
    }

    private void cmdRedo(String[] args) {
        HistoryManager.getInstance().redo();
    }

    private void cmdExpand(String[] args) {
        if (args.length < 2) { sendMsg("§c用法: //expand <数量> <方向(up/down/north/south/east/west)>"); return; }

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        int amount = parseInt(args[0], 1);
        Direction dir = parseDirection(args[1]);
        if (dir == null) { sendMsg("§c无效方向: " + args[1]); return; }

        BlockPos[] result = ShapeGenerator.expand(sel.getPos1(), sel.getPos2(), amount, dir);
        sel.setPos1(result[0]);
        sel.setPos2(result[1]);
    }

    private void cmdContract(String[] args) {
        if (args.length < 2) { sendMsg("§c用法: //contract <数量> <方向>"); return; }

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        int amount = parseInt(args[0], 1);
        Direction dir = parseDirection(args[1]);
        if (dir == null) { sendMsg("§c无效方向: " + args[1]); return; }

        BlockPos[] result = ShapeGenerator.contract(sel.getPos1(), sel.getPos2(), amount, dir);
        if (result[0] == sel.getPos1() && result[1] == sel.getPos2()) {
            sendMsg("§c无法收缩（已达最小尺寸）");
            return;
        }
        sel.setPos1(result[0]);
        sel.setPos2(result[1]);
    }

    private void cmdInset(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //inset <数量>"); return; }

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        int amount = parseInt(args[0], 1);
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) return;

        BlockPos newMin = min.offset(amount, amount, amount);
        BlockPos newMax = max.offset(-amount, -amount, -amount);

        if (newMin.getX() > newMax.getX() || newMin.getY() > newMax.getY() || newMin.getZ() > newMax.getZ()) {
            sendMsg("§c收缩后选区为空");
            return;
        }

        sel.setPos1(newMin);
        sel.setPos2(newMax);
    }

    private void cmdOutset(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //outset <数量>"); return; }

        SelectionManager sel = SelectionManager.getInstance();
        if (!sel.hasSelection()) { sendMsg("§c请先设置选区"); return; }

        int amount = parseInt(args[0], 1);
        BlockPos min = sel.getMin();
        BlockPos max = sel.getMax();
        if (min == null || max == null) return;

        sel.setPos1(min.offset(-amount, -amount, -amount));
        sel.setPos2(max.offset(amount, amount, amount));
    }

    private void cmdSchematic(String[] args) {
        if (args.length < 2) { sendMsg("§c用法: //schematic save/load <名称>"); return; }

        String name = args[1];
        if (args[0].equalsIgnoreCase("save")) {
            ClipboardManager.getInstance().saveSchematic(name);
        } else if (args[0].equalsIgnoreCase("load")) {
            ClipboardManager.getInstance().loadSchematic(name);
        } else {
            sendMsg("§c子命令: save 或 load");
        }
    }

    private void cmdPos1(String[] args) {
        if (Minecraft.getInstance().player == null) return;
        SelectionManager.getInstance().setPos1(Minecraft.getInstance().player.blockPosition());
    }

    private void cmdPos2(String[] args) {
        if (Minecraft.getInstance().player == null) return;
        SelectionManager.getInstance().setPos2(Minecraft.getInstance().player.blockPosition());
    }

    private void cmdHelp(String[] args) {
        sendMsg("§6=== WorldEdit Lite 帮助 ===");
        sendMsg("§e//wand §7- 获取选区工具");
        sendMsg("§e//pos1 §7- 设置当前位置为Pos1");
        sendMsg("§e//pos2 §7- 设置当前位置为Pos2");
        sendMsg("§e//sel §7- 显示选区信息");
        sendMsg("§e//desel §7- 清除选区");
        sendMsg("§e//set <方块> §7- 填充选区");
        sendMsg("§e//replace [from] <to> §7- 替换方块");
        sendMsg("§e//sphere <方块> <半径> [hollow] §7- 球体");
        sendMsg("§e//cyl <方块> <半径> <高度> [hollow] §7- 圆柱");
        sendMsg("§e//pyramid <方块> <大小> [hollow] §7- 金字塔");
        sendMsg("§e//wall <方块> §7- 围墙");
        sendMsg("§e//roof <方块> §7- 屋顶");
        sendMsg("§e//copy §7- 复制选区");
        sendMsg("§e//paste §7- 粘贴剪贴板");
        sendMsg("§e//undo §7- 撤销");
        sendMsg("§e//redo §7- 重做");
        sendMsg("§e//expand <数量> <方向> §7- 扩展");
        sendMsg("§e//contract <数量> <方向> §7- 收缩");
        sendMsg("§e//inset <数量> §7- 向内缩进");
        sendMsg("§e//outset <数量> §7- 向外扩展");
        sendMsg("§e//schematic save/load <名称> §7- 结构文件");
        sendMsg("§e//tool tree/remover/info §7- 工具");
        sendMsg("§e//cancel §7- 取消当前任务");
        sendMsg("§e//pause/resume §7- 暂停/继续");
    }

    private void cmdSel(String[] args) {
        SelectionManager sel = SelectionManager.getInstance();
        if (sel.hasSelection()) {
            BlockPos min = sel.getMin();
            BlockPos max = sel.getMax();
            if (min != null && max != null) {
                long vol = sel.getVolume();
                sendMsg("§e选区: " + min.getX() + "," + min.getY() + "," + min.getZ()
                        + " → " + max.getX() + "," + max.getY() + "," + max.getZ());
                sendMsg("§e大小: " + (Math.abs(max.getX()-min.getX())+1) + "×"
                        + (Math.abs(max.getY()-min.getY())+1) + "×"
                        + (Math.abs(max.getZ()-min.getZ())+1) + " = " + vol + " 方块");
            }
        } else {
            sendMsg("§c未设置选区");
        }
    }

    private void cmdDesel(String[] args) {
        SelectionManager.getInstance().clearSelection();
    }

    private void cmdTool(String[] args) {
        if (args.length < 1) { sendMsg("§c用法: //tool tree/remover/info"); return; }

        if (!WorldEditConfig.getInstance().enabled) {
            WorldEditConfig.getInstance().setEnabled(true);
        }

        switch (args[0].toLowerCase()) {
            case "tree" -> ToolManager.getInstance().setTool("tree");
            case "remover" -> ToolManager.getInstance().setTool("remover");
            case "info" -> ToolManager.getInstance().setTool("info");
            default -> sendMsg("§c支持的工具: tree, remover, info");
        }
    }

    private void cmdCancel(String[] args) {
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().cancel();
            sendMsg("§a任务已取消");
        } else {
            sendMsg("§e当前没有运行中的任务");
        }
    }

    private void cmdPause(String[] args) {
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().setPaused(true);
            sendMsg("§a任务已暂停");
        } else {
            sendMsg("§e当前没有运行中的任务");
        }
    }

    private void cmdResume(String[] args) {
        TaskQueue.getInstance().setPaused(false);
        if (TaskQueue.getInstance().isRunning()) {
            sendMsg("§a任务已恢复");
        }
    }

    // ═══════════════ 工具方法 ═══════════════

    /**
     * 解析方块状态
     */
    private BlockState parseBlockState(String input) {
        // 支持格式: minecraft:stone, stone, dirt
        String blockId = input.contains(":") ? input : "minecraft:" + input;
        Block block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(blockId));
        if (block == null) {
            // 尝试不同格式
            blockId = "minecraft:" + input;
            block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(ResourceLocation.tryParse(blockId));
        }
        if (block == null) {
            sendMsg("§c未知方块: " + input);
            return null;
        }
        return block.defaultBlockState();
    }

    private int parseInt(String s, int defaultVal) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private Direction parseDirection(String s) {
        return switch (s.toLowerCase()) {
            case "up", "u" -> Direction.UP;
            case "down", "d" -> Direction.DOWN;
            case "north", "n" -> Direction.NORTH;
            case "south", "s" -> Direction.SOUTH;
            case "east", "e" -> Direction.EAST;
            case "west", "w" -> Direction.WEST;
            default -> null;
        };
    }

    private void sendMsg(String msg) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.literal("§7[WorldEdit] " + msg), true);
        }
    }

    @FunctionalInterface
    interface CommandHandler {
        void handle(String[] args) throws Exception;
    }
}
