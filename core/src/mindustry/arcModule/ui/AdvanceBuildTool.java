package mindustry.arcModule.ui;

import arc.func.Cons;
import arc.func.Floatf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Rect;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Tmp;
import mindustry.arcModule.ui.dialogs.BlockSelectDialog;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.content.UnitTypes;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Teams;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.Vars.*;
import static mindustry.arcModule.DrawUtilities.arcDrawText;
import static mindustry.gen.Tex.*;
import static mindustry.ui.Styles.flatDown;
import static mindustry.ui.Styles.flatOver;

public class AdvanceBuildTool extends Table {
    private boolean expandList = false;

    private TextButton.TextButtonStyle textStyle, NCtextStyle;

    BuildRange placement = BuildRange.global;
    Rect selection = new Rect();

    private Block original = Blocks.conveyor, newBlock = Blocks.titaniumConveyor;
    private Block autoBuild = Blocks.turbineCondenser;

    public BuildTiles buildTiles = new BuildTiles();

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
                    tt.button((placement == BuildRange.global ? "[cyan]" : "[gray]") + "", NCtextStyle, () -> {
                        placement = BuildRange.global;
                        rebuild();
                    }).tooltip("[cyan]全局检查").size(30f);
                    tt.button((placement == BuildRange.zone ? "[cyan]" : "[gray]") + "\uE818", NCtextStyle, () -> {
                        selection = control.input.lastSelection;
                        if (selection.area() < 10f) return;
                        placement = BuildRange.zone;
                        rebuild();
                    }).tooltip("[cyan]选择范围").size(30f);
                    tt.button((placement == BuildRange.team ? "" : "[gray]") + Blocks.coreShard.emoji(), NCtextStyle, () -> {
                        placement = BuildRange.team;
                        rebuild();
                    }).tooltip("[cyan]队伍区域").size(30f);
                    tt.button((placement == BuildRange.player ? "" : "[gray]") + UnitTypes.gamma.emoji(), NCtextStyle, () -> {
                        placement = BuildRange.player;
                        rebuild();
                    }).tooltip("[cyan]玩家建造区").size(30f);
                    tt.update(() -> {
                        if (placement != BuildRange.zone) return;
                        arcDrawText("建造区域", 0.2f, selection.x * tilesize + selection.width * tilesize * 0.5f, selection.y * tilesize + selection.height * tilesize, 1);
                        Draw.color(Pal.stat, 0.7f);
                        Draw.z(Layer.effect - 1f);
                        Lines.stroke(Math.min(Math.abs(width), Math.abs(height)) / tilesize / 10f);
                        Lines.rect(selection.x * tilesize, selection.y * tilesize, selection.width * tilesize, selection.height * tilesize);
                        Draw.reset();
                    });
                }).fillX().row();
                t.table(tt -> {
                    tt.button("R", NCtextStyle, this::replaceBlock).tooltip("[cyan]替换方块").size(30f);
                    tt.button(replaceBlockName(), NCtextStyle, this::replaceBlockSetting).tooltip("[acid]设置替换").width(100f).height(30f);
                }).fillX().row();
                t.table(tt -> {
                    tt.button(autoBuild.emoji(), NCtextStyle, () -> blockAutoPlacer(autoBuild)).size(30f);
                    tt.button("\uE87C", NCtextStyle, () -> {
                        new BlockSelectDialog(Block::isPlaceable, block -> autoBuild = block, block -> autoBuild == block).show();
                        rebuild();
                    }).size(30f);
                    tt.update(() -> {
                        if (control.input.selectedBlock()) {
                            autoBuild = control.input.block;
                            rebuild();
                        }
                    });
                }).fillX();
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
        player.team().data().buildings.each(building -> building.block() == ori && contain(building.tile),
                building -> player.unit().addBuild(new BuildPlan(building.tile.x, building.tile.y, building.rotation, re, building.config())));
    }

    void blockAutoPlacer(Block block) {
        buildTiles.buildBlock(block, tile -> getBlockEff(block, tile));
    }

    float getBlockEff(Block block, Tile tile) {
        if (block instanceof ThermalGenerator) return block.sumAttribute(((ThermalGenerator) block).attribute, tile);
        if (block instanceof Drill) return ((Drill) block).countOreArc(tile);
        return 1f;
    }

    boolean contain(Tile tile) {
        if (placement == BuildRange.global) return true;
        if (placement == BuildRange.zone) return selection.contains(tile.x, tile.y);
        if (placement == BuildRange.player) return tile.within(player.x, player.y, buildingRange);
        if (placement == BuildRange.team) {
            if (state.rules.polygonCoreProtection) {
                float mindst = Float.MAX_VALUE;
                CoreBlock.CoreBuild closest = null;
                for (Teams.TeamData data : state.teams.active) {
                    for (CoreBlock.CoreBuild tiles : data.cores) {
                        float dst = tiles.dst2(tile.x * tilesize, tile.y * tilesize);
                        if (dst < mindst) {
                            closest = tiles;
                            mindst = dst;
                        }
                    }
                }
                if (closest != null && closest.team != player.team()) {
                    return false;
                }
            } else if (state.teams.anyEnemyCoresWithin(player.team(), tile.x * tilesize, tile.y * tilesize, state.rules.enemyCoreBuildRadius + tilesize)) {
                return false;
            }
        }
        return true;
    }

    enum BuildRange {
        global, zone, team, player;
    }

    class BuildTiles {
        Seq<Tile> validTile = new Seq<>();
        Seq<Float> eff = new Seq<>();

        float efficiency = 0;

        Block block;

        boolean canBuild = true;

        public BuildTiles() {
        }

        void buildBlock(Block buildBlock, Floatf<Tile> tilef) {
            block = buildBlock;
            updateTiles();
            checkValid();
            calBlockEff(tilef);
            eff.sort().reverse().remove(0f);
            eff.each(this::buildEff);
        }

        public void updateTiles() {
            validTile.clear();
            eff.clear();
            world.tiles.eachTile(tile -> {
                if (tile == null) return;
                if (!contain(tile)) return;
                validTile.add(tile);
            });
            validTile.each(tile -> tile.buildEff = 0f);
        }

        void checkValid() {
            validTile.each(tile -> {
                if (
                        (block.size == 2 && world.getDarkness(tile.x, tile.y) >= 3) ||
                                (state.rules.staticFog && state.rules.fog && !fogControl.isDiscovered(player.team(), tile.x, tile.y)) ||
                                (tile.floor().isDeep() && !block.floating && !block.requiresWater && !block.placeableLiquid) || //deep water
                                (block == tile.block() && tile.build != null && rotation == tile.build.rotation && block.rotate) || //same block, same rotation
                                !tile.interactable(player.team()) || //cannot interact
                                !tile.floor().placeableOn || //solid wall
                                //replacing a block that should be replaced (e.g. payload placement)
                                !((block.canReplace(tile.block()) || //can replace type
                                        (tile.build instanceof ConstructBlock.ConstructBuild build && build.current == block && tile.centerX() == tile.x && tile.centerY() == tile.y)) && //same type in construction
                                        block.bounds(tile.x, tile.y, Tmp.r1).grow(0.01f).contains(tile.block().bounds(tile.centerX(), tile.centerY(), Tmp.r2))) || //no replacement
                                (block.requiresWater && tile.floor().liquidDrop != Liquids.water) //requires water but none found
                ) tile.buildEff = -1; // cannot build
            });
        }

        void calBlockEff(Floatf<Tile> tilef) {
            validTile.each(tile -> {
                canBuild = true;
                getLinkedTiles(tile, tile1 -> canBuild = tile1.buildEff != -1 && canBuild);   //不可能建造
                if (canBuild) {
                    efficiency = tilef.get(tile);
                    tile.buildEff = efficiency;
                    if (!eff.contains(efficiency)) eff.add(efficiency);
                } else {
                    tile.buildEff = 0f;
                }
            });
        }

        void buildEff(float e) {
            if (e == 0) return;
            validTile.each(tile -> {
                if (tile.buildEff != e) return;
                if (!block.canPlaceOn(tile, player.team(), 0)) return;
                player.unit().addBuild(new BuildPlan(tile.x, tile.y, 0, block));
                getFullLinkedTiles(tile, tile1 -> tile1.buildEff = 0f);
            });
        }

        private void getLinkedTiles(Tile tile, Cons<Tile> cons) {
            if (block.isMultiblock()) {
                int size = block.size, o = block.sizeOffset;
                for (int dx = 0; dx < size; dx++) {
                    for (int dy = 0; dy < size; dy++) {
                        Tile other = world.tile(tile.x + dx + o, tile.y + dy + o);
                        if (other != null) cons.get(other);
                    }
                }
            } else {
                cons.get(tile);
            }
        }

        private void getFullLinkedTiles(Tile tile, Cons<Tile> cons) {
            if (block.isMultiblock()) {
                int size = block.size, o = 0;
                for (int dx = -size + 1; dx < size; dx++) {
                    for (int dy = -size + 1; dy < size; dy++) {
                        Tile other = world.tile(tile.x + dx + o, tile.y + dy + o);
                        if (other != null) cons.get(other);
                    }
                }
            } else {
                cons.get(tile);
            }
        }

    }

}
