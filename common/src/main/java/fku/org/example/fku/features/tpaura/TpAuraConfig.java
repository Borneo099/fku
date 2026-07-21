package fku.org.example.fku.features.tpaura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;

public class TpAuraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static TpAuraConfig instance;
    public String attackMode = "Smart";
    public double cooldownThreshold = 1.0;
    public int attackDelay = 0;
    public boolean autoSwitch = true;
    public boolean requireMace = false;
    public boolean swingHand = true;
    public boolean silentSwap = true;
    public String mode = "Paper";
    public double maxRange = 49.0;
    public boolean goUp = true;
    public int paperPackets = 8;
    public boolean limitCeiling = true;
    public int ceilingScanStep = 1;
    public boolean returnPos = true;
    public boolean offsetFix = true;
    public boolean attackAllEntities = true;
    public String entityTypes = "PLAYER";
    public int attackDistance = 3;
    public int tpOffset = 0;
    public boolean ignoreFriends = false;
    public boolean ignoreNamed = true;
    public boolean ignoreTamed = false;
    public boolean whitelistEnabled = false;
    public String whitelist = "";
    public boolean renderPath = true;
    public int pathColorR = 255;
    public int pathColorG = 0;
    public int pathColorB = 0;
    public int pathColorA = 100;
    public int targetColorR = 255;
    public int targetColorG = 0;
    public int targetColorB = 0;
    public int targetColorA = 200;
    public int hotkeyKey = -1;
    public String hotkeyName = "";
    public boolean totemBypass = false;
    public int totemAttacks = 2;
    public int totemHeightIncrease = 9;
    public boolean autoFlight = false;
    public double autoFlightSpeed = 0.3;
    public double autoFlightHorizontalSpeed = 1.0;
    public boolean enabled = false;

    private static File getConfigFile() {
        File configDir = new File(TpAuraConfig.getGameDirectory(), "fku");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        return new File(configDir, "tpaura.json");
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
        return Paths.get("config", new String[0]).toAbsolutePath().normalize().getParent().toFile();
    }

    public static TpAuraConfig getInstance() {
        if (instance == null) {
            TpAuraConfig.load();
        }
        return instance;
    }

    public static void load() {
        File configFile = TpAuraConfig.getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile);){
                instance = (TpAuraConfig)GSON.fromJson(reader, TpAuraConfig.class);
            }
            catch (IOException e) {
                instance = new TpAuraConfig();
            }
        } else {
            instance = new TpAuraConfig();
            TpAuraConfig.save();
        }
    }

    public static void save() {
        if (instance == null) {
            return;
        }
        File configFile = TpAuraConfig.getConfigFile();
        try (FileWriter writer = new FileWriter(configFile);){
            GSON.toJson(instance, writer);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getEntityTypeSet() {
        if (this.entityTypes == null || this.entityTypes.isEmpty()) {
            return new HashSet<String>(Arrays.asList("player"));
        }
        HashSet<String> set = new HashSet<String>();
        for (String s : this.entityTypes.split(",")) {
            set.add(s.trim().toLowerCase());
        }
        return set;
    }

    public int getPathColor() {
        return this.pathColorA << 24 | (this.pathColorR & 0xFF) << 16 | (this.pathColorG & 0xFF) << 8 | this.pathColorB & 0xFF;
    }

    public int getTargetColor() {
        return this.targetColorA << 24 | (this.targetColorR & 0xFF) << 16 | (this.targetColorG & 0xFF) << 8 | this.targetColorB & 0xFF;
    }

    public void setEnabled(boolean v) {
        this.enabled = v;
        TpAuraConfig.save();
    }

    public void setAttackMode(String v) {
        this.attackMode = "Smart".equals(v) || "Fast".equals(v) || "Universal".equals(v) ? v : "Smart";
        TpAuraConfig.save();
    }

    public void setCooldownThreshold(double v) {
        this.cooldownThreshold = Math.max(0.1, Math.min(1.0, v));
        TpAuraConfig.save();
    }

    public void setAttackDelay(int v) {
        this.attackDelay = Math.max(0, Math.min(20, v));
        TpAuraConfig.save();
    }

    public void setAutoSwitch(boolean v) {
        this.autoSwitch = v;
        TpAuraConfig.save();
    }

    public void setRequireMace(boolean v) {
        this.requireMace = v;
        TpAuraConfig.save();
    }

    public void setSwingHand(boolean v) {
        this.swingHand = v;
        TpAuraConfig.save();
    }

    public void setSilentSwap(boolean v) {
        this.silentSwap = v;
        TpAuraConfig.save();
    }

    public void setMode(String v) {
        this.mode = "Vanilla".equals(v) || "Paper".equals(v) ? v : "Paper";
        TpAuraConfig.save();
    }

    public void setMaxRange(double v) {
        this.maxRange = Math.max(1.0, Math.min(99.0, v));
        TpAuraConfig.save();
    }

    public void setGoUp(boolean v) {
        this.goUp = v;
        TpAuraConfig.save();
    }

    public void setPaperPackets(int v) {
        this.paperPackets = Math.max(1, Math.min(20, v));
        TpAuraConfig.save();
    }

    public void setLimitCeiling(boolean v) {
        this.limitCeiling = v;
        TpAuraConfig.save();
    }

    public void setCeilingScanStep(int v) {
        this.ceilingScanStep = Math.max(1, Math.min(2, v));
        TpAuraConfig.save();
    }

    public void setReturnPos(boolean v) {
        this.returnPos = v;
        TpAuraConfig.save();
    }

    public void setOffsetFix(boolean v) {
        this.offsetFix = v;
        TpAuraConfig.save();
    }

    public void setEntityTypes(String v) {
        this.entityTypes = v != null && !v.isEmpty() ? v : "player";
        TpAuraConfig.save();
    }

    public void setAttackAllEntities(boolean v) {
        this.attackAllEntities = v;
        TpAuraConfig.save();
    }

    public void setAttackDistance(int v) {
        this.attackDistance = Math.max(3, Math.min(6, v));
        TpAuraConfig.save();
    }

    public void setTpOffset(int v) {
        this.tpOffset = Math.max(0, Math.min(6, v));
        TpAuraConfig.save();
    }

    public void setIgnoreFriends(boolean v) {
        this.ignoreFriends = v;
        TpAuraConfig.save();
    }

    public void setIgnoreNamed(boolean v) {
        this.ignoreNamed = v;
        TpAuraConfig.save();
    }

    public void setIgnoreTamed(boolean v) {
        this.ignoreTamed = v;
        TpAuraConfig.save();
    }

    public void setWhitelistEnabled(boolean v) {
        this.whitelistEnabled = v;
        TpAuraConfig.save();
    }

    public void setWhitelist(String v) {
        this.whitelist = v != null ? v : "";
        TpAuraConfig.save();
    }

    public void setHotkeyKey(int key) {
        this.hotkeyKey = key;
        TpAuraConfig.save();
    }

    public void setHotkeyName(String name) {
        this.hotkeyName = name != null ? name : "";
        TpAuraConfig.save();
    }

    public void setRenderPath(boolean v) {
        this.renderPath = v;
        TpAuraConfig.save();
    }

    public void setPathColor(int r, int g, int b, int a) {
        this.pathColorR = r;
        this.pathColorG = g;
        this.pathColorB = b;
        this.pathColorA = a;
        TpAuraConfig.save();
    }

    public void setTargetColor(int r, int g, int b, int a) {
        this.targetColorR = r;
        this.targetColorG = g;
        this.targetColorB = b;
        this.targetColorA = a;
        TpAuraConfig.save();
    }

    public void setTotemBypass(boolean v) {
        this.totemBypass = v;
        TpAuraConfig.save();
    }

    public void setTotemAttacks(int v) {
        this.totemAttacks = Math.max(1, Math.min(3, v));
        TpAuraConfig.save();
    }

    public void setTotemHeightIncrease(int v) {
        this.totemHeightIncrease = Math.max(1, Math.min(100, v));
        TpAuraConfig.save();
    }

    public void setAutoFlight(boolean v) {
        this.autoFlight = v;
        TpAuraConfig.save();
    }

    public void setAutoFlightSpeed(double v) {
        this.autoFlightSpeed = Math.max(0.0, Math.min(2.0, v));
        TpAuraConfig.save();
    }

    public void setAutoFlightHorizontalSpeed(double v) {
        this.autoFlightHorizontalSpeed = Math.max(0.0, Math.min(3.0, v));
        TpAuraConfig.save();
    }
}

