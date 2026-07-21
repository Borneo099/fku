package fku.org.example.fku.features.displaymodel;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import fku.org.example.fku.Fku;
import fku.org.example.fku.features.displaymodel.DisplayModelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="fku", bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DisplayModelFeature {
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(DisplayModelFeature::onRegisterClientCommands);
    }

    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher dispatcher = event.getDispatcher();
        dispatcher.register((LiteralArgumentBuilder)Commands.m_82127_((String)"fku").then(Commands.m_82127_((String)"displaymodel").executes(DisplayModelFeature::openDisplayModelScreen)));
    }

    private static int openDisplayModelScreen(CommandContext<CommandSourceStack> context) {
        Fku.LOGGER.info("Opening DisplayModelScreen");
        Minecraft.getInstance().setScreen((Screen)new DisplayModelScreen());
        return 1;
    }
}

