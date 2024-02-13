package mindustry.arcModule.claj;

import arc.Core;
import mindustry.Vars;
import mindustry.arcModule.claj.dialogs.JoinViaClajDialog;
import mindustry.arcModule.claj.dialogs.ManageRoomsDialog;

public class Claj {
    public static JoinViaClajDialog joinViaClaj;
    public static ManageRoomsDialog manageRooms;

    public static void init() {
        ClajIntegration.load();
        joinViaClaj = new JoinViaClajDialog();
        manageRooms = new ManageRoomsDialog();
    }

    public static void copy(String text) {
        if (text == null) return;

        Core.app.setClipboardText(text);
        Vars.ui.showInfoFade("@copied");
    }
}