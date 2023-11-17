package mindustry.arcModule.ui;

import arc.Core;
import arc.audio.Music;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Log;
import arc.util.Scaling;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class XiBao {
    static Music music = new Music();
    static boolean hasMusic = false;
    static TextureRegionDrawable cached;
    public void show(String tt, String text, Runnable afterFill) {
        music.stop();
        Group root = Core.scene.root;
        Group trans = new WidgetGroup();
        Image img;
        trans.addChild(img = new Image(get(), Scaling.stretch));
        img.setFillParent(true);
        Label label;
        trans.addChild(label = new Label("你被踢出了服务器！", new Label.LabelStyle(Fonts.outline, Color.gold)));
        label.update(() -> {
            label.setPosition((trans.getWidth() - label.getPrefWidth()) / 2, (trans.getHeight() - label.getPrefHeight()) * 0.77f);
            label.setFontScale(trans.getHeight() * 0.006f);
        });
        trans.addChild(new Table(t -> {
            t.setFillParent(true);
            t.table(t2 -> {
                t2.setBackground(Styles.black3);
                t2.table(t3 -> t3.add(tt).left()).row();
                t2.table(t3 -> t3.add(text)).left().minWidth(200).row();
                t2.button("@ok", () -> {
                    trans.touchable = Touchable.disabled;
                    trans.update(() -> music.setVolume(trans.color.a));
                    trans.actions(Actions.alpha(0, 0.6f), Actions.run(music::stop), Actions.remove());
                }).size(110, 50).pad(4);
            }).pad(10).get().pack();
            t.marginTop(20f);
        }));
        trans.setTransform(true);
        trans.touchable = Touchable.enabled;
        trans.setScale(0.00001f);
        trans.actions(Actions.scaleTo(1, 1, 3));
        trans.update(() -> {
            float scale = (1 - trans.scaleX);
            trans.setPosition(Core.graphics.getWidth() / 2f * scale, Core.graphics.getHeight() / 2f * scale);
            if (scale == 0) {
                trans.update(null);
                afterFill.run();
            }
        });
        if (hasMusic) {
            music.setVolume(1);
            music.setPosition(0);
            music.play();
        }
        trans.setFillParent(true);
        root.addChild(trans);
    }

    public static TextureRegionDrawable get() {
        if (cached != null) return cached;
        cached = new TextureRegionDrawable(new TextureRegion(new Texture(Core.files.internal("icons/xibao.png"))));
        try {
            music.load(Core.files.internal("music/haoyunlai.ogg"));
            hasMusic = true;
        } catch (Exception e) {
            Log.err(e);
        }
        return cached;
    }
}
