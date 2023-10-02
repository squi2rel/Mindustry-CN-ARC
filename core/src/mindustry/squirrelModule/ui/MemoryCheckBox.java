package mindustry.squirrelModule.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.scene.ui.CheckBox;
import mindustry.squirrelModule.modules.hack.Config;

public class MemoryCheckBox extends CheckBox {
    public Config conf;

    public MemoryCheckBox(String name, String tips, boolean def) {
        super(tips);
        memory("s-" + name + "-b", def);
    }

    public MemoryCheckBox(String name, String tips, boolean def, CheckBoxStyle style) {
        super(tips, style);
        memory("s-" + name + "-b", def);
    }

    private void memory(String name, boolean def) {
        setChecked(Core.settings.getBool(name, def));
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == MemoryCheckBox.this) {
                    Core.settings.put(name, isChecked());
                }
            }
        });
    }
}
