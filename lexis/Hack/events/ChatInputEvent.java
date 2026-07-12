package lexis.Hack.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.network.chat.Component;

public class ChatInputEvent extends CancellableEvent {
   private Component component;
   private List chatLines;

   public ChatInputEvent(Component component, List chatLines) {
      this.component = component;
      this.chatLines = chatLines;
   }

   public Component getComponent() {
      return this.component;
   }

   public void setComponent(Component component) {
      this.component = component;
   }

   public List getChatLines() {
      return this.chatLines;
   }

   public void fire(ArrayList listeners) {
      Iterator var2 = listeners.iterator();

      while(var2.hasNext()) {
         ChatInputListener listener = (ChatInputListener)var2.next();
         listener.onReceivedMessage(this);
         if (this.isCancelled()) {
            break;
         }
      }

   }

   public Class getListenerType() {
      return ChatInputListener.class;
   }
}
