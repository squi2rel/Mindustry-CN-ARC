package mindustry.ios;

import mindustry.arcModule.ARCClassLoader;

public class IOSLauncher0 {
    public static void main(String[] arg) {
        try {
            Class<?> clazz = new ARCClassLoader().loadClass("mindustry.ios.IOSLauncher");
            clazz.getDeclaredMethod("main", String[].class).invoke(null, (Object) arg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
