package lexis.Hack.Hacks.Misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lexis.Hack.Hack;
import lexis.Hack.Hackutil.NotificationManager;
import lexis.Hack.Hackutil.Notebot.InstrumentDetectMode;
import lexis.Hack.Hackutil.Notebot.Note;
import lexis.Hack.Hackutil.Notebot.NotebotConfig;
import lexis.Hack.Hackutil.Notebot.NotebotSongsScreen;
import lexis.Hack.Hackutil.Notebot.Song;
import lexis.Hack.Hackutil.Notebot.SongDecoders;
import lexis.Hack.Utils.HeadOnlyLookUtils.HeadOnlyLook;
import lexis.Hack.events.RenderListener;
import lexis.Hack.events.UpdateListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class NotebotHack extends Hack implements UpdateListener, RenderListener {
   private NotebotConfig config;
   private static final String CONFIG_KEY = "音符盒演奏";
   public int tickDelay = 1;
   public int concurrentTuneBlocks = 1;
   public NotebotMode mode;
   public InstrumentDetectMode instrumentDetectMode;
   public boolean polyphonic;
   public boolean autoRotate;
   public boolean autoPlay;
   public boolean roundOutOfRange;
   public boolean swingArm;
   public int checkNoteblocksAgainDelay;
   public double speedMultiplier;
   public boolean renderBoxes;
   public boolean showScannedNoteblocks;
   public int untunedColor;
   public int tunedColor;
   public int tuneHitColor;
   public int scannedColor;
   private CompletableFuture loadingSongFuture;
   private Song currentSong;
   public final Map noteBlockPositions;
   public final Map scannedNoteblocks;
   public final List clickedBlocks;
   private Stage stage;
   private PlayingMode playingMode;
   private boolean isPlaying;
   private int currentTick;
   private int ticks;
   private boolean anyNoteblockTuned;
   private final Map tuneHits;
   private int waitTicks;
   private long lastTickTime;
   private double accumulatedTicks;
   private final Map instrumentMap;

   public NotebotHack() {
      super("音符盒演奏", "自动演奏音符盒", Hack.Category.MISC, true);
      this.mode = NotebotHack.NotebotMode.ExactInstruments;
      this.instrumentDetectMode = InstrumentDetectMode.BlockState;
      this.polyphonic = true;
      this.autoRotate = true;
      this.autoPlay = false;
      this.roundOutOfRange = false;
      this.swingArm = true;
      this.checkNoteblocksAgainDelay = 10;
      this.speedMultiplier = 1.0;
      this.renderBoxes = true;
      this.showScannedNoteblocks = false;
      this.untunedColor = 13369344;
      this.tunedColor = 52224;
      this.tuneHitColor = 16750848;
      this.scannedColor = 16776960;
      this.noteBlockPositions = new HashMap();
      this.scannedNoteblocks = new HashMap();
      this.clickedBlocks = new ArrayList();
      this.stage = NotebotHack.Stage.None;
      this.playingMode = NotebotHack.PlayingMode.None;
      this.isPlaying = false;
      this.currentTick = 0;
      this.ticks = 0;
      this.anyNoteblockTuned = false;
      this.tuneHits = new HashMap();
      this.waitTicks = -1;
      this.lastTickTime = 0L;
      this.accumulatedTicks = 0.0;
      this.instrumentMap = new HashMap();
      this.addSetting(new Hack.Setting("tick延迟", "加载歌曲时的延迟", 1, 1, 20, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("同时调音", "同时调音的音符盒数量", 1, 1, 20, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("模式", "演奏模式", "ExactInstruments", new String[]{"AnyInstrument", "ExactInstruments"}));
      this.addSetting(new Hack.Setting("乐器检测", "乐器检测模式", "BlockState", new String[]{"BlockState", "BelowBlock"}));
      this.addSetting(new Hack.Setting("复音", "允许多个音符同时播放", true));
      this.addSetting(new Hack.Setting("自动旋转", "自动看向音符盒", true));
      this.addSetting(new Hack.Setting("自动播放", "自动随机播放歌曲", false));
      this.addSetting(new Hack.Setting("范围外调整", "调整范围外的音符", false));
      this.addSetting(new Hack.Setting("挥手", "播放时挥手", true));
      this.addSetting(new Hack.Setting("重新检查延迟", "调音后重新检查延迟", 10, 1, 20, Hack.ValueDisplay.INTEGER));
      this.addSetting(new Hack.Setting("播放速度", "歌曲播放速度", 1.0, 0.1, 256.0, Hack.ValueDisplay.DECIMAL));
      this.addSetting(new Hack.Setting("渲染方框", "显示音符盒方框", true));
      this.addSetting(new Hack.Setting("显示扫描", "显示扫描的音符盒", false));
      this.addSetting(new Hack.Setting("扫描音符盒", "扫描周围的已设置的音符盒", "开始扫描", () -> {
         if (mc != null) {
            this.scanForNoteblocks();
            this.sendMessage("§a扫描完成，找到 " + this.scannedNoteblocks.size() + " 个音符盒");
         }

      }));
      this.addSetting(new Hack.Setting("打开歌曲选择", "选择要播放的歌曲", "选择歌曲", () -> {
         if (mc != null) {
            mc.m_91152_(new NotebotSongsScreen(this));
         }

      }));
      this.config = NotebotConfig.getInstance();
      this.loadConfig();
      this.initInstrumentMap();
   }

   private void loadConfig() {
      this.tickDelay = this.config.getIntSetting("音符盒演奏", "tick延迟", 1);
      this.concurrentTuneBlocks = this.config.getIntSetting("音符盒演奏", "同时调音", 1);
      this.mode = NotebotHack.NotebotMode.valueOf(this.config.getStringSetting("音符盒演奏", "模式", "ExactInstruments"));
      this.instrumentDetectMode = InstrumentDetectMode.valueOf(this.config.getStringSetting("音符盒演奏", "乐器检测", "BlockState"));
      this.polyphonic = this.config.getBooleanSetting("音符盒演奏", "复音", true);
      this.autoRotate = this.config.getBooleanSetting("音符盒演奏", "自动旋转", true);
      this.autoPlay = this.config.getBooleanSetting("音符盒演奏", "自动播放", false);
      this.roundOutOfRange = this.config.getBooleanSetting("音符盒演奏", "范围外调整", false);
      this.swingArm = this.config.getBooleanSetting("音符盒演奏", "挥手", true);
      this.checkNoteblocksAgainDelay = this.config.getIntSetting("音符盒演奏", "重新检查延迟", 10);
      this.speedMultiplier = this.config.getDoubleSetting("音符盒演奏", "播放速度", 1.0);
      this.renderBoxes = this.config.getBooleanSetting("音符盒演奏", "渲染方框", true);
      this.showScannedNoteblocks = this.config.getBooleanSetting("音符盒演奏", "显示扫描", false);
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "tick延迟":
               setting.setValue(this.tickDelay);
               break;
            case "同时调音":
               setting.setValue(this.concurrentTuneBlocks);
               break;
            case "模式":
               setting.setValue(this.mode.name());
               break;
            case "乐器检测":
               setting.setValue(this.instrumentDetectMode.name());
               break;
            case "复音":
               setting.setValue(this.polyphonic);
               break;
            case "自动旋转":
               setting.setValue(this.autoRotate);
               break;
            case "自动播放":
               setting.setValue(this.autoPlay);
               break;
            case "范围外调整":
               setting.setValue(this.roundOutOfRange);
               break;
            case "挥手":
               setting.setValue(this.swingArm);
               break;
            case "重新检查延迟":
               setting.setValue(this.checkNoteblocksAgainDelay);
               break;
            case "播放速度":
               setting.setValue(this.speedMultiplier);
               break;
            case "渲染方框":
               setting.setValue(this.renderBoxes);
               break;
            case "显示扫描":
               setting.setValue(this.showScannedNoteblocks);
         }
      }

   }

   private void initInstrumentMap() {
      this.instrumentMap.clear();
      Iterator var1 = this.getSettings().iterator();

      while(true) {
         while(var1.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var1.next();
            NoteBlockInstrument[] var3 = NoteBlockInstrument.values();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               NoteBlockInstrument inst = var3[var5];
               if (this.beautifyText(inst.name()).equals(setting.getName())) {
                  this.instrumentMap.put(inst, NotebotHack.OptionalInstrument.valueOf(setting.getString()));
                  break;
               }
            }
         }

         return;
      }
   }

   public static void renderNotebotProgress(GuiGraphics gui, NotebotHack notebot) {
      if (notebot.isEnabled() && notebot.currentSong != null && notebot.isPlaying) {
         int screenWidth = mc.m_91268_().m_85445_();
         int screenHeight = mc.m_91268_().m_85446_();
         int x = screenWidth / 2 - 100;
         int y = screenHeight - 35;
         gui.m_280509_(x, y, x + 200, y + 5, -2013265920);
         float progress = (float)notebot.currentTick / (float)notebot.currentSong.getLastTick();
         int progressWidth = (int)(200.0F * progress);
         gui.m_280509_(x, y, x + progressWidth, y + 5, -16711936);
         String info = String.format("%s - %d/%d (%.1fx)", notebot.currentSong.getTitle(), notebot.currentTick, notebot.currentSong.getLastTick(), notebot.speedMultiplier);
         gui.m_280488_(mc.f_91062_, info, x, y - 10, 16777215);
      }
   }

   private String beautifyText(String text) {
      text = text.toLowerCase(Locale.ROOT);
      String[] arr = text.split("_");
      StringBuilder sb = new StringBuilder();
      String[] var4 = arr;
      int var5 = arr.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String s = var4[var6];
         sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1).toLowerCase());
      }

      return sb.toString();
   }

   public void onEnable() {
      this.ticks = 0;
      this.lastTickTime = System.currentTimeMillis();
      this.accumulatedTicks = 0.0;
      this.resetVariables();
   }

   public void onDisable() {
      if (this.autoRotate && HeadOnlyLook.isLooking()) {
         HeadOnlyLook.stopLooking();
      }

      this.resetVariables();
   }

   private void resetVariables() {
      if (this.loadingSongFuture != null) {
         this.loadingSongFuture.cancel(true);
         this.loadingSongFuture = null;
      }

      this.clickedBlocks.clear();
      this.tuneHits.clear();
      this.anyNoteblockTuned = false;
      this.currentTick = 0;
      this.playingMode = NotebotHack.PlayingMode.None;
      this.isPlaying = false;
      this.stage = NotebotHack.Stage.None;
      this.currentSong = null;
      this.noteBlockPositions.clear();
      this.scannedNoteblocks.clear();
      this.accumulatedTicks = 0.0;
   }

   public void onUpdate() {
      Iterator var1 = this.getSettings().iterator();

      while(var1.hasNext()) {
         Hack.Setting setting = (Hack.Setting)var1.next();
         switch (setting.getName()) {
            case "tick延迟":
               this.tickDelay = setting.getInt();
               break;
            case "同时调音":
               this.concurrentTuneBlocks = setting.getInt();
               break;
            case "模式":
               this.mode = NotebotHack.NotebotMode.valueOf(setting.getString());
               break;
            case "乐器检测":
               this.instrumentDetectMode = InstrumentDetectMode.valueOf(setting.getString());
               break;
            case "复音":
               this.polyphonic = setting.getBoolean();
               break;
            case "自动旋转":
               this.autoRotate = setting.getBoolean();
               break;
            case "自动播放":
               this.autoPlay = setting.getBoolean();
               break;
            case "范围外调整":
               this.roundOutOfRange = setting.getBoolean();
               break;
            case "挥手":
               this.swingArm = setting.getBoolean();
               break;
            case "重新检查延迟":
               this.checkNoteblocksAgainDelay = setting.getInt();
               break;
            case "播放速度":
               this.speedMultiplier = setting.getDouble();
               break;
            case "渲染方框":
               this.renderBoxes = setting.getBoolean();
               break;
            case "显示扫描":
               this.showScannedNoteblocks = setting.getBoolean();
         }
      }

      this.initInstrumentMap();
      ++this.ticks;
      this.clickedBlocks.clear();
      if (this.stage == NotebotHack.Stage.WaitingToCheckNoteblocks) {
         --this.waitTicks;
         if (this.waitTicks == 0) {
            this.waitTicks = -1;
            this.sendMessage("重新检查音符盒");
            this.setupTuneHitsMap();
            this.stage = NotebotHack.Stage.Tune;
         }
      } else if (this.stage == NotebotHack.Stage.SetUp) {
         this.scanForNoteblocks();
         if (this.scannedNoteblocks.isEmpty()) {
            this.sendMessage("§c找不到附近的音符盒！");
            this.stop();
            return;
         }

         this.setupNoteblocksMap();
         if (this.noteBlockPositions.isEmpty()) {
            this.sendMessage("§c找不到可用的音符盒来演奏歌曲！");
            this.stop();
            return;
         }

         this.setupTuneHitsMap();
         if (this.tuneHits.isEmpty()) {
            this.sendMessage("§a所有音符盒已经调好了！");
            this.stage = NotebotHack.Stage.Playing;
            this.play();
         } else {
            this.sendMessage("§e需要调音的音符盒: " + this.tuneHits.size() + " 个");
            this.stage = NotebotHack.Stage.Tune;
         }

         this.setupTuneHitsMap();
         this.stage = NotebotHack.Stage.Tune;
      } else if (this.stage == NotebotHack.Stage.Tune) {
         this.tune();
      } else if (this.stage == NotebotHack.Stage.Playing) {
         if (!this.isPlaying) {
            return;
         }

         if (mc.f_91074_ == null || this.currentTick > this.currentSong.getLastTick()) {
            this.onSongEnd();
            return;
         }

         long currentTime = System.currentTimeMillis();
         if (this.lastTickTime == 0L) {
            this.lastTickTime = currentTime;
         }

         long delta = currentTime - this.lastTickTime;
         double tickDelta = (double)delta / 50.0 * this.speedMultiplier;

         for(this.accumulatedTicks += tickDelta; this.accumulatedTicks >= 1.0 && this.currentTick <= this.currentSong.getLastTick(); --this.accumulatedTicks) {
            if (this.currentSong.getNotesMap().containsKey(this.currentTick)) {
               if (this.playingMode == NotebotHack.PlayingMode.Preview) {
                  this.onTickPreview();
               } else {
                  this.onTickPlay();
               }
            }

            ++this.currentTick;
         }

         this.lastTickTime = currentTime;
      }

   }

   public void onRender(PoseStack poseStack, float partialTicks) {
      if (this.isEnabled() && mc.f_91074_ != null && mc.f_91073_ != null) {
         if (this.stage == NotebotHack.Stage.SetUp || this.stage == NotebotHack.Stage.Tune || this.stage == NotebotHack.Stage.WaitingToCheckNoteblocks || this.isPlaying) {
            Vec3 cameraPos = mc.f_91063_.m_109153_().m_90583_();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(GameRenderer::m_172811_);
            RenderSystem.depthMask(false);
            RenderSystem.lineWidth(2.0F);
            poseStack.m_85836_();
            poseStack.m_85837_(-cameraPos.f_82479_, -cameraPos.f_82480_, -cameraPos.f_82481_);
            if (this.renderBoxes) {
               this.renderBoxes(poseStack);
            }

            poseStack.m_85849_();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.lineWidth(1.0F);
         }
      }
   }

   private void renderBoxes(PoseStack poseStack) {
      Tesselator tesselator = Tesselator.m_85913_();
      BufferBuilder buffer = tesselator.m_85915_();
      Matrix4f matrix = poseStack.m_85850_().m_252922_();
      buffer.m_166779_(Mode.QUADS, DefaultVertexFormat.f_85815_);
      Iterator var5;
      if (this.showScannedNoteblocks) {
         var5 = this.scannedNoteblocks.values().iterator();

         while(var5.hasNext()) {
            List positions = (List)var5.next();
            Iterator var7 = positions.iterator();

            while(var7.hasNext()) {
               BlockPos pos = (BlockPos)var7.next();
               this.renderBlockBox(buffer, matrix, pos, this.scannedColor, 0.3F);
            }
         }
      } else {
         var5 = this.noteBlockPositions.entrySet().iterator();

         while(var5.hasNext()) {
            Map.Entry entry = (Map.Entry)var5.next();
            BlockPos pos = (BlockPos)entry.getValue();
            BlockState state = mc.f_91073_.m_8055_(pos);
            if (state.m_60734_() == Blocks.f_50065_) {
               int level = (Integer)state.m_61143_(NoteBlock.f_55013_);
               int targetLevel = ((Note)entry.getKey()).getNoteLevel();
               int color;
               if (this.clickedBlocks.contains(pos)) {
                  color = this.tuneHitColor;
               } else if (level == targetLevel) {
                  color = this.tunedColor;
               } else {
                  color = this.untunedColor;
               }

               this.renderBlockBox(buffer, matrix, pos, color, 0.3F);
            }
         }
      }

      tesselator.m_85914_();
   }

   private void renderBlockBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, int color, float alpha) {
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      float minX = (float)pos.m_123341_();
      float minY = (float)pos.m_123342_();
      float minZ = (float)pos.m_123343_();
      float maxX = (float)(pos.m_123341_() + 1);
      float maxY = (float)(pos.m_123342_() + 1);
      float maxZ = (float)(pos.m_123343_() + 1);
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, minY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, minZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, minY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, maxX, maxY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, minY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
      buffer.m_252986_(matrix, minX, maxY, maxZ).m_85950_(r, g, b, alpha).m_5752_();
   }

   public void scanForNoteblocks() {
      if (mc.f_91072_ != null && mc.f_91073_ != null && mc.f_91074_ != null) {
         this.scannedNoteblocks.clear();
         int range = 5;
         BlockPos center = mc.f_91074_.m_20183_();

         for(int x = -range; x <= range; ++x) {
            for(int y = -range; y <= range; ++y) {
               for(int z = -range; z <= range; ++z) {
                  BlockPos pos = center.m_7918_(x, y, z);
                  BlockState state = mc.f_91073_.m_8055_(pos);
                  if (state.m_60734_() == Blocks.f_50065_) {
                     double distance = Math.sqrt(pos.m_123331_(center));
                     if (!(distance > 5.0) && this.isValidScanSpot(pos)) {
                        Note note = this.getNoteFromBlock(state, pos);
                        ((List)this.scannedNoteblocks.computeIfAbsent(note, (k) -> {
                           return new ArrayList();
                        })).add(pos.m_7949_());
                     }
                  }
               }
            }
         }

      }
   }

   private Note getNoteFromBlock(BlockState state, BlockPos pos) {
      int level = (Integer)state.m_61143_(NoteBlock.f_55013_);
      NoteBlockInstrument instrument = null;
      if (this.mode == NotebotHack.NotebotMode.ExactInstruments) {
         instrument = this.instrumentDetectMode.getFunction().detectInstrument(state, pos);
      }

      return new Note(instrument, level);
   }

   private boolean isValidScanSpot(BlockPos pos) {
      return mc.f_91073_ == null ? false : mc.f_91073_.m_8055_(pos.m_7494_()).m_60795_();
   }

   private void setupNoteblocksMap() {
      this.noteBlockPositions.clear();
      List uniqueNotes = new ArrayList(this.currentSong.getRequirements());
      Map incorrectNotes = new HashMap();
      Iterator var3 = this.scannedNoteblocks.entrySet().iterator();

      Map.Entry entry;
      while(var3.hasNext()) {
         entry = (Map.Entry)var3.next();
         Note note = (Note)entry.getKey();
         List positions = new ArrayList((Collection)entry.getValue());
         if (uniqueNotes.contains(note)) {
            this.noteBlockPositions.put(note, (BlockPos)positions.remove(0));
            uniqueNotes.remove(note);
         }

         if (!positions.isEmpty()) {
            NoteBlockInstrument inst = note.getInstrument();
            if (inst != null) {
               ((List)incorrectNotes.computeIfAbsent(inst, (k) -> {
                  return new ArrayList();
               })).addAll(positions);
            }
         }
      }

      var3 = incorrectNotes.entrySet().iterator();

      while(true) {
         while(var3.hasNext()) {
            entry = (Map.Entry)var3.next();
            List positions = (List)entry.getValue();
            if (this.mode == NotebotHack.NotebotMode.ExactInstruments) {
               List foundNotes = (List)uniqueNotes.stream().filter((n) -> {
                  return n.getInstrument() == entry.getKey();
               }).collect(Collectors.toList());
               Iterator var14 = positions.iterator();

               while(var14.hasNext()) {
                  BlockPos pos = (BlockPos)var14.next();
                  if (foundNotes.isEmpty()) {
                     break;
                  }

                  Note note = (Note)foundNotes.remove(0);
                  this.noteBlockPositions.put(note, pos);
                  uniqueNotes.remove(note);
               }
            } else {
               Iterator var11 = positions.iterator();

               while(var11.hasNext()) {
                  BlockPos pos = (BlockPos)var11.next();
                  if (uniqueNotes.isEmpty()) {
                     break;
                  }

                  Note note = (Note)uniqueNotes.remove(0);
                  this.noteBlockPositions.put(note, pos);
               }
            }
         }

         if (!uniqueNotes.isEmpty()) {
            this.sendMessage("§e少了 " + uniqueNotes.size() + " 个音符！");
         }

         return;
      }
   }

   private void setupTuneHitsMap() {
      this.tuneHits.clear();
      if (mc.f_91073_ != null) {
         Iterator var1 = this.noteBlockPositions.entrySet().iterator();

         while(var1.hasNext()) {
            Map.Entry entry = (Map.Entry)var1.next();
            int targetLevel = ((Note)entry.getKey()).getNoteLevel();
            BlockPos pos = (BlockPos)entry.getValue();
            BlockState state = mc.f_91073_.m_8055_(pos);
            if (state.m_60734_() == Blocks.f_50065_) {
               int currentLevel = (Integer)state.m_61143_(NoteBlock.f_55013_);
               if (targetLevel != currentLevel) {
                  this.tuneHits.put(pos, this.calcHits(currentLevel, targetLevel));
               }
            }
         }

      }
   }

   private int calcHits(int from, int to) {
      return from > to ? 25 - from + to : to - from;
   }

   private void tune() {
      if (this.tuneHits.isEmpty()) {
         if (this.anyNoteblockTuned) {
            this.anyNoteblockTuned = false;
            this.waitTicks = this.checkNoteblocksAgainDelay;
            this.stage = NotebotHack.Stage.WaitingToCheckNoteblocks;
            this.sendMessage("等重新检查音符盒");
         } else {
            this.stage = NotebotHack.Stage.Playing;
            this.sendMessage("§a设置完成！");
            this.play();
         }

      } else if (this.ticks >= this.tickDelay) {
         this.tuneBlocks();
         this.ticks = 0;
      }
   }

   private void tuneBlocks() {
      if (mc.f_91073_ != null && mc.f_91074_ != null) {
         if (this.swingArm) {
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         }

         int iterations = 0;
         Iterator iterator = this.tuneHits.entrySet().iterator();

         do {
            if (!iterator.hasNext()) {
               return;
            }

            Map.Entry entry = (Map.Entry)iterator.next();
            BlockPos pos = (BlockPos)entry.getKey();
            int hitsLeft = (Integer)entry.getValue();
            this.tuneNoteblock(pos);
            this.clickedBlocks.add(pos);
            --hitsLeft;
            entry.setValue(hitsLeft);
            if (hitsLeft == 0) {
               iterator.remove();
            }

            ++iterations;
         } while(iterations != this.concurrentTuneBlocks);

      } else {
         this.stop();
      }
   }

   private void tuneNoteblock(BlockPos pos) {
      if (mc.f_91072_ != null && mc.f_91074_ != null) {
         BlockHitResult hitResult = new BlockHitResult(Vec3.m_82512_(pos), Direction.DOWN, pos, false);
         mc.f_91072_.m_233732_(mc.f_91074_, InteractionHand.MAIN_HAND, hitResult);
         this.anyNoteblockTuned = true;
      }
   }

   private void onTickPreview() {
      if (mc.f_91074_ != null) {
         List notes = (List)this.currentSong.getNotesMap().get(this.currentTick);
         if (notes != null) {
            Iterator var2 = notes.iterator();

            while(var2.hasNext()) {
               Note note = (Note)var2.next();
               float pitch = (float)Math.pow(2.0, (double)(note.getNoteLevel() - 12) / 12.0);
               mc.f_91074_.m_5496_((SoundEvent)SoundEvents.f_12214_.m_203334_(), 1.0F, pitch);
            }
         }

      }
   }

   private void onTickPlay() {
      List notes = (List)this.currentSong.getNotesMap().get(this.currentTick);
      if (notes != null && !notes.isEmpty()) {
         if (this.autoRotate && !notes.isEmpty()) {
            BlockPos firstPos = (BlockPos)this.noteBlockPositions.get(notes.get(0));
            if (firstPos != null) {
               HeadOnlyLook.startLookingAt(firstPos, 500L);
            }
         }

         if (this.swingArm) {
            mc.f_91074_.m_6674_(InteractionHand.MAIN_HAND);
         }

         Iterator var5 = notes.iterator();

         while(var5.hasNext()) {
            Note note = (Note)var5.next();
            BlockPos pos = (BlockPos)this.noteBlockPositions.get(note);
            if (pos != null) {
               this.playNote(pos);
               if (!this.polyphonic) {
                  break;
               }
            }
         }

      }
   }

   private void playNote(BlockPos pos) {
      if (mc.m_91403_() != null) {
         mc.m_91403_().m_104955_(new ServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, pos, Direction.DOWN));
      }
   }

   public NoteBlockInstrument getMappedInstrument(NoteBlockInstrument original) {
      if (this.mode == NotebotHack.NotebotMode.ExactInstruments) {
         OptionalInstrument opt = (OptionalInstrument)this.instrumentMap.get(original);
         if (opt != null && opt.toMinecraft() != null) {
            return opt.toMinecraft();
         }
      }

      return original;
   }

   public void loadSong(File file) {
      if (!this.isEnabled()) {
         this.setEnabled(true);
      }

      this.resetVariables();
      this.playingMode = NotebotHack.PlayingMode.Noteblocks;
      if (!this.loadFileToMap(file, () -> {
         this.stage = NotebotHack.Stage.SetUp;
         this.sendMessage("§a开始自动调音中");
      })) {
         this.onSongEnd();
      }
   }

   public void previewSong(File file) {
      if (!this.isEnabled()) {
         this.setEnabled(true);
      }

      this.resetVariables();
      this.playingMode = NotebotHack.PlayingMode.Preview;
      this.loadFileToMap(file, () -> {
         this.stage = NotebotHack.Stage.Playing;
         this.play();
      });
   }

   private boolean loadFileToMap(File file, Runnable callback) {
      if (file.exists() && file.isFile()) {
         if (!SongDecoders.hasDecoder(file)) {
            this.sendMessage("§c不支持的文件格式！");
            return false;
         } else {
            this.sendMessage("§f加载歌曲: §e" + file.getName());
            this.loadingSongFuture = CompletableFuture.supplyAsync(() -> {
               try {
                  return SongDecoders.parse(file, this);
               } catch (Exception var3) {
                  throw new RuntimeException(var3);
               }
            });
            this.loadingSongFuture.completeOnTimeout((Object)null, 60L, TimeUnit.SECONDS);
            this.stage = NotebotHack.Stage.LoadingSong;
            this.loadingSongFuture.whenComplete((song, ex) -> {
               if (ex == null) {
                  if (song == null) {
                     this.sendMessage("§c加载歌曲超时！");
                     this.onSongEnd();
                     return;
                  }

                  this.currentSong = song;
                  this.sendMessage("§a歌曲加载成功！");
                  callback.run();
               } else {
                  this.sendMessage("§c加载歌曲失败！");
                  this.onSongEnd();
               }

            });
            return true;
         }
      } else {
         this.sendMessage("§c文件不存在！");
         return false;
      }
   }

   public void play() {
      if (mc.f_91074_ != null) {
         if (mc.f_91074_.m_150110_().f_35937_ && this.playingMode != NotebotHack.PlayingMode.Preview) {
            this.sendMessage("§c你需要是生存模式！");
         } else if (this.stage == NotebotHack.Stage.Playing) {
            this.isPlaying = true;
            this.lastTickTime = System.currentTimeMillis();
            this.accumulatedTicks = 0.0;
            this.sendMessage("§a开始播放，当前速度: " + this.speedMultiplier + "x");
         } else {
            this.sendMessage("§c没有加载歌曲");
         }

      }
   }

   public void stop() {
      this.sendMessage("§e音乐播放完成！，停止播放了");
      this.resetVariables();
   }

   public void onSongEnd() {
      if (this.autoPlay && this.playingMode != NotebotHack.PlayingMode.Preview) {
         this.playRandomSong();
      } else {
         this.stop();
      }

   }

   public void playRandomSong() {
      File folder = new File("C:/karucn/Lexis/config/hack/Notebot/");
      File[] files = folder.listFiles();
      if (files != null) {
         List validFiles = new ArrayList();
         File[] var4 = files;
         int var5 = files.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            File f = var4[var6];
            if (SongDecoders.hasDecoder(f)) {
               validFiles.add(f);
            }
         }

         if (validFiles.isEmpty()) {
            this.sendMessage("§c没有找到可播放的歌曲！");
         } else {
            File randomSong = (File)validFiles.get((new Random()).nextInt(validFiles.size()));
            this.loadSong(randomSong);
         }
      }
   }

   public void sendMessage(String msg) {
      NotificationManager.info("音符盒演奏：", msg, 6);
   }

   public void onClick() {
      this.toggle();
   }

   public static enum NotebotMode {
      AnyInstrument,
      ExactInstruments;

      // $FF: synthetic method
      private static NotebotMode[] $values() {
         return new NotebotMode[]{AnyInstrument, ExactInstruments};
      }
   }

   public static enum Stage {
      None,
      LoadingSong,
      SetUp,
      Tune,
      WaitingToCheckNoteblocks,
      Playing;

      // $FF: synthetic method
      private static Stage[] $values() {
         return new Stage[]{None, LoadingSong, SetUp, Tune, WaitingToCheckNoteblocks, Playing};
      }
   }

   public static enum PlayingMode {
      None,
      Preview,
      Noteblocks;

      // $FF: synthetic method
      private static PlayingMode[] $values() {
         return new PlayingMode[]{None, Preview, Noteblocks};
      }
   }

   public static enum OptionalInstrument {
      None((NoteBlockInstrument)null),
      Harp(NoteBlockInstrument.HARP),
      Basedrum(NoteBlockInstrument.BASEDRUM),
      Snare(NoteBlockInstrument.SNARE),
      Hat(NoteBlockInstrument.HAT),
      Bass(NoteBlockInstrument.BASS),
      Flute(NoteBlockInstrument.FLUTE),
      Bell(NoteBlockInstrument.BELL),
      Guitar(NoteBlockInstrument.GUITAR),
      Chime(NoteBlockInstrument.CHIME),
      Xylophone(NoteBlockInstrument.XYLOPHONE),
      IronXylophone(NoteBlockInstrument.IRON_XYLOPHONE),
      CowBell(NoteBlockInstrument.COW_BELL),
      Didgeridoo(NoteBlockInstrument.DIDGERIDOO),
      Bit(NoteBlockInstrument.BIT),
      Banjo(NoteBlockInstrument.BANJO),
      Pling(NoteBlockInstrument.PLING);

      private final NoteBlockInstrument minecraftInstrument;
      private static final Map BY_MINECRAFT = new HashMap();

      private OptionalInstrument(NoteBlockInstrument inst) {
         this.minecraftInstrument = inst;
      }

      public NoteBlockInstrument toMinecraft() {
         return this.minecraftInstrument;
      }

      public static OptionalInstrument fromMinecraftInstrument(NoteBlockInstrument inst) {
         return (OptionalInstrument)BY_MINECRAFT.get(inst);
      }

      // $FF: synthetic method
      private static OptionalInstrument[] $values() {
         return new OptionalInstrument[]{None, Harp, Basedrum, Snare, Hat, Bass, Flute, Bell, Guitar, Chime, Xylophone, IronXylophone, CowBell, Didgeridoo, Bit, Banjo, Pling};
      }

      static {
         OptionalInstrument[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            OptionalInstrument opt = var0[var2];
            if (opt.minecraftInstrument != null) {
               BY_MINECRAFT.put(opt.minecraftInstrument, opt);
            }
         }

      }
   }
}
