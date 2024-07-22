package mindustry.squirrelModule.modules.tools;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Strings;
import arc.util.serialization.Base64Coder;

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
        str = Strings.stripColors(str);
        for (int i = 0, l = str.length(); i < l; i++) {
            rot = (rot + step) % PI20;
            sb.append("[#").append(color(rot).toString(), 0, 6).append("]").append(str.charAt(i));
            if (str.charAt(i) == '[') sb.append('[');
        }
        return sb + "[white]";
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
