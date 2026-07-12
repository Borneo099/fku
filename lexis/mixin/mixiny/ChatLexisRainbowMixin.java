package lexis.mixin.mixiny;

import java.util.List;
import lexis.Hack.Utils.Chat.RainbowTagProcessor;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatComponent.class})
public abstract class ChatLexisRainbowMixin {
   @Shadow
   @Final
   private List f_93761_;

   @Inject(
      method = {"render"},
      at = {@At("HEAD")}
   )
   private void lexis$updateRainbow(GuiGraphics p_282077_, int p_283491_, int p_282406_, int p_283111_, CallbackInfo ci) {
      if (!this.f_93761_.isEmpty()) {
         long now = System.currentTimeMillis();

         for(int i = 0; i < this.f_93761_.size(); ++i) {
            GuiMessage.Line line = (GuiMessage.Line)this.f_93761_.get(i);
            Component rebuilt = RainbowTagProcessor.tryRebuild(line.f_240339_(), now);
            if (rebuilt != null) {
               FormattedCharSequence newSeq = rebuilt.m_7532_();
               GuiMessage.Line newLine = new GuiMessage.Line(line.f_240350_(), newSeq, line.f_240351_(), line.f_240367_());
               this.f_93761_.set(i, newLine);
            }
         }

      }
   }
}
