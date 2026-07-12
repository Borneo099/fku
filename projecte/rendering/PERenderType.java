package moze_intel.projecte.rendering;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;

public class PERenderType extends RenderType {
   public static final Function SPRITE_RENDERER = Util.m_143827_((resourceLocation) -> {
      RenderType.CompositeState state = CompositeState.m_110628_().m_173292_(RenderStateShard.f_173102_).m_173290_(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).m_110691_(true);
      return m_173215_("projecte_sprite_renderer", DefaultVertexFormat.f_85817_, Mode.QUADS, 256, true, false, state);
   });
   public static final Function YEU_RENDERER = Util.m_143827_((resourceLocation) -> {
      RenderType.CompositeState state = CompositeState.m_110628_().m_173292_(RenderStateShard.f_173101_).m_173290_(new RenderStateShard.TextureStateShard(resourceLocation, false, false)).m_110685_(f_110139_).m_110661_(f_110110_).m_110691_(true);
      return m_173215_("projecte_yeu_renderer", DefaultVertexFormat.f_85818_, Mode.QUADS, 256, true, false, state);
   });
   public static final RenderType TRANSMUTATION_OVERLAY;

   private PERenderType(String name, VertexFormat format, VertexFormat.Mode drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTask, Runnable clearTask) {
      super(name, format, drawMode, bufferSize, useDelegate, needsSorting, setupTask, clearTask);
   }

   static {
      TRANSMUTATION_OVERLAY = m_173215_("projecte_transmutation_overlay", DefaultVertexFormat.f_85815_, Mode.QUADS, 256, true, false, CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110661_(f_110110_).m_110687_(f_110115_).m_110669_(f_110118_).m_110691_(true));
   }
}
