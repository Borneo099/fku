package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.FkuConfig;
import fku.org.example.fku.features.healthtag.HealthTagComponent;
import fku.org.example.fku.features.killfx.KillFXComponent;
import fku.org.example.fku.features.killicon.KillIconComponent;
import fku.org.example.fku.features.attackindicator.AttackIndicatorComponent;
import fku.org.example.fku.features.trail.TrailComponent;
import fku.org.example.fku.features.playeresp.PlayerEspComponent;

public class VisualPanel extends GuiPanel {

    public VisualPanel() {
        super("视觉", FkuConfig.visualXPos.get(), FkuConfig.visualYPos.get(), 120, 215);
    }

    @Override
    protected void init() {
        addComponent(new HealthTagComponent(0, 0, 110, 20));
        addComponent(new KillFXComponent(0, 0, 110, 20));
        addComponent(new KillIconComponent(0, 0, 110, 20));
        // ★ 攻击指示器功能开关（左键开关，右键配置界面；借鉴Wurst的AttackIndicator设计）
        addComponent(new AttackIndicatorComponent(0, 0, 110, 20));
        // ★ 拖尾特效功能开关（左键开关，右键配置界面）
        addComponent(new TrailComponent(0, 0, 110, 20));
        // ★ 玩家ESP功能开关（移植自 Lexis PlayerEspHack）
        addComponent(new PlayerEspComponent(0, 0, 110, 20));
    }

    @Override
    protected void savePosition() {
        FkuConfig.visualXPos.set(this.x);
        FkuConfig.visualYPos.set(this.y);
    }
}