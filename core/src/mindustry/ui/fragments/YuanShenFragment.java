package mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Scaling;
import mindustry.gen.Tex;
import mindustry.graphics.YuanShenLoadRenderer;

public class YuanShenFragment {
    private static TextureRegionDrawable YuanShenTexture;

    public void build() {
        if (YuanShenTexture == null) {
            YuanShenTexture = new TextureRegionDrawable(new TextureRegion(new Texture(Core.files.internal("icons/yuanshen.png"))));
        }
        Group group = new WidgetGroup();
        group.setFillParent(true);
        Core.scene.add(group);
        Table t = new Table();
        t.setFillParent(true);
        group.update(group::toFront);
        group.addChild(t);
        t.add(new Image(YuanShenTexture, Scaling.fit) {
            @Override
            public void draw() {
                if (YuanShenLoadRenderer.shouldPlay) {
                    super.draw();
                }
            }
        }).grow().update(i -> {
            if (YuanShenLoadRenderer.shouldPlay) {
                i.actions(Actions.sequence(Actions.fadeIn(0.9f), Actions.delay(2f), Actions.fadeOut(0.7f), Actions.run(() -> t.actions(Actions.fadeOut(0.5f), Actions.remove(group)))));
                i.update(() -> {});
            }
        }).get().color.a = 0;
        t.setBackground(Tex.whiteui);
    }
}
