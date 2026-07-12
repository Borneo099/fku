package lexis.mixin.accessor;

import net.minecraft.client.GuiMessage;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({GuiMessage.class})
public interface GuiMessageAccessor {
   @Accessor("content")
   Component getContent();

   @Accessor("content")
   void setContent(Component component);
}
