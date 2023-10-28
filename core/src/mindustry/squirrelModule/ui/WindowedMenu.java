package mindustry.squirrelModule.ui;

import arc.func.Cons;
import arc.func.Cons2;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.Vars;
import mindustry.arcModule.ARCVars;
import mindustry.arcModule.ui.window.Window;
import mindustry.ui.Styles;

public class WindowedMenu {
    public static Window newMenu(String title, String message, String[][] options, Cons2<Integer, Window> buttonListener) {
        Window w = ARCVars.arcui.WindowManager.createWindow();
        w.setTitle(title);
        w.setBody(buildMenu(message, options, i -> buttonListener.get(i, w)));
        return w;
    }

    public static Table buildMenu(String message, String[][] options, Cons<Integer> buttonListener) {
        Table t = new Table();
        t.setBackground(Styles.black3);
        t.pane(table -> {
            table.add(message).get().setAlignment(Align.center);
            table.row();
            int option = 0;
            for(String[] optionsRow : options){
                Table buttonRow = table.table().growX().get();
                table.row();
                for (String s : optionsRow) {
                    if (s == null) continue;
                    int finalOption = option;
                    buttonRow.button(s, () -> buttonListener.get(finalOption)).growX().height(50).pad(4);
                    option++;
                }
            }
        }).grow().get().setScrollingDisabledX(true);
        return t;
    }
}
