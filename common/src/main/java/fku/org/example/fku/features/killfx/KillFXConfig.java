package fku.org.example.fku.features.killfx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import net.minecraft.client.Minecraft;

public class KillFXConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public boolean enabled = false;
    public boolean onlyTargeted = true;
    public double targetTimeout = 3.5;
    public boolean useLightning = true;
    public int lightningAmount = 1;
    public boolean useLightningSound = true;
    public boolean useParticles = true;
    public String particleCategory = "Magic";
    public String combatParticle = "CRIT";
    public String magicParticle = "END_ROD";
    public String fireParticle = "FLAME";
    public String natureParticle = "HEART";
    public String updateParticle = "DRAGON_BREATH";
    public String miscParticle = "SCULK_SOUL";
    public String particleShape = "Burst";
    public int particleCount = 40;
    public double particleSpeed = 0.2;
    public boolean useSound = true;
    public String soundGroup = "Combat";
    public String combatSound = "THUNDER";
    public String magicSound = "ANCHOR_CHARGE";
    public String creatureSound = "WARDEN";
    public String funSound = "PLING";
    public double volume = 1.0;
    public double pitch = 1.0;
    public boolean useFirework = false;
    public boolean useExplosion = false;
    public boolean useShader = false;
    public String shaderType = "\u65e0";
    public double shaderIntensity = 1.0;
    public int shaderDuration = 20;
    public double blackholeScale = 1.0;
    public String crystalStyle = "\u57fa\u7840\u6676\u4f53";
    public String crystalTintColor = "88CCFF";
    public double crystalRadius = 1.0;
    public double crystalGlowIntensity = 0.8;
    public double crystalRotationSpeed = 1.5;
    public boolean crystalPulse = true;
    private static KillFXConfig instance;

    private static File getConfigFile() {
        File configDir = new File(KillFXConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "killfx.json");
    }

    private static File getGameDirectory() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                return mc.gameDirectory;
            }
        }
        catch (Exception exception) {
            // ignored
        }
        return Paths.get(".", new String[0]).toAbsolutePath().normalize().toFile();
    }

    public static KillFXConfig getInstance() {
        if (instance == null) {
            KillFXConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = KillFXConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (KillFXConfig)GSON.fromJson(reader, KillFXConfig.class);
            }
            catch (IOException e) {
                instance = new KillFXConfig();
            }
        } else {
            instance = new KillFXConfig();
            KillFXConfig.save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(KillFXConfig.getConfigFile());){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

