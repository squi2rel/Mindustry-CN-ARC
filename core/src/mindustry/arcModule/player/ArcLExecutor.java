package mindustry.arcModule.player;

import arc.math.Mathf;
import arc.util.Nullable;
import mindustry.ai.types.LogicAI;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.ctype.Content;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.logic.LExecutor;
import mindustry.logic.LUnitControl;
import mindustry.type.Item;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.meta.BuildVisibility;

import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;
/*
public class ArcLExecutor extends LExecutor {

    public static class PlayerControlI implements LInstruction{
        public LPlayerControl type = LPlayerControl.idle;
        public int p1, p2, p3, p4, p5;

        public PlayerControlI(LPlayerControl type, int p1, int p2, int p3, int p4, int p5){
            this.type = type;
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
            this.p4 = p4;
            this.p5 = p5;
        }

        public PlayerControlI(){
        }


        @Override
        public void run(LExecutor exec){
            float x1 = World.unconv(exec.numf(p1)), y1 = World.unconv(exec.numf(p2)), d1 = World.unconv(exec.numf(p3));

            switch(type){

                /**
                case idle, autoPathfind -> {
                    ai.control = type;
                }
                case move, stop, approach, pathfind -> {
                    ai.control = type;
                    ai.moveX = x1;
                    ai.moveY = y1;
                    if(type == LUnitControl.approach){
                        ai.moveRad = d1;
                    }

                    //stop mining/building
                    if(type == LUnitControl.stop){
                        unit.mineTile = null;
                        unit.clearBuilding();
                    }
                }
                case unbind -> {
                    //TODO is this a good idea? will allocate
                    unit.resetController();
                    exec.setobj(varUnit, null);
                }
                case within -> {
                    exec.setnum(p4, unit.within(x1, y1, d1) ? 1 : 0);
                }
                case target -> {
                    ai.posTarget.set(x1, y1);
                    ai.aimControl = type;
                    ai.mainTarget = null;
                    ai.shoot = exec.bool(p3);
                }
                case targetp -> {
                    ai.aimControl = type;
                    ai.mainTarget = exec.obj(p1) instanceof Teamc t ? t : null;
                    ai.shoot = exec.bool(p2);
                }
                case boost -> {
                    ai.boost = exec.bool(p1);
                }
                case flag -> {
                    unit.flag = exec.num(p1);
                }
                case mine -> {
                    Tile tile = world.tileWorld(x1, y1);
                    if(unit.canMine()){
                        unit.mineTile = unit.validMine(tile) ? tile : null;
                    }
                }
                case payDrop -> {
                    if(!exec.timeoutDone(unit, LogicAI.transferDelay)) return;

                    if(unit instanceof Payloadc pay && pay.hasPayload()){
                        Call.payloadDropped(unit, unit.x, unit.y);
                        exec.updateTimeout(unit);
                    }
                }
                case payTake -> {
                    if(!exec.timeoutDone(unit, LogicAI.transferDelay)) return;

                    if(unit instanceof Payloadc pay){
                        //units
                        if(exec.bool(p1)){
                            Unit result = Units.closest(unit.team, unit.x, unit.y, unit.type.hitSize * 2f, u -> u.isAI() && u.isGrounded() && pay.canPickup(u) && u.within(unit, u.hitSize + unit.hitSize * 1.2f));

                            if(result != null){
                                Call.pickedUnitPayload(unit, result);
                            }
                        }else{ //buildings
                            Building build = world.buildWorld(unit.x, unit.y);

                            //TODO copy pasted code
                            if(build != null && build.team == unit.team){
                                Payload current = build.getPayload();
                                if(current != null && pay.canPickupPayload(current)){
                                    Call.pickedBuildPayload(unit, build, false);
                                    //pick up whole building directly
                                }else if(build.block.buildVisibility != BuildVisibility.hidden && build.canPickup() && pay.canPickup(build)){
                                    Call.pickedBuildPayload(unit, build, true);
                                }
                            }
                        }
                        exec.updateTimeout(unit);
                    }
                }
                case payEnter -> {
                    Building build = world.buildWorld(unit.x, unit.y);
                    if(build != null && unit.team() == build.team && build.canControlSelect(unit)){
                        Call.unitBuildingControlSelect(unit, build);
                    }
                }
                case build -> {
                    if((state.rules.logicUnitBuild || exec.privileged) && unit.canBuild() && exec.obj(p3) instanceof Block block && block.canBeBuilt() && (block.unlockedNow() || unit.team.isAI())){
                        int x = World.toTile(x1 - block.offset/tilesize), y = World.toTile(y1 - block.offset/tilesize);
                        int rot = Mathf.mod(exec.numi(p4), 4);

                        //reset state of last request when necessary
                        if(ai.plan.x != x || ai.plan.y != y || ai.plan.block != block || unit.plans.isEmpty()){
                            ai.plan.progress = 0;
                            ai.plan.initialized = false;
                            ai.plan.stuck = false;
                        }

                        var conf = exec.obj(p5);
                        ai.plan.set(x, y, rot, block);
                        ai.plan.config = conf instanceof Content c ? c : conf instanceof Building b ? b : null;

                        unit.clearBuilding();
                        Tile tile = ai.plan.tile();

                        if(tile != null && !(tile.block() == block && tile.build != null && tile.build.rotation == rot)){
                            unit.updateBuilding = true;
                            unit.addBuild(ai.plan);
                        }
                    }
                }
                case getBlock -> {
                    float range = Math.max(unit.range(), unit.type.buildRange);
                    if(!unit.within(x1, y1, range)){
                        exec.setobj(p3, null);
                        exec.setobj(p4, null);
                        exec.setobj(p5, null);
                    }else{
                        Tile tile = world.tileWorld(x1, y1);
                        if(tile == null){
                            exec.setobj(p3, null);
                            exec.setobj(p4, null);
                            exec.setobj(p5, null);
                        }else{
                            //any environmental solid block is returned as StoneWall, aka "@solid"
                            Block block = !tile.synthetic() ? (tile.solid() ? Blocks.stoneWall : Blocks.air) : tile.block();
                            exec.setobj(p3, block);
                            exec.setobj(p4, tile.build != null ? tile.build : null);
                            //Allows reading of ore tiles if they are present (overlay is not air) otherwise returns the floor
                            exec.setobj(p5, tile.overlay() == Blocks.air ? tile.floor() : tile.overlay());
                        }
                    }
                }
                case itemDrop -> {
                    if(!exec.timeoutDone(unit, LogicAI.transferDelay)) return;

                    //clear item when dropping to @air
                    if(exec.obj(p1) == Blocks.air){
                        //only server-side; no need to call anything, as items are synced in snapshots
                        if(!net.client()){
                            unit.clearItem();
                        }
                        exec.updateTimeout(unit);
                    }else{
                        Building build = exec.building(p1);
                        int dropped = Math.min(unit.stack.amount, exec.numi(p2));
                        if(build != null && build.team == unit.team && build.isValid() && dropped > 0 && unit.within(build, logicItemTransferRange + build.block.size * tilesize/2f)){
                            int accepted = build.acceptStack(unit.item(), dropped, unit);
                            if(accepted > 0){
                                Call.transferItemTo(unit, unit.item(), accepted, unit.x, unit.y, build);
                                exec.updateTimeout(unit);
                            }
                        }
                    }
                }
                case itemTake -> {
                    if(!exec.timeoutDone(unit, LogicAI.transferDelay)) return;

                    Building build = exec.building(p1);
                    int amount = exec.numi(p3);

                    if(build != null && build.team == unit.team && build.isValid() && build.items != null &&
                            exec.obj(p2) instanceof Item item && unit.within(build, logicItemTransferRange + build.block.size * tilesize/2f)){
                        int taken = Math.min(build.items.get(item), Math.min(amount, unit.maxAccepted(item)));

                        if(taken > 0){
                            Call.takeItems(build, item, taken, unit);
                            exec.updateTimeout(unit);
                        }
                    }
                }
                default -> {}
            }
        }
    }

}*/
