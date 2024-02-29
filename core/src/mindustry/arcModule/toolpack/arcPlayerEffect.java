package mindustry.arcModule.toolpack;

import arc.Core;
import arc.Events;
import arc.scene.ui.Dialog;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.game.EventType;
import mindustry.ui.dialogs.BaseDialog;

import java.lang.reflect.Field;

import static mindustry.arcModule.ARCVars.arcui;
import static mindustry.arcModule.ElementUtils.*;

public class arcPlayerEffect {
    public static Effect playerEffect = Fx.none;
    public static float effectChance = 0;

    private static int effectTime = 0;

    private static int effectCooldown = 1;
    private static Effect refEffect = Fx.none;

    public static boolean show = true;
    private static int curCounter = 0;

    static {
        Events.run(EventType.Trigger.update, () -> {
            curCounter += 1;
            if (curCounter >= effectCooldown) {
                curCounter = 0;
                show = true;
            } else show = false;
        });
        Events.on(EventType.WorldLoadEvent.class,event-> readConfig());
    }

    public static void arcPlayerEffectSetting() {
        Dialog efTable = new BaseDialog("玩家特效设置");
        arcSliderTable(efTable.cont, "特效概率", effectChance, 0, 1, 0.05f, i -> (int) (i * 100) + "%", s -> effectChance = s);
        arcSliderTableInt(efTable.cont, "特效间隔", effectCooldown, 1, 30, 1, i -> "每" + i + "帧", s -> effectCooldown = s);
        arcSliderTableInt(efTable.cont, "特效时长", (int) playerEffect.lifetime, 0, 300, 5, i -> i + "帧", s -> {
            playerEffect.lifetime = s;
            effectTime = s;
        });
        efTable.cont.pane(tt -> {
            for (Field field : Fx.class.getDeclaredFields()) {
                if (field.getGenericType() != Effect.class) continue;
                Effect thisEffect;
                try {
                    thisEffect = (Effect) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                tt.check(field.getName(), refEffect == thisEffect, a -> {
                    playerEffect = thisEffect;
                    refEffect = thisEffect;
                    playerEffect.lifetime = effectTime;
                }).left().row();
            }
        });
        efTable.addCloseButton();
        efTable.buttons.button("保存", arcPlayerEffect::saveConfig);
        efTable.buttons.button("读取", arcPlayerEffect::readConfig);
        efTable.show();
    }

    private static void saveConfig() {
        Core.settings.put("arcPlayerEffectPro", effectChance);
        Core.settings.put("arcPlayerEffectTime", playerEffect.lifetime);
        Core.settings.put("arcPlayerEffectType", refEffect.id);
        Core.settings.put("arcPlayerEffectCooldown", effectCooldown);
        arcui.arcInfo("[cyan]已保存设置");
    }

    private static void readConfig() {
        effectChance = Core.settings.getFloat("arcPlayerEffectPro");
        refEffect = Effect.get(Core.settings.getInt("arcPlayerEffectType"));
        playerEffect = refEffect;
        playerEffect.lifetime = Core.settings.getFloat("arcPlayerEffectTime");
        effectTime = (int) playerEffect.lifetime;
        effectCooldown = Core.settings.getInt("arcPlayerEffectCooldown");
        arcui.arcInfo("[cyan]已读取设置");
    }

}