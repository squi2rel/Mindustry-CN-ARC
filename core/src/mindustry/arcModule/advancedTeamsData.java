package mindustry.arcModule;

import arc.graphics.Color;
import arc.struct.ObjectSet;
import mindustry.arcModule.ui.PowerInfo;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.type.UnitType;

import static mindustry.Vars.content;

public class advancedTeamsData {



    class advancedTeamData extends Team {
        protected advancedTeamData(int id, String name, Color color) {
            super(id, name, color);
        }

        PowerInfo powerStat = new PowerInfo();
        private final ObjectSet<Item> usedItems = new ObjectSet<>();
        private final ObjectSet<UnitType> usedUnits = new ObjectSet<>();
        private int[] updateItems = new int[content.items().size];
        private int[] lastItems = new int[content.items().size];



    }

}
