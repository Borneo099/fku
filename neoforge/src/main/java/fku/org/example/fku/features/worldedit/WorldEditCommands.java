package fku.org.example.fku.features.worldedit; /* water */

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fku.org.example.fku.Fku;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * WorldEdit 命令注册 — 严格遵循创世神（WorldEdit）的指令格式
 *
 * 入口：
 *   1. //<子命令>  — 直接输入 //wand, //set stone 等（完全符合创世神习惯）
 *   2. /fku we <子命令>  — 备用入口
 */
@Mod.EventBusSubscriber(modid = Fku.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WorldEditCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // ══════════ 1. /<子命令> — 严格创世神格式 ══════════
        // 在聊天框输入 //wand，MC 移除第一个 /，剩余 /wand 传给 dispatcher，
        // 因此注册为 literal("/wand")，完美匹配 //wand 输入
        dispatcher.register(Commands.literal("/wand").executes(ctx -> { exec("//wand"); return 1; }));
        dispatcher.register(Commands.literal("/pos1").executes(ctx -> { exec("//pos1"); return 1; }));
        dispatcher.register(Commands.literal("/pos2").executes(ctx -> { exec("//pos2"); return 1; }));
        dispatcher.register(Commands.literal("/sel").executes(ctx -> { exec("//sel"); return 1; }));
        dispatcher.register(Commands.literal("/desel").executes(ctx -> { exec("//desel"); return 1; }));
        dispatcher.register(Commands.literal("/undo").executes(ctx -> { exec("//undo"); return 1; }));
        dispatcher.register(Commands.literal("/redo").executes(ctx -> { exec("//redo"); return 1; }));
        dispatcher.register(Commands.literal("/copy").executes(ctx -> { exec("//copy"); return 1; }));
        dispatcher.register(Commands.literal("/paste").executes(ctx -> { exec("//paste"); return 1; }));
        dispatcher.register(Commands.literal("/cancel").executes(ctx -> { exec("//cancel"); return 1; }));
        dispatcher.register(Commands.literal("/help").executes(ctx -> { exec("//help"); return 1; }));
        // 带参数的子命令
        dispatcher.register(Commands.literal("/set")
                .then(Commands.argument("block", StringArgumentType.word())
                        .executes(ctx -> { exec("//set " + getString(ctx, "block")); return 1; })));
        dispatcher.register(Commands.literal("/replace")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//replace " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("/sphere")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//sphere " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("/cyl")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//cyl " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("/pyramid")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//pyramid " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("//wall")
                .then(Commands.argument("block", StringArgumentType.word())
                        .executes(ctx -> { exec("//wall " + getString(ctx, "block")); return 1; })));
        dispatcher.register(Commands.literal("/roof")
                .then(Commands.argument("block", StringArgumentType.word())
                        .executes(ctx -> { exec("//roof " + getString(ctx, "block")); return 1; })));
        dispatcher.register(Commands.literal("/expand")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//expand " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("//contract")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//contract " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("//inset")
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(ctx -> { exec("//inset " + getInt(ctx, "amount")); return 1; })));
        dispatcher.register(Commands.literal("//outset")
                .then(Commands.argument("amount", IntegerArgumentType.integer())
                        .executes(ctx -> { exec("//outset " + getInt(ctx, "amount")); return 1; })));
        dispatcher.register(Commands.literal("/schematic")
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .executes(ctx -> { exec("//schematic " + getString(ctx, "args")); return 1; })));
        dispatcher.register(Commands.literal("/tool")
                .then(Commands.argument("type", StringArgumentType.word())
                        .executes(ctx -> { exec("//tool " + getString(ctx, "type")); return 1; })));

        // ══════════ 2. /fku we <子命令> — 备用入口 ══════════
        dispatcher.register(Commands.literal("fku")
                .then(Commands.literal("we")
                        .then(Commands.literal("help").executes(ctx -> { exec("//help"); return 1; }))
                        .then(Commands.literal("wand").executes(ctx -> { exec("//wand"); return 1; }))
                        .then(Commands.literal("pos1").executes(ctx -> { exec("//pos1"); return 1; }))
                        .then(Commands.literal("pos2").executes(ctx -> { exec("//pos2"); return 1; }))
                        .then(Commands.literal("sel").executes(ctx -> { exec("//sel"); return 1; }))
                        .then(Commands.literal("desel").executes(ctx -> { exec("//desel"); return 1; }))
                        .then(Commands.literal("undo").executes(ctx -> { exec("//undo"); return 1; }))
                        .then(Commands.literal("redo").executes(ctx -> { exec("//redo"); return 1; }))
                        .then(Commands.literal("copy").executes(ctx -> { exec("//copy"); return 1; }))
                        .then(Commands.literal("paste").executes(ctx -> { exec("//paste"); return 1; }))
                        .then(Commands.literal("cancel").executes(ctx -> { exec("//cancel"); return 1; }))
                        .then(Commands.literal("set")
                                .then(Commands.argument("block", StringArgumentType.word())
                                        .executes(ctx -> { exec("//set " + getString(ctx, "block")); return 1; })))
                        .then(Commands.literal("replace")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//replace " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("sphere")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//sphere " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("cyl")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//cyl " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("pyramid")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//pyramid " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("wall")
                                .then(Commands.argument("block", StringArgumentType.word())
                                        .executes(ctx -> { exec("//wall " + getString(ctx, "block")); return 1; })))
                        .then(Commands.literal("roof")
                                .then(Commands.argument("block", StringArgumentType.word())
                                        .executes(ctx -> { exec("//roof " + getString(ctx, "block")); return 1; })))
                        .then(Commands.literal("expand")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//expand " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("contract")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//contract " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("inset")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> { exec("//inset " + getInt(ctx, "amount")); return 1; })))
                        .then(Commands.literal("outset")
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(ctx -> { exec("//outset " + getInt(ctx, "amount")); return 1; })))
                        .then(Commands.literal("schematic")
                                .then(Commands.argument("args", StringArgumentType.greedyString())
                                        .executes(ctx -> { exec("//schematic " + getString(ctx, "args")); return 1; })))
                        .then(Commands.literal("tool")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(ctx -> { exec("//tool " + getString(ctx, "type")); return 1; })))
                ));
    }

    private static void exec(String cmd) {
        // ① 装备橡木按钮到头盔槽（赋予超远交互距离）
        boolean needsHelmet = !SuperDistanceInteraction.getInstance().isHelmetEquipped();
        if (needsHelmet) {
            SuperDistanceInteraction.getInstance().enable();
        }

        CommandRegistry.getInstance().execute(cmd);

        // ② 如果启动了任务队列（批量操作），等队列完成再恢复头盔
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().setOnComplete(success -> {
                if (needsHelmet) {
                    SuperDistanceInteraction.getInstance().disable();
                }
            });
        } else {
            // 单次操作，立即恢复
            if (needsHelmet) {
                SuperDistanceInteraction.getInstance().disable();
            }
        }
    }

    private static String getString(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String name) {
        return StringArgumentType.getString(ctx, name);
    }

    private static int getInt(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }
}
