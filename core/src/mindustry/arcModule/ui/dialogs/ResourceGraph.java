package mindustry.arcModule.ui.dialogs;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import mindustry.*;
import mindustry.arcModule.ARCVars;
import mindustry.arcModule.toolpack.ARCTeam;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;

import static mindustry.arcModule.toolpack.ARCTeam.statisticsCounter;
import static mindustry.arcModule.toolpack.ARCTeam.statisticsInterval;

public class ResourceGraph extends Table {
    public Seq<SpawnGroup> groups = new Seq<>();
    public int from = 0, to = 20;
    private int max;
    private Table colors;
    private Seq<Team> showTeams = ARCVars.arcTeam.activeTeam.copy();
    private Seq<Item> showRes = Vars.content.items().copy();
    private Seq<Item> removeRes = new Seq<>();

    public ResourceGraph() {
        background(Tex.pane);

        rect((x, y, width, height) -> {
            if (statisticsCounter == 0) return;
            Lines.stroke(Scl.scl(3f));

            GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
            Font font = Fonts.outline;

            lay.setText(font, "1");

            //int maxY = nextStep(max);
            int maxY = max;

            float fh = lay.height;
            float offsetX = Scl.scl(lay.width * (maxY + "").length() * 2), offsetY = Scl.scl(22f) + fh + Scl.scl(5f);

            float graphX = x + offsetX, graphY = y + offsetY, graphW = width - offsetX - 30f, graphH = height - offsetY;
            float spacing = graphW / (statisticsCounter - 1);
            for (Team team : showTeams) {
                for (Item item : showRes) {
                    if (showTeams.size == 1) {
                        Draw.color(item.color);
                    } else {
                        Draw.color(team.color);
                    }
                    Draw.alpha(parentAlpha);

                    Lines.beginLine();

                    for (int i = 0; i < statisticsCounter; i++) {
                        int val = team.arcTeamData.resStatistics[i][item.id];
                        float cx = graphX + i * spacing, cy = graphY + val * graphH / maxY;
                        Lines.linePoint(cx, cy);
                    }
                    Lines.endLine();
                }
            }

            for (Team team : showTeams) {
                for (Item item : showRes) {
                    String name = "";
                    if (showTeams.size == 1) name = item.emoji();
                    else if (showRes.size == 1) name = item.emoji();
                    else name = team.coloredName() + "|" + item.emoji();
                    font.draw(name, graphX + (statisticsCounter - 1) * spacing + 15f, graphY + team.arcTeamData.resStatistics[statisticsCounter - 1][item.id] * graphH / maxY, Align.center);
                }
            }

            //how many numbers can fit here
            float totalMarks = Mathf.clamp(maxY, 1, 10);

            int markSpace = Math.max(1, Mathf.ceil(maxY / totalMarks));

            Draw.color(Color.lightGray);
            Draw.alpha(0.1f);

            for (int i = 0; i < maxY; i += markSpace) {
                float cy = graphY + i * graphH / maxY, cx = graphX;

                Lines.line(cx, cy, cx + graphW, cy);

                lay.setText(font, "" + i);

                font.draw("" + i, cx, cy + lay.height / 2f, Align.right);
            }
            Draw.alpha(1f);

            float len = Scl.scl(4f);
            font.setColor(Color.lightGray);

            for (int i = 0; i < statisticsCounter; i++) {
                float cy = y + fh, cx = graphX + i * spacing;
                Lines.line(cx, cy, cx, cy + len);
                int time = i * statisticsInterval;
                int min = time / 60;
                int sec = time % 60;
                if (statisticsCounter * statisticsInterval / 60 < 2 || (sec == 0 && (statisticsCounter * statisticsInterval < 60 || min % 5 == 0))) {
                    font.draw(min + "'" + sec, cx, cy - Scl.scl(2f), Align.center);
                }
            }
            font.setColor(Color.white);

            Pools.free(lay);

            Draw.reset();
        }).pad(4).padBottom(10).grow();

        row();

        table(t -> colors = t).growX();
    }

    public void rebuild() {
        max = 1;
        removeRes.clear();
        for (Item item : showRes) {
            boolean showItem = false;
            for (int i = 0; i <= statisticsCounter; i++) {
                for (Team team : showTeams) {
                    if (team.arcTeamData.resStatistics[i][item.id] > 0) {
                        showItem = true;
                        break;
                    }
                }
            }
            if (!showItem) removeRes.add(item);
        }
        removeRes.each(item -> showRes.remove(item));
        for (Item item : showRes) {
            for (int i = 0; i <= statisticsCounter; i++) {
                for (Team team : showTeams) {
                    max = Math.max(max, team.arcTeamData.resStatistics[i][item.id]);
                }
            }
        }
        colors.clear();
        colors.left();
        colors.pane(t -> {
            t.pane(ts -> {
                ts.left();
                ts.button("[green]+", Styles.fullTogglet, this::rebuild).checked(false).height(35f).width(50f);
                for (Team team : ARCVars.arcTeam.activeTeam) {
                    ts.button(team.coloredName(), Styles.fullTogglet, () -> {
                        if (showTeams.contains(team)) showTeams.remove(team);
                        else showTeams.add(team);
                        rebuild();
                    }).height(35f).checked(i -> showTeams.contains(team)).width(50f);
                }
            }).scrollY(false).growX();
            t.row();
            t.pane(ts -> {
                ts.left();
                for (Item item : Vars.content.items()) {
                    ts.button(item.emoji(), Styles.fullTogglet, () -> {
                        if (showRes.contains(item)) showRes.remove(item);
                        else showRes.add(item);
                        rebuild();
                    }).height(35f).checked(i -> showRes.contains(item)).width(50f);
                }
            }).scrollY(false).growX();
        });
    }

    int nextStep(float value) {
        int order = 1;
        while (order < value) {
            if (order * 2 > value) {
                return order * 2;
            }
            if (order * 5 > value) {
                return order * 5;
            }
            if (order * 10 > value) {
                return order * 10;
            }
            order *= 10;
        }
        return order;
    }

}
