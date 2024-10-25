package mindustry.arcModule.ui;

import arc.Core;
import arc.Graphics;
import arc.math.Interp;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import arc.util.Log;
import mindustry.ClientLauncher;
import mindustry.arcModule.RFuncs;
import mindustry.arcModule.ui.dialogs.AboutCN_ARCDialog;
import mindustry.arcModule.ui.dialogs.AchievementsDialog;
import mindustry.arcModule.ui.dialogs.MessageDialog;
import mindustry.arcModule.ui.dialogs.MusicDialog;
import mindustry.arcModule.ui.window.WindowManager;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.CustomRulesDialog;
import mindustry.ui.dialogs.UpdateDialog;
import mindustry.ui.fragments.FadeInFragment;
import mindustry.ui.fragments.YuanShenFragment;

import static mindustry.arcModule.toolpack.arcWaveSpawner.initArcWave;

public class ARCUI {

    public Graphics.Cursor resizeHorizontalCursor, resizeVerticalCursor, resizeLeftCursor, resizeRightCursor;

    public AboutCN_ARCDialog aboutcn_arc;
    public UpdateDialog updatedialog;
    public CustomRulesDialog customrules;
    public AchievementsDialog achievements;
    //public MindustryWikiDialog mindustrywiki;
    public mindustry.arcModule.ui.dialogs.MessageDialog MessageDialog;

    public mindustry.arcModule.ui.dialogs.MusicDialog MusicDialog;
    public mindustry.arcModule.ui.window.WindowManager WindowManager;
    public LabelController LabelController;


    /** Display text in the upper of the screen, then fade out. */
    public void arcInfo(String text, float duration){
        Table t = new Table(Styles.black3);
        t.touchable = Touchable.disabled;
        t.margin(8f).add(text).style(Styles.outlineLabel).labelAlign(Align.center);
        t.update(() -> t.setPosition(Core.graphics.getWidth()/2f, Core.graphics.getHeight()/4f, Align.center));
        t.actions(Actions.fadeOut(duration, Interp.pow4In), Actions.remove());
        t.pack();
        t.act(0.1f);
        Core.scene.add(t);
    }

    public void arcInfo(String text){
        arcInfo(text, 3);
    }

    public void load() {
        resizeHorizontalCursor = RFuncs.customCursor("resizeHorizontal", Fonts.cursorScale());
        resizeVerticalCursor = RFuncs.customCursor("resizeVertical", Fonts.cursorScale());
        resizeLeftCursor = RFuncs.customCursor("resizeLeft", Fonts.cursorScale());
        resizeRightCursor = RFuncs.customCursor("resizeRight", Fonts.cursorScale());
    }

    public void init(Group group) {
        aboutcn_arc = new AboutCN_ARCDialog();
        updatedialog = new UpdateDialog();
        customrules = new CustomRulesDialog();
        achievements = new AchievementsDialog();
        //mindustrywiki = new MindustryWikiDialog();
        MessageDialog = new MessageDialog();
        MusicDialog = new MusicDialog();
        LabelController = new LabelController();
        WindowManager = new WindowManager();

        if (ClientLauncher.YuanShenLoader) {
            new YuanShenFragment().build();
        } else {
            new FadeInFragment().build(group);
        }
        Core.settings.put("yuanshen", ClientLauncher.YuanShenLoader);

        initArcWave();
    }
}
