package mindustry.squirrelModule.ui;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Time;
import mindustry.input.Binding;
import mindustry.squirrelModule.modules.hack.Config;
import mindustry.squirrelModule.modules.hack.Hack;
import mindustry.squirrelModule.modules.hack.Manager;
import mindustry.squirrelModule.modules.tools.SMisc;

import static mindustry.squirrelModule.modules.tools.SMisc.PI20;

public class InfoControl {
    public static final float step = 0.1f, lineAdd = 1.5f;
    public Manager manager;
    Table table = new Table();
    float color = 0;

    public void toggle(boolean show) {
        table.visible = show;
    }

    public void build(Group root) {
        root.addChild(table);
        root.addChild(new Table(t -> t.update(this::update)));
        table.setFillParent(true);
        table.align(Align.topLeft);
        manager = new Manager(root);
        Hack.init();
        manager.buildClickHUD();
    }

    public float getColor() {
        return color;
    }

    private void update() {
        if (Core.input.keyTap(Binding.hack)) manager.controlGroup.visible = !manager.controlGroup.visible;
        final float[] thisColor = {color};
        if (table.visible) {
            table.clear();
            table.table(t -> t.add(SMisc.packColor(thisColor[0]) + "VAPE").fontScale(2.5f).pad(50, 5, 0, 5)).left().row();
            table.table(t -> manager.flatList.each((k, v) -> {
                if (!v.enabled) return;
                table.add(SMisc.packColor(thisColor[0]) + v.displayName + " [white]" + v.func.text()).fontScale(1.25f).pad(0, 5, 0, 5).left().row();
                thisColor[0] = (thisColor[0] + lineAdd) % PI20;
            })).left();
            table.toFront();
        }
        manager.controlGroup.toFront();
        color = (color + step * Time.delta) % PI20;
    }

    public Config getConfig(String name) {
        return manager.getConfig(name);
    }
}
