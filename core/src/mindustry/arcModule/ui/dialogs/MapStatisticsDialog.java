package mindustry.arcModule.ui.dialogs;

import mindustry.ui.dialogs.BaseDialog;

import static mindustry.arcModule.toolpack.ARCTeam.statisticsInterval;

public class MapStatisticsDialog extends BaseDialog {

    private ResourceGraph graph;

    public MapStatisticsDialog() {
        super("ARC~游戏详情");
        if (statisticsInterval != 0){
            cont.add(graph = new ResourceGraph()).grow();
            graph.rebuild();
        }
        addCloseButton();
    }
}
