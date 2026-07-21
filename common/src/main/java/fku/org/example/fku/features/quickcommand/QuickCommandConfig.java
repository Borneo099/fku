package fku.org.example.fku.features.quickcommand;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;

public class QuickCommandConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static QuickCommandConfig instance;
    public boolean enabled = true;
    public List<CommandEntry> commands = new ArrayList<CommandEntry>();

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameDirectory != null) {
                return new File(new File(mc.gameDirectory, "fku"), "quick_command.json");
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return new File(Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile(), "fku/quick_command.json");
    }

    public static QuickCommandConfig getInstance() {
        if (instance == null) {
            QuickCommandConfig.load();
        }
        return instance;
    }

    public static void load() {
        File f = QuickCommandConfig.getConfigFile();
        if (f.exists()) {
            try (FileReader r = new FileReader(f);){
                instance = (QuickCommandConfig)GSON.fromJson(r, QuickCommandConfig.class);
            }
            catch (Exception e) {
                instance = new QuickCommandConfig();
            }
        } else {
            instance = new QuickCommandConfig();
            QuickCommandConfig.save();
        }
        if (QuickCommandConfig.instance.commands == null) {
            QuickCommandConfig.instance.commands = new ArrayList<CommandEntry>();
        }
        if (QuickCommandConfig.instance.commands.isEmpty()) {
            CommandEntry e = new CommandEntry();
            QuickCommandConfig.instance.commands.add(e);
            QuickCommandConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        QuickCommandConfig.getConfigFile().getParentFile().mkdirs();
        try (FileWriter w = new FileWriter(QuickCommandConfig.getConfigFile());){
            GSON.toJson(instance, w);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        QuickCommandConfig.save();
    }

    public static class CommandEntry {
        public String command = "/say hello";
        public int hotkeyKey = -1;
        public int hotkeyModifiers = 0;
        public boolean enabled = true;
    }
}

