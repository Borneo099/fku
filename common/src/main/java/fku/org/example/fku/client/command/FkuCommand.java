package fku.org.example.fku.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import fku.org.example.fku.client.gui.ClickGuiScreen;

import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.features.teleport.TeleportFeature;
import fku.org.example.fku.features.tpgoto.TpGotoPlayerFeature;
import fku.org.example.fku.features.tpgoto.TpGotoPosFeature;
import fku.org.example.fku.config.MovementConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fku", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FkuCommand {

    // ★ 建议提供者：在线玩家昵称 + stop
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PLAYERS = (ctx, builder) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (Player p : mc.level.players()) {
                if (p != mc.player) {
                    builder.suggest(p.getName().getString());
                }
            }
        }
        builder.suggest("stop");
        return builder.buildFuture();
    };

    // ★ 建议提供者：相机准星瞄准的方块坐标（取整）
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_COORD_X = (ctx, builder) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) mc.hitResult).getBlockPos();
            builder.suggest(String.valueOf(pos.getX()));
        } else if (mc.player != null) {
            builder.suggest(String.format("%.0f", mc.player.getX()));
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_COORD_Y = (ctx, builder) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) mc.hitResult).getBlockPos();
            builder.suggest(String.valueOf(pos.getY()));
        } else if (mc.player != null) {
            builder.suggest(String.format("%.0f", mc.player.getY()));
        }
        return builder.buildFuture();
    };
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_COORD_Z = (ctx, builder) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.core.BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) mc.hitResult).getBlockPos();
            builder.suggest(String.valueOf(pos.getZ()));
        } else if (mc.player != null) {
            builder.suggest(String.format("%.0f", mc.player.getZ()));
        }
        return builder.buildFuture();
    };

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
                                .suggests(SUGGEST_COORD_X)
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                        .suggests(SUGGEST_COORD_Y)
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .suggests(SUGGEST_COORD_Z)
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
                // ★ 传送前往玩家：/fku tpgoto <玩家名>
                //   Tab 提示在线玩家昵称 + stop
                .then(Commands.literal("tpgoto")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(SUGGEST_PLAYERS)
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "player");
                                    if ("stop".equalsIgnoreCase(name)) {
                                        TpGotoPlayerFeature.stopWalking("§a已手动停止");
                                    } else {
                                        TpGotoPlayerFeature.startTeleport(name);
                                    }
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("§e用法: /fku tpgoto <玩家名>"), false);
                            return 1;
                        })
                )
                // ★ 传送前往坐标：/fku tpgotoPos <x> <y> <z>
                //   Tab 提示当前玩家坐标（取整）+ stop
                .then(Commands.literal("tpgotoPos")
                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                .suggests(SUGGEST_COORD_X)
                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                        .suggests(SUGGEST_COORD_Y)
                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                .suggests(SUGGEST_COORD_Z)
                                                .executes(ctx -> {
                                                    double x = DoubleArgumentType.getDouble(ctx, "x");
                                                    double y = DoubleArgumentType.getDouble(ctx, "y");
                                                    double z = DoubleArgumentType.getDouble(ctx, "z");
                                                    TpGotoPosFeature.startTeleport(x, y, z);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("stop")
                                .executes(ctx -> {
                                    TpGotoPosFeature.stopWalking("§a已手动停止");
                                    return 1;
                                })
                        )
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal("§e用法: /fku tpgotoPos <x> <y> <z>"), false);
                            return 1;
                        })
                )
                // ★ 传送前往准星方块：/fku tpgotocrosshair
                //   始终以相机准星为准（含灵魂出窍模式），不依赖玩家位置/视角
                .then(Commands.literal("tpgotocrosshair")
                        .executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player == null || mc.level == null) return 0;

                            // 统一以相机准星为准（无论是否灵魂出窍）
                            var cam = mc.gameRenderer.getMainCamera();
                            Vec3 from = cam.getPosition();
                            Vec3 look = Vec3.directionFromRotation(cam.getXRot(), cam.getYRot());

                            // 跨距离射线检测（最大 1024 格）
                            Vec3 to = from.add(look.scale(1024.0));
                            net.minecraft.world.level.ClipContext clip = new net.minecraft.world.level.ClipContext(
                                from, to, net.minecraft.world.level.ClipContext.Block.OUTLINE,
                                net.minecraft.world.level.ClipContext.Fluid.NONE, mc.player);
                            var hit = mc.level.clip(clip);

                            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                                net.minecraft.core.BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) hit).getBlockPos();
                                Vec3 targetPos = new Vec3(pos.getX(), pos.getY(), pos.getZ());
                                TpGotoPosFeature.startTeleport(targetPos.x, targetPos.y, targetPos.z);
                            } else {
                                ctx.getSource().sendSuccess(() -> Component.literal("§c未检测到准星瞄准的方块（请对准方块再试）"), false);
                            }
                            return 1;
                        })
                )
        );
    }
}