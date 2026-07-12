package lexis.Hack.Utils.Chat;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public class BetterChatComponent implements Component {
   private final Component original;

   public BetterChatComponent(Component original) {
      this.original = original;
   }

   public Component unwrap() {
      return this.original;
   }

   public Style m_7383_() {
      return this.original.m_7383_();
   }

   public ComponentContents m_214077_() {
      return this.original.m_214077_();
   }

   public List m_7360_() {
      return this.original.m_7360_();
   }

   public FormattedCharSequence m_7532_() {
      return this.original.m_7532_();
   }
}
