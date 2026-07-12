package lexis.Hack.events;

public class ChatMessage {
   public String text;
   public String sender;
   public long timestamp;

   public ChatMessage(String text, String sender) {
      this.text = text;
      this.sender = sender;
      this.timestamp = System.currentTimeMillis();
   }

   public ChatMessage(String text, String sender, long timestamp) {
      this.text = text;
      this.sender = sender;
      this.timestamp = timestamp;
   }
}
