package fku.org.example.fku.features.worldedit;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fku.org.example.fku.features.worldedit.CommandRegistry;
import fku.org.example.fku.features.worldedit.SuperDistanceInteraction;
import fku.org.example.fku.features.worldedit.TaskQueue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class WorldEditCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher dispatcher = event.getDispatcher();
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/wand").executes(ctx -> {
            WorldEditCommands.exec("//wand");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/pos1").executes(ctx -> {
            WorldEditCommands.exec("//pos1");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/pos2").executes(ctx -> {
            WorldEditCommands.exec("//pos2");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/sel").executes(ctx -> {
            WorldEditCommands.exec("//sel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/desel").executes(ctx -> {
            WorldEditCommands.exec("//desel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/undo").executes(ctx -> {
            WorldEditCommands.exec("//undo");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/redo").executes(ctx -> {
            WorldEditCommands.exec("//redo");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/copy").executes(ctx -> {
            WorldEditCommands.exec("//copy");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/paste").executes(ctx -> {
            WorldEditCommands.exec("//paste");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/cancel").executes(ctx -> {
            WorldEditCommands.exec("//cancel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/help").executes(ctx -> {
            WorldEditCommands.exec("//help");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/set").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//set " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/replace").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//replace " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/sphere").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//sphere " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/cyl").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//cyl " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/pyramid").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//pyramid " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"//wall").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//wall " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/roof").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//roof " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/expand").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//expand " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"//contract").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//contract " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"//inset").then(Commands.m_82129_((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//inset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"//outset").then(Commands.m_82129_((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//outset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/schematic").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//schematic " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"/tool").then(Commands.m_82129_((String)"type", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//tool " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "type"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"fku").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"we").then(Commands.m_82127_((String)"help").executes(ctx -> {
            WorldEditCommands.exec("//help");
            return 1;
        }))).then(Commands.m_82127_((String)"wand").executes(ctx -> {
            WorldEditCommands.exec("//wand");
            return 1;
        }))).then(Commands.m_82127_((String)"pos1").executes(ctx -> {
            WorldEditCommands.exec("//pos1");
            return 1;
        }))).then(Commands.m_82127_((String)"pos2").executes(ctx -> {
            WorldEditCommands.exec("//pos2");
            return 1;
        }))).then(Commands.m_82127_((String)"sel").executes(ctx -> {
            WorldEditCommands.exec("//sel");
            return 1;
        }))).then(Commands.m_82127_((String)"desel").executes(ctx -> {
            WorldEditCommands.exec("//desel");
            return 1;
        }))).then(Commands.m_82127_((String)"undo").executes(ctx -> {
            WorldEditCommands.exec("//undo");
            return 1;
        }))).then(Commands.m_82127_((String)"redo").executes(ctx -> {
            WorldEditCommands.exec("//redo");
            return 1;
        }))).then(Commands.m_82127_((String)"copy").executes(ctx -> {
            WorldEditCommands.exec("//copy");
            return 1;
        }))).then(Commands.m_82127_((String)"paste").executes(ctx -> {
            WorldEditCommands.exec("//paste");
            return 1;
        }))).then(Commands.m_82127_((String)"cancel").executes(ctx -> {
            WorldEditCommands.exec("//cancel");
            return 1;
        }))).then(Commands.m_82127_((String)"set").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//set " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.m_82127_((String)"replace").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//replace " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"sphere").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//sphere " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"cyl").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//cyl " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"pyramid").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//pyramid " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"wall").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//wall " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.m_82127_((String)"roof").then(Commands.m_82129_((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//roof " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.m_82127_((String)"expand").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//expand " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"contract").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//contract " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"inset").then(Commands.m_82129_((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//inset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })))).then(Commands.m_82127_((String)"outset").then(Commands.m_82129_((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//outset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })))).then(Commands.m_82127_((String)"schematic").then(Commands.m_82129_((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//schematic " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.m_82127_((String)"tool").then(Commands.m_82129_((String)"type", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//tool " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "type"));
            return 1;
        })))));
    }

    private static void exec(String cmd) {
        boolean needsHelmet;
        boolean bl = needsHelmet = !SuperDistanceInteraction.getInstance().isHelmetEquipped();
        if (needsHelmet) {
            SuperDistanceInteraction.getInstance().enable();
        }
        CommandRegistry.getInstance().execute(cmd);
        if (TaskQueue.getInstance().isRunning()) {
            TaskQueue.getInstance().setOnComplete(success -> {
                if (needsHelmet) {
                    SuperDistanceInteraction.getInstance().disable();
                }
            });
        } else if (needsHelmet) {
            SuperDistanceInteraction.getInstance().disable();
        }
    }

    private static String getString(CommandContext<CommandSourceStack> ctx, String name) {
        return StringArgumentType.getString(ctx, (String)name);
    }

    private static int getInt(CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, (String)name);
    }
}

