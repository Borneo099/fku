package lexis.mixin.accessor;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({ChatComponent.class})
public interface AccessorChatComponent {
   @Accessor("allMessages")
   List getAllMessages();

   @Invoker("refreshTrimmedMessage")
   void invokeRefreshTrimmedMessage();
}
