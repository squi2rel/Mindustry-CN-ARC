package mindustry.arcModule;

import arc.func.Cons2;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.Log;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.ValidateException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ARCClient {
    ObjectMap<String, ObjectSet<Cons2<Player, byte[]>>> handler = new ObjectMap<>();

    public void addHandler(String name, Cons2<Player, byte[]> h) {
        handler.get(name, ObjectSet::new).add(h);
    }

    public boolean removeHandler(String name, Cons2<Player, byte[]> h) {
        ObjectSet<Cons2<Player, byte[]>> set = handler.get(name);
        if (set == null) return false;
        return set.remove(h);
    }

    public boolean clearHandler(String name) {
        ObjectSet<Cons2<Player, byte[]>> set = handler.get(name);
        if (set == null) return false;
        set.clear();
        return true;
    }

    public void reset() {
        handler.clear();
    }

    public void handle(Player player, int[] raw) {
        if (raw == null || raw[0] > 0) return;
        DataInputStream s = new DataInputStream(new ByteArrayInputStream(ARCProtocol.decode(raw)));
        try {
            ObjectSet<Cons2<Player, byte[]>> set = handler.get(s.readUTF());
            if (set == null) return;
            int length = s.readInt();
            if (length > 32768) throw new ValidateException(player, "数据太大");
            byte[] data = new byte[length];
            int read = s.read(data);
            if (length != read) throw new ValidateException(player, "无效数据");
            set.each(c -> c.get(player, data));
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void send(String name, byte[] data) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(o);
        try {
            s.writeUTF(name);
            s.writeInt(data.length);
            s.write(data);
        } catch (Exception e) {
            Log.err(e);
        }
        int[] encoded = ARCProtocol.encode(o.toByteArray());
        Call.setUnitCommand(null, encoded, null);
    }

    public static class ARCProtocol {
        public static int[] encode(byte[] in) {
            int length = in.length / 4 + 2, id;
            int[] out = new int[length];
            out[0] = -in.length;
            for (int i = 0; i < in.length; i++) {
                id = i % 4;
                out[i / 4 + 1] |= (in[i] & 255) << id * 8;
            }
            return out;
        }

        public static byte[] decode(int[] in) {
            byte[] out = new byte[-in[0]];
            for (int i = 0; i < out.length; i++) {
                out[i] = (byte) (in[i / 4 + 1] >> (i % 4) * 8);
            }
            return out;
        }
    }
}
