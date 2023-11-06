package mindustry._extraClasses;

import arc.files.Fi;
import arc.util.serialization.BaseJsonWriter;
import arc.util.serialization.JsonWriter;
import mindustry.core.Version;

import java.io.*;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Packer {
    public static void main(String[] args) throws IOException {
        Fi file = Fi.get("./assets/classes.zip");
        file.parent().mkdirs();
        System.out.println(file.absolutePath());
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file.file())));
        Fi root;
        JsonWriter json = new JsonWriter(new FileWriter("./assets/classes.json"));
        BaseJsonWriter writer = json.object();
        writer.set("version", 1);
        writer.set("lastUpdate", new Date().getTime());
        json.close();
        (root = Fi.get("./build/classes/java/main/mindustry/_extraClasses/classes")).walk(f -> {
            if (!f.extEquals("class")) return;
            try {
                out.putNextEntry(new ZipEntry("extra/" + f.path().replace(root.path(), "").substring(1)));
                byte[] bytes = f.readBytes();
                out.write(bytes, 0, bytes.length);
                out.flush();
                out.closeEntry();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        out.close();
    }
}
