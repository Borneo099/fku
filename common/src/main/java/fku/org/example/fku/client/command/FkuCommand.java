package fku.org.example.fku.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fku.org.example.fku.client.gui.ClickGuiScreen;
import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.features.healthtag.HealthTagConfig;
import fku.org.example.fku.features.teleport.TeleportFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public class FkuCommand {
    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher dispatcher = event.getDispatcher();
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"fku").then(Commands.m_82127_((String)"gui").executes(ctx -> {
            Minecraft.getInstance().setScreen(new ClickGuiScreen());
            return 1;
        }))).then(Commands.m_82127_((String)"ypos").executes(ctx -> {
            MovementConfig config = MovementConfig.getInstance();
            config.yPosOverlayEnabled = !config.yPosOverlayEnabled;
            MovementConfig.save();
            String status = config.yPosOverlayEnabled ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed";
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.literal((String)("YPosOverlay \u5df2 " + status)), false);
            return 1;
        }))).then(Commands.m_82127_((String)"health").executes(ctx -> {
            HealthTagConfig config = HealthTagConfig.getInstance();
            config.enabled = !config.enabled;
            HealthTagConfig.save();
            String status = config.enabled ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed";
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.literal((String)("HealthTag \u5df2 " + status)), false);
            return 1;
        }))).then(Commands.m_82127_((String)"arrowdmgfly").executes(ctx -> {
            MovementConfig config = MovementConfig.getInstance();
            config.arrowDmgFlyEnabled = !config.arrowDmgFlyEnabled;
            MovementConfig.save();
            String status = config.arrowDmgFlyEnabled ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed";
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.literal((String)("ArrowDmgFly \u5df2 " + status)), false);
            return 1;
        }))).then(Commands.m_82127_((String)"nojumpdelay").executes(ctx -> {
            MovementConfig config = MovementConfig.getInstance();
            config.noJumpDelayEnabled = !config.noJumpDelayEnabled;
            MovementConfig.save();
            String status = config.noJumpDelayEnabled ? "\u00a7a\u5f00\u542f" : "\u00a7c\u5173\u95ed";
            ((CommandSourceStack)ctx.getSource()).m_288197_(() -> Component.literal((String)("NoJumpDelay \u5df2 " + status)), false);
            return 1;
        }))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"tp").then(Commands.m_82129_((String)"x", (ArgumentType)IntegerArgumentType.integer()).then(Commands.m_82129_((String)"y", (ArgumentType)IntegerArgumentType.integer()).then(((RequiredArgumentBuilder)Commands.m_82129_((String)"z", (ArgumentType)IntegerArgumentType.integer()).executes(ctx -> {
            int x = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"x");
            int y = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"y");
            int z = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"z");
            TeleportFeature.teleportTo(x + 0.5, y, z + 0.5, true);
            return 1;
        })).then(Commands.m_82129_((String)"snap", (ArgumentType)BoolArgumentType.bool()).executes(ctx -> {
            int x = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"x");
            int y = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"y");
            int z = IntegerArgumentType.getInteger((CommandContext)ctx, (String)"z");
            boolean snap = BoolArgumentType.getBool((CommandContext)ctx, (String)"snap");
            TeleportFeature.teleportTo(x + 0.5, y, z + 0.5, snap);
            return 1;
        })))))).executes(ctx -> {
            TeleportFeature.teleportToCrosshair();
            return 1;
        })));
    }
}

