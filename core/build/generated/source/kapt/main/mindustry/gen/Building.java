package mindustry.gen;

import arc.*;
import arc.Core;
import arc.Graphics.Cursor.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;

import java.util.*;
import mindustry.*;
import mindustry.arcModule.ARCVars;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.EventType.*;
import mindustry.game.Teams.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.heat.HeatConductor.*;
import mindustry.world.blocks.logic.LogicBlock.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

import arc.Graphics;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Position;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.IntSet;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Nullable;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.annotations.Annotations;
import mindustry.audio.SoundLoop;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.PayloadSeq;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.meta.BlockStatus;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import mindustry.world.modules.PowerModule;

@SuppressWarnings("deprecation")
public class Building implements Buildingc, Entityc, Healthc, IndexableEntity__all, IndexableEntity__build, Posc, Teamc, Timerc {
  public static final EventType.BuildDamageEvent bulletDamageEvent = new BuildDamageEvent();

  public static final float hitDuration = 9.0F;

  public static final float recentDamageTime = 60.0F * 5.0F;

  public static int sleepingEntities = 0;

  public static final EventType.BuildTeamChangeEvent teamChangeEvent = new BuildTeamChangeEvent();

  public static final Seq<Building> tempBuilds = new Seq<>();

  public static final float timeToSleep = 60.0F * 1;

  public static final ObjectSet<Building> tmpTiles = new ObjectSet<>();

  protected transient boolean added;

  public transient Block block;

  public transient int cdump;

  public transient boolean dead;

  protected transient float dumpAccum;

  public transient float efficiency;

  public transient boolean enabled = true;

  public transient float healSuppressionTime = -1.0F;

  public float health;

  public transient float hitTime;

  public transient int id = EntityGroup.nextId();

  protected transient int index__all = -1;

  protected transient int index__build = -1;

  protected transient boolean initialized;

  @Nullable
  public ItemModule items;

  public transient String lastAccessed;

  protected transient float lastDamageTime = -recentDamageTime;

  @Nullable
  public transient Building lastDisabler;

  public transient float lastHealTime = -120.0F * 10.0F;

  @Nullable
  public transient Building lastLogicController;

  @Nullable
  public LiquidModule liquids;

  public transient float maxHealth = 1.0F;

  public transient float optionalEfficiency;

  public transient float payloadRotation;

  public transient float potentialEfficiency;

  @Nullable
  public PowerModule power;

  public transient Seq<Building> proximity = new Seq<>(6);

  public transient int rotation;

  protected transient float sleepTime;

  protected transient boolean sleeping;

  @Nullable
  protected transient SoundLoop sound;

  public Team team = Team.derelict;

  public transient Tile tile;

  protected transient float timeScale = 1.0F;

  protected transient float timeScaleDuration;

  public transient Interval timer = new Interval(6);

  public transient long visibleFlags;

  public transient float visualLiquid;

  public transient boolean wasDamaged;

  public transient boolean wasVisible;

  @Annotations.SyncField(true)
  @Annotations.SyncLocal
  public float x;

  @Annotations.SyncField(true)
  @Annotations.SyncLocal
  public float y;

  protected Building() {
  }

  @Override
  public Building lastDisabler() {
    return lastDisabler;
  }

  @Override
  public Building lastLogicController() {
    return lastLogicController;
  }

  @Override
  public Seq<Building> proximity() {
    return proximity;
  }

  @Override
  public Interval timer() {
    return timer;
  }

  @Override
  public boolean dead() {
    return dead;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public boolean wasDamaged() {
    return wasDamaged;
  }

  @Override
  public boolean wasVisible() {
    return wasVisible;
  }

  @Override
  public float efficiency() {
    return efficiency;
  }

  @Override
  public float healSuppressionTime() {
    return healSuppressionTime;
  }

  @Override
  public float health() {
    return health;
  }

  @Override
  public float hitTime() {
    return hitTime;
  }

  @Override
  public float lastHealTime() {
    return lastHealTime;
  }

  @Override
  public float maxHealth() {
    return maxHealth;
  }

  @Override
  public float optionalEfficiency() {
    return optionalEfficiency;
  }

  @Override
  public float payloadRotation() {
    return payloadRotation;
  }

  @Override
  public float potentialEfficiency() {
    return potentialEfficiency;
  }

  @Override
  public float visualLiquid() {
    return visualLiquid;
  }

  @Override
  public float x() {
    return x;
  }

  @Override
  public float y() {
    return y;
  }

  @Override
  public int cdump() {
    return cdump;
  }

  @Override
  public int classId() {
    return 6;
  }

  @Override
  public int id() {
    return id;
  }

  @Override
  public int rotation() {
    return rotation;
  }

  @Override
  public String lastAccessed() {
    return lastAccessed;
  }

  @Override
  public long visibleFlags() {
    return visibleFlags;
  }

  @Override
  public Team team() {
    return team;
  }

  @Override
  public Block block() {
    return block;
  }

  @Override
  public Tile tile() {
    return tile;
  }

  @Override
  public ItemModule items() {
    return items;
  }

  @Override
  public LiquidModule liquids() {
    return liquids;
  }

  @Override
  public PowerModule power() {
    return power;
  }

  @Override
  public void block(Block block) {
    this.block = block;
  }

  @Override
  public void cdump(int cdump) {
    this.cdump = cdump;
  }

  @Override
  public void dead(boolean dead) {
    this.dead = dead;
  }

  @Override
  public void efficiency(float efficiency) {
    this.efficiency = efficiency;
  }

  @Override
  public void enabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void healSuppressionTime(float healSuppressionTime) {
    this.healSuppressionTime = healSuppressionTime;
  }

  @Override
  public void health(float health) {
    this.health = health;
  }

  @Override
  public void hitTime(float hitTime) {
    this.hitTime = hitTime;
  }

  @Override
  public void id(int id) {
    this.id = id;
  }

  @Override
  public void items(ItemModule items) {
    this.items = items;
  }

  @Override
  public void lastAccessed(String lastAccessed) {
    this.lastAccessed = lastAccessed;
  }

  @Override
  public void lastDisabler(Building lastDisabler) {
    this.lastDisabler = lastDisabler;
  }

  @Override
  public void lastHealTime(float lastHealTime) {
    this.lastHealTime = lastHealTime;
  }

  @Override
  public void lastLogicController(Building lastLogicController) {
    this.lastLogicController = lastLogicController;
  }

  @Override
  public void liquids(LiquidModule liquids) {
    this.liquids = liquids;
  }

  @Override
  public void maxHealth(float maxHealth) {
    this.maxHealth = maxHealth;
  }

  @Override
  public void optionalEfficiency(float optionalEfficiency) {
    this.optionalEfficiency = optionalEfficiency;
  }

  @Override
  public void payloadRotation(float payloadRotation) {
    this.payloadRotation = payloadRotation;
  }

  @Override
  public void potentialEfficiency(float potentialEfficiency) {
    this.potentialEfficiency = potentialEfficiency;
  }

  @Override
  public void power(PowerModule power) {
    this.power = power;
  }

  @Override
  public void proximity(Seq<Building> proximity) {
    this.proximity = proximity;
  }

  @Override
  public void rotation(int rotation) {
    this.rotation = rotation;
  }

  @Override
  public void setIndex__all(int index) {
    index__all = index;
  }

  @Override
  public void setIndex__build(int index) {
    index__build = index;
  }

  @Override
  public void team(Team team) {
    this.team = team;
  }

  @Override
  public void tile(Tile tile) {
    this.tile = tile;
  }

  @Override
  public void timer(Interval timer) {
    this.timer = timer;
  }

  @Override
  public void visibleFlags(long visibleFlags) {
    this.visibleFlags = visibleFlags;
  }

  @Override
  public void visualLiquid(float visualLiquid) {
    this.visualLiquid = visualLiquid;
  }

  @Override
  public void wasDamaged(boolean wasDamaged) {
    this.wasDamaged = wasDamaged;
  }

  @Override
  public void wasVisible(boolean wasVisible) {
    this.wasVisible = wasVisible;
  }

  @Override
  public void x(float x) {
    this.x = x;
  }

  @Override
  public void y(float y) {
    this.y = y;
  }

  @Annotations.CallSuper
  public void placed() {

        if (net.client()) return;
        if ((block.consumesPower || block.outputsPower) && block.hasPower && block.connectedPower) {
            PowerNode.getNodeLinks(tile, block, team, (other)->{
                if (!other.power.links.contains(pos())) {
                    other.configureAny(pos());
                }
            });
        }
  }

  @Annotations.CallSuper
  public void playerPlaced(Object config) {

  }

  @Annotations.CallSuper
  public void read(Reads read, byte revision) {

  }

  @Annotations.CallSuper
  public void write(Writes write) {
    building: {

    }
    entity: {

    }
  }

  public <T extends Entityc> T self() {

        return (T)this;
  }

  public <T> T as() {

        return (T)this;
  }

  public Building back() {

        int trns = block.size / 2 + 1;
        return nearby(Geometry.d4(rotation + 2).x * trns, Geometry.d4(rotation + 2).y * trns);
  }

  public Building buildOn() {

        return world.buildWorld(x, y);
  }

  public Building create(Block block, Team team) {

        this.block = block;
        this.team = team;
        if (block.loopSound != Sounds.none) {
            sound = new SoundLoop(block.loopSound, block.loopSoundVolume);
        }
        health = block.health;
        maxHealth(block.health);
        timer(new Interval(block.timers));
        if (block.hasItems) items = new ItemModule();
        if (block.hasLiquids) liquids = new LiquidModule();
        if (block.hasPower) {
            power = new PowerModule();
            power.graph.add(this);
        }
        initialized = true;
        return this;
  }

  public Building front() {

        int trns = block.size / 2 + 1;
        return nearby(Geometry.d4(rotation).x * trns, Geometry.d4(rotation).y * trns);
  }

  public Building getLiquidDestination(Building from, Liquid liquid) {

        return this;
  }

  public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {

        if (!initialized) {
            create(tile.block(), team);
        } else {
            if (block.hasPower) {
                power.init = false;
                new PowerGraph().add(this);
            }
        }
        proximity.clear();
        this.rotation = rotation;
        this.tile = tile;
        set(tile.drawx(), tile.drawy());
        if (shouldAdd) {
            add();
        }
        created();
        return this;
  }

  public Building left() {

        int trns = block.size / 2 + 1;
        return nearby(Geometry.d4(rotation + 1).x * trns, Geometry.d4(rotation + 1).y * trns);
  }

  public Building nearby(int dx, int dy) {

        return world.build(tile.x + dx, tile.y + dy);
  }

  public Building nearby(int rotation) {

        return switch (rotation) {
        case 0 ->world.build(tile.x + 1, tile.y);
        case 1 ->world.build(tile.x, tile.y + 1);
        case 2 ->world.build(tile.x - 1, tile.y);
        case 3 ->world.build(tile.x, tile.y - 1);
        default ->null;
        };
  }

  public Building right() {

        int trns = block.size / 2 + 1;
        return nearby(Geometry.d4(rotation + 3).x * trns, Geometry.d4(rotation + 3).y * trns);
  }

  public Graphics.Cursor getCursor() {

        if (Core.settings.getBool("showOtherTeamState")) {
            return block.configurable ? SystemCursor.hand : SystemCursor.arrow;
        } else {
            return block.configurable && interactable(player.team()) ? SystemCursor.hand : SystemCursor.arrow;
        }
  }

  public TextureRegion getDisplayIcon() {

        return block.uiIcon;
  }

  public Vec2 getCommandPosition() {

        return null;
  }

  public Seq<Building> getPowerConnections(Seq<Building> out) {

        out.clear();
        if (power == null) return out;
        for (Building other : proximity) {
            if (other != null && other.power != null && other.team == team && !(block.consumesPower && other.block.consumesPower && !block.outputsPower && !other.block.outputsPower && !block.conductivePower && !other.block.conductivePower) && conductsTo(other) && other.conductsTo(this) && !power.links.contains(other.pos())) {
                out.add(other);
            }
        }
        for (int i = 0; i < power.links.size; i++) {
            Tile link = world.tile(power.links.get(i));
            if (link != null && link.build != null && link.build.power != null && link.build.team == team) out.add(link.build);
        }
        return out;
  }

  public boolean absorbLasers() {

        return block.absorbLasers;
  }

  public boolean acceptItem(Building source, Item item) {

        return block.consumesItem(item) && items.get(item) < getMaximumAccepted(item);
  }

  public boolean acceptLiquid(Building source, Liquid liquid) {

        return block.hasLiquids && block.consumesLiquid(liquid);
  }

  public boolean acceptPayload(Building source, Payload payload) {

        return false;
  }

  public boolean allowUpdate() {

        return team != Team.derelict && block.supportsEnv(state.rules.env) && (!state.rules.limitMapArea || !state.rules.disableOutsideArea || Rect.contains(state.rules.limitX, state.rules.limitY, state.rules.limitWidth, state.rules.limitHeight, tile.x, tile.y));
  }

  public boolean canConsume() {

        return potentialEfficiency > 0;
  }

  public boolean canControlSelect(Unit player) {

        return false;
  }

  public boolean canDump(Building to, Item item) {

        return true;
  }

  public boolean canDumpLiquid(Building to, Liquid liquid) {

        return true;
  }

  public boolean canPickup() {

        return true;
  }

  public boolean canResupply() {

        return block.allowResupply;
  }

  public boolean canUnload() {

        return block.unloadable;
  }

  public boolean canWithdraw() {

        return true;
  }

  public boolean cheating() {

        return team.rules().cheat;
  }

  public boolean checkSolid() {

        return false;
  }

  public boolean checkSuppression() {

        if (isHealSuppressed()) {
            if (Mathf.chanceDelta(0.03)) {
                Fx.regenSuppressParticle.at(x + Mathf.range(block.size * tilesize / 2.0F - 1.0F), y + Mathf.range(block.size * tilesize / 2.0F - 1.0F));
            }
            return true;
        }
        return false;
  }

  public boolean collide(Bullet other) {

        return true;
  }

  public boolean collision(Bullet other) {

        boolean wasDead = health <= 0;
        float damage = other.damage() * other.type().buildingDamageMultiplier;
        if (!other.type.pierceArmor) {
            damage = Damage.applyArmor(damage, block.armor);
        }
        damage(other.team, damage);
        Events.fire(bulletDamageEvent.set(this, other));
        if (health <= 0 && !wasDead) {
            Events.fire(new BuildingBulletDestroyEvent(this, other));
        }
        return true;
  }

  public boolean conductsTo(Building other) {

        return !block.insulated;
  }

  public boolean configTapped() {

        return true;
  }

  public boolean consumeTriggerValid() {

        return false;
  }

  public boolean damaged() {

        return health < maxHealth - 0.001F;
  }

  public boolean dump() {

        return dump(null);
  }

  public boolean dump(Item todump) {

        if (!block.hasItems || items.total() == 0 || proximity.size == 0 || (todump != null && !items.has(todump))) return false;
        int dump = this.cdump;
        var allItems = content.items();
        int itemSize = allItems.size;
        Object[] itemArray = allItems.items;
        for (int i = 0; i < proximity.size; i++) {
            Building other = proximity.get((i + dump) % proximity.size);
            if (todump == null) {
                for (int ii = 0; ii < itemSize; ii++) {
                    if (!items.has(ii)) continue;
                    Item item = (Item)itemArray[ii];
                    if (other.acceptItem(this, item) && canDump(other, item)) {
                        other.handleItem(this, item);
                        items.remove(item, 1);
                        incrementDump(proximity.size);
                        return true;
                    }
                }
            } else {
                if (other.acceptItem(this, todump) && canDump(other, todump)) {
                    other.handleItem(this, todump);
                    items.remove(todump, 1);
                    incrementDump(proximity.size);
                    return true;
                }
            }
            incrementDump(proximity.size);
        }
        return false;
  }

  public boolean dumpAccumulate() {

        return dumpAccumulate(null);
  }

  public boolean dumpAccumulate(Item item) {

        boolean res = false;
        dumpAccum += delta();
        while (dumpAccum >= 1.0F) {
            res |= dump(item);
            dumpAccum -= 1.0F;
        }
        return res;
  }

  public boolean dumpPayload(Payload todump) {

        if (proximity.size == 0) return false;
        int dump = this.cdump;
        for (int i = 0; i < proximity.size; i++) {
            Building other = proximity.get((i + dump) % proximity.size);
            if (other.acceptPayload(this, todump)) {
                other.handlePayload(this, todump);
                incrementDump(proximity.size);
                return true;
            }
            incrementDump(proximity.size);
        }
        return false;
  }

  public boolean inFogTo(Team viewer) {

        if (team == viewer || !state.rules.fog) return false;
        int size = block.size;
        int of = block.sizeOffset;
        int tx = tile.x;
        int ty = tile.y;
        if (!isDiscovered(viewer)) return true;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (fogControl.isVisibleTile(viewer, tx + x + of, ty + y + of)) {
                    return false;
                }
            }
        }
        return true;
  }

  public boolean interactable(Team team) {

        return state.teams.canInteract(team, team());
  }

  public boolean isAdded() {

        return added;
  }

  public boolean isDiscovered(Team viewer) {

        if (state.rules.limitMapArea && world.getDarkness(tile.x, tile.y) >= 3) {
            return false;
        }
        if (viewer == null || !state.rules.staticFog || !state.rules.fog) {
            return true;
        }
        if (block.size <= 2) {
            return fogControl.isDiscovered(viewer, tile.x, tile.y);
        } else {
            int s = block.size / 2;
            return fogControl.isDiscovered(viewer, tile.x, tile.y) || fogControl.isDiscovered(viewer, tile.x - s, tile.y - s) || fogControl.isDiscovered(viewer, tile.x - s, tile.y + s) || fogControl.isDiscovered(viewer, tile.x + s, tile.y + s) || fogControl.isDiscovered(viewer, tile.x + s, tile.y - s);
        }
  }

  public boolean isHealSuppressed() {

        return block.suppressable && Time.time <= healSuppressionTime;
  }

  public boolean isInsulated() {

        return block.insulated;
  }

  public boolean isLocal() {

        return ((Object)this) == player || ((Object)this) instanceof Unitc u && u.controller() == player;
  }

  public boolean isNull() {

        return false;
  }

  public boolean isPayload() {

        return tile == emptyTile;
  }

  public boolean isRemote() {

        return ((Object)this) instanceof Unitc u && u.isPlayer() && !isLocal();
  }

  public boolean isValid() {

        return tile.build == this && !dead();
  }

  public boolean moveForward(Item item) {

        Building other = front();
        if (other != null && other.team == team && other.acceptItem(this, item)) {
            other.handleItem(this, item);
            return true;
        }
        return false;
  }

  public boolean movePayload(Payload todump) {

        int trns = block.size / 2 + 1;
        Tile next = tile.nearby(Geometry.d4(rotation).x * trns, Geometry.d4(rotation).y * trns);
        if (next != null && next.build != null && next.build.team == team && next.build.acceptPayload(this, todump)) {
            next.build.handlePayload(this, todump);
            return true;
        }
        return false;
  }

  public boolean onConfigureBuildTapped(Building other) {

        if (block.clearOnDoubleTap) {
            if (this == other) {
                deselect();
                configure(null);
                return false;
            }
            return true;
        }
        return this != other;
  }

  public boolean onConfigureTapped(float x, float y) {

        return false;
  }

  public boolean onSolid() {

        Tile tile = tileOn();
        return tile == null || tile.solid();
  }

  public boolean payloadCheck(int conveyorRotation) {

        return block.rotate && (rotation + 2) % 4 == conveyorRotation;
  }

  public boolean productionValid() {

        return true;
  }

  public boolean put(Item item) {

        int dump = this.cdump;
        for (int i = 0; i < proximity.size; i++) {
            incrementDump(proximity.size);
            Building other = proximity.get((i + dump) % proximity.size);
            if (other.acceptItem(this, item) && canDump(other, item)) {
                other.handleItem(this, item);
                return true;
            }
        }
        return false;
  }

  public boolean serialize() {
    return false;
  }

  public boolean shouldActiveSound() {

        return false;
  }

  public boolean shouldAmbientSound() {

        return shouldConsume();
  }

  public boolean shouldConsume() {

        return enabled;
  }

  public boolean shouldHideConfigure(Player player) {

        return false;
  }

  public boolean shouldShowConfigure(Player player) {

        return true;
  }

  public boolean timer(int index, float time) {

        if (Float.isInfinite(time)) return false;
        return timer.get(index, time);
  }

  public boolean wasRecentlyDamaged() {

        return lastDamageTime + recentDamageTime >= Time.time;
  }

  public boolean wasRecentlyHealed(float duration) {

        return lastHealTime + duration >= Time.time;
  }

  public byte relativeTo(Building build) {

        if (Math.abs(x - build.x) > Math.abs(y - build.y)) {
            if (x <= build.x - 1) return 0;
            if (x >= build.x + 1) return 2;
        } else {
            if (y <= build.y - 1) return 1;
            if (y >= build.y + 1) return 3;
        }
        return -1;
  }

  public byte relativeTo(int cx, int cy) {

        return tile.absoluteRelativeTo(cx, cy);
  }

  public byte relativeTo(Tile tile) {

        return relativeTo(tile.x, tile.y);
  }

  public byte relativeToEdge(Tile other) {

        return relativeTo(Edges.getFacingEdge(other, tile));
  }

  public byte version() {

        return 0;
  }

  public double sense(Content content) {

        if (content instanceof Item i && items != null) return items.get(i);
        if (content instanceof Liquid l && liquids != null) return liquids.get(l);
        return Float.NaN;
  }

  public double sense(LAccess sensor) {

        return switch (sensor) {
        case x ->World.conv(x);
        case y ->World.conv(y);
        case color ->Color.toDoubleBits(team.color.r, team.color.g, team.color.b, 1.0F);
        case dead ->!isValid() ? 1 : 0;
        case team ->team.id;
        case health ->health;
        case maxHealth ->maxHealth;
        case efficiency ->efficiency;
        case timescale ->timeScale;
        case range ->this instanceof Ranged r ? r.range() / tilesize : 0;
        case rotation ->rotation;
        case totalItems ->items == null ? 0 : items.total();
        case totalLiquids ->liquids == null ? 0 : liquids.currentAmount();
        case totalPower ->power == null || block.consPower == null ? 0 : power.status * (block.consPower.buffered ? block.consPower.capacity : 1.0F);
        case itemCapacity ->block.hasItems ? block.itemCapacity : 0;
        case liquidCapacity ->block.hasLiquids ? block.liquidCapacity : 0;
        case powerCapacity ->block.consPower != null ? block.consPower.capacity : 0.0F;
        case powerNetIn ->power == null ? 0 : power.graph.getLastScaledPowerIn() * 60;
        case powerNetOut ->power == null ? 0 : power.graph.getLastScaledPowerOut() * 60;
        case powerNetStored ->power == null ? 0 : power.graph.getLastPowerStored();
        case powerNetCapacity ->power == null ? 0 : power.graph.getLastCapacity();
        case enabled ->enabled ? 1 : 0;
        case controlled ->this instanceof ControlBlock c && c.isControlled() ? GlobalVars.ctrlPlayer : 0;
        case payloadCount ->getPayload() != null ? 1 : 0;
        case size ->block.size;
        default ->Float.NaN;
        };
  }

  public float activeSoundVolume() {

        return 1.0F;
  }

  public float ambientVolume() {

        return efficiency;
  }

  public float calculateHeat(float[] sideHeat) {

        return calculateHeat(sideHeat, null);
  }

  public float calculateHeat(float[] sideHeat, IntSet cameFrom) {

        Arrays.fill(sideHeat, 0.0F);
        if (cameFrom != null) cameFrom.clear();
        float heat = 0.0F;
        for (var edge : block.getEdges()) {
            Building build = nearby(edge.x, edge.y);
            if (build != null && build.team == team && build instanceof HeatBlock heater) {
                if (heater instanceof HeatConductorBuild cond) {
                    cond.updateHeat();
                }
                boolean split = build.block instanceof HeatConductor cond && cond.splitHeat;
                if (!build.block.rotate || (!split && (relativeTo(build) + 2) % 4 == build.rotation) || (split && relativeTo(build) != build.rotation)) {
                    if (!(build instanceof HeatConductorBuild hc && hc.cameFrom.contains(id()))) {
                        float add = heater.heat() / build.block.size;
                        if (split) {
                            add /= 3.0F;
                        }
                        sideHeat[Mathf.mod(relativeTo(build), 4)] += add;
                        heat += add;
                    }
                    if (cameFrom != null) {
                        cameFrom.add(build.id);
                        if (build instanceof HeatConductorBuild hc) {
                            cameFrom.addAll(hc.cameFrom);
                        }
                    }
                }
            }
        }
        return heat;
  }

  public float delta() {

        return Time.delta * timeScale;
  }

  public float drawrot() {

        return block.rotate && block.rotateDraw ? rotation * 90 : 0.0F;
  }

  public float edelta() {

        return efficiency * delta();
  }

  public float efficiencyScale() {

        return 1.0F;
  }

  public float fogRadius() {

        return block.fogRadius;
  }

  public float getDisplayEfficiency() {

        return getProgressIncrease(1.0F) / edelta();
  }

  public float getPowerProduction() {

        return 0.0F;
  }

  public float getProgressIncrease(float baseTime) {

        return 1.0F / baseTime * edelta();
  }

  public float getX() {

        return x;
  }

  public float getY() {

        return y;
  }

  public float handleDamage(float amount) {

        return amount;
  }

  public float healthf() {

        return health / maxHealth;
  }

  public float hitSize() {

        return tile.block().size * tilesize;
  }

  public float moveLiquid(Building next, Liquid liquid) {

        if (next == null) return 0;
        next = next.getLiquidDestination(this, liquid);
        if (next.team == team && next.block.hasLiquids && liquids.get(liquid) > 0.0F) {
            float ofract = next.liquids.get(liquid) / next.block.liquidCapacity;
            float fract = liquids.get(liquid) / block.liquidCapacity * block.liquidPressure;
            float flow = Math.min(Mathf.clamp((fract - ofract)) * (block.liquidCapacity), liquids.get(liquid));
            flow = Math.min(flow, next.block.liquidCapacity - next.liquids.get(liquid));
            if (flow > 0.0F && ofract <= fract && next.acceptLiquid(this, liquid)) {
                next.handleLiquid(this, liquid, flow);
                liquids.remove(liquid, flow);
                return flow;
            } else if (!next.block.consumesLiquid(liquid) && next.liquids.currentAmount() / next.block.liquidCapacity > 0.1F && fract > 0.1F) {
                float fx = (x + next.x) / 2.0F;
                float fy = (y + next.y) / 2.0F;
                Liquid other = next.liquids.current();
                if (other.blockReactive && liquid.blockReactive) {
                    if ((other.flammability > 0.3F && liquid.temperature > 0.7F) || (liquid.flammability > 0.3F && other.temperature > 0.7F)) {
                        damageContinuous(1);
                        next.damageContinuous(1);
                        if (Mathf.chanceDelta(0.1)) {
                            Fx.fire.at(fx, fy);
                        }
                    } else if ((liquid.temperature > 0.7F && other.temperature < 0.55F) || (other.temperature > 0.7F && liquid.temperature < 0.55F)) {
                        liquids.remove(liquid, Math.min(liquids.get(liquid), 0.7F * Time.delta));
                        if (Mathf.chanceDelta(0.2F)) {
                            Fx.steam.at(fx, fy);
                        }
                    }
                }
            }
        }
        return 0;
  }

  public float moveLiquidForward(boolean leaks, Liquid liquid) {

        Tile next = tile.nearby(rotation);
        if (next == null) return 0;
        if (next.build != null) {
            return moveLiquid(next.build, liquid);
        } else if (leaks && !next.block().solid && !next.block().hasLiquids) {
            float leakAmount = liquids.get(liquid) / 1.5F;
            Puddles.deposit(next, tile, liquid, leakAmount, true, true);
            liquids.remove(liquid, leakAmount);
        }
        return 0;
  }

  public float progress() {

        return 0.0F;
  }

  public float rotdeg() {

        return rotation * 90;
  }

  public float timeScale() {

        return timeScale;
  }

  public float totalProgress() {

        return Time.time;
  }

  public float warmup() {

        return 0.0F;
  }

  public int acceptStack(Item item, int amount, Teamc source) {

        if (acceptItem(this, item) && block.hasItems && (source == null || source.team() == team)) {
            return Math.min(getMaximumAccepted(item) - items.get(item), amount);
        } else {
            return 0;
        }
  }

  public int explosionItemCap() {

        return block.itemCapacity;
  }

  public int getMaximumAccepted(Item item) {

        return block.itemCapacity;
  }

  public int moduleBitmask() {

        return (items != null ? 1 : 0) | (power != null ? 2 : 0) | (liquids != null ? 4 : 0) | 8;
  }

  public int pos() {

        return tile.pos();
  }

  public int removeStack(Item item, int amount) {

        if (items == null) return 0;
        amount = Math.min(amount, items.get(item));
        noSleep();
        items.remove(item, amount);
        return amount;
  }

  public int tileX() {

        return tile.x;
  }

  public int tileY() {

        return tile.y;
  }

  public Object config() {

        return null;
  }

  public Object senseObject(LAccess sensor) {

        return switch (sensor) {
        case type ->block;
        case firstItem ->items == null ? null : items.first();
        case config ->block.configSenseable() ? config() : null;
        case payloadType ->getPayload() instanceof UnitPayload p1 ? p1.unit.type : getPayload() instanceof BuildPayload p2 ? p2.block() : null;
        default ->noSensed;
        };
  }

  public String getDisplayName() {

        return team == Team.derelict ? block.localizedName + "\n" + Core.bundle.get("block.derelict") : "[#" + team.color + "]" + (Core.settings.getBool("colorizedContent") && block.localizedName.length() > 11 ? block.localizedName.substring(11) : block.localizedName) + (team == player.team() || team.emoji.isEmpty() ? "" : " " + team.emoji + (team.id > 5 ? "[" + team.id + "]" : ""));
  }

  public String toString() {

        return "Building#" + id() + "[" + tileX() + "," + tileY() + "]:" + block;
  }

  public PayloadSeq getPayloads() {

        return null;
  }

  public Block blockOn() {

        Tile tile = tileOn();
        return tile == null ? Blocks.air : tile.block();
  }

  public Tile findClosestEdge(Position to, Boolf<Tile> solid) {

        Tile best = null;
        float mindst = 0.0F;
        for (var point : Edges.getEdges(block.size)) {
            Tile other = Vars.world.tile(tile.x + point.x, tile.y + point.y);
            if (other != null && !solid.get(other) && (best == null || to.dst2(other) < mindst)) {
                best = other;
                mindst = other.dst2(other);
            }
        }
        return best;
  }

  public Tile tileOn() {

        return world.tileWorld(x, y);
  }

  public Floor floor() {

        return tile.floor();
  }

  public Floor floorOn() {

        Tile tile = tileOn();
        return tile == null || tile.block() != Blocks.air ? (Floor)Blocks.air : tile.floor();
  }

  public Payload getPayload() {

        return null;
  }

  public Payload takePayload() {

        return null;
  }

  public CoreBlock.CoreBuild closestCore() {

        return state.teams.closestCore(x, y, team);
  }

  public CoreBlock.CoreBuild closestEnemyCore() {

        return state.teams.closestEnemyCore(x, y, team);
  }

  public CoreBlock.CoreBuild core() {

        return team.core();
  }

  public BlockStatus status() {

        if (!enabled) {
            return BlockStatus.logicDisable;
        }
        if (!shouldConsume()) {
            return BlockStatus.noOutput;
        }
        if (efficiency <= 0 || !productionValid()) {
            return BlockStatus.noInput;
        }
        return ((state.tick / 30.0F) % 1.0F) < efficiency ? BlockStatus.active : BlockStatus.noInput;
  }

  public ItemModule flowItems() {

        return items;
  }

  public static Building create() {
    return new Building();
  }

  public void add() {
    if(added == true) return;
    index__all = Groups.all.addIndex(this);
    index__build = Groups.build.addIndex(this);
    building: {

        if (power != null) {
            power.graph.checkAdd();
        }
    }
    entity: {

        added = true;
    }
  }

  public void addPlan(boolean checkPrevious) {

        addPlan(checkPrevious, false);
  }

  public void addPlan(boolean checkPrevious, boolean ignoreConditions) {

        if (!ignoreConditions && (!block.rebuildable || (team == state.rules.defaultTeam && state.isCampaign() && !block.isVisible()))) return;
        Object overrideConfig = null;
        Block toAdd = this.block;
        if (this instanceof ConstructBuild entity) {
            if (entity.current != null && entity.current.synthetic() && entity.wasConstructing) {
                toAdd = entity.current;
                overrideConfig = entity.lastConfig;
            } else {
                return;
            }
        }
        TeamData data = team.data();
        if (checkPrevious) {
            for (int i = 0; i < data.plans.size; i++) {
                BlockPlan b = data.plans.get(i);
                if (b.x == tile.x && b.y == tile.y) {
                    data.plans.removeIndex(i);
                    break;
                }
            }
        }
        data.plans.addFirst(new BlockPlan(tile.x, tile.y, (short)rotation, toAdd.id, overrideConfig == null ? config() : overrideConfig));
  }

  public void afterDestroyed() {

        if (block.destroyBullet != null) {
            block.destroyBullet.create(this, block.destroyBulletSameTeam ? team : Team.derelict, x, y, Mathf.randomSeed(id(), 360.0F));
        }
  }

  public void afterPickedUp() {

        if (power != null) {
            power.graph = new PowerGraph();
            power.links.clear();
            if (block.consPower != null && !block.consPower.buffered) {
                power.status = 0.0F;
            }
        }
  }

  public void afterRead() {

  }

  public void applyBoost(float intensity, float duration) {

        if (intensity >= this.timeScale - 0.001F) {
            timeScaleDuration = Math.max(timeScaleDuration, duration);
        }
        timeScale = Math.max(timeScale, intensity);
  }

  public void applyHealSuppression(float amount) {

        healSuppressionTime = Math.max(healSuppressionTime, Time.time + amount);
  }

  public void applySlowdown(float intensity, float duration) {

        if (intensity <= this.timeScale - 0.001F) {
            timeScaleDuration = Math.max(timeScaleDuration, duration);
        }
        timeScale = Math.min(timeScale, intensity);
  }

  public void buildConfiguration(Table table) {

  }

  public void changeTeam(Team next) {

        if (this.team == next) return;
        Team last = this.team;
        boolean was = isValid();
        if (was) indexer.removeIndex(tile);
        this.team = next;
        if (was) {
            indexer.addIndex(tile);
            Events.fire(teamChangeEvent.set(last, this));
        }
  }

  public void clampHealth() {

        health = Math.min(health, maxHealth);
  }

  public void configure(Object value) {

        block.lastConfig = value;
        Call.tileConfig(player, this, value);
  }

  public void configureAny(Object value) {

        Call.tileConfig(null, this, value);
  }

  public void configured(Unit builder, Object value) {

        Class<?> type = value == null ? void.class : value.getClass().isAnonymousClass() ? value.getClass().getSuperclass() : value.getClass();
        if (value instanceof Item) type = Item.class;
        if (value instanceof Block) type = Block.class;
        if (value instanceof Liquid) type = Liquid.class;
        if (value instanceof UnitType) type = UnitType.class;
        if (builder != null && builder.isPlayer()) {
            lastAccessed = builder.getPlayer().coloredName();
        }
        if (block.configurations.containsKey(type)) {
            block.configurations.get(type).get(this, value);
        } else if (value instanceof Building build) {
            var conf = build.config();
            if (conf != null && !(conf instanceof Building)) {
                configured(builder, conf);
            }
        }
  }

  public void consume() {

        for (Consume cons : block.consumers) {
            cons.trigger(this);
        }
  }

  public void control(LAccess type, double p1, double p2, double p3, double p4) {

        if (type == LAccess.enabled) {
            enabled = !Mathf.zero((float)p1);
        }
  }

  public void control(LAccess type, Object p1, double p2, double p3, double p4) {

        if (type == LAccess.config && block.logicConfigurable && !(p1 instanceof LogicBuild)) {
            configured(null, p1);
        }
  }

  public void created() {

  }

  public void damage(Bullet bullet, Team source, float damage) {

        damage(source, damage);
        Events.fire(bulletDamageEvent.set(this, bullet));
  }

  public void damage(float amount, boolean withEffect) {

        float pre = hitTime;
        damage(amount);
        if (!withEffect) {
            hitTime = pre;
        }
  }

  public void damage(float damage) {

        if (dead()) return;
        float dm = state.rules.blockHealth(team);
        lastDamageTime = Time.time;
        if (Mathf.zero(dm)) {
            damage = health + 1;
        } else {
            damage /= dm;
        }
        if (!net.client()) {
            health -= handleDamage(damage);
        }
        healthChanged();
        if (health <= 0) {
            Call.buildDestroyed(this);
        }
  }

  public void damage(Team source, float damage) {

        damage(damage);
  }

  public void damageContinuous(float amount) {

        damage(amount * Time.delta, hitTime <= -10 + hitDuration);
  }

  public void damageContinuousPierce(float amount) {

        damagePierce(amount * Time.delta, hitTime <= -20 + hitDuration);
  }

  public void damagePierce(float amount) {

        damagePierce(amount, true);
  }

  public void damagePierce(float amount, boolean withEffect) {

        damage(amount, withEffect);
  }

  public void deselect() {

        if (!headless && control.input.config.getSelected() == this) {
            control.input.config.hideConfig();
        }
  }

  public void display(Table table) {

        table.table((t)->{
            t.left();
            t.add(new Image(block.getDisplayIcon(tile))).size(8 * 4);
            t.labelWrap(block.getDisplayName(tile)).left().width(190.0F).padLeft(5);
        }).growX().left();
        table.row();
        if (ARCVars.arcInfoControl(team)) {
            table.table((bars)->{
                bars.defaults().growX().height(18.0F).pad(4);
                displayBars(bars);
            }).growX();
            table.row();
            table.table(this::displayConsumption).growX();
            boolean displayFlow = (block.category == Category.distribution || block.category == Category.liquid) && block.displayFlow;
            if (displayFlow) {
                String ps = " " + StatUnit.perSecond.localized();
                var flowItems = flowItems();
                if (flowItems != null) {
                    table.row();
                    table.left();
                    table.table((l)->{
                        Bits current = new Bits();
                        Runnable rebuild = ()->{
                            l.clearChildren();
                            l.left();
                            for (Item item : content.items()) {
                                if (flowItems.hasFlowItem(item)) {
                                    l.image(item.uiIcon).scaling(Scaling.fit).padRight(3.0F);
                                    l.label(()->flowItems.getFlowRate(item) < 0 ? "..." : Strings.fixed(flowItems.getFlowRate(item), 1) + ps).color(Color.lightGray);
                                    l.row();
                                }
                            }
                        };
                        rebuild.run();
                        l.update(()->{
                            for (Item item : content.items()) {
                                if (flowItems.hasFlowItem(item) && !current.get(item.id)) {
                                    current.set(item.id);
                                    rebuild.run();
                                }
                            }
                        });
                    }).left();
                }
                if (liquids != null) {
                    table.row();
                    table.left();
                    table.table((l)->{
                        Bits current = new Bits();
                        Runnable rebuild = ()->{
                            l.clearChildren();
                            l.left();
                            for (var liquid : content.liquids()) {
                                if (liquids.hasFlowLiquid(liquid)) {
                                    l.image(liquid.uiIcon).scaling(Scaling.fit).size(32.0F).padRight(3.0F);
                                    l.label(()->liquids.getFlowRate(liquid) < 0 ? "..." : Strings.fixed(liquids.getFlowRate(liquid), 1) + ps).color(Color.lightGray);
                                    l.row();
                                }
                            }
                        };
                        rebuild.run();
                        l.update(()->{
                            for (var liquid : content.liquids()) {
                                if (liquids.hasFlowLiquid(liquid) && !current.get(liquid.id)) {
                                    current.set(liquid.id);
                                    rebuild.run();
                                }
                            }
                        });
                    }).left();
                }
            }
            if (net.active() && lastAccessed != null) {
                table.row();
                table.add(Core.bundle.format("lastaccessed", lastAccessed)).growX().wrap().left();
            }
            table.marginBottom(-5);
        }
  }

  public void displayBars(Table table) {

        for (Func<Building, Bar> bar : block.listBars()) {
            var result = bar.get(this);
            if (result == null) continue;
            table.add(result).growX();
            table.row();
        }
        if (lastLogicController != null) {
            table.add(lastLogicController.block.emoji() + " [lightgray](" + lastLogicController.tileX() + ", " + lastLogicController.tileY() + ")").growX().left().row();
        }
        if (Time.time < healSuppressionTime) {
            table.add("\uf89b[red]\ue815").update((label)->{
                if (healSuppressionTime > 0) label.setText("\uf89b[red]\ue815 [white]~ " + UI.formatTime(healSuppressionTime - Time.time)); else label.visible = false;
            }).row();
        }
  }

  public void displayConsumption(Table table) {

        table.left();
        for (Consume cons : block.consumers) {
            if (cons.optional && cons.booster) continue;
            cons.build(this, table);
        }
  }

  public void draw() {

        if (block.variants == 0 || block.variantRegions == null) {
            Draw.rect(block.region, x, y, drawrot());
        } else {
            Draw.rect(block.variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, block.variantRegions.length - 1))], x, y, drawrot());
        }
        drawTeamTop();
  }

  public void drawBars() {

        Draw.z(Layer.turret + 4.0F);
        if (maxHealth < Core.settings.getInt("blockbarminhealth") || (health / maxHealth > 0.9F)) return;
        Draw.color(team.color, 0.3F);
        Lines.stroke(4.0F);
        Lines.line(x - block.size * tilesize / 2.0F * 0.6F, y + block.size * tilesize / 2.5F, x + block.size * tilesize / 2.0F * 0.6F, y + block.size * tilesize / 2.5F);
        Draw.color(Pal.health, 0.6F);
        Lines.stroke(2.0F);
        Lines.line(x - block.size * tilesize / 2.0F * 0.6F, y + block.size * tilesize / 2.5F, x + 0.6F * (Mathf.clamp(health / maxHealth, 0.0F, 1.0F) - 0.5F) * block.size * tilesize, y + block.size * tilesize / 2.5F);
        Draw.color();
  }

  public void drawConfigure() {

        Draw.color(Pal.accent);
        Lines.stroke(1.0F);
        Lines.square(x, y, block.size * tilesize / 2.0F + 1.0F);
        Draw.reset();
  }

  public void drawCracks() {

        if (!block.drawCracks || !damaged() || block.size > BlockRenderer.maxCrackSize) return;
        int id = pos();
        TextureRegion region = renderer.blocks.cracks[block.size - 1][Mathf.clamp((int)((1.0F - healthf()) * BlockRenderer.crackRegions), 0, BlockRenderer.crackRegions - 1)];
        Draw.colorl(0.2F, 0.1F + (1.0F - healthf()) * 0.6F);
        Draw.rect(region, x, y, (id % 4) * 90);
        Draw.color();
  }

  public void drawDisabled() {

        Draw.color(Color.scarlet);
        Draw.alpha(0.8F);
        float size = 6.0F;
        Draw.rect(Icon.cancel.getRegion(), x, y, size, size);
        Draw.reset();
  }

  public void drawLight() {

        Liquid liq = block.hasLiquids && block.lightLiquid == null ? liquids.current() : block.lightLiquid;
        if (block.hasLiquids && block.drawLiquidLight && liq.lightColor.a > 0.001F) {
            visualLiquid = Mathf.lerpDelta(visualLiquid, liquids.get(liq) >= 0.01F ? 1.0F : 0.0F, 0.06F);
            drawLiquidLight(liq, visualLiquid);
        }
  }

  public void drawLiquidLight(Liquid liquid, float amount) {

        if (amount > 0.01F) {
            Color color = liquid.lightColor;
            float fract = 1.0F;
            float opacity = color.a * fract;
            if (opacity > 0.001F) {
                Drawf.light(x, y, block.size * 30.0F * fract, color, opacity * amount);
            }
        }
  }

  public void drawSelect() {

        block.drawOverlay(x, y, rotation);
  }

  public void drawStatus() {

        if (block.enableDrawStatus && block.consumers.length > 0) {
            float multiplier = block.size > 1 ? 1 : 0.64F;
            float brcx = x + (block.size * tilesize / 2.0F) - (tilesize * multiplier / 2.0F);
            float brcy = y - (block.size * tilesize / 2.0F) + (tilesize * multiplier / 2.0F);
            Draw.z(Layer.power + 1);
            Draw.color(Pal.gray);
            Fill.square(brcx, brcy, 2.5F * multiplier, 45);
            Draw.color(status().color);
            Fill.square(brcx, brcy, 1.5F * multiplier, 45);
            Draw.color();
        }
  }

  public void drawTeam() {

        Draw.color(team.color);
        Draw.rect("block-border", x - block.size * tilesize / 2.0F + 4, y - block.size * tilesize / 2.0F + 4);
        Draw.color();
  }

  public void drawTeamTop() {

        if (block.teamRegion.found()) {
            if (block.teamRegions[team.id] == block.teamRegion) Draw.color(team.color);
            Draw.rect(block.teamRegions[team.id], x, y);
            Draw.color();
        }
  }

  public void dropped() {

  }

  public void dumpLiquid(Liquid liquid) {

        dumpLiquid(liquid, 2.0F);
  }

  public void dumpLiquid(Liquid liquid, float scaling) {

        dumpLiquid(liquid, scaling, -1);
  }

  public void dumpLiquid(Liquid liquid, float scaling, int outputDir) {

        int dump = this.cdump;
        if (liquids.get(liquid) <= 1.0E-4F) return;
        if (!net.client() && state.isCampaign() && team == state.rules.defaultTeam) liquid.unlock();
        for (int i = 0; i < proximity.size; i++) {
            incrementDump(proximity.size);
            Building other = proximity.get((i + dump) % proximity.size);
            if (outputDir != -1 && (outputDir + rotation) % 4 != relativeTo(other)) continue;
            other = other.getLiquidDestination(this, liquid);
            if (other != null && other.block.hasLiquids && canDumpLiquid(other, liquid) && other.liquids != null) {
                float ofract = other.liquids.get(liquid) / other.block.liquidCapacity;
                float fract = liquids.get(liquid) / block.liquidCapacity;
                if (ofract < fract) transferLiquid(other, (fract - ofract) * block.liquidCapacity / scaling, liquid);
            }
        }
  }

  public void getStackOffset(Item item, Vec2 trns) {

  }

  public void handleItem(Building source, Item item) {

        items.add(item, 1);
  }

  public void handleLiquid(Building source, Liquid liquid, float amount) {

        liquids.add(liquid, amount);
  }

  public void handlePayload(Building source, Payload payload) {

  }

  public void handleStack(Item item, int amount, Teamc source) {

        noSleep();
        items.add(item, amount);
  }

  public void handleString(Object value) {

  }

  public void handleUnitPayload(Unit unit, Cons<Payload> grabber) {

        Fx.spawn.at(unit);
        if (unit.isPlayer()) {
            unit.getPlayer().clearUnit();
        }
        unit.remove();
        if (net.client()) {
            unit.id = EntityGroup.nextId();
        } else {
            Core.app.post(()->unit.id = EntityGroup.nextId());
        }
        grabber.get(new UnitPayload(unit));
        Fx.unitDrop.at(unit);
  }

  public void heal() {
    health: {

        dead = false;
        health = maxHealth;
    }
    building: {

        healthChanged();
    }
  }

  public void heal(float amount) {
    health: {

        health += amount;
        clampHealth();
    }
    building: {

        healthChanged();
    }
  }

  public void healFract(float amount) {

        heal(amount * maxHealth);
  }

  public void healthChanged() {

        if (net.server()) {
            netServer.buildHealthUpdate(this);
        }
        indexer.notifyHealthChanged(this);
  }

  public void hitbox(Rect out) {

        out.setCentered(x, y, block.size * tilesize, block.size * tilesize);
  }

  public void incrementDump(int prox) {

        cdump = ((cdump + 1) % prox);
  }

  public void itemTaken(Item item) {

  }

  public void kill() {

        Call.buildDestroyed(this);
  }

  public void killed() {
    building: {

        Events.fire(new BlockDestroyEvent(tile));
        block.destroySound.at(tile);
        onDestroyed();
        if (tile != emptyTile) {
            tile.remove();
        }
        remove();
        afterDestroyed();
    }
    health: {

    }
  }

  public void noSleep() {

        sleepTime = 0.0F;
        if (sleeping) {
            add();
            sleeping = false;
            sleepingEntities--;
        }
  }

  public void offload(Item item) {

        produced(item, 1);
        int dump = this.cdump;
        for (int i = 0; i < proximity.size; i++) {
            incrementDump(proximity.size);
            Building other = proximity.get((i + dump) % proximity.size);
            if (other.acceptItem(this, item) && canDump(other, item)) {
                other.handleItem(this, item);
                return;
            }
        }
        handleItem(this, item);
  }

  public void onCommand(Vec2 target) {

  }

  public void onConfigureClosed() {

  }

  public void onControlSelect(Unit player) {

  }

  public void onDestroyed() {

        float explosiveness = block.baseExplosiveness;
        float flammability = 0.0F;
        float power = 0.0F;
        if (block.hasItems) {
            for (Item item : content.items()) {
                int amount = Math.min(items.get(item), explosionItemCap());
                explosiveness += item.explosiveness * amount;
                flammability += item.flammability * amount;
                power += item.charge * Mathf.pow(amount, 1.1F) * 150.0F;
            }
        }
        if (block.hasLiquids) {
            flammability += liquids.sum((liquid,amount)->liquid.flammability * amount / 2.0F);
            explosiveness += liquids.sum((liquid,amount)->liquid.explosiveness * amount / 2.0F);
        }
        if (block.consPower != null && block.consPower.buffered) {
            power += this.power.status * block.consPower.capacity;
        }
        if (block.hasLiquids && state.rules.damageExplosions) {
            liquids.each((liquid,amount)->{
                float splash = Mathf.clamp(amount / 4.0F, 0.0F, 10.0F);
                for (int i = 0; i < Mathf.clamp(amount / 5, 0, 30); i++) {
                    Time.run(i / 2.0F, ()->{
                        Tile other = world.tileWorld(x + Mathf.range(block.size * tilesize / 2), y + Mathf.range(block.size * tilesize / 2));
                        if (other != null) {
                            Puddles.deposit(other, liquid, splash);
                        }
                    });
                }
            });
        }
        Damage.dynamicExplosion(x, y, flammability, explosiveness * 3.5F, power, tilesize * block.size / 2.0F, state.rules.damageExplosions, block.destroyEffect);
        if (block.createRubble && !floor().solid && !floor().isLiquid) {
            Effect.rubble(x, y, block.size);
        }
  }

  public void onProximityAdded() {

        if (power != null) {
            updatePowerGraph();
        }
  }

  public void onProximityRemoved() {

        if (power != null) {
            powerGraphRemoved();
        }
  }

  public void onProximityUpdate() {

        noSleep();
  }

  public void onRemoved() {

  }

  public void overwrote(Seq<Building> previous) {

  }

  public void payloadDraw() {

        draw();
  }

  public void pickedUp() {

  }

  public void powerGraphRemoved() {

        if (power == null) return;
        power.graph.remove(this);
        for (int i = 0; i < power.links.size; i++) {
            Tile other = world.tile(power.links.get(i));
            if (other != null && other.build != null && other.build.power != null) {
                other.build.power.links.removeValue(pos());
            }
        }
        power.links.clear();
  }

  public void produced(Item item) {

        produced(item, 1);
  }

  public void produced(Item item, int amount) {

        if (Vars.state.rules.sector != null && team == state.rules.defaultTeam) {
            Vars.state.rules.sector.info.handleProduction(item, amount);
            if (!net.client()) item.unlock();
        }
  }

  public void read(Reads read) {

        afterRead();
  }

  public void readAll(Reads read, byte revision) {

        readBase(read);
        read(read, revision);
  }

  public void readBase(Reads read) {

        health = Math.min(read.f(), block.health);
        byte rot = read.b();
        team = Team.get(read.b());
        rotation = rot & 127;
        int moduleBits = moduleBitmask();
        boolean legacy = true;
        byte version = 0;
        if ((rot & 128) != 0) {
            version = read.b();
            if (version >= 1) {
                byte on = read.b();
                this.enabled = on == 1;
            }
            if (version >= 2) {
                moduleBits = read.b();
            }
            legacy = false;
        }
        if ((moduleBits & 1) != 0) (items == null ? new ItemModule() : items).read(read, legacy);
        if ((moduleBits & 2) != 0) (power == null ? new PowerModule() : power).read(read, legacy);
        if ((moduleBits & 4) != 0) (liquids == null ? new LiquidModule() : liquids).read(read, legacy);
        if (version <= 2) read.bool();
        if (version >= 3) {
            efficiency = potentialEfficiency = read.ub() / 255.0F;
            optionalEfficiency = read.ub() / 255.0F;
        }
        if (version == 4) {
            visibleFlags = read.l();
        }
  }

  public void recentlyHealed() {

        lastHealTime = Time.time;
  }

  public void remove() {
    if(added == false) return;
    Groups.all.removeIndex(this, index__all);;
    index__all = -1;
    Groups.build.removeIndex(this, index__build);;
    index__build = -1;
    building: {

        if (sound != null) {
            sound.stop();
        }
    }
    entity: {

        added = false;
    }
  }

  public void removeFromProximity() {

        onProximityRemoved();
        tmpTiles.clear();
        Point2[] nearby = Edges.getEdges(block.size);
        for (Point2 point : nearby) {
            Building other = world.build(tile.x + point.x, tile.y + point.y);
            if (other != null) {
                tmpTiles.add(other);
            }
        }
        for (Building other : tmpTiles) {
            other.proximity.remove(this, true);
            other.onProximityUpdate();
        }
        proximity.clear();
  }

  public void set(Position pos) {

        set(pos.getX(), pos.getY());
  }

  public void set(float x, float y) {

        this.x = x;
        this.y = y;
  }

  public void setProp(UnlockableContent content, double value) {

        if (content instanceof Item item && items != null) {
            int amount = (int)value;
            if (items.get(item) != amount) {
                if (items.get(item) < amount) {
                    handleStack(item, acceptStack(item, amount - items.get(item), null), null);
                } else if (amount >= 0) {
                    removeStack(item, items.get(item) - amount);
                }
            }
        } else if (content instanceof Liquid liquid && liquids != null) {
            float amount = Mathf.clamp((float)value, 0.0F, block.liquidCapacity);
            if (amount < liquids.get(liquid) || (acceptLiquid(this, liquid) && (liquids.current() == liquid || liquids.currentAmount() <= 0.1F || block.consumesLiquid(liquid)))) {
                liquids.set(liquid, amount);
            }
        }
  }

  public void setProp(LAccess prop, double value) {

        switch (prop) {
        case health -> {
            health = (float)Mathf.clamp(value, 0, maxHealth);
            healthChanged();
            if (health <= 0.0F && !dead()) {
                Call.buildDestroyed(this);
            }
        }
        case team -> {
            Team team = Team.get((int)value);
            if (this.team != team) {
                changeTeam(team);
            }
        }
        case totalPower -> {
            if (power != null && block.consPower != null && block.consPower.buffered) {
                power.status = Mathf.clamp((float)(value / block.consPower.capacity));
            }
        }
        }
  }

  public void setProp(LAccess prop, Object value) {

        switch (prop) {
        case team -> {
            if (value instanceof Team team && this.team != team) {
                changeTeam(team);
            }
        }
        }
  }

  public void sleep() {

        sleepTime += Time.delta;
        if (!sleeping && sleepTime >= timeToSleep) {
            remove();
            sleeping = true;
            sleepingEntities++;
        }
  }

  public void tapped() {

  }

  public void transferLiquid(Building next, float amount, Liquid liquid) {

        float flow = Math.min(next.block.liquidCapacity - next.liquids.get(liquid), amount);
        if (next.acceptLiquid(this, liquid)) {
            next.handleLiquid(this, liquid, flow);
            liquids.remove(liquid, flow);
        }
  }

  public void trns(Position pos) {

        trns(pos.getX(), pos.getY());
  }

  public void trns(float x, float y) {

        set(this.x + x, this.y + y);
  }

  public void unitOn(Unit unit) {

  }

  public void unitRemoved(Unit unit) {

  }

  public void update() {

        if (state.isEditor()) return;
        if ((timeScaleDuration -= Time.delta) <= 0.0F || !block.canOverdrive) {
            timeScale = 1.0F;
        }
        if (!allowUpdate()) {
            enabled = false;
        }
        if (!headless && !wasVisible && state.rules.fog && !inFogTo(player.team())) {
            visibleFlags |= (1L << player.team().id);
            wasVisible = true;
            renderer.blocks.updateShadow(this);
            renderer.minimap.update(tile);
        }
        if (!headless) {
            if (sound != null) {
                sound.update(x, y, shouldActiveSound(), activeSoundVolume());
            }
            if (block.ambientSound != Sounds.none && shouldAmbientSound()) {
                control.sound.loop(block.ambientSound, this, block.ambientSoundVolume * ambientVolume());
            }
        }
        updateConsumption();
        if (enabled || !block.noUpdateDisabled) {
            updateTile();
        }
  }

  public void updateConsumption() {

        if (!block.hasConsumers || cheating()) {
            potentialEfficiency = enabled && productionValid() ? 1.0F : 0.0F;
            efficiency = optionalEfficiency = shouldConsume() ? potentialEfficiency : 0.0F;
            updateEfficiencyMultiplier();
            return;
        }
        if (!enabled) {
            potentialEfficiency = efficiency = optionalEfficiency = 0.0F;
            return;
        }
        boolean update = shouldConsume() && productionValid();
        float minEfficiency = 1.0F;
        efficiency = optionalEfficiency = 1.0F;
        for (var cons : block.nonOptionalConsumers) {
            minEfficiency = Math.min(minEfficiency, cons.efficiency(this));
        }
        for (var cons : block.optionalConsumers) {
            optionalEfficiency = Math.min(optionalEfficiency, cons.efficiency(this));
        }
        efficiency = minEfficiency;
        optionalEfficiency = Math.min(optionalEfficiency, minEfficiency);
        potentialEfficiency = efficiency;
        if (!update) {
            efficiency = optionalEfficiency = 0.0F;
        }
        updateEfficiencyMultiplier();
        if (update && efficiency > 0) {
            for (var cons : block.updateConsumers) {
                cons.update(this);
            }
        }
  }

  public void updateEfficiencyMultiplier() {

        float scale = efficiencyScale();
        efficiency *= scale;
        optionalEfficiency *= scale;
  }

  public void updatePayload(Unit unitHolder, Building buildingHolder) {

        update();
  }

  public void updatePowerGraph() {

        for (Building other : getPowerConnections(tempBuilds)) {
            if (other.power != null) {
                other.power.graph.addGraph(power.graph);
            }
        }
  }

  public void updateProximity() {

        tmpTiles.clear();
        proximity.clear();
        Point2[] nearby = Edges.getEdges(block.size);
        for (Point2 point : nearby) {
            Building other = world.build(tile.x + point.x, tile.y + point.y);
            if (other == null || other.team != team) continue;
            other.proximity.addUnique(this);
            tmpTiles.add(other);
        }
        for (Building tile : tmpTiles) {
            proximity.add(tile);
        }
        onProximityAdded();
        onProximityUpdate();
        for (Building other : tmpTiles) {
            other.onProximityUpdate();
        }
  }

  public void updateTableAlign(Table table) {

        Vec2 pos = Core.input.mouseScreen(x, y - block.size * tilesize / 2.0F - 1);
        table.setPosition(pos.x, pos.y, Align.top);
  }

  public void updateTile() {

  }

  public void writeAll(Writes write) {

        writeBase(write);
        write(write);
  }

  public void writeBase(Writes write) {

        boolean writeVisibility = state.rules.fog && visibleFlags != 0;
        write.f(health);
        write.b(rotation | 128);
        write.b(team.id);
        write.b(writeVisibility ? 4 : 3);
        write.b(enabled ? 1 : 0);
        write.b(moduleBitmask());
        if (items != null) items.write(write);
        if (power != null) power.write(write);
        if (liquids != null) liquids.write(write);
        write.b((byte)(Mathf.clamp(efficiency) * 255.0F));
        write.b((byte)(Mathf.clamp(optionalEfficiency) * 255.0F));
        if (writeVisibility) {
            write.l(visibleFlags);
        }
  }
}
