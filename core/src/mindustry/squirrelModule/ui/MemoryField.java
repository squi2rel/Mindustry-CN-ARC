package mindustry.squirrelModule.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.scene.ui.TextField;
import mindustry.squirrelModule.modules.hack.Config;

public class MemoryField extends TextField {
    public Config conf;

    public MemoryField(String name, String def) {
        super(def);
        memory("s-" + name + "-s", def);
    }

    private void memory(String name, String def) {
        setText(Core.settings.getString(name, def));
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == MemoryField.this) Core.settings.put(name, getText());
            }
        });
    }
}
