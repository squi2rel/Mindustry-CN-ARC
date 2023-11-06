package mindustry._extraClasses.classes;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;

public class Test {
    public static void init() {
        Events.on(EventType.ClientLoadEvent.class, e -> Vars.ui.showInfo("extra test!"));
    }
}
