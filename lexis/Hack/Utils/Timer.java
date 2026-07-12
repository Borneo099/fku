package lexis.Hack.Utils;

public class Timer {
   private long time = -1L;

   public Timer() {
      this.reset();
   }

   public Timer reset() {
      this.time = System.nanoTime();
      return this;
   }

   public boolean passedMs(long ms) {
      return System.nanoTime() - this.time >= ms * 1000000L;
   }

   public boolean passedMs(double ms) {
      return this.passedMs((long)ms);
   }

   public boolean passedS(double s) {
      return this.passedMs(s * 1000.0);
   }

   public long getPassedTimeMs() {
      return (System.nanoTime() - this.time) / 1000000L;
   }
}
