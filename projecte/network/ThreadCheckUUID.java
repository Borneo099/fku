package moze_intel.projecte.network;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import moze_intel.projecte.PECore;

public class ThreadCheckUUID extends Thread {
   private static boolean hasRunServer = false;
   private static boolean hasRunClient = false;
   private static final String uuidURL = "https://raw.githubusercontent.com/sinkillerj/ProjectE/mc1.14.x/haUUID.txt";
   private final boolean isServerSide;

   public ThreadCheckUUID(boolean isServer) {
      this.isServerSide = isServer;
      this.setName("ProjectE UUID Checker " + (isServer ? "Server" : "Client"));
   }

   public void run() {
      try {
         BufferedReader reader = new BufferedReader(new InputStreamReader((new URL("https://raw.githubusercontent.com/sinkillerj/ProjectE/mc1.14.x/haUUID.txt")).openStream()));

         try {
            String line = reader.readLine();
            if (line == null) {
               PECore.LOGGER.error(LogUtils.FATAL_MARKER, "UUID check failed!");
               throw new IOException("No data from github UUID list!");
            }

            List uuids = new ArrayList();

            while(true) {
               if ((line = reader.readLine()) == null || line.startsWith("###UUID")) {
                  PECore.uuids.addAll(uuids);
                  break;
               }

               if (!line.isEmpty()) {
                  uuids.add(line);
               }
            }
         } catch (Throwable var10) {
            try {
               reader.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }

            throw var10;
         }

         reader.close();
      } catch (IOException var11) {
         PECore.LOGGER.error(LogUtils.FATAL_MARKER, "Caught exception in UUID Checker thread!", var11);
      } finally {
         if (this.isServerSide) {
            hasRunServer = true;
         } else {
            hasRunClient = true;
         }

      }

   }

   public static boolean hasRunServer() {
      return hasRunServer;
   }

   public static boolean hasRunClient() {
      return hasRunClient;
   }
}
