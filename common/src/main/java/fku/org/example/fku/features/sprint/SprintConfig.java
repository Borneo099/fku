package fku.org.example.fku.features.sprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class SprintConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = false;
    public String mode = "OMNIROTATIONAL";
    public boolean ignoreBlindness = false;
    public boolean ignoreHunger = false;
    public boolean ignoreCollision = false;
    public boolean stopOnGround = false;
    public boolean stopOnAir = false;
    public boolean elytraRotation = true;
    public boolean smoothRotation = false;
    public int rotationSpeed = 90;
    private static SprintConfig instance;

    private static File getConfigFile() {
        try {
            Minecraft mc = Minecraft.getInstance();
            File configDir = new File(mc.gameDirectory, "fku");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            return new File(configDir, "sprint.json");
        }
        catch (Exception exception) {
            return Paths.get(".", "config", "fku", "sprint.json").toAbsolutePath().normalize().toFile();
        }
    }

    public static SprintConfig getInstance() {
        if (instance == null) {
            SprintConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = SprintConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (SprintConfig)GSON.fromJson(reader, SprintConfig.class);
                System.out.println("[Sprint] \u914d\u7f6e\u5df2\u52a0\u8f7d: " + configFile.getAbsolutePath());
            }
            catch (IOException e) {
                System.out.println("[Sprint] \u914d\u7f6e\u52a0\u8f7d\u5931\u8d25\uff0c\u4f7f\u7528\u9ed8\u8ba4\u503c");
                instance = new SprintConfig();
            }
        } else {
            instance = new SprintConfig();
            SprintConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = SprintConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
            System.out.println("[Sprint] \u914d\u7f6e\u5df2\u4fdd\u5b58: " + configFile.getAbsolutePath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Mode getMode() {
        try {
            return Mode.valueOf(this.mode);
        }
        catch (IllegalArgumentException e) {
            return Mode.OMNIROTATIONAL;
        }
    }

    public static enum Mode {
        LEGIT("Legit"),
        OMNIDIRECTIONAL("Omnidirectional"),
        OMNIROTATIONAL("Omnirotational");

        private final String label;

        private Mode(String label) {
            this.label = label;
        }

        public String toString() {
            return this.label;
        }

        public String getLabel() {
            return this.label;
        }

        public String getChineseLabel() {
            return switch (this) {
                default -> throw new IncompatibleClassChangeError();
                case LEGIT -> "\u539f\u7248\u6a21\u5f0f";
                case OMNIDIRECTIONAL -> "\u5168\u5411\u75be\u8dd1";
                case OMNIROTATIONAL -> "\u5168\u5411\u65cb\u8f6c";
            };
        }
    }
}

