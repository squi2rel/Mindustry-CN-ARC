package mindustry.arcModule;

import arc.Events;
import arc.func.Cons;
import arc.func.Cons2;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.util.Log;
import arc.util.Timer;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.net.ValidateException;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

@SuppressWarnings({"unused", "NewApi"})
public class ARCClient {
    private static final IntMap<byte[]> keys = new IntMap<>();
    private static final byte[] emptyByte = new byte[0];
    private final ObjectMap<String, ObjectSet<Cons2<Player, byte[]>>> handler = new ObjectMap<>();
    public static byte[] myKey;
    public static PrivateKey myPrivate;
    private static Timer.Task keyTask;

    public ARCClient() {
        try {
            KeyPair keyPair = generateKeyPair();
            myKey = keyPair.getPublic().getEncoded();
            myPrivate = keyPair.getPrivate();
            addHandlerStream("ARCKey", (p, s) -> {
                try {
                    short l = s.readShort();
                    if (l != myKey.length) return;
                    byte[] bytes = new byte[l];
                    int r = s.read(bytes);
                    if (r != l) return;
                    keys.put(p.id, bytes);
                    Events.fire(new ARCEvents.PlayerKeyReceived(p, bytes));
                } catch (Exception ignored) {
                }
            });
            Events.on(ARCEvents.PlayerJoin.class, e -> sendKey());
            Events.on(ARCEvents.PlayerLeave.class, e -> keys.remove(e.player.id));
            Events.on(EventType.WorldLoadEndEvent.class, e -> send("ARCKeyRequest", emptyByte));
            addHandler("ARCKeyRequest", (p, d) -> sendKey());
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void sendKey() {
        if (keyTask != null) keyTask.cancel();
        keyTask = Timer.schedule(() -> send("ARCKey", s -> {
            try {
                s.writeShort(myKey.length);
                s.write(myKey);
            } catch (Exception ignored) {
            }
        }), 1);
        Events.fire(new ARCEvents.PlayerKeySend(myKey));
    }

    public void addHandler(String name, Cons2<Player, byte[]> h) {
        handler.get(name, ObjectSet::new).add(h);
    }

    public void addHandlerString(String name, Cons2<Player, String> h) {
        handler.get(name, ObjectSet::new).add((p, d) -> h.get(p, new String(d, StandardCharsets.UTF_8)));
    }

    public void addHandlerStream(String name, Cons2<Player, DataInputStream> h) {
        handler.get(name, ObjectSet::new).add((p, d) -> h.get(p, new DataInputStream(new ByteArrayInputStream(d))));
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
            if (length > 16384) throw new ValidateException(player, "数据太大");
            byte[] data = new byte[length];
            int read = s.read(data);
            if (length != read) throw new ValidateException(player, "无效数据");
            set.each(c -> c.get(player, data));
        } catch (ValidateException ignored) {
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void send(String name, byte[] data) {
        if (data.length > 16384) throw new IllegalArgumentException("数据太大: " + data.length);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream s = new DataOutputStream(o);
        try {
            s.writeUTF(name);
            s.writeInt(data.length);
            s.write(data);
            send0(o);
        } catch (Exception e) {
            Log.err(e);
        }
    }

    public static void send(String name, String data) {
        send(name, data.getBytes(StandardCharsets.UTF_8));
    }

    public static void send(String name, Cons<DataOutputStream> data) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        data.get(new DataOutputStream(o));
        send(name, o.toByteArray());
    }

    private static void send0(ByteArrayOutputStream o) {
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

    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        return keyPairGenerator.generateKeyPair();
    }

    public static byte[] encrypt(byte[] data, Player player) throws Exception {
        byte[] key = keys.get(player.id);
        if (key == null) throw new ValidateException(player, "未找到玩家key");
        return encrypt(data, generatePublic(key));
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = data.length;
        int offLen = 0;
        int i = 0;
        ByteArrayOutputStream bops = new ByteArrayOutputStream();
        while (inputLen - offLen > 0) {
            byte[] cache;
            if(inputLen - offLen > 117) {
                cache = cipher.doFinal(data, offLen,117);
            } else {
                cache = cipher.doFinal(data, offLen,inputLen - offLen);
            }
            bops.write(cache);
            i++;
            offLen = 117 * i;
        }
        bops.close();
        return bops.toByteArray();
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = data.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > 128) {
                cache = cipher.doFinal(data, offSet, 128);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * 128;
        }
        return out.toByteArray();
    }

    public static PublicKey generatePublic(byte[] data) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
