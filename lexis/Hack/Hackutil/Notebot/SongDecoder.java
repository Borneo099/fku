package lexis.Hack.Hackutil.Notebot;

import java.io.File;
import lexis.Hack.Hacks.Misc.NotebotHack;

public abstract class SongDecoder {
   protected NotebotHack notebot;

   public void setNotebot(NotebotHack notebot) {
      this.notebot = notebot;
   }

   public abstract Song parse(File file) throws Exception;
}
