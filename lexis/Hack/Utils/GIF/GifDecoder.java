package lexis.Hack.Utils.GIF;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

public class GifDecoder {
   public static List decode(InputStream inputStream) {
      try {
         ImageInputStream input = ImageIO.createImageInputStream(inputStream);

         ArrayList var27;
         try {
            ImageReader reader = (ImageReader)ImageIO.getImageReadersByFormatName("gif").next();
            reader.setInput(input, false, false);
            int numFrames = reader.getNumImages(true);
            int canvasW = reader.getWidth(0);
            int canvasH = reader.getHeight(0);

            try {
               IIOMetadata streamMeta = reader.getStreamMetadata();
               if (streamMeta != null) {
                  IIOMetadataNode tree = (IIOMetadataNode)streamMeta.getAsTree(streamMeta.getNativeMetadataFormatName());
                  IIOMetadataNode lsd = (IIOMetadataNode)tree.getElementsByTagName("LogicalScreenDescriptor").item(0);
                  if (lsd != null) {
                     canvasW = Integer.parseInt(lsd.getAttribute("logicalScreenWidth"));
                     canvasH = Integer.parseInt(lsd.getAttribute("logicalScreenHeight"));
                  }
               }
            } catch (Exception var21) {
            }

            List frames = new ArrayList(numFrames);
            BufferedImage canvas = new BufferedImage(canvasW, canvasH, 2);
            Graphics2D g = canvas.createGraphics();
            int i = 0;

            while(true) {
               if (i >= numFrames) {
                  g.dispose();
                  reader.dispose();
                  var27 = frames;
                  break;
               }

               BufferedImage frame = reader.read(i);
               IIOMetadataNode root = (IIOMetadataNode)reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
               IIOMetadataNode imgDescr = (IIOMetadataNode)root.getElementsByTagName("ImageDescriptor").item(0);
               int x = Integer.parseInt(imgDescr.getAttribute("imageLeftPosition"));
               int y = Integer.parseInt(imgDescr.getAttribute("imageTopPosition"));
               IIOMetadataNode gce = (IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
               String disposal = gce != null ? gce.getAttribute("disposalMethod") : "none";
               int delay = 100;
               if (gce != null) {
                  String d = gce.getAttribute("delayTime");
                  if (d != null && !d.isEmpty()) {
                     delay = Integer.parseInt(d) * 10;
                  }
               }

               g.drawImage(frame, x, y, (ImageObserver)null);
               BufferedImage snapshot = new BufferedImage(canvasW, canvasH, 2);
               Graphics2D sg = snapshot.createGraphics();
               sg.drawImage(canvas, 0, 0, (ImageObserver)null);
               sg.dispose();
               frames.add(new GifFrame(snapshot, delay));
               if ("restoreToBackgroundColor".equals(disposal)) {
                  g.setComposite(AlphaComposite.Clear);
                  g.fillRect(x, y, frame.getWidth(), frame.getHeight());
                  g.setComposite(AlphaComposite.SrcOver);
               }

               ++i;
            }
         } catch (Throwable var22) {
            if (input != null) {
               try {
                  input.close();
               } catch (Throwable var20) {
                  var22.addSuppressed(var20);
               }
            }

            throw var22;
         }

         if (input != null) {
            input.close();
         }

         return var27;
      } catch (IOException var23) {
         var23.printStackTrace();
         return null;
      }
   }

   public static class GifFrame {
      public final BufferedImage image;
      public final int delayMs;

      public GifFrame(BufferedImage image, int delayMs) {
         this.image = image;
         this.delayMs = delayMs;
      }
   }
}
