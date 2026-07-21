package fku.org.example.fku.features.fakeplayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class FakePlayerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = false;
    public String name = "FakePlayer";
    public int health = 20;
    public boolean copyInv = true;
    public boolean simulateDamage = true;
    public int invulnerableTicks = 20;
    public boolean autoTotem = true;
    public boolean showDamage = true;
    public boolean respawn = false;
    private static FakePlayerConfig instance;

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            return new File(configDir, "fake_player.json");
        }
        catch (Exception exception) {
            return Paths.get(".", "config", "fku", "fake_player.json").toAbsolutePath().normalize().toFile();
        }
    }

    public static FakePlayerConfig getInstance() {
        if (instance == null) {
            FakePlayerConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = FakePlayerConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (FakePlayerConfig)GSON.fromJson(reader, FakePlayerConfig.class);
                System.out.println("[FakePlayer] \u914d\u7f6e\u5df2\u52a0\u8f7d: " + configFile.getAbsolutePath());
            }
            catch (IOException e) {
                System.out.println("[FakePlayer] \u914d\u7f6e\u52a0\u8f7d\u5931\u8d25\uff0c\u4f7f\u7528\u9ed8\u8ba4\u503c");
                instance = new FakePlayerConfig();
            }
        } else {
            instance = new FakePlayerConfig();
            FakePlayerConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = FakePlayerConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
            System.out.println("[FakePlayer] \u914d\u7f6e\u5df2\u4fdd\u5b58: " + configFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        FakePlayerConfig.save();
    }

    public void setName(String v) {
        this.name = v;
        FakePlayerConfig.save();
    }

    public void setHealth(int v) {
        this.health = v;
        FakePlayerConfig.save();
    }

    public void setCopyInv(boolean v) {
        this.copyInv = v;
        FakePlayerConfig.save();
    }

    public void setSimulateDamage(boolean v) {
        this.simulateDamage = v;
        FakePlayerConfig.save();
    }

    public void setAutoTotem(boolean v) {
        this.autoTotem = v;
        FakePlayerConfig.save();
    }

    public void setShowDamage(boolean v) {
        this.showDamage = v;
        FakePlayerConfig.save();
    }

    public void setInvulnerableTicks(int v) {
        this.invulnerableTicks = v;
        FakePlayerConfig.save();
    }

    public void setRespawn(boolean v) {
        this.respawn = v;
        FakePlayerConfig.save();
    }
}

