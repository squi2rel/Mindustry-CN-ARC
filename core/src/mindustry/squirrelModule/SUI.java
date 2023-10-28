package mindustry.squirrelModule;

import arc.Core;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import mindustry.squirrelModule.ui.InfoControl;

import static mindustry.squirrelModule.modules.hack.Hack.sui;

public class SUI {
    public InfoControl infoControl;
    public WidgetGroup squirrelGroup;

    public void init() {
        squirrelGroup = new WidgetGroup();
        infoControl = new InfoControl();

        squirrelGroup.setFillParent(true);
        squirrelGroup.touchable = Touchable.childrenOnly;
        squirrelGroup.update(() -> squirrelGroup.toFront());
        Core.scene.add(squirrelGroup);
        sui.infoControl.build(squirrelGroup);
    }
}
