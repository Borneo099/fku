package lexis.Hack.Hacks.Fun;

import lexis.Hack.Hack;
import lexis.Hack.Hackutil.config.HackConfig;

public class FakeContainerOpenHack extends Hack {
   private HackConfig config = HackConfig.getInstance();
   private static final String CONFIG_KEY = "假错误容器开启动画";

   public FakeContainerOpenHack() {
      super("假错误容器开启动画", new String[]{"这是假错误容器开启动画、别人能看见这容器还是开启动画呢", "能仅一次右键打开容器能开启动画bug、点容器方块会上次容器恢复动画了", "你远离了在容器方块会关闭动画了、箱子是不会关闭、别人看见是关闭动画了", "§c§l注意：关闭功能后当前容器方块恢复不了"}, Hack.Category.FUN, true);
      this.loadConfig();
   }

   private void loadConfig() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   public void onUpdate() {
   }

   public void onClick() {
      this.toggle();
   }
}
