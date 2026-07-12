package lexis.Hack.events;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public class PacketInterceptor {
   public static final PacketInterceptor INSTANCE = new PacketInterceptor();
   private final List clientHandlers = new ArrayList();
   private Connection connection;

   private PacketInterceptor() {
   }

   public void init(Connection conn) {
      if (this.connection == null) {
         this.connection = conn;
         ChannelPipeline pipeline = conn.channel().pipeline();
         if (pipeline.get("lexis_packet_writer") == null) {
            pipeline.addBefore("packet_handler", "lexis_packet_writer", new ChannelOutboundHandlerAdapter() {
               public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
                  if (!(msg instanceof Packet)) {
                     ctx.write(msg, promise);
                  } else {
                     ClientPacketArgs args = new ClientPacketArgs();
                     args.packet = (Packet)msg;
                     Iterator var5 = PacketInterceptor.this.clientHandlers.iterator();

                     do {
                        if (!var5.hasNext()) {
                           ctx.write(msg, promise);
                           return;
                        }

                        Consumer handler = (Consumer)var5.next();
                        handler.accept(args);
                     } while(!args.skip);

                     promise.setSuccess();
                  }
               }
            });
         }

      }
   }

   public void addClientHandler(Consumer handler) {
      this.clientHandlers.add(handler);
   }

   public void sendPacket(Packet packet) {
      if (this.connection != null) {
         this.connection.m_129512_(packet);
      }

   }

   public static class ClientPacketArgs {
      public Packet packet;
      public boolean skip;
   }
}
