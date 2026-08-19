package fku.org.example.fku.client.gui.components;

import fku.org.example.fku.config.MovementConfig;
import fku.org.example.fku.features.antiknockback.AntiKnockbackComponent;
import fku.org.example.fku.features.antipush.AntiPushComponent;
import fku.org.example.fku.features.flight.FlightComponent;
import fku.org.example.fku.features.nofall.NoFallComponent;
import fku.org.example.fku.features.nojumpdelay.NoJumpDelayComponent;
import fku.org.example.fku.features.sprint.SprintComponent;
import fku.org.example.fku.features.teleport.TeleportComponent;
import fku.org.example.fku.features.tpgoto.TpGotoComponent;
import fku.org.example.fku.features.waterwalk.WaterWalkComponent;

public class MovementPanel extends GuiPanel {

    public MovementPanel() {
        super("移动", MovementConfig.getInstance().guiX, MovementConfig.getInstance().guiY, 120, 290);
    }

    @Override
    protected void init() {
        addComponent(new NoJumpDelayComponent(0, 0, 110, 20));
        addComponent(new SprintComponent(0, 0, 110, 20));
        addComponent(new FlightComponent(0, 0, 110, 20));
        addComponent(new NoFallComponent(0, 0, 110, 20));
        addComponent(new AntiPushComponent(0, 0, 110, 20));
        addComponent(new TeleportComponent(0, 0, 110, 22));
        addComponent(new TpGotoComponent(0, 0, 110, 20));
        addComponent(new WaterWalkComponent(0, 0, 110, 20));
        addComponent(new AntiKnockbackComponent(0, 0, 110, 20));
    }

    @Override
    protected void savePosition() {
        MovementConfig.getInstance().setGuiX(this.x);
        MovementConfig.getInstance().setGuiY(this.y);
    }
}
