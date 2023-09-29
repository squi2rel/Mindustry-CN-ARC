package mindustry.squirrelModule.modules.tools;

import arc.func.Boolp;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import arc.util.serialization.Base64Coder;
import mindustry.arcModule.ui.window.Window;

import javax.swing.text.Element;

public class SMisc {
    private static final Color tmp = new Color();
    public static final float PI20 = (float) Math.PI * 20;
    public static Color color(float rot) {
        return tmp.set(Color.packRgba((int) (Mathf.sin(0.1f * rot) * 127 + 128), (int) (Mathf.sin(0.1f * rot + 2 * Mathf.PI / 3) * 127 + 128), (int) (Math.sin(0.1f * rot + 4 * Mathf.PI / 3) * 127 + 128), 255));
    }

    public static String packColor(float rot) {
        return "[#" + color(rot) + "]";
    }

    public static String color(String str, float step, float rot) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0, l = str.length(); i < l; i++) {
            rot = (rot + step) % PI20;
            if (str.charAt(i) == '[') {
                sb.append("[#").append(color(rot).toString(), 0, 6).append("]").append("[[");
                i++;
                continue;
            }
            sb.append("[#").append(color(rot).toString(), 0, 6).append("]").append(str.charAt(i));
            if (str.charAt(i) == '[') sb.append('[');
        }
        return sb + "[white]";
    }

    public static void draggable(Table t, Boolp drag) {
        t.addListener(new InputListener() {
            float lastX, lastY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                Vec2 v = t.localToParentCoordinates(Tmp.v1.set(x, y));
                lastX = v.x;
                lastY = v.y;
                t.toFront();
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!drag.get()) return;
                Vec2 v = t.localToParentCoordinates(Tmp.v1.set(x, y));
                t.x += v.x - lastX;
                t.y += v.y - lastY;
                lastX = v.x;
                lastY = v.y;
            }
        });
    }

    public static String randomBase64(int length) {
        byte[] result = new byte[length];
        new Rand().nextBytes(result);
        return new String(Base64Coder.encode(result));
    }

    public static boolean base64Valid(String s, int length) {
        try {
            return Base64Coder.decode(s).length == length;
        } catch (Exception e) {
            return false;
        }
    }
}
