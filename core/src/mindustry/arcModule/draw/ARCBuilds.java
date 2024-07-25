package mindustry.arcModule.draw;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.arcModule.ARCVars;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.MassDriver;

import static mindustry.Vars.*;
import static mindustry.arcModule.draw.ARCUnits.selectedUnitsFlyer;
import static mindustry.arcModule.draw.ARCUnits.selectedUnitsLand;

public class ARCBuilds {
    static boolean targetAir = false, targetGround = false, canShoot = false;
    static boolean turretForceShowRange = false;

    static int turretShowRange = 0, turretAlertRange;

    static boolean canHitPlayer = false, canHitCommand = false, canHitPlans = false, canHitMouse = false;

    static boolean showTurretAmmo = false, showTurretAmmoAmount = false;

    static boolean blockWeaponTargetLine = false, blockWeaponTargetLineWhenIdle = false;

    static int msLineAlpha = 0, msLineInterval = 0, msSlicer = 0;
    static boolean msShootingDraw;
    static String msLineColor = "";
    static Building target;
    static float msSin;

    static {
        // 减少性能开销
        Events.run(EventType.Trigger.update, () -> {
            turretForceShowRange = Core.settings.getBool("turretForceShowRange");
            turretShowRange = Core.settings.getInt("turretShowRange");

            turretAlertRange = Core.settings.getInt("turretAlertRange") * tilesize;

            showTurretAmmo = Core.settings.getBool("showTurretAmmo");
            showTurretAmmoAmount = Core.settings.getBool("showTurretAmmoAmount");

            blockWeaponTargetLine = Core.settings.getBool("blockWeaponTargetLine");
            blockWeaponTargetLineWhenIdle = Core.settings.getBool("blockWeaponTargetLineWhenIdle");

            msLineAlpha = Core.settings.getInt("msLineAlpha");
            msLineInterval = Core.settings.getInt("msLineInterval");
            msLineColor = Core.settings.getString("msLineColor");
            msShootingDraw = Core.settings.getBool("msShootingDraw");
            msSin = Mathf.absin(Time.time, 6f, 1f);
        });
    }

    public static void drawARCBuilds(Building build) {
        if (build == null || !ARCVars.arcInfoControl(build.team)) return;
        if (build instanceof BaseTurret.BaseTurretBuild builds) arcTurret(builds);
        else if (build instanceof MassDriver.MassDriverBuild builds && msLineAlpha > 0) arcMassDriver(builds);

    }

    private static void drawRange(BaseTurret.BaseTurretBuild build) {
        Draw.z(Layer.turret - 0.8f);
        Draw.color(build.team.color, 0.6f);
        Lines.circle(build.x, build.y, build.range());
        Draw.reset();
    }

    private static void arcTurret(BaseTurret.BaseTurretBuild build) {
        Draw.z(Layer.turret);

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

        if (turretForceShowRange || canShoot) {
            if ((turretShowRange == 3 || (turretShowRange == 2 && targetAir) || (turretShowRange == 1 && targetGround)))
                drawRange(build);
            else if (turretAlertRange > 0 && build.team != player.team()) {
                canHitPlayer = !player.unit().isNull() && player.unit().hittable() && (player.unit().isFlying() ? targetAir : targetGround)
                        && build.within(player.unit().x, player.unit().y, build.range() + turretAlertRange);
                canHitMouse = build.within(Core.input.mouseWorldX(), Core.input.mouseWorldY(), build.range() + turretAlertRange);
                canHitCommand = control.input.commandMode && ((selectedUnitsFlyer && targetAir) || (selectedUnitsLand && targetGround));
                canHitPlans = (control.input.block != null || control.input.selectPlans.size > 0) && targetGround;
                if (canHitPlayer || (canHitMouse && (canHitCommand || canHitPlans))) drawRange(build);
            }

            if (showTurretAmmo && build instanceof ItemTurret.ItemTurretBuild it && it.ammo.any()) {
                //lc参考miner代码
                ItemTurret.ItemEntry entry = (ItemTurret.ItemEntry) it.ammo.peek();
                Item lastAmmo = entry.item;

                Draw.z(Layer.turret + 0.1f);

                float size = Math.max(4f, build.block.size * tilesize / 2.5f);
                float ammoX = build.x - (build.block.size * tilesize / 2.0F) + (size / 2);
                float ammoY = build.y - (build.block.size * tilesize / 2.0F) + (size / 2);

                Draw.rect(lastAmmo.uiIcon, ammoX, ammoY, size, size);

                float leftAmmo = Mathf.lerp(0, 1, Math.min(1f, (float) entry.amount / ((ItemTurret) it.block).maxAmmo));
                if (leftAmmo < 0.75f && showTurretAmmoAmount) {
                    Draw.alpha(0.5f);
                    Draw.color(lastAmmo.color);
                    Lines.stroke(Lines.getStroke() * build.block.size * 0.5f);
                    Lines.arc(ammoX, ammoY, size * 0.5f, leftAmmo);
                }

                Draw.reset();
            }
            if (targetPos.x != 0 && targetPos.y != 0 && blockWeaponTargetLine && Mathf.len(targetPos.x - build.x, targetPos.y - build.y) <= 1500f) {
                if (!(build instanceof Turret.TurretBuild) || ((Turret.TurretBuild) build).isShooting() || ((Turret.TurretBuild) build).isControlled()) {
                    Draw.color(1f, 0.2f, 0.2f, 0.8f);
                    Lines.stroke(1.5f);
                    Lines.line(build.x, build.y, targetPos.x, targetPos.y);
                    Lines.dashCircle(targetPos.x, targetPos.y, 8);
                } else if (blockWeaponTargetLineWhenIdle) {
                    Draw.color(1f, 1f, 1f, 0.3f);
                    Lines.stroke(1.5f);
                    Lines.line(build.x, build.y, targetPos.x, targetPos.y);
                    Lines.dashCircle(targetPos.x, targetPos.y, 8);
                }
            }
        }
        Draw.reset();
    }

    private static void arcMassDriver(MassDriver.MassDriverBuild build) {
        target = world.build(build.link);
        if (target == null) return;
        if (build.state != MassDriver.DriverState.shooting && msShootingDraw) return;

        Draw.z(Layer.effect);

        Lines.stroke(2f, Pal.placing);

        Draw.alpha(msLineAlpha);
        Drawf.dashLine(msLineColor(), build.x, build.y, target.x, target.y);
        Draw.alpha(msLineAlpha);
        Drawf.arrow(build.x, build.y, target.x, target.y, build.block.size * tilesize + msSin, 4f + msSin, Pal.place);
        Tmp.v2.set(target.x - build.x, target.y - build.y);

        msSlicer = (int) (Tmp.v2.len() / msLineInterval);
        Tmp.v2.limit2(Tmp.v2.len() / (msSlicer + 1));
        for (int slicer = 1; slicer <= msSlicer; slicer++) {
            Draw.alpha(msLineAlpha);
            Drawf.arrow(build.x + Tmp.v2.x * slicer * tilesize, build.y + Tmp.v2.y * slicer * tilesize, target.x, target.y, build.block.size * tilesize + msSin, 4f + msSin, Pal.place);
        }

        Draw.reset();
    }

    public static Color msLineColor() {
        String color = Core.settings.getString("msLineColor");
        if (color != null && color.length() == 6) {
            for (char c : color.toCharArray()) {
                if (!Character.isDigit(c) && (!Character.isLetter(c) || (Character.toLowerCase(c) - 'a' > 'f' - 'a'))) {
                    return Color.valueOf("ff8c66");
                }
            }
            return Color.valueOf(color);
        }
        return Color.valueOf("ff8c66");
    }
}
