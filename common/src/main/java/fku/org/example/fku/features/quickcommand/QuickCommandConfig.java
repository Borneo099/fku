package fku.org.example.fku.features.quickcommand; /* water */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuickCommandConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QuickCommandConfig instance;
    public boolean enabled = true;
    /** 多条指令，每条可有独立热键 */
    public List<CommandEntry> commands = new ArrayList<>();

    public static class CommandEntry {
        public String command = "/say hello";
        public int hotkeyKey = -1;         // GLFW key code
        public int hotkeyModifiers = 0;    // bit0=Shift, bit1=Ctrl, bit2=Alt
        public boolean enabled = true;
    }

    private static File getConfigFile() {
        try { var mc = net.minecraft.client.Minecraft.getInstance(); if (mc != null && mc.gameDirectory != null) return new File(new File(mc.gameDirectory, "fku"), "quick_command.json"); } catch (Exception ignored) {}
        return new File(Paths.get("config").toAbsolutePath().normalize().getParent().toFile(), "fku/quick_command.json");
    }
    public static QuickCommandConfig getInstance() { if (instance == null) load(); return instance; }
    public static void load() { File f = getConfigFile(); if (f.exists()) { try (FileReader r = new FileReader(f)) { instance = GSON.fromJson(r, QuickCommandConfig.class); } catch (Exception e) { instance = new QuickCommandConfig(); } } else { instance = new QuickCommandConfig(); save(); } if (instance.commands == null) instance.commands = new ArrayList<>(); if (instance.commands.isEmpty()) { CommandEntry e = new CommandEntry(); instance.commands.add(e); save(); } }
    public static void save() { if (instance == null) return; getConfigFile().getParentFile().mkdirs(); try (FileWriter w = new FileWriter(getConfigFile())) { GSON.toJson(instance, w); } catch (IOException e) { e.printStackTrace(); } }
    public void setEnabled(boolean v) { this.enabled = v; save(); }
}
