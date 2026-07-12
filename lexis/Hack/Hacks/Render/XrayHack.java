package lexis.Hack.Hacks.Render;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.settings.BlockListSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class XrayHack extends Hack {
   public static boolean enabled = false;
   private static final Set VISIBLE = Collections.newSetFromMap(new IdentityHashMap());
   private BlockListSetting blockListSetting = new BlockListSetting("方块列表", "透视显示的方块", new String[0]);
   private static final String[] DEFAULT_IDS = new String[]{"minecraft:coal_ore", "minecraft:deepslate_coal_ore", "minecraft:iron_ore", "minecraft:deepslate_iron_ore", "minecraft:copper_ore", "minecraft:deepslate_copper_ore", "minecraft:gold_ore", "minecraft:deepslate_gold_ore", "minecraft:nether_gold_ore", "minecraft:redstone_ore", "minecraft:deepslate_redstone_ore", "minecraft:lapis_ore", "minecraft:deepslate_lapis_ore", "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore", "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore", "minecraft:nether_quartz_ore", "minecraft:ancient_debris", "minecraft:spawner", "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel", "minecraft:shulker_box", "minecraft:amethyst_block", "minecraft:budding_amethyst", "minecraft:water", "minecraft:lava", "minecraft:obsidian", "minecraft:crying_obsidian", "minecraft:bedrock", "minecraft:end_portal", "minecraft:end_portal_frame", "minecraft:beacon", "minecraft:conduit"};

   public XrayHack() {
      super("X-ray", new String[]{"Xray！X光！透视方块", "右键打开设置选择处理方块", "§4§l源码来自：Wurst1.12.2 Forge - Xray"}, Hack.Category.RENDER, true);
      this.addSetting(this.blockListSetting);
      this.loadBlockList();
   }

   private void loadBlockList() {
      HackConfig config = HackConfig.getInstance();
      List savedList = (List)config.getHackSettings(this.getName()).get("方块列表");
      if (savedList != null && !savedList.isEmpty()) {
         this.blockListSetting.setValue(savedList);
      } else {
         List defaultList = Arrays.asList(DEFAULT_IDS);
         this.blockListSetting.setValue(defaultList);
         this.autoSave();
      }

      this.syncVisibleFromSetting();
   }

   public void onEnable() {
      enabled = true;
      this.syncVisibleFromSetting();
      reloadChunks();
   }

   public void onDisable() {
      enabled = false;
      reloadChunks();
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }

   private void syncVisibleFromSetting() {
      VISIBLE.clear();
      Iterator var1 = this.blockListSetting.getBlockNames().iterator();

      while(var1.hasNext()) {
         String id = (String)var1.next();
         Block b = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
         if (b != null && b != Blocks.f_50016_) {
            VISIBLE.add(b);
         }
      }

   }

   public static boolean isVisible(Block b) {
      return VISIBLE.contains(b);
   }

   private static void reloadChunks() {
      Minecraft mc = Minecraft.m_91087_();
      if (mc.f_91060_ != null) {
         mc.f_91060_.m_109818_();
      }

   }
}
