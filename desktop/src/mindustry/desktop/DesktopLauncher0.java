package mindustry.desktop;

import mindustry.ClientLauncher;
import mindustry.arcModule.ARCClassLoader;

public class DesktopLauncher0 extends ClientLauncher {
    public static void main(String[] arg) {
        try {
            Class<?> clazz = new ARCClassLoader().loadClass("mindustry.desktop.DesktopLauncher");
            clazz.getDeclaredMethod("main", String[].class).invoke(null, (Object) arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
