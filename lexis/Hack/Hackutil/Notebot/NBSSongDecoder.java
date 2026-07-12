package lexis.Hack.Hackutil.Notebot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class NBSSongDecoder extends SongDecoder {
   public static final int NOTE_OFFSET = 33;

   public Song parse(File songFile) throws Exception {
      return this.parse((InputStream)(new FileInputStream(songFile)));
   }

   private Song parse(InputStream inputStream) throws Exception {
      Map notesMap = new HashMap();
      DataInputStream dataInputStream = new DataInputStream(inputStream);
      short length = this.readShort(dataInputStream);
      int nbsversion = 0;
      if (length == 0) {
         nbsversion = dataInputStream.readByte();
         dataInputStream.readByte();
         if (nbsversion >= 3) {
            this.readShort(dataInputStream);
         }
      }

      this.readShort(dataInputStream);
      String title = this.readString(dataInputStream);
      String author = this.readString(dataInputStream);
      this.readString(dataInputStream);
      this.readString(dataInputStream);
      float speed = (float)this.readShort(dataInputStream) / 100.0F;
      dataInputStream.readBoolean();
      dataInputStream.readByte();
      dataInputStream.readByte();
      this.readInt(dataInputStream);
      this.readInt(dataInputStream);
      this.readInt(dataInputStream);
      this.readInt(dataInputStream);
      this.readInt(dataInputStream);
      this.readString(dataInputStream);
      if (nbsversion >= 4) {
         dataInputStream.readByte();
         dataInputStream.readByte();
         this.readShort(dataInputStream);
      }

      double tick = -1.0;

      while(true) {
         short jumpTicks = this.readShort(dataInputStream);
         if (jumpTicks == 0) {
            return new Song(notesMap, title, author);
         }

         tick += (double)((float)jumpTicks * (20.0F / speed));

         while(true) {
            short jumpLayers = this.readShort(dataInputStream);
            if (jumpLayers == 0) {
               break;
            }

            byte instrument = dataInputStream.readByte();
            byte key = dataInputStream.readByte();
            if (nbsversion >= 4) {
               dataInputStream.readUnsignedByte();
               dataInputStream.readUnsignedByte();
               this.readShort(dataInputStream);
            }

            NoteBlockInstrument inst = this.fromNBSInstrument(instrument);
            if (inst != null) {
               Note note = new Note(inst, key - 33);
               int tickInt = (int)Math.round(tick);
               ((List)notesMap.computeIfAbsent(tickInt, (k) -> {
                  return new ArrayList();
               })).add(note);
            }
         }
      }
   }

   private short readShort(DataInputStream dis) throws IOException {
      int byte1 = dis.readUnsignedByte();
      int byte2 = dis.readUnsignedByte();
      return (short)(byte1 + (byte2 << 8));
   }

   private int readInt(DataInputStream dis) throws IOException {
      int byte1 = dis.readUnsignedByte();
      int byte2 = dis.readUnsignedByte();
      int byte3 = dis.readUnsignedByte();
      int byte4 = dis.readUnsignedByte();
      return byte1 + (byte2 << 8) + (byte3 << 16) + (byte4 << 24);
   }

   private String readString(DataInputStream dis) throws IOException {
      int length = this.readInt(dis);
      if (length >= 0 && length <= dis.available()) {
         byte[] bytes = new byte[length];
         dis.readFully(bytes);
         return new String(bytes);
      } else {
         return "";
      }
   }

   private NoteBlockInstrument fromNBSInstrument(int instrument) {
      NoteBlockInstrument var10000;
      switch (instrument) {
         case 0:
            var10000 = NoteBlockInstrument.HARP;
            break;
         case 1:
            var10000 = NoteBlockInstrument.BASS;
            break;
         case 2:
            var10000 = NoteBlockInstrument.BASEDRUM;
            break;
         case 3:
            var10000 = NoteBlockInstrument.SNARE;
            break;
         case 4:
            var10000 = NoteBlockInstrument.HAT;
            break;
         case 5:
            var10000 = NoteBlockInstrument.GUITAR;
            break;
         case 6:
            var10000 = NoteBlockInstrument.FLUTE;
            break;
         case 7:
            var10000 = NoteBlockInstrument.BELL;
            break;
         case 8:
            var10000 = NoteBlockInstrument.CHIME;
            break;
         case 9:
            var10000 = NoteBlockInstrument.XYLOPHONE;
            break;
         case 10:
            var10000 = NoteBlockInstrument.IRON_XYLOPHONE;
            break;
         case 11:
            var10000 = NoteBlockInstrument.COW_BELL;
            break;
         case 12:
            var10000 = NoteBlockInstrument.DIDGERIDOO;
            break;
         case 13:
            var10000 = NoteBlockInstrument.BIT;
            break;
         case 14:
            var10000 = NoteBlockInstrument.BANJO;
            break;
         case 15:
            var10000 = NoteBlockInstrument.PLING;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }
}
