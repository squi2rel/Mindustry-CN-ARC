package mindustry.squirrelModule.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.scene.ui.Slider;
import mindustry.squirrelModule.modules.hack.Config;

public class MemorySlider extends Slider {
    public Config conf;

    public MemorySlider(String name, float min, float max, float stepSize, float def, boolean vertical) {
        super(min, max, stepSize, vertical);
        memory("s-" + name + "-v", def);
    }

    private void memory(String name, float def) {
        setValue(Core.settings.getFloat(name, def));
        addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == MemorySlider.this) Core.settings.put(name, getValue());
            }
        });
    }
}
