package mindustry.arcModule;

import arc.Core;
import arc.assets.Loadable;
import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.game.Gamemode;
import mindustry.game.Team;

import static arc.Core.settings;

public class ARCVars implements Loadable {
    public static final int minimapSize = 40;
    public static boolean unitHide = false;

    /** ARC */
    public static String arcVersion = Version.arcBuild <= 0 ? "dev" : String.valueOf(Version.arcBuild);
    public static String arcVersionPrefix = "<ARC~" + arcVersion + ">";
    public static int changeLogRead = 18;
    public static Seq<District.advDistrict> districtList = new Seq<>();
    /** 服务器远程控制允许或移除作弊功能 */
    public static Boolean arcCheatServer = false;
    public static boolean replaying = false;
    public static ReplayController replayController;

    public static int getMaxSchematicSize(){
        int s = Core.settings.getInt("maxSchematicSize");
        return s == 501 ? Integer.MAX_VALUE : s;
    }

    public static int getMinimapSize(){
        return settings.getInt("minimapSize",minimapSize);
    }

    public static String getThemeColorCode(){
        return "[#" + getThemeColor() + "]";
    }

    public static Color getThemeColor(){
        try {
            return Color.valueOf(settings.getString("themeColor"));
        }catch(Exception e){
            return Color.valueOf("ffd37f");
        }
    }

    public static Color getPlayerEffectColor(){
        try {
            return Color.valueOf(settings.getString("playerEffectColor"));
        }catch(Exception e){
            return Color.valueOf("ffd37f");
        }
    }

    public static Boolean arcInfoControl(Team team){
        return team == Vars.player.team() || arcInfoControl();
    }

    public static Boolean arcInfoControl(){
        return   (!arcCheatServer && (Core.settings.getBool("showOtherTeamState") ||
                        Vars.player.team().id == 255 || Vars.state.rules.mode() != Gamemode.pvp));
    }
}
