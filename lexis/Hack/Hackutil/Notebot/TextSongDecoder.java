package lexis.Hack.Hackutil.Notebot;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import org.apache.commons.io.FilenameUtils;

public class TextSongDecoder extends SongDecoder {
   public Song parse(File file) throws Exception {
      List data = Files.readAllLines(file.toPath());
      Map notesMap = new HashMap();
      String title = FilenameUtils.getBaseName(file.getName());
      String author = "Unknown";

      for(int lineNumber = 0; lineNumber < data.size(); ++lineNumber) {
         String line = ((String)data.get(lineNumber)).trim();
         if (line.startsWith("// Name: ")) {
            title = line.substring(9);
         } else if (line.startsWith("// Author: ")) {
            author = line.substring(11);
         } else if (!line.isEmpty() && !line.startsWith("//")) {
            String[] parts = line.split(":");
            if (parts.length < 2) {
               if (this.notebot != null) {
                  this.notebot.sendMessage("§c第 " + lineNumber + " 行格式错误");
               }
            } else {
               try {
                  int tick = Integer.parseInt(parts[0]);
                  int noteLevel = Integer.parseInt(parts[1]);
                  int instrumentType = 0;
                  if (parts.length > 2) {
                     instrumentType = Integer.parseInt(parts[2]);
                  }

                  NoteBlockInstrument instrument = NoteBlockInstrument.values()[instrumentType];
                  Note note = new Note(instrument, noteLevel);
                  ((List)notesMap.computeIfAbsent(tick, (k) -> {
                     return new ArrayList();
                  })).add(note);
               } catch (NumberFormatException var14) {
                  if (this.notebot != null) {
                     this.notebot.sendMessage("§c第 " + lineNumber + " 行包含无效数字");
                  }
               }
            }
         }
      }

      return new Song(notesMap, title, author);
   }
}
