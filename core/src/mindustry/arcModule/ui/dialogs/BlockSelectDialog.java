package mindustry.arcModule.ui.dialogs;

import arc.Core;
import arc.func.Boolf;
import arc.func.Cons;
import arc.struct.EnumSet;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;

import static mindustry.Vars.content;
import static mindustry.Vars.getThemeColor;

public class BlockSelectDialog extends BaseDialog {
    public BlockSelectDialog(Boolf<Block> condition, Cons<Block> cons, Boolf<Block> checked) {
        this(condition, cons, checked, true);
    }

    public BlockSelectDialog(Boolf<Block> condition, Cons<Block> cons, Boolf<Block> checked, boolean autoHide) {
        super("方块选择器");
        cont.pane(td -> {
            Seq<Block> blocks = content.blocks().select(block -> condition.get(block) && (Core.settings.getBool("allBlocksReveal") || !block.isHidden())).sort(block -> block.group.ordinal());
            Seq<BlockGroup> blockGroups = blocks.map(block -> block.group).distinct();
            blockGroups.each(blockGroup -> {
                td.row();
                td.add(blockGroup.toString()).row();
                td.image().color(getThemeColor()).fillX().row();
                td.table(ttd -> blocks.select(block1 -> block1.group == blockGroup).each(block1 -> {
                    ttd.button(block1.emoji(), Styles.cleart, () -> {
                        cons.get(block1);
                        if (autoHide) hide();
                    }).pad(3f).checked(b -> checked.get(block1)).size(50f);
                    if (ttd.getChildren().size % 10 == 0) ttd.row();
                }));
            });
        });
        addCloseButton();
    }
}
