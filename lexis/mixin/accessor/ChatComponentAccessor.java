package lexis.mixin.accessor;

import java.util.List;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ChatComponent.class})
public interface ChatComponentAccessor {
   @Accessor("allMessages")
   List getAllMessages();

   @Accessor("trimmedMessages")
   List getTrimmedMessages();
}
