package mindustry.arcModule;

import mindustry.gen.Player;

public class ARCEvents {
    public static class PlayerJoin {
        public final Player player;

        public PlayerJoin(Player player) {
            this.player = player;
        }
    }

    public static class PlayerLeave {
        public final Player player;

        public PlayerLeave(Player player) {
            this.player = player;
        }
    }

    public static class PlayerKeyReceived {
        public final Player player;
        public final byte[] key;

        public PlayerKeyReceived(Player player, byte[] key) {
            this.player = player;
            this.key = key;
        }
    }

    public static class PlayerKeySend {
        public final byte[] key;

        public PlayerKeySend(byte[] key) {
            this.key = key;
        }
    }

    public static class Connect {
        public Connect() {
        }
    }

    public static class Disconnected {
        public final String reason;
        public Disconnected(String reason) {
            this.reason = reason;
        }
    }
}
