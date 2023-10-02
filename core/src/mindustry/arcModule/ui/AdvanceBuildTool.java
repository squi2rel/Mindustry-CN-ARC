package mindustry.arcModule.ui;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.arcModule.ui.dialogs.BlockSelectDialog;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import static mindustry.Vars.*;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.flatDown;
import static mindustry.ui.Styles.flatOver;

public class AdvanceBuildTool extends Table {
    private boolean expandList = false;

    private TextButton.TextButtonStyle textStyle, NCtextStyle;

    private boolean allBlock = false;

    public boolean writeInProperty = false;

    private Block original = Blocks.conveyor, newBlock = Blocks.titaniumConveyor;
    private Block autoBuild = Blocks.turbineCondenser;

    public AdvanceBuildTool() {
        textStyle = new TextButton.TextButtonStyle() {{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            checked = flatDown;
        }};

        NCtextStyle = new TextButton.TextButtonStyle() {{
            down = flatOver;
            up = pane;
            over = flatDownBase;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
        }};
        rebuild();
        right();
    }

    void rebuild() {
        clearChildren();
        if (expandList) {
            table(t -> {
                t.setBackground(Styles.black6);
                t.table(tt -> {
                    tt.button("R", NCtextStyle, this::replaceBlock).tooltip("[cyan]替换方块").size(30f);
                    tt.button(replaceBlockName(), NCtextStyle, this::replaceBlockSetting).tooltip("[acid]设置替换").width(100f).height(30f);
                }).row();
                t.table(tt -> {
                    tt.button(autoBuild.emoji(), NCtextStyle, () -> blockAutoPlacer(autoBuild)).size(30f);
                    tt.button("\uE87C", NCtextStyle, () -> {
                        new BlockSelectDialog(Block::isPlaceable, block -> autoBuild = block, block -> autoBuild == block).show();
                        rebuild();
                    }).size(30f);
                });
            });
        }

        button(Blocks.buildTower.emoji(), textStyle, () -> {
            expandList = !expandList;
            rebuild();
        }).size(40f).fillY();
    }

    void replaceBlockSetting() {
        BaseDialog dialog = new BaseDialog("方块替换器");
        dialog.cont.table(t -> {
            t.table(tt -> tt.label(() -> "当前选择：" + replaceBlockName())).row();
            t.image().color(getThemeColor()).fillX().row();
            t.table(tt -> {
                replaceBlockGroup(dialog, tt, Blocks.conveyor, Blocks.titaniumConveyor);
                replaceBlockGroup(dialog, tt, Blocks.conveyor, Blocks.duct);
                replaceBlockGroup(dialog, tt, Blocks.conduit, Blocks.pulseConduit);
                replaceBlockGroup(dialog, tt, Blocks.conduit, Blocks.reinforcedConduit);
            }).padTop(5f).row();
            t.image().color(getThemeColor()).padTop(5f).fillX().row();
            t.table(tt -> {
                tt.button("源方块", () -> new BlockSelectDialog(block -> block.replaceable, block -> original = block, block -> original == block).show()).width(100f).height(30f).row();
                tt.button("新方块", () -> new BlockSelectDialog(block -> original.canReplace(block), block -> newBlock = block, block -> newBlock == block).show()).width(100f).height(30f).row();
            }).padTop(5f).row();
        });
        dialog.hidden(this::rebuild);
        dialog.addCloseButton();
        dialog.show();
    }

    void replaceBlockGroup(Dialog dialog, Table t, Block ori, Block re) {
        t.button(replaceBlockName(ori, re), () -> {
            original = ori;
            newBlock = re;
            dialog.hide();
        }).width(100f).height(30f);
    }

    String replaceBlockName() {
        return replaceBlockName(original, newBlock);
    }

    String replaceBlockName(Block ori, Block re) {
        return ori.emoji() + "\uE803" + re.emoji();
    }

    void replaceBlock() {
        replaceBlock(original, newBlock);
    }

    void replaceBlock(Block ori, Block re) {
        player.team().data().buildings.each(building -> building.block() == ori,
                building -> player.unit().addBuild(new BuildPlan(building.tile.x, building.tile.y, building.rotation, re, building.config())));
    }

    void blockAutoPlacer(Block block) {
        world.tiles.eachTile(tile -> {
            if (tile == null) return;
            if (!block.canPlaceOn(tile, player.team(), 0)) return;
            player.unit().addBuild(new BuildPlan(tile.x, tile.y, 0, block));
        });
    }

}
