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

public class NetworkPacketsController {
   public static final NetworkPacketsController instance = new NetworkPacketsController();
   private final List clientHandlers = new ArrayList();
   private Connection connection;
   private boolean injected = false;

   public void addClientHandler(Consumer handler) {
      this.clientHandlers.add(handler);
   }

   public void inject(Connection conn) {
      if (!this.injected) {
         this.connection = conn;
         ChannelPipeline pipeline = conn.channel().pipeline();
         pipeline.addBefore("packet_handler", "lexis_packet_writer", new ChannelOutboundHandlerAdapter() {
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
               if (!(msg instanceof Packet)) {
                  ctx.write(msg, promise);
               } else {
                  ClientPacketArgs args = new ClientPacketArgs();
                  args.packet = (Packet)msg;
                  Iterator var5 = NetworkPacketsController.this.clientHandlers.iterator();

                  do {
                     if (!var5.hasNext()) {
                        ctx.write(args.packet, promise);
                        return;
                     }

                     Consumer handler = (Consumer)var5.next();
                     handler.accept(args);
                  } while(!args.skip);

                  promise.setSuccess();
               }
            }
         });
         this.injected = true;
      }
   }

   public void sendPacket(Packet packet) {
      if (this.connection != null) {
         this.connection.m_129512_(packet);
      }

   }

   public static class ClientPacketArgs {
      public Packet packet;
      public boolean skip = false;
   }
}
