package mindustry.world.blocks.defense.turrets;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.*;
import arc.math.geom.Vec2;
import arc.struct.*;
import arc.util.*;
import mindustry.arcModule.ARCVars;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.Item;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class BaseTurret extends Block{
    public float range = 80f;
    public float placeOverlapMargin = 8 * 7f;
    public float rotateSpeed = 5;
    public float fogRadiusMultiplier = 1f;

    /** Effect displayed when coolant is used. */
    public Effect coolEffect = Fx.fuelburn;
    /** How much reload is lowered by for each unit of liquid of heat capacity. */
    public float coolantMultiplier = 5f;
    /** If not null, this consumer will be used for coolant. */
    public @Nullable ConsumeLiquidBase coolant;

    public BaseTurret(String name){
        super(name);

        update = true;
        solid = true;
        outlineIcon = true;
        attacks = true;
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
    }

    @Override
    public void init(){
        if(coolant == null){
            coolant = findConsumer(c -> c instanceof ConsumeCoolant);
        }

        //just makes things a little more convenient
        if(coolant != null){
            //TODO coolant fix
            coolant.update = false;
            coolant.booster = true;
            coolant.optional = true;

            //json parsing does not add to consumes
            if(!hasConsumer(coolant)) consume(coolant);
        }

        placeOverlapRange = Math.max(placeOverlapRange, range + placeOverlapMargin);
        fogRadius = Math.max(Mathf.round(range / tilesize * fogRadiusMultiplier), fogRadius);
        super.init();
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, Pal.placing);
        if(state.rules.placeRangeCheck && Core.settings.getBool("arcTurretPlaceCheck")){
            Draw.alpha(0.5f);
            Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, placeOverlapRange, Pal.remove);
        }
        if(fogRadiusMultiplier < 0.99f && state.rules.fog){
            Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range * fogRadiusMultiplier, Pal.lightishGray);
        }

    }

    public void drawRange(BaseTurretBuild build) {
        if (build == null) return;
        Draw.z(Layer.turret);

        boolean targetAir = false, targetGround = false, canShoot = false;
        Vec2 targetPos = Vec2.ZERO;
        if (build.block instanceof Turret t) {
            targetAir = t.targetAir;
            targetGround = t.targetGround;
            targetPos = ((Turret.TurretBuild) build).targetPos;
            canShoot = ((Turret.TurretBuild) build).hasAmmo();
        } else if (build.block instanceof TractorBeamTurret t) {
            targetAir = t.targetAir;
            targetGround = t.targetGround;
            Unit target = ((TractorBeamTurret.TractorBeamBuild) build).target;
            if (target != null) {
                targetPos = Tmp.v1.set(target.x, target.y);
            }
            canShoot = build.potentialEfficiency > 0;
        }
        if (build instanceof PowerTurret.PowerTurretBuild) {
            canShoot = build.efficiency > 0;
        }
        if (Core.settings.getBool("turretForceShowRange")) {
            int turretShowRange = Core.settings.getInt("turretShowRange");
            if (turretShowRange == 3 || (turretShowRange == 2 && targetAir) || (turretShowRange == 1 && targetGround)) {
                Draw.z(Layer.turret - 0.8f);
                Draw.color(build.team.color, 0.05f);
                Fill.circle(build.x, build.y, build.range());
                Draw.color(build.team.color, 0.3f);
                Lines.circle(build.x, build.y, build.range());
            }
            Draw.reset();
        }
        if (canShoot && ARCVars.arcInfoControl(build.team)) {
            boolean canHitCommand = (control.input.block != null && targetGround) || (control.input.commandMode && (control.input.selectedUnits.size > 0));
            boolean turretAlert = Core.settings.getInt("turretAlertRange") > 0f &&
                    ((!player.unit().isNull() && player.unit().targetable(build.team)) || canHitCommand);
            if (turretAlert) {
                boolean canHitPlayer = player.unit().isFlying() ? targetAir : targetGround;
                boolean showHitPlayer = build.team != player.team() && canHitPlayer && (player.unit().dst(build.x, build.y) <= (build.range() + (float) Core.settings.getInt("turretAlertRange") * tilesize));
                boolean showHitCommand = build.team != player.team() && canHitCommand &&
                        Core.input.mouseWorld().dst(build.x, build.y) <= (build.range() + (float) Core.settings.getInt("turretAlertRange") * tilesize);

                if (showHitPlayer || showHitCommand) {
                    Draw.color(build.team.color, 0.8f);
                    Lines.circle(build.x, build.y, build.range());
                }
                int turretShowRange = Core.settings.getInt("turretShowRange");
                if (turretShowRange == 3 || (turretShowRange == 2 && targetAir) || (turretShowRange == 1 && targetGround)) {
                    Draw.z(Layer.turret - 0.8f);
                    Draw.color(build.team.color, 0.05f);
                    Fill.circle(build.x, build.y, build.range());
                    Draw.color(build.team.color, 0.3f);
                    Lines.circle(build.x, build.y, build.range());
                }
                Draw.reset();
            }
            if (Core.settings.getBool("showTurretAmmo") && build instanceof ItemTurret.ItemTurretBuild it && it.ammo.any()) {
                //lc参考miner代码
                ItemTurret.ItemEntry entry = (ItemTurret.ItemEntry) it.ammo.peek();
                Item lastAmmo = entry.item;

                Draw.z(Layer.turret + 0.1f);

                float size = Math.max(4f, build.block.size * tilesize / 2.5f);
                float ammoX = build.x - (build.block.size * tilesize / 2.0F) + (size / 2);
                float ammoY = build.y - (build.block.size * tilesize / 2.0F) + (size / 2);

                Draw.rect(lastAmmo.uiIcon, ammoX, ammoY, size, size);

                float leftAmmo = Mathf.lerp(0, 1, Math.min(1f, (float) entry.amount / ((ItemTurret) it.block).maxAmmo));
                if (leftAmmo < 0.75f && Core.settings.getBool("showTurretAmmoAmount")) {
                    Draw.alpha(0.5f);
                    Draw.color(lastAmmo.color);
                    Lines.stroke(Lines.getStroke() * build.block.size * 0.5f);
                    Lines.arc(ammoX, ammoY, size * 0.5f, leftAmmo);
                }

                Draw.reset();
            }
            if (targetPos.x != 0 && targetPos.y != 0 && Core.settings.getBool("blockWeaponTargetLine") && Mathf.len(targetPos.x - build.x, targetPos.y - build.y) <= 1500f) {
                if (!(build instanceof Turret.TurretBuild) || ((Turret.TurretBuild) build).isShooting() || ((Turret.TurretBuild) build).isControlled()) {
                    Draw.color(1f, 0.2f, 0.2f, 0.8f);
                    Lines.stroke(1.5f);
                    Lines.line(build.x, build.y, targetPos.x, targetPos.y);
                    Lines.dashCircle(targetPos.x, targetPos.y, 8);
                } else if (Core.settings.getBool("blockWeaponTargetLineWhenIdle")) {
                    Draw.color(1f, 1f, 1f, 0.3f);
                    Lines.stroke(1.5f);
                    Lines.line(build.x, build.y, targetPos.x, targetPos.y);
                    Lines.dashCircle(targetPos.x, targetPos.y, 8);
                }
            }

        }
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.shootRange, range / tilesize, StatUnit.blocks);
    }

    public class BaseTurretBuild extends Building implements Ranged{
        public float rotation = 90;

        @Override
        public float range(){
            return range;
        }

        @Override
        public void drawSelect(){
            Drawf.dashCircle(x, y, range(), team.color);
        }

        public float estimateDps(){
            return 0f;
        }
    }
}
