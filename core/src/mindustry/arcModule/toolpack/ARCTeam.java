package mindustry.arcModule.toolpack;

import arc.Core;
import arc.Events;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.arcModule.ui.PowerInfo;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.content;
import static mindustry.Vars.state;

public class ARCTeam {

    public Seq<Team> updateTeam = new Seq<>();      //当前显示的队伍
    public Seq<Team> lastUpdateTeam = new Seq<>();  //上一个更新的队伍
    public Seq<Team> forceUpdateTeam = new Seq<>(); //强制显示的队伍，开始新地图时清空

    public Seq<Team> activeTeam = new Seq<>(); //

    public float lastUpd = 0, resUpd = 0, statisUpd = 0;

    private static final int updateTimer = 5;

    public static int statisticsInterval = 0, setStatisticsInterval = 0;
    public int[][] resources;
    public static int statisticsCounter = 0, size = 0;
    /**
     * 默认记录时长 ~ 5小时
     */
    public static final int defaultTime = 5 * 60 * 60;


    public ARCTeam() {
        Events.on(EventType.WorldLoadEvent.class, e -> {
            forceUpdateTeam.clear();
            initTeamList();
            updateTeam.each(team -> team.arcTeamData.init());
            statisticsCounter = 0;
        });

        Events.run(EventType.Trigger.update, () -> {
            if (!state.isGame()) return;
            setStatisticsInterval = Core.settings.getInt("statisticsInterval", 0);

            if (Time.time - lastUpd > 2 * 60f) {
                lastUpd = Time.time;
                updateTeamList();
            }
            if (Time.time - resUpd > 60f) {
                resUpd = Time.time;
                updateTeam.each(team -> team.arcTeamData.updateDiffItems());
            }
            update();

            if (statisticsInterval != setStatisticsInterval){
                statisticsInterval = setStatisticsInterval;
                updateTeam.each(team -> team.arcTeamData.init());
                statisticsCounter = 0;
            }

            if (statisticsInterval != 0){
                size = defaultTime / statisticsInterval;
                if (Time.time - statisUpd > statisticsInterval * 60 && statisticsCounter < size) {
                    statisUpd = Time.time;
                    updateTeam.each(team -> team.arcTeamData.updateRes());
                    statisticsCounter ++;
                }
            }
        });
    }

    private void initTeamList() {
        updateTeam.clear();
        Vars.state.teams.getActive().each(teamData -> updateTeam.add(teamData.team));
        if (state.rules.waveTimer) updateTeam.addUnique(state.rules.waveTeam);
    }

    private void updateTeamList() {
        activeTeam = state.teams.getActive().map(teamData -> teamData.team);
        updateTeam.retainAll(team -> activeTeam.contains(team));
        forceUpdateTeam.each(team -> updateTeam.addUnique(team));
    }

    private void update() {
        if (lastUpdateTeam != updateTeam) {
            updateTeam.each(team -> {
                if (!lastUpdateTeam.contains(team)) team.arcTeamData.init();
            });
        }
        lastUpdateTeam = updateTeam;
        updatePower();
        updateTeam.each(team -> team.arcTeamData.update());
    }

    private void updatePower() {
        updateTeam.each(team -> team.arcTeamData.powerInfo.clear());
        Groups.powerGraph.each(item -> {
            if (updateTeam.contains(item.graph().team))
                item.graph().team.arcTeamData.powerInfo.add(item.graph().getPowerBalance(), item.graph().getLastPowerStored(), item.graph().getLastCapacity(), item.graph().getLastPowerProduced(), item.graph().getLastPowerNeeded());
        });
    }

    public static class ARCTeamData {
        public Team team;
        private CoreBlock.CoreBuild core;

        public int[] currentItems = new int[content.items().size];
        public float[] updateItems = new float[content.items().size];
        public int[] lastItems = new int[content.items().size];

        public PowerInfo powerInfo = new PowerInfo();

        public int[][] resStatistics;

        public ARCTeamData(Team thisTeam) {
            team = thisTeam;
        }

        public void init() {
            core = team.core();
            currentItems = new int[content.items().size];
            updateItems = new float[content.items().size];
            lastItems = new int[content.items().size];
            if (statisticsInterval != 0)
                resStatistics = new int[defaultTime / statisticsInterval][content.items().size]; // 记录5小时的量
        }

        public void update() {
            core = team.core();
            if (core == null) return;
            updateItems();
        }

        public void updateItems() {
            if (currentItems.length == 0) init();
            for (Item item : Vars.content.items()) {
                currentItems[item.id] = core.items.get(item);
            }
        }

        public void updateDiffItems() {
            for (Item item : Vars.content.items()) {
                updateItems[item.id] = (updateItems[item.id] * (updateTimer - 1) + currentItems[item.id] - lastItems[item.id]) / updateTimer;
                lastItems[item.id] = currentItems[item.id];
            }
        }

        public void updateRes() {
            resStatistics[statisticsCounter] = currentItems.clone();
        }
    }
}
