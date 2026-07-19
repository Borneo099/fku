package fku.org.example.fku.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.features.teleport.TeleportFeature;
import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FkuCommand {

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("fku")
                .then(Commands.literal("gui")
                        .executes(ctx -> {
                            Minecraft.getInstance().setScreen(new ClickGuiScreen());
                            return 1;
                        })
                )
                .then(Commands.literal("ypos")
                        .executes(ctx -> {
                            MovementConfig config = MovementConfig.getInstance();
                            config.yPosOverlayEnabled = !config.yPosOverlayEnabled;
                            MovementConfig.save();
                            
                            String status = config.yPosOverlayEnabled ? "§a开启" : "§c关闭";
                            ctx.getSource().sendSuccess(() -> Component.literal("YPosOverlay 已 " + status), false);
                            return 1;
                        })
                )
                .then(Commands.literal("health")
                        .executes(ctx -> {
                            HealthTagConfig config = HealthTagConfig.getInstance();
                            config.enabled = !config.enabled;
                            HealthTagConfig.save();
                            
                            String status = config.enabled ? "§a开启" : "§c关闭";
                            ctx.getSource().sendSuccess(() -> Component.literal("HealthTag 已 " + status), false);
                            return 1;
                        })
                )
                .then(Commands.literal("arrowdmgfly")
                        .executes(ctx -> {
                            MovementConfig config = MovementConfig.getInstance();
                            config.arrowDmgFlyEnabled = !config.arrowDmgFlyEnabled;
                            MovementConfig.save();

                            String status = config.arrowDmgFlyEnabled ? "§a开启" : "§c关闭";
                            ctx.getSource().sendSuccess(() -> Component.literal("ArrowDmgFly 已 " + status), false);
                            return 1;
                        })
                )
                .then(Commands.literal("nojumpdelay")
                        .executes(ctx -> {
                            MovementConfig config = MovementConfig.getInstance();
                            config.noJumpDelayEnabled = !config.noJumpDelayEnabled;
                            MovementConfig.save();

                            String status = config.noJumpDelayEnabled ? "§a开启" : "§c关闭";
                            ctx.getSource().sendSuccess(() -> Component.literal("NoJumpDelay 已 " + status), false);
                            return 1;
                        })
                )
                // ★ 瞬移指令：/fku tp <x> <y> <z> [snap]
                .then(Commands.literal("tp")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    int x = IntegerArgumentType.getInteger(ctx, "x");
                                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                                    int z = IntegerArgumentType.getInteger(ctx, "z");
                                                    TeleportFeature.teleportTo(x + 0.5, y, z + 0.5, true);
                                                    return 1;
                                                })
                                                .then(Commands.argument("snap", BoolArgumentType.bool())
                                                        .executes(ctx -> {
                                                            int x = IntegerArgumentType.getInteger(ctx, "x");
                                                            int y = IntegerArgumentType.getInteger(ctx, "y");
                                                            int z = IntegerArgumentType.getInteger(ctx, "z");
                                                            boolean snap = BoolArgumentType.getBool(ctx, "snap");
                                                            TeleportFeature.teleportTo(x + 0.5, y, z + 0.5, snap);
                                                            return 1;
                                                        })
                                                )
                                        )
                                )
                        )
                        .executes(ctx -> {
                            // 无参数 = 准星瞬移
                            TeleportFeature.teleportToCrosshair();
                            return 1;
                        })
                )
        );
    }
}