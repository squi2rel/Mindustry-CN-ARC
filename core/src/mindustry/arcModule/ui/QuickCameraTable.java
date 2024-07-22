package mindustry.arcModule.ui;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.BlockUnitc;
import mindustry.gen.Unit;
import mindustry.input.Binding;

import static mindustry.Vars.*;
import static mindustry.arcModule.ARCVars.arcui;
import static mindustry.arcModule.RFuncs.arcSetCamera;
import static mindustry.arcModule.ui.RStyles.*;

public class QuickCameraTable extends Table {
    private static int quickHudSize = Core.settings.getInt("quickHudSize", 0);
    private static SingleHud[] hudList = new SingleHud[10];

    boolean hoverMode = Core.settings.getBool("arcCameraHoverMode", false);
    boolean saveScale = Core.settings.getBool("arcCameraSaveScale", true);

    Binding[] cameraSelect = {
            Binding.camera_select_01,
            Binding.camera_select_02,
            Binding.camera_select_03,
            Binding.camera_select_04,
            Binding.camera_select_05,
            Binding.camera_select_06,
            Binding.camera_select_07,
            Binding.camera_select_08,
            Binding.camera_select_09,
            Binding.camera_select_10
    };

    {
        init();
        Events.on(EventType.WorldLoadEvent.class, event -> init());
        Events.run(EventType.Trigger.update, () -> {
            if (quickHudSize != Core.settings.getInt("quickHudSize", 0)) {
                quickHudSize = Core.settings.getInt("quickHudSize", 0);
                init();
            }
            if (quickHudSize == 0) return;
            if (!hudList[0].isValid() && player.unit() != null) {
                hudList[0] = new SingleHud(player.unit());
                hudList[0].showScale = false;
            }
            for (int i = 0; i < quickHudSize; i++) {
                if (Core.input.keyTap(cameraSelect[i])) hudList[i].getHud();
            }
        });
    }

    private void init() {
        if (quickHudSize == 0) return;
        hudList = new SingleHud[quickHudSize];
        for (int i = 0; i < quickHudSize; i++) {
            hudList[i] = new SingleHud();
        }
        rebuild();
    }

    public QuickCameraTable() {
        if (quickHudSize == 0) return;
        rebuild();
    }

    private void rebuild() {
        quickHudSize = Core.settings.getInt("quickHudSize", 0);
        clear();
        table(t -> {
            //t.setBackground(Styles.black3);
            t.button(Blocks.radar.emoji(), clearLineNonet, () -> arcui.arcInfo("[acid]ARC-快捷窗口[white]\n" +
                    "附身炮台下点会锁定炮台，指挥模式控兵下会锁定兵，其余情况下锁定当前界面"
            ));
            if (!mobile) {
                t.button("\uE88E", clearLineNoneTogglet, () -> {
                    hoverMode = !hoverMode;
                    arcui.arcInfo("[acid]当前为" + (hoverMode ? "悬浮" : "点击") + "切换视角模式");
                    Core.settings.put("arcCameraHoverMode", hoverMode);
                    rebuild();
                }).checked(hoverMode).width(50f);
            }
            t.button("\uE80B", clearLineNoneTogglet, () -> {
                saveScale = !saveScale;
                arcui.arcInfo("[acid]当前" + (saveScale ? "" : "不") + "保存视角缩放");
                Core.settings.put("arcCameraSaveScale", saveScale);
            }).checked(saveScale).width(50f);
            for (int i = 0; i < quickHudSize; i++) hudButton(t, i);
        }).height(50f);
    }

    private void hudButton(Table table, int index) {
        table.table(t -> {
            Label field = t.label(() -> (Core.keybinds.get(cameraSelect[index]).key == KeyCode.unknown ? "" : "[cyan]" + Core.keybinds.get(cameraSelect[index]).key.toString()) + hudList[index].getName()).get();
            if (hoverMode) field.hovered(() -> hudList[index].getHud());
            else field.clicked(() -> hudList[index].getHud());
            if (hudList[index].isValid()) {
                t.button("\uE815", clearLineNonet, () -> {
                    hudList[index] = new SingleHud();
                    rebuild();
                });
            } else {
                t.button("\uE813", clearLineNonet, () -> {
                    addHud(index);
                    rebuild();
                });
            }

        }).padRight(30f);
    }

    private void addHud(int index) {
        if (player.unit() instanceof BlockUnitc unitc) {
            hudList[index] = new SingleHud(unitc.tileX(), unitc.tileY());
        } else if (control.input.commandMode && control.input.selectedUnits.size > 0) {
            hudList[index] = new SingleHud(control.input.selectedUnits.first());
        } else
            hudList[index] = new SingleHud((int) (Core.camera.position.x / tilesize), (int) (Core.camera.position.y / tilesize));
        hudList[index].showScale = saveScale;
    }

    static class SingleHud {
        int x = -1;
        int y = -1;

        float scale = 1f;
        boolean showScale = true;
        @Nullable
        Unit unit;

        public SingleHud() {
        }   //会创建一个无效Hud

        public SingleHud(int x1, int y1) {
            x = x1;
            y = y1;
            scale = renderer.getScale();
        }

        public SingleHud(Unit unit1) {
            unit = unit1;
            scale = renderer.getScale();
        }

        public boolean isValid() {
            return x != -1 || unit != null || y != -1;
        }

        public String getName() {
            if (!isValid()) return "[lightgray]\uE815";
            StringBuilder name = new StringBuilder();
            if (unit != null) {
                return name.append("[white]").append(unit.type.emoji()).append("[white]([acid]").append(unit.tileX()).append("[white],[acid]").append(unit.tileY()).append("[white])").toString();
            } else
                return name.append("[white]([acid]").append(x).append("[white],[acid]").append(y).append("[white])").toString();
        }

        public void getHud() {
            if (!isValid()) return;
            if (showScale) renderer.setScale(scale);
            if (unit != null) arcSetCamera(unit.x, unit.y, false);
            else arcSetCamera(x * tilesize, y * tilesize, false);
        }
    }

}
