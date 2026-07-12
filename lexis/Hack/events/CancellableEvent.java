package lexis.Hack.events;

public abstract class CancellableEvent extends Event {
   private boolean cancelled = false;

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void cancel() {
      this.cancelled = true;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}
