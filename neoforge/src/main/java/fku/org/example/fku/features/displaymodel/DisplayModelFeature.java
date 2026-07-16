package fku.org.example.fku.features.displaymodel; /* water */

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import fku.org.example.fku.Fku;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Fku.MOD_ID)
public class DisplayModelFeature {
    
    public static void init() {
        // 注册事件监听器
        NeoForge.EVENT_BUS.addListener(DisplayModelFeature::onRegisterClientCommands);
    }
    
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("fku")
                .then(Commands.literal("displaymodel")
                        .executes(DisplayModelFeature::openDisplayModelScreen)));
    }
    
    private static int openDisplayModelScreen(CommandContext<CommandSourceStack> context) {
        Fku.LOGGER.info("Opening DisplayModelScreen");
        net.minecraft.client.Minecraft.getInstance().setScreen(new DisplayModelScreen());
        return 1;
    }
}