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
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/wand").executes(ctx -> {
            WorldEditCommands.exec("//wand");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/pos1").executes(ctx -> {
            WorldEditCommands.exec("//pos1");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/pos2").executes(ctx -> {
            WorldEditCommands.exec("//pos2");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/sel").executes(ctx -> {
            WorldEditCommands.exec("//sel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/desel").executes(ctx -> {
            WorldEditCommands.exec("//desel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/undo").executes(ctx -> {
            WorldEditCommands.exec("//undo");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/redo").executes(ctx -> {
            WorldEditCommands.exec("//redo");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/copy").executes(ctx -> {
            WorldEditCommands.exec("//copy");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/paste").executes(ctx -> {
            WorldEditCommands.exec("//paste");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/cancel").executes(ctx -> {
            WorldEditCommands.exec("//cancel");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/help").executes(ctx -> {
            WorldEditCommands.exec("//help");
            return 1;
        }));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/set").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//set " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/replace").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//replace " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/sphere").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//sphere " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/cyl").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//cyl " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/pyramid").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//pyramid " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"//wall").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//wall " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/roof").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//roof " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/expand").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//expand " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"//contract").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//contract " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"//inset").then(Commands.argument((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//inset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"//outset").then(Commands.argument((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//outset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/schematic").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//schematic " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"/tool").then(Commands.argument((String)"type", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//tool " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "type"));
            return 1;
        })));
        dispatcher.register((LiteralArgumentBuilder)Commands.literal((String)"fku").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"we").then(Commands.literal((String)"help").executes(ctx -> {
            WorldEditCommands.exec("//help");
            return 1;
        }))).then(Commands.literal((String)"wand").executes(ctx -> {
            WorldEditCommands.exec("//wand");
            return 1;
        }))).then(Commands.literal((String)"pos1").executes(ctx -> {
            WorldEditCommands.exec("//pos1");
            return 1;
        }))).then(Commands.literal((String)"pos2").executes(ctx -> {
            WorldEditCommands.exec("//pos2");
            return 1;
        }))).then(Commands.literal((String)"sel").executes(ctx -> {
            WorldEditCommands.exec("//sel");
            return 1;
        }))).then(Commands.literal((String)"desel").executes(ctx -> {
            WorldEditCommands.exec("//desel");
            return 1;
        }))).then(Commands.literal((String)"undo").executes(ctx -> {
            WorldEditCommands.exec("//undo");
            return 1;
        }))).then(Commands.literal((String)"redo").executes(ctx -> {
            WorldEditCommands.exec("//redo");
            return 1;
        }))).then(Commands.literal((String)"copy").executes(ctx -> {
            WorldEditCommands.exec("//copy");
            return 1;
        }))).then(Commands.literal((String)"paste").executes(ctx -> {
            WorldEditCommands.exec("//paste");
            return 1;
        }))).then(Commands.literal((String)"cancel").executes(ctx -> {
            WorldEditCommands.exec("//cancel");
            return 1;
        }))).then(Commands.literal((String)"set").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//set " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.literal((String)"replace").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//replace " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"sphere").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//sphere " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"cyl").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//cyl " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"pyramid").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//pyramid " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"wall").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//wall " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.literal((String)"roof").then(Commands.argument((String)"block", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
            WorldEditCommands.exec("//roof " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "block"));
            return 1;
        })))).then(Commands.literal((String)"expand").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//expand " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"contract").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//contract " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"inset").then(Commands.argument((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//inset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })))).then(Commands.literal((String)"outset").then(Commands.argument((String)"amount", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            WorldEditCommands.exec("//outset " + WorldEditCommands.getInt((CommandContext<CommandSourceStack>)ctx, "amount"));
            return 1;
        })))).then(Commands.literal((String)"schematic").then(Commands.argument((String)"args", (ArgumentType)StringArgumentType.greedyString()).executes(ctx -> {
            WorldEditCommands.exec("//schematic " + WorldEditCommands.getString((CommandContext<CommandSourceStack>)ctx, "args"));
            return 1;
        })))).then(Commands.literal((String)"tool").then(Commands.argument((String)"type", (ArgumentType)StringArgumentType.word()).executes(ctx -> {
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

