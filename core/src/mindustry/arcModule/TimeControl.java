package mindustry.arcModule;

import arc.Core;
import arc.Events;
import arc.func.Floatp;
import arc.math.WindowedMean;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.game.EventType;

public class TimeControl {
    public static float gameSpeed = 1f;
    public static int targetFps = 60;
    public static boolean fpsLock = false;
    public static WindowedMean gameSpeedBalance = new WindowedMean(120);

    public static final Floatp deltaProvider = () -> {
        float delta = Core.graphics.getDeltaTime();
        delta = (Float.isNaN(delta) || Float.isInfinite(delta)) ? 1f : Math.max(delta, 0.0001f);

        if (fpsLock) {
            gameSpeedBalance.add(1f / (delta * targetFps));
            return 60f / targetFps;
        }
        else {
            return delta * 60f * gameSpeed;
        }
    };
    static {
        Events.on(EventType.ResetEvent.class, e -> {
            gameSpeed = 1f;
            targetFps = 60;
            fpsLock = false;
        });
    }
    public static void setGameSpeed(float speed){
        gameSpeed = speed;
        if (!fpsLock) {
            Vars.ui.announce(Strings.format("当前游戏速度：@倍", gameSpeed));
        }
        else unlockFps();
    }
    public static void changeGameSpeed(float mul){
        gameSpeed *= mul;
        if (!fpsLock) {
            Vars.ui.announce(Strings.format("当前游戏速度：@倍", gameSpeed));
        }
        else unlockFps();
    }
    public static float getGameSpeed() {
        if (fpsLock) return gameSpeedBalance.rawMean();
        return gameSpeed;
    }

    public static void setTargetFps(int fps) {
        targetFps = fps;
        if (fpsLock) {
            Vars.ui.announce(Strings.format("当前帧率锁定：@", targetFps));
        }
    }

    public static void lockFps() {
        fpsLock = true;

        Vars.ui.announce(Strings.format("已开启帧率锁定模式\n当前帧率锁定：@", targetFps));
    }

    public static void unlockFps() {
        fpsLock = false;
        gameSpeedBalance.clear();

        Vars.ui.announce(Strings.format("已关闭帧率锁定模式\n当前游戏速度：@倍", gameSpeed));
    }
}
