package lexis.Hack.Hackutil.ChatSpam;

import lexis.Hack.HackButton;
import lexis.Hack.Hacks.Chat.ChatSpamHack;
import lexis.Hack.Hackutil.config.ChatSpamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ChatSpamSettingsScreen extends Screen {
   private final ChatSpamHack hack;
   private final HackButton parentButton;
   private final Screen parent;
   private EditBox messageBox;
   private EditBox speedBox;
   private EditBox maxMsgBox;
   private EditBox cooldownBox;
   private ChatSpamConfig config;

   public ChatSpamSettingsScreen(ChatSpamHack hack, HackButton parentButton) {
      super(Component.m_237113_("消息发送设置"));
      this.hack = hack;
      this.parentButton = parentButton;
      this.parent = Minecraft.m_91087_().f_91080_;
      this.config = ChatSpamConfig.getInstance();
   }

   protected void m_7856_() {
      super.m_7856_();
      int centerX = this.f_96543_ / 2;
      int y = 50;
      this.messageBox = new EditBox(this.f_96547_, centerX - 100, y, 200, 20, Component.m_237113_("消息内容"));
      this.messageBox.m_94144_(this.hack.getMessage());
      this.messageBox.m_94199_(256);
      this.m_142416_(this.messageBox);
      y += 30;
      this.speedBox = new EditBox(this.f_96547_, centerX - 100, y, 200, 20, Component.m_237113_("速度"));
      this.speedBox.m_94144_(String.valueOf(this.hack.getSpeed()));
      this.m_142416_(this.speedBox);
      y += 30;
      this.maxMsgBox = new EditBox(this.f_96547_, centerX - 100, y, 200, 20, Component.m_237113_("最大数量"));
      this.maxMsgBox.m_94144_(String.valueOf(this.hack.getMaxMessages()));
      this.m_142416_(this.maxMsgBox);
      y += 30;
      this.cooldownBox = new EditBox(this.f_96547_, centerX - 100, y, 200, 20, Component.m_237113_("冷却时间"));
      this.cooldownBox.m_94144_(String.valueOf(this.hack.getCooldownTime()));
      this.m_142416_(this.cooldownBox);
      y += 50;
      this.m_142416_(Button.m_253074_(Component.m_237113_("保存设置"), (btn) -> {
         this.saveSettings();
         if (this.parent != null) {
            Minecraft.m_91087_().m_91152_(this.parent);
         } else {
            Minecraft.m_91087_().m_91152_((Screen)null);
         }

      }).m_252987_(centerX - 110, y, 100, 20).m_253136_());
      this.m_142416_(Button.m_253074_(Component.m_237113_("取消"), (btn) -> {
         if (this.parent != null) {
            Minecraft.m_91087_().m_91152_(this.parent);
         } else {
            Minecraft.m_91087_().m_91152_((Screen)null);
         }

      }).m_252987_(centerX + 10, y, 100, 20).m_253136_());
   }

   private void saveSettings() {
      try {
         String newMessage = this.messageBox.m_94155_();
         double newSpeed = Double.parseDouble(this.speedBox.m_94155_());
         int newMaxMessages = Integer.parseInt(this.maxMsgBox.m_94155_());
         int newCooldown = Integer.parseInt(this.cooldownBox.m_94155_());
         newSpeed = Math.max(0.5, Math.min(10.0, newSpeed));
         newMaxMessages = Math.max(1, Math.min(20, newMaxMessages));
         newCooldown = Math.max(1, Math.min(30, newCooldown));
         this.hack.updateSettings(newMessage, newSpeed, newMaxMessages, newCooldown);
      } catch (NumberFormatException var6) {
      }

   }

   public void m_88315_(GuiGraphics gui, int mouseX, int mouseY, float delta) {
      this.m_280273_(gui);
      gui.m_280137_(this.f_96547_, "消息发送设置", this.f_96543_ / 2, 20, 16766720);
      int centerX = this.f_96543_ / 2;
      int y = 50;
      gui.m_280056_(this.f_96547_, "消息内容:", centerX - 150, y + 5, 16777215, false);
      y += 30;
      gui.m_280056_(this.f_96547_, "发送速度 (秒):", centerX - 150, y + 5, 16777215, false);
      y += 30;
      gui.m_280056_(this.f_96547_, "最大数量:", centerX - 150, y + 5, 16777215, false);
      y += 30;
      gui.m_280056_(this.f_96547_, "冷却时间 (秒):", centerX - 150, y + 5, 16777215, false);
      super.m_88315_(gui, mouseX, mouseY, delta);
   }

   public boolean m_7043_() {
      return false;
   }
}
