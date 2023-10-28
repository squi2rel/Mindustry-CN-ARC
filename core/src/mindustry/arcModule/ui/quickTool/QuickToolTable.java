package mindustry.arcModule.ui.quickTool;

import arc.scene.ui.layout.Table;
import mindustry.arcModule.ElementUtils;
import mindustry.arcModule.ui.AdvanceBuildTool;
import mindustry.arcModule.ui.HudSettingsTable;
import mindustry.gen.Iconc;

public class QuickToolTable extends ElementUtils.ToolTable {

    public HudSettingsTable hudSettingsTable = new HudSettingsTable();
    public AdvanceBuildTool advanceBuildTool = new AdvanceBuildTool();

    public QuickToolTable() {
        icon = String.valueOf(Iconc.list);
        rebuild();
        left();
    }

    @Override
    public void rebuild(){
        clear();
        table(t->{
            button((expand ? "":"[lightgray]") + icon, textStyle, () -> {
                expand = !expand;
                rebuild();
            }).right().size(40f);
        }).growX().row();
        if (expand) {
            buildTable();
        }
    }


    @Override
    protected void buildTable(){
        table(t -> {
            t.add(hudSettingsTable).growX().row();
            t.add(advanceBuildTool).growX();
        });
    }
}
