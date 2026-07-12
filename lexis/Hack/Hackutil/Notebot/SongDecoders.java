package lexis.Hack.Hackutil.Notebot;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lexis.Hack.Hacks.Misc.NotebotHack;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.apache.commons.io.FilenameUtils;

public class SongDecoders {
   private static final Map decoders = new HashMap();

   public static void registerDecoder(String extension, SongDecoder decoder) {
      decoders.put(extension, decoder);
   }

   public static SongDecoder getDecoder(File file) {
      return (SongDecoder)decoders.get(FilenameUtils.getExtension(file.getName()).toLowerCase());
   }

   public static boolean hasDecoder(File file) {
      return decoders.containsKey(FilenameUtils.getExtension(file.getName()).toLowerCase());
   }

   public static boolean hasDecoder(Path path) {
      return hasDecoder(path.toFile());
   }

   public static Song parse(File file, NotebotHack notebot) throws Exception {
      if (!hasDecoder(file)) {
         throw new IllegalStateException("Decoder for this file does not exist!");
      } else {
         SongDecoder decoder = getDecoder(file);
         decoder.setNotebot(notebot);
         Song song = decoder.parse(file);
         fixSong(song, notebot);
         song.finishLoading();
         return song;
      }
   }

   private static void fixSong(Song song, NotebotHack notebot) {
      Map notesMap = song.getNotesMap();
      Map newMap = new HashMap();
      Iterator var4 = notesMap.entrySet().iterator();

      label48:
      while(var4.hasNext()) {
         Map.Entry entry = (Map.Entry)var4.next();
         int tick = (Integer)entry.getKey();
         List notes = (List)entry.getValue();
         List newNotes = new ArrayList();
         Iterator var9 = notes.iterator();

         while(true) {
            Note note;
            while(true) {
               if (!var9.hasNext()) {
                  if (!newNotes.isEmpty()) {
                     newMap.put(tick, newNotes);
                  }
                  continue label48;
               }

               note = (Note)var9.next();
               int n = note.getNoteLevel();
               if (n >= 0 && n <= 24) {
                  break;
               }

               if (notebot.roundOutOfRange) {
                  note.setNoteLevel(n < 0 ? 0 : 24);
                  break;
               }

               notebot.sendMessage("§e警告: 第 " + tick + " tick 的音符超出范围");
            }

            if (notebot.mode == NotebotHack.NotebotMode.ExactInstruments) {
               NoteBlockInstrument newInstrument = notebot.getMappedInstrument(note.getInstrument());
               if (newInstrument != null) {
                  note.setInstrument(newInstrument);
               }

               newNotes.add(note);
            } else {
               note.setInstrument((NoteBlockInstrument)null);
               newNotes.add(note);
            }
         }
      }

      song.getNotesMap().clear();
      song.getNotesMap().putAll(newMap);
   }

   static {
      registerDecoder("nbs", new NBSSongDecoder());
      registerDecoder("txt", new TextSongDecoder());
   }
}
