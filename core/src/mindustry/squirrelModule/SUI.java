package mindustry.squirrelModule;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.squirrelModule.ui.InfoControl;

import static mindustry.squirrelModule.modules.hack.Hack.sui;

public class SUI {
    public InfoControl infoControl;
    public WidgetGroup squirrelGroup;

    public void init() {
        squirrelGroup = new WidgetGroup();
        infoControl = new InfoControl();

        squirrelGroup.setFillParent(true);
        squirrelGroup.touchable = Touchable.childrenOnly;
        squirrelGroup.update(() -> squirrelGroup.toFront());
        Core.scene.add(squirrelGroup);
        sui.infoControl.build(squirrelGroup);

        if (Vars.mobile) {
            Image img;
            sui.infoControl.manager.controlGroup.addChild(img = new Image(new TextureRegion(new Texture(Core.files.internal("icons/icon_64.png")))));
            img.x = img.y = 100;
            img.addListener(new InputListener() {
                float lastX, lastY;
                boolean dragged;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    dragged = false;
                    Vec2 v = img.localToParentCoordinates(Tmp.v1.set(x, y));
                    lastX = v.x;
                    lastY = v.y;
                    img.toFront();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    Vec2 v = img.localToParentCoordinates(Tmp.v1.set(x, y));
                    if (!dragged && Tmp.v2.set(v).sub(Tmp.v3.set(lastX, lastY)).len() < 5) return;
                    dragged = true;
                    img.x += v.x - lastX;
                    img.y += v.y - lastY;
                    lastX = v.x;
                    lastY = v.y;
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (!dragged) sui.infoControl.manager.hack.visible = !sui.infoControl.manager.hack.visible;
                    dragged = false;
                }
            });
        }
    }
}
