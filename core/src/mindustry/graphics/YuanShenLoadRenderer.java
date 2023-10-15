package mindustry.graphics;

import arc.Core;
import arc.Events;
import arc.assets.Loadable;
import arc.audio.Music;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Scaling;
import arc.util.Time;
import arc.util.Timer;
import mindustry.game.EventType;

public class YuanShenLoadRenderer extends LoadRenderer {
    private static Music fadeIn;
    private static Music loop;
    private static Music fadeOut;
    private static Pixmap pixmap;
    private static TextureRegion YuanShen;
    private static final Color draw = new Color(1684301055);
    private static final Color bg = new Color(-202116097);
    private static boolean loaded = false;
    private static boolean loadEnd = false;
    private static int status = 0;
    private static long time = 0;
    public static boolean shouldPlay = false;

    public YuanShenLoadRenderer() {
        try {
            fadeIn = new Music(Core.files.internal("music/yuanshen_in.ogg"));
            loop = new Music(Core.files.internal("music/yuanshen_loop.ogg"));
            fadeOut = new Music(Core.files.internal("music/yuanshen_out.ogg"));
            pixmap = new Pixmap(Core.files.internal("icons/yuanshen1.png"));
            YuanShen = new TextureRegion(new Texture(pixmap));
            loaded = true;
            loop.setLooping(true);
        } catch (Exception e) {
            Log.err(e);
        }
    }

    @Override
    public void dispose() {
        fadeIn.dispose();
        YuanShen.texture.dispose();
        fadeIn = null;
        pixmap = null;
        YuanShen = null;
        super.dispose();
        loaded = false;
    }

    @Override
    public void draw() {
        if (!loaded) {
            super.draw();
            return;
        }
        int width = Core.graphics.getWidth(), height = Core.graphics.getHeight();
        Vec2 size = Scaling.fit.apply(pixmap.getWidth(), pixmap.getHeight(), width, height);
        Core.graphics.clear(Color.white);
        Draw.color(bg);
        Fill.crect((width - size.x) / 2f, 0, size.x, size.y);
        Draw.color(draw);
        Fill.crect((width - size.x) / 2f, 0, status == 3 ? size.x * 0.93f : status >= 4 ? size.x : size.x * Core.assets.getProgress(), size.y);
        Draw.color(Color.white);
        Draw.rect(YuanShen, width / 2f, size.y / 2f, size.x, size.y);
        if (status >= 4) {
            Draw.alpha(Mathf.clamp((float) (Time.millis() - time) / 1000, 0, 1f));
            Fill.rect(width / 2f, size.y / 2f, size.x, size.y);
        }
        Draw.flush();
        switch (status) {
            case 0 -> {
                fadeIn.play();
                status++;
            }
            case 1 -> {
                if (fadeIn.getPosition() != 0) status++;
            }
            case 2 -> {
                if (fadeIn.getPosition() == 0) {
                    loop.play();
                    status++;
                }
            }
            case 3 -> {
                if (Core.assets.getCurrentLoading() == null || Core.assets.getCurrentLoading().type == LoadLock.class) {
                    time = Time.millis();
                    status++;
                }
            }
            case 4 -> {
                if ((Time.millis() - time) > 1000) {
                    status++;
                    loadEnd = true;
                }
            }
        }
        Events.on(EventType.ClientLoadEvent.class, e -> loop.setLooping(false));
        Events.run(EventType.Trigger.update, () -> {
            if (loop != null && !loop.isLooping() && loop.getPosition() == 0) {
                fadeOut.play();
                Timer.schedule(() -> shouldPlay = true, 8f);
                Timer.schedule(() -> {
                    fadeOut.dispose();
                    fadeOut = null;
                }, 20);
                loop.dispose();
                loop = null;
            }
        });
    }

    public static class LoadLock implements Loadable {
        @Override
        public void loadAsync() {
            while (!loadEnd) Thread.yield();
        }
    }
}
