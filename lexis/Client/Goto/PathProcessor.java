package lexis.Client.Goto;

import java.util.ArrayList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public abstract class PathProcessor {
   protected static final Minecraft MC = Minecraft.m_91087_();
   protected static final LexisUtil LEXIS;
   private static final KeyMapping[] CONTROLS;
   protected final ArrayList path;
   protected int index;
   protected boolean done;
   protected int ticksOffPath;
   private static boolean controlsLocked;

   public PathProcessor(ArrayList path) {
      if (path.isEmpty()) {
         throw new IllegalStateException("There is no path!");
      } else {
         this.path = path;
      }
   }

   public abstract void process();

   public final int getIndex() {
      return this.index;
   }

   public final boolean isDone() {
      return this.done;
   }

   public final int getTicksOffPath() {
      return this.ticksOffPath;
   }

   protected final void facePosition(BlockPos pos) {
      LEXIS.getRotationFaker().faceVectorClientIgnorePitch(Vec3.m_82512_(pos));
   }

   public static final void lockControls() {
      controlsLocked = true;
      MC.f_91066_.f_92085_.m_7249_(false);
      MC.f_91066_.f_92087_.m_7249_(false);
      MC.f_91066_.f_92086_.m_7249_(false);
      MC.f_91066_.f_92088_.m_7249_(false);
      MC.f_91066_.f_92089_.m_7249_(false);
      MC.f_91066_.f_92090_.m_7249_(false);
   }

   public static final void releaseControls() {
      controlsLocked = false;
      MC.f_91066_.f_92085_.m_7249_(false);
      MC.f_91066_.f_92087_.m_7249_(false);
      MC.f_91066_.f_92086_.m_7249_(false);
      MC.f_91066_.f_92088_.m_7249_(false);
      MC.f_91066_.f_92089_.m_7249_(false);
      MC.f_91066_.f_92090_.m_7249_(false);
   }

   public static boolean areControlsLocked() {
      return controlsLocked;
   }

   static {
      LEXIS = LexisUtil.INSTANCE;
      controlsLocked = false;
      CONTROLS = new KeyMapping[]{MC.f_91066_.f_92085_, MC.f_91066_.f_92087_, MC.f_91066_.f_92086_, MC.f_91066_.f_92088_, MC.f_91066_.f_92089_, MC.f_91066_.f_92090_};
   }
}
