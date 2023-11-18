package mindustry.squirrelModule.modules.hack;

import arc.Core;
import arc.scene.Group;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.squirrelModule.ui.ControlTable;
import mindustry.ui.Styles;

public class Manager {
    public WidgetGroup controlGroup = new WidgetGroup(), hack = new WidgetGroup();
    ControlTable control;
    public ObjectMap<String, Seq<Config>> list = new ObjectMap<>();
    public ObjectMap<String, Config> flatList = new ObjectMap<>();

    public Manager(Group root) {
        hack.setFillParent(true);
        controlGroup.addChild(hack);
        control = new ControlTable(list);
        root.addChild(control);
        Core.scene.add(controlGroup);
        controlGroup.setFillParent(true);
        controlGroup.touchable = hack.touchable = Touchable.childrenOnly;
        hack.visible = false;
        hack.addChild(new Table(t -> {
            t.setFillParent(true);
            t.setBackground(Styles.black3);
            t.update(t::toBack);
        }));
    }

    public Config getConfig(String name) {
        return flatList.get(name);
    }

    public void register(String type, String name, Config conf) {
        conf.internalName = name;
        Seq<Config> s = list.get(type, new Seq<>());
        list.put(type, s);
        s.add(conf);
        flatList.put(name, conf);
        conf.func.onInit();
        conf.func.onConfigure();
        if (Core.settings.getBool(name + "e")) {
            conf.func.onEnable();
            conf.func.onChanged(true);
            conf.enabled = true;
        } else {
            conf.func.onChanged(false);
        }
    }

    public void enable(Config conf) {
        conf.func.onEnable();
        conf.func.onChanged(true);
        conf.enabled = true;
    }

    public void disable(Config conf) {
        conf.func.onDisable();
        conf.func.onChanged(false);
        conf.enabled = false;
    }

    public void toggle(Config conf) {
        if (conf.enabled) {
            disable(conf);
        } else {
            enable(conf);
        }
    }

    public void buildClickHUD() {
        control.buildClickHUD(hack);
    }
}
