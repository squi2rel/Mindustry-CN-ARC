package mindustry.ui.fragments;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Scaling;
import mindustry.gen.Tex;

public class YuanShenFragment {
    private static TextureRegionDrawable YuanShenTexture;

    public void build(Group parent) {
        if (YuanShenTexture == null) {
            YuanShenTexture = new TextureRegionDrawable(new TextureRegion(new Texture(Core.files.internal("icons/yuanshen.png"))));
        }
        Table t = new Table();
        parent.addChild(t);
        t.setFillParent(true);
        t.add(new Image(YuanShenTexture, Scaling.fit)).growY().get().actions(Actions.sequence(Actions.alpha(0), Actions.delay(0.5f), Actions.fadeIn(0.7f), Actions.delay(2f), Actions.fadeOut(0.7f), Actions.run(() -> t.actions(Actions.fadeOut(0.3f), Actions.remove()))));
        t.setBackground(Tex.whiteui);
    }
}
