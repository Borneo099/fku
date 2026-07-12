package lexis.Hack.Hackutil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lexis.Hack.Hack;
import lexis.Hack.Hacks.Baritone.BaritoneParkourHack;
import lexis.Hack.Hacks.Baritone.BaritoneSpeedHack;
import lexis.Hack.Hacks.Baritone.BaritoneThirdPersonLookHack;
import lexis.Hack.Hacks.Baritone.ElytraAnywhereHack;
import lexis.Hack.Hacks.Baritone.StructureLocatorHack;
import lexis.Hack.Hacks.Baritone.XrayExposedAutoMineHack;
import lexis.Hack.Hacks.Blocks.AirPlaceHack;
import lexis.Hack.Hacks.Blocks.AntiCactusHack;
import lexis.Hack.Hacks.Blocks.AutoFarmHack;
import lexis.Hack.Hacks.Blocks.AutoFixGroundHack;
import lexis.Hack.Hacks.Blocks.AutoSignHack;
import lexis.Hack.Hacks.Blocks.AutoToolHack;
import lexis.Hack.Hacks.Blocks.FastBreakHack;
import lexis.Hack.Hacks.Blocks.InstantRebreakHack;
import lexis.Hack.Hacks.Blocks.KaboomHack;
import lexis.Hack.Hacks.Blocks.ScaffoldWalkHack;
import lexis.Hack.Hacks.Chat.AntiSpamHack;
import lexis.Hack.Hacks.Chat.AsciiArtPrinterHack;
import lexis.Hack.Hacks.Chat.BetterChatHack;
import lexis.Hack.Hacks.Chat.ChatAIHack;
import lexis.Hack.Hacks.Chat.ChatHistoryHack;
import lexis.Hack.Hacks.Chat.ChatKeepOpenHack;
import lexis.Hack.Hacks.Chat.ChatNoTrimHack;
import lexis.Hack.Hacks.Chat.ChatRetainHack;
import lexis.Hack.Hacks.Chat.ChatSpamHack;
import lexis.Hack.Hacks.Chat.FancyChatHack;
import lexis.Hack.Hacks.Chat.InfiniChatHack;
import lexis.Hack.Hacks.Chat.NotifierHack;
import lexis.Hack.Hacks.Chat.PlayerNotifierHack;
import lexis.Hack.Hacks.Combat.AnchorAuraHack;
import lexis.Hack.Hacks.Combat.ArrowDmgHack;
import lexis.Hack.Hacks.Combat.ArrowDmgsHack;
import lexis.Hack.Hacks.Combat.AutoArmorHack;
import lexis.Hack.Hacks.Combat.AutoAttackHack;
import lexis.Hack.Hacks.Combat.AutoCriticalsHack;
import lexis.Hack.Hacks.Combat.AutoRevengeHack;
import lexis.Hack.Hacks.Combat.AutoTotemHack;
import lexis.Hack.Hacks.Combat.BowAimbotHack;
import lexis.Hack.Hacks.Combat.BowSpamHack;
import lexis.Hack.Hacks.Combat.CrystalAuraHack;
import lexis.Hack.Hacks.Combat.FreezePlayerHack;
import lexis.Hack.Hacks.Combat.KillauraHack;
import lexis.Hack.Hacks.Combat.MultiAuraHack;
import lexis.Hack.Hacks.Combat.TpAuraHack;
import lexis.Hack.Hacks.Combat.TpAurasHack;
import lexis.Hack.Hacks.Fun.DerpHack;
import lexis.Hack.Hacks.Fun.FakeContainerOpenHack;
import lexis.Hack.Hacks.Fun.HelicopterElytraHack;
import lexis.Hack.Hacks.Fun.ImitatePlayerHack;
import lexis.Hack.Hacks.Fun.MileyCyrusHack;
import lexis.Hack.Hacks.Fun.SkinDerpHack;
import lexis.Hack.Hacks.Fun.SpinHack;
import lexis.Hack.Hacks.Fun.StareAtPlayerHack;
import lexis.Hack.Hacks.Fun.SuperThrowHack;
import lexis.Hack.Hacks.Fun.UpsideDownHack;
import lexis.Hack.Hacks.Items.ArmorStandPrinterHack;
import lexis.Hack.Hacks.Items.AutoDropHack;
import lexis.Hack.Hacks.Items.CrashChestHack;
import lexis.Hack.Hacks.Items.CrashTextHack;
import lexis.Hack.Hacks.Items.FlagDisplayHack;
import lexis.Hack.Hacks.Items.FumiGeneratorLoopHack;
import lexis.Hack.Hacks.Items.HeadlessPistonHack;
import lexis.Hack.Hacks.Items.RetainCraftingHack;
import lexis.Hack.Hacks.Items.TridentDupeHack;
import lexis.Hack.Hacks.L_Enders_Cataclysm_C.CataclysmLocatorHack;
import lexis.Hack.Hacks.L_Enders_Cataclysm_C.NoScreenShakeHack;
import lexis.Hack.Hacks.Lexis.BetterVanillaGlowHack;
import lexis.Hack.Hacks.Lexis.BindsDisplayHack;
import lexis.Hack.Hacks.Lexis.GuiKeyBindHack;
import lexis.Hack.Hacks.Lexis.HUDSettingsHack;
import lexis.Hack.Hacks.Lexis.LexisLogoHack;
import lexis.Hack.Hacks.Lexis.NotificationHack;
import lexis.Hack.Hacks.Lexis.TabGuiHack;
import lexis.Hack.Hacks.Misc.AntiAfkHack;
import lexis.Hack.Hacks.Misc.AntiPacketKickHack;
import lexis.Hack.Hacks.Misc.ContainerCrashHack;
import lexis.Hack.Hacks.Misc.DamageHack;
import lexis.Hack.Hacks.Misc.GhostHack;
import lexis.Hack.Hacks.Misc.IpHack;
import lexis.Hack.Hacks.Misc.NeteaseMusicHack;
import lexis.Hack.Hacks.Misc.NoServerRotateHack;
import lexis.Hack.Hacks.Misc.NotebotHack;
import lexis.Hack.Hacks.Misc.PacketCancellerHack;
import lexis.Hack.Hacks.Misc.PacketLoggerHack;
import lexis.Hack.Hacks.Misc.ThrowHack;
import lexis.Hack.Hacks.Movement.AirJumpHack;
import lexis.Hack.Hacks.Movement.AntiExplosionHack;
import lexis.Hack.Hacks.Movement.AntiHungerHack;
import lexis.Hack.Hacks.Movement.AntiKnockbackHack;
import lexis.Hack.Hacks.Movement.AntiPushHack;
import lexis.Hack.Hacks.Movement.AntiVoidHack;
import lexis.Hack.Hacks.Movement.AutoSprintHack;
import lexis.Hack.Hacks.Movement.AutoSwimHack;
import lexis.Hack.Hacks.Movement.BoatFrictionHack;
import lexis.Hack.Hacks.Movement.BouncyHack;
import lexis.Hack.Hacks.Movement.CreativeFlightHack;
import lexis.Hack.Hacks.Movement.ElytraFlysHack;
import lexis.Hack.Hacks.Movement.ExtraElytraHack;
import lexis.Hack.Hacks.Movement.FastLadderHack;
import lexis.Hack.Hacks.Movement.FireworkElytraFlyHack;
import lexis.Hack.Hacks.Movement.FlightHack;
import lexis.Hack.Hacks.Movement.GUIMoveHack;
import lexis.Hack.Hacks.Movement.HighJumpHack;
import lexis.Hack.Hacks.Movement.JesusHack;
import lexis.Hack.Hacks.Movement.NoFallHack;
import lexis.Hack.Hacks.Movement.NoFallsHack;
import lexis.Hack.Hacks.Movement.NoJumpDelayHack;
import lexis.Hack.Hacks.Movement.NoLevitationHack;
import lexis.Hack.Hacks.Movement.NoMomentumHack;
import lexis.Hack.Hacks.Movement.NoSlowdownHack;
import lexis.Hack.Hacks.Movement.NoSneakSlowHack;
import lexis.Hack.Hacks.Movement.NoWebHack;
import lexis.Hack.Hacks.Movement.PacketFlyHack;
import lexis.Hack.Hacks.Movement.SlippyHack;
import lexis.Hack.Hacks.Movement.SnowShoeHack;
import lexis.Hack.Hacks.Movement.SpeedHack;
import lexis.Hack.Hacks.Movement.SpiderHack;
import lexis.Hack.Hacks.Movement.SpringJumpHack;
import lexis.Hack.Hacks.Movement.StepHack;
import lexis.Hack.Hacks.Protect.EntityHiderHack;
import lexis.Hack.Hacks.Protect.EntityNameLimiterHack;
import lexis.Hack.Hacks.Protect.ParticleProtectHack;
import lexis.Hack.Hacks.Protect.PreventGameCloseHack;
import lexis.Hack.Hacks.Render.AimEntityGlowHack;
import lexis.Hack.Hacks.Render.AntiBlindHack;
import lexis.Hack.Hacks.Render.ArmorOverlayHack;
import lexis.Hack.Hacks.Render.BlinkHack;
import lexis.Hack.Hacks.Render.BlockAnimationHack;
import lexis.Hack.Hacks.Render.BlockEspHack;
import lexis.Hack.Hacks.Render.BlockSelectionHack;
import lexis.Hack.Hacks.Render.BreakIndicatorHack;
import lexis.Hack.Hacks.Render.ButtonBeautifyHack;
import lexis.Hack.Hacks.Render.CameraDistanceHack;
import lexis.Hack.Hacks.Render.CameraSmoothHack;
import lexis.Hack.Hacks.Render.CapeHack;
import lexis.Hack.Hacks.Render.ChromaticAberrationHack;
import lexis.Hack.Hacks.Render.ColorCodeHack;
import lexis.Hack.Hacks.Render.EntityEspHack;
import lexis.Hack.Hacks.Render.EntityOwnerHack;
import lexis.Hack.Hacks.Render.EntitySearcherHack;
import lexis.Hack.Hacks.Render.FreeCamHack;
import lexis.Hack.Hacks.Render.FullBrightHack;
import lexis.Hack.Hacks.Render.GodViewHack;
import lexis.Hack.Hacks.Render.GuiAnimationHack;
import lexis.Hack.Hacks.Render.ItemOutlineHack;
import lexis.Hack.Hacks.Render.MatrixOverlayHack;
import lexis.Hack.Hacks.Render.MotionBlurHack;
import lexis.Hack.Hacks.Render.NametagsHack;
import lexis.Hack.Hacks.Render.NoBackgroundHack;
import lexis.Hack.Hacks.Render.NoFireOverlayHack;
import lexis.Hack.Hacks.Render.NoHurtcamHack;
import lexis.Hack.Hacks.Render.NoPumpkinHack;
import lexis.Hack.Hacks.Render.NoRenderHack;
import lexis.Hack.Hacks.Render.NoSprintFovHack;
import lexis.Hack.Hacks.Render.OldWeaponHack;
import lexis.Hack.Hacks.Render.PlayerEspHack;
import lexis.Hack.Hacks.Render.PortalEspHack;
import lexis.Hack.Hacks.Render.PortalGuiHack;
import lexis.Hack.Hacks.Render.SelfNametagHack;
import lexis.Hack.Hacks.Render.StarSkyHack;
import lexis.Hack.Hacks.Render.TimeChangerHack;
import lexis.Hack.Hacks.Render.TrueSightHack;
import lexis.Hack.Hacks.Render.VirtualShaderHack;
import lexis.Hack.Hacks.Render.VoidEspHack;
import lexis.Hack.Hacks.Render.WallHack;
import lexis.Hack.Hacks.Render.XrayExposedHack;
import lexis.Hack.Hacks.Render.XrayHack;
import lexis.Hack.Hacks.TaCZ.AimbotHack;
import lexis.Hack.Hacks.TaCZ.AutoReloadHack;
import lexis.Hack.Hacks.TaCZ.BulletTracersHack;
import lexis.Hack.Hacks.TaCZ.EndlessAimbotHack;
import lexis.Hack.Hacks.TaCZ.InstantAimHack;
import lexis.Hack.Hacks.TaCZ.NoRecoilHack;
import lexis.Hack.Hacks.TaCZ.NoSprintInterruptHack;
import lexis.Hack.Hacks.TaCZ.SniperFullAutoHack;
import lexis.Hack.Hacks.TaCZ_Server.BoltActionFullAutoHack;
import lexis.Hack.Hacks.TaCZ_Server.InfiniteAmmoHack;
import lexis.Hack.Hacks.TaCZ_Server.MaxRpmHack;
import lexis.Hack.Hacks.TaCZ_Server.NoSpreadHack;
import lexis.Hack.Hacks.World.AntiKickMineHack;
import lexis.Hack.Hacks.World.AutoPickupHack;
import lexis.Hack.Hacks.World.AutoRespawnHack;
import lexis.Hack.Hacks.World.AutoTorchHack;
import lexis.Hack.Hacks.World.CustomCrystalSpinHack;
import lexis.Hack.Hacks.World.EndermanLookHack;
import lexis.Hack.Hacks.World.EntityControlHack;
import lexis.Hack.Hacks.World.NoGhostBlocksHack;
import lexis.Hack.Hacks.World.NukerHack;
import lexis.Hack.Hacks.World.PacketMineHack;
import lexis.Hack.Hacks.World.PortalGodModeHack;
import lexis.Hack.Hacks.World.TimerHack;
import lexis.Hack.Hacks.World.WorldBorderBypassHack;
import lexis.Hack.Hackutil.config.HackConfig;
import lexis.Hack.Hackutil.config.ToggleHacksConfig;
import lexis.Hack.Utils.BaritoneBridge;
import lexis.Hack.Utils.TaczBridge;
import net.minecraftforge.fml.ModList;

public class HackManager {
   private static HackManager instance;
   private List hacks = new ArrayList();
   private ToggleHacksConfig toggleConfig = ToggleHacksConfig.getInstance();
   private final List pendingEnable = new ArrayList();

   private HackManager() {
      this.initHacks();
      this.loadToggleStates();
   }

   public static HackManager getInstance() {
      if (instance == null) {
         instance = new HackManager();
      }

      return instance;
   }

   public List getActiveHacks() {
      List active = new ArrayList();
      Iterator var2 = this.hacks.iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();
         if (hack.isEnabled()) {
            active.add(hack);
         }
      }

      return active;
   }

   private void initHacks() {
      this.hacks.add(new SuperThrowHack());
      this.hacks.add(new SpinHack());
      this.hacks.add(new HelicopterElytraHack());
      this.hacks.add(new UpsideDownHack());
      this.hacks.add(new SkinDerpHack());
      this.hacks.add(new ImitatePlayerHack());
      this.hacks.add(new FakeContainerOpenHack());
      this.hacks.add(new DerpHack());
      this.hacks.add(new MileyCyrusHack());
      this.hacks.add(new StareAtPlayerHack());
      this.hacks.add(new RetainCraftingHack());
      this.hacks.add(new HeadlessPistonHack());
      this.hacks.add(new AutoDropHack());
      this.hacks.add(new ArmorStandPrinterHack());
      this.hacks.add(new FumiGeneratorLoopHack());
      this.hacks.add(new CrashChestHack());
      this.hacks.add(new FlagDisplayHack());
      this.hacks.add(new CrashTextHack());
      this.hacks.add(new TridentDupeHack());
      this.hacks.add(new AutoSignHack());
      this.hacks.add(new AutoFarmHack());
      this.hacks.add(new AirPlaceHack());
      this.hacks.add(new AntiCactusHack());
      this.hacks.add(new InstantRebreakHack());
      this.hacks.add(new AutoFixGroundHack());
      this.hacks.add(new KaboomHack());
      this.hacks.add(new AutoToolHack());
      this.hacks.add(new ScaffoldWalkHack());
      this.hacks.add(new FastBreakHack());
      this.hacks.add(new FreezePlayerHack());
      this.hacks.add(new MultiAuraHack());
      this.hacks.add(new ArrowDmgHack());
      this.hacks.add(new AnchorAuraHack());
      this.hacks.add(new AutoRevengeHack());
      this.hacks.add(new AutoAttackHack());
      this.hacks.add(new BowSpamHack());
      this.hacks.add(new AutoTotemHack());
      this.hacks.add(new TpAuraHack());
      this.hacks.add(new KillauraHack());
      this.hacks.add(new AutoArmorHack());
      this.hacks.add(new AutoCriticalsHack());
      this.hacks.add(new BowAimbotHack());
      this.hacks.add(new CrystalAuraHack());
      this.hacks.add(new TpAurasHack());
      this.hacks.add(new ArrowDmgsHack());
      this.hacks.add(new NoJumpDelayHack());
      this.hacks.add(new AutoSwimHack());
      this.hacks.add(new FireworkElytraFlyHack());
      this.hacks.add(new NoFallHack());
      this.hacks.add(new BoatFrictionHack());
      this.hacks.add(new SpringJumpHack());
      this.hacks.add(new BouncyHack());
      this.hacks.add(new NoSneakSlowHack());
      this.hacks.add(new NoMomentumHack());
      this.hacks.add(new AirJumpHack());
      this.hacks.add(new AntiVoidHack());
      this.hacks.add(new SlippyHack());
      this.hacks.add(new AutoSprintHack());
      this.hacks.add(new CreativeFlightHack());
      this.hacks.add(new ElytraFlysHack());
      this.hacks.add(new AntiHungerHack());
      this.hacks.add(new NoLevitationHack());
      this.hacks.add(new FastLadderHack());
      this.hacks.add(new SpiderHack());
      this.hacks.add(new SnowShoeHack());
      this.hacks.add(new HighJumpHack());
      this.hacks.add(new FlightHack());
      this.hacks.add(new PacketFlyHack());
      this.hacks.add(new JesusHack());
      this.hacks.add(new StepHack());
      this.hacks.add(new NoFallsHack());
      this.hacks.add(new GUIMoveHack());
      this.hacks.add(new ExtraElytraHack());
      this.hacks.add(new NoSlowdownHack());
      this.hacks.add(new AntiExplosionHack());
      this.hacks.add(new AntiKnockbackHack());
      this.hacks.add(new AntiPushHack());
      this.hacks.add(new NoWebHack());
      this.hacks.add(new XrayHack());
      this.hacks.add(new XrayExposedHack());
      this.hacks.add(new AimEntityGlowHack());
      this.hacks.add(new EntitySearcherHack());
      this.hacks.add(new GodViewHack());
      this.hacks.add(new NoPumpkinHack());
      this.hacks.add(new AntiBlindHack());
      this.hacks.add(new ButtonBeautifyHack());
      this.hacks.add(new NoSprintFovHack());
      this.hacks.add(new ArmorOverlayHack());
      this.hacks.add(new FullBrightHack());
      this.hacks.add(new BlockSelectionHack());
      this.hacks.add(new PortalEspHack());
      this.hacks.add(new VoidEspHack());
      this.hacks.add(new ColorCodeHack());
      this.hacks.add(new TrueSightHack());
      this.hacks.add(new BlockAnimationHack());
      this.hacks.add(new BetterVanillaGlowHack());
      this.hacks.add(new WallHack());
      this.hacks.add(new MatrixOverlayHack());
      this.hacks.add(new ChromaticAberrationHack());
      this.hacks.add(new MotionBlurHack());
      this.hacks.add(new VirtualShaderHack());
      this.hacks.add(new NoHurtcamHack());
      this.hacks.add(new NoFireOverlayHack());
      this.hacks.add(new GuiAnimationHack());
      this.hacks.add(new OldWeaponHack());
      this.hacks.add(new PlayerEspHack());
      this.hacks.add(new PortalGuiHack());
      this.hacks.add(new BreakIndicatorHack());
      this.hacks.add(new NoRenderHack());
      this.hacks.add(new NametagsHack());
      this.hacks.add(new EntityOwnerHack());
      this.hacks.add(new ItemOutlineHack());
      this.hacks.add(new SelfNametagHack());
      this.hacks.add(new TimeChangerHack());
      this.hacks.add(new CapeHack());
      this.hacks.add(new BlinkHack());
      this.hacks.add(new SpeedHack());
      this.hacks.add(new EntityEspHack());
      this.hacks.add(new NoBackgroundHack());
      this.hacks.add(new CameraSmoothHack());
      this.hacks.add(new BlockEspHack());
      this.hacks.add(new FreeCamHack());
      this.hacks.add(new CameraDistanceHack());
      this.hacks.add(new StarSkyHack());
      this.hacks.add(new PortalGodModeHack());
      this.hacks.add(new CustomCrystalSpinHack());
      this.hacks.add(new PacketMineHack());
      this.hacks.add(new AutoPickupHack());
      this.hacks.add(new EntityControlHack());
      this.hacks.add(new AutoTorchHack());
      this.hacks.add(new WorldBorderBypassHack());
      this.hacks.add(new TimerHack());
      this.hacks.add(new NukerHack());
      this.hacks.add(new EndermanLookHack());
      this.hacks.add(new NoGhostBlocksHack());
      this.hacks.add(new AutoRespawnHack());
      this.hacks.add(new AntiKickMineHack());
      this.hacks.add(new BindsDisplayHack());
      this.hacks.add(new GuiKeyBindHack());
      this.hacks.add(new TabGuiHack());
      this.hacks.add(new LexisLogoHack());
      this.hacks.add(new NotificationHack());
      this.hacks.add(new GhostHack());
      this.hacks.add(new NoServerRotateHack());
      this.hacks.add(new NotifierHack());
      this.hacks.add(new NeteaseMusicHack());
      this.hacks.add(new PacketLoggerHack());
      this.hacks.add(new NotebotHack());
      this.hacks.add(new IpHack());
      this.hacks.add(new AntiAfkHack());
      this.hacks.add(new ContainerCrashHack());
      this.hacks.add(new DamageHack());
      this.hacks.add(new PacketCancellerHack());
      this.hacks.add(new ThrowHack());
      this.hacks.add(new AntiPacketKickHack());
      HUDSettingsHack hudHack = new HUDSettingsHack();
      this.hacks.add(hudHack);
      this.hacks.add(new ChatAIHack());
      this.hacks.add(new AntiSpamHack());
      this.hacks.add(new BetterChatHack());
      this.hacks.add(new ChatRetainHack());
      this.hacks.add(new FancyChatHack());
      this.hacks.add(new ChatHistoryHack());
      this.hacks.add(new PlayerNotifierHack());
      this.hacks.add(new InfiniChatHack());
      this.hacks.add(new ChatNoTrimHack());
      this.hacks.add(new ChatKeepOpenHack());
      this.hacks.add(new AsciiArtPrinterHack());
      this.hacks.add(new ChatSpamHack());
      this.hacks.add(new PreventGameCloseHack());
      this.hacks.add(new EntityNameLimiterHack());
      this.hacks.add(new EntityHiderHack());
      this.hacks.add(new ParticleProtectHack());
      if (BaritoneBridge.isAvailable()) {
         this.hacks.add(new BaritoneSpeedHack());
         this.hacks.add(new BaritoneParkourHack());
         this.hacks.add(new BaritoneThirdPersonLookHack());
         this.hacks.add(new ElytraAnywhereHack());
         this.hacks.add(new StructureLocatorHack());
         this.hacks.add(new XrayExposedAutoMineHack());
      }

      if (TaczBridge.isAvailable()) {
         this.hacks.add(new NoRecoilHack());
         this.hacks.add(new InstantAimHack());
         this.hacks.add(new NoSprintInterruptHack());
         this.hacks.add(new BulletTracersHack());
         this.hacks.add(new AimbotHack());
         this.hacks.add(new EndlessAimbotHack());
         this.hacks.add(new AutoReloadHack());
         this.hacks.add(new SniperFullAutoHack());
         this.hacks.add(new MaxRpmHack());
         this.hacks.add(new NoSpreadHack());
         this.hacks.add(new InfiniteAmmoHack());
         this.hacks.add(new BoltActionFullAutoHack());
      }

      if (ModList.get().isLoaded("cataclysm")) {
         this.hacks.add(new CataclysmLocatorHack());
         this.hacks.add(new NoScreenShakeHack());
      }

      this.hacks.sort(Comparator.comparing(Hack::getCategory).thenComparing(Hack::getName));
      this.loadSavedSettings();
   }

   private void loadSavedSettings() {
      Iterator var1 = this.hacks.iterator();

      while(true) {
         Hack hack;
         Map saved;
         do {
            if (!var1.hasNext()) {
               return;
            }

            hack = (Hack)var1.next();
            saved = HackConfig.getInstance().getHackSettings(hack.getName());
         } while(saved.isEmpty());

         Iterator var4 = hack.getSettings().iterator();

         while(var4.hasNext()) {
            Hack.Setting setting = (Hack.Setting)var4.next();
            Object val = saved.get(setting.getName());
            if (val != null) {
               try {
                  setting.setValue(val);
               } catch (Exception var8) {
               }
            }
         }
      }
   }

   private void loadToggleStates() {
      Hack.setLoading(true);
      Iterator var1 = this.hacks.iterator();

      while(var1.hasNext()) {
         Hack hack = (Hack)var1.next();
         boolean enabled = this.toggleConfig.isEnabled(hack.getName());
         if ((hack.getName().equals("Lexis Logo") || hack.getName().equals("显示HUD") || hack.getName().equals("通知系统")) && !enabled) {
            enabled = true;
            this.toggleConfig.setEnabled(hack.getName(), true);
         }

         if (enabled) {
            hack.setEnabled(enabled);
            this.pendingEnable.add(hack);
         }
      }

      Hack.setLoading(false);
   }

   public void finishLoading() {
      ArrayList copy;
      synchronized(this.pendingEnable) {
         copy = new ArrayList(this.pendingEnable);
         this.pendingEnable.clear();
      }

      Iterator var2 = copy.iterator();

      while(var2.hasNext()) {
         Hack hack = (Hack)var2.next();

         try {
            if (!hack.isEnabled()) {
               hack.setEnabled(true);
            } else {
               hack.onEnable();
            }
         } catch (Throwable var7) {
            PrintStream var10000 = System.err;
            String var10001 = hack.getName();
            var10000.println("[Lexis] finishLoading error for " + var10001 + ": " + var7.getMessage());

            try {
               if (Hack.mc.f_91080_ != null) {
                  Hack.mc.f_91080_ = null;
               }
            } catch (Throwable var6) {
            }
         }
      }

   }

   public void saveToggleState(Hack hack) {
      this.toggleConfig.setEnabled(hack.getName(), hack.isEnabled());
   }

   public List getHacks() {
      return this.hacks;
   }

   public List getHacksByCategory(Hack.Category category) {
      List result = new ArrayList();
      Iterator var3 = this.hacks.iterator();

      while(var3.hasNext()) {
         Hack hack = (Hack)var3.next();
         if (hack.getCategory() == category) {
            result.add(hack);
         }
      }

      return result;
   }

   public void onUpdate() {
      if (Hack.mc.f_91074_ != null && Hack.mc.f_91073_ != null) {
         Iterator var1 = this.hacks.iterator();

         while(var1.hasNext()) {
            Hack hack = (Hack)var1.next();
            if (hack.isEnabled()) {
               hack.onUpdate();
            }
         }

      }
   }
}
