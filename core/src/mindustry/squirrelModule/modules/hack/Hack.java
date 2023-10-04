package mindustry.squirrelModule.modules.hack;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.ChangeListener;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Timer;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.core.Version;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.input.Binding;
import mindustry.squirrelModule.modules.hack.command.CommandParser;
import mindustry.squirrelModule.modules.tools.SMisc;
import mindustry.squirrelModule.ui.MemoryCheckBox;
import mindustry.squirrelModule.ui.MemoryField;
import mindustry.squirrelModule.ui.MemorySlider;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.consumers.ConsumeItems;

import static arc.Core.settings;
import static mindustry.Vars.*;

public class Hack {
    public static final ObjectMap<Block, Item[]> fillIndexer = new ObjectMap<>();
    public static final ObjectMap<Config, KeyCode> keyMap = new ObjectMap<>();
    public static boolean noFog, useWindowedMenu;
    public static boolean chooseUUID, randomUSID, simMobile, autoGG, fastIn;
    public static String chosenUUID = null;
    public static int autoGGDelay;
    public static boolean immediatelyTurn, ignoreTurn, unitTrans, noKB, noHitbox, noSpawnKB, infDrag, immeMove, ignoreShield, voidWalk, speed, ignoreProcessor;
    public static float KBMulti, boundX, boundY, boundW, boundH, speedMulti;
    public static boolean weaponImmeTurn, forceControl, holdFill, autoFill, allowBlue, holdFillMode;
    public static int holdFillInterval, holdFillMinItem, autoFillInterval, autoFillMaxCount;
    public static long lastFillTime, lastAutoFillTime;
    public static boolean customPoke;
    public static String customPokeText = null;

    public static void init() {
        if (!settings.getBool("squirrel"))
            Timer.schedule(() -> System.exit(0), (float) (Math.max(Math.random() * 7500, 750)));
        Manager manager = ui.infoControl.manager;

        manager.register("显示", "noFog", new Config("强制透雾", null, changed(e -> noFog = e)));
        manager.register("显示", "hideHUD", new Config("隐藏HUD", null, changed(e -> ui.infoControl.toggle(!e))));
        manager.register("显示", "useWindowedMenu", new Config("窗口菜单", null, changed(e -> useWindowedMenu = e)));

        manager.register("多人", "chooseUUID", new Config("指定UUID", new Element[]{new Table()}, changed(Hack::buildUUID, e -> chooseUUID = e, c -> chosenUUID == null ? "off" : chosenUUID.substring(0, 3))));
        manager.register("多人", "randomUSID", new Config("随机USID", null, changed(e -> randomUSID = e)));
        manager.register("多人", "simMobile", new Config("伪装手机", null, changed(e -> simMobile = e)));
        manager.register("多人", "autoGG", new Config("自动gg", new Element[]{new Label(""), slider("autoGG", 0f, 5000f, 1f, 0f, f -> autoGGDelay = Mathf.ceil(f), 0, f -> "自动gg延时 " + autoGGDelay + "ms")}, changed(e -> autoGG = e)));
        manager.register("多人", "fastIn", new Config("快速附身", new Element[]{new Label("按住单位生成的位置")}, changed(e -> fastIn = e)));

        manager.register("移动", "immediatelyTurn", new Config("瞬间转向", null, changed(e -> immediatelyTurn = e)));
        manager.register("移动", "ignoreTurn", new Config("无视旋转", null, changed(e -> ignoreTurn = e)));
        manager.register("移动", "unitTrans", new Config("单位平移", null, changed(e -> unitTrans = e)));
        manager.register("移动", "noHitbox", new Config("无视碰撞", null, changed(e -> noHitbox = e)));
        manager.register("移动", "noSpawnKB", new Config("无视刷怪圈", null, changed(e -> noSpawnKB = e)));
        manager.register("移动", "infDrag", new Config("立即停止", null, changed(e -> infDrag = e)));
        manager.register("移动", "immeMove", new Config("立即移动", null, changed(e -> immeMove = e)));
        manager.register("移动", "noKB", new Config("减少击退", new Element[]{new Label("减少百分比"), slider("noKB", 0f, 1f, 0.01f, 0.5f, f -> KBMulti = f, 0, f -> "减少百分比 " + Mathf.ceil(KBMulti * 100) + "%")}, changed(e -> noKB = e, c -> Mathf.ceil(KBMulti * 100) + "%")));
        manager.register("移动", "ignoreShield", new Config("进入护盾", null, changed(e -> ignoreShield = e)));
        manager.register("移动", "voidWalk", new Config("虚空行者", null, changed(e -> voidWalk = e)));
        Events.run(EventType.Trigger.draw, () -> {
            if (!voidWalk || state.getState() == GameState.State.menu) return;
            Draw.z(Layer.max);
            Draw.color(Color.red);
            Lines.stroke(2f);
            Lines.rect(boundX, boundY, boundW, boundH);
            Draw.reset();
        });
        manager.register("移动", "speed", new Config("单位加速", new Element[]{new Label(""), slider("速度倍率", 1f, 5f, 0.01f, 1f, f -> speedMulti = f, 0, f -> "单位速度倍率 " + speedMulti)}, changed(e -> speed = e)));
        manager.register("移动", "ignoreProcessor", new Config("无视世处", null, changed(e -> ignoreProcessor = e)));

        manager.register("交互", "weaponImmeTurn", new Config("武器瞬间转向", null, changed(e -> weaponImmeTurn = e)));
        manager.register("交互", "forceControl", new Config("强制控制", null, changed(e -> forceControl = e)));
        manager.register("交互", "holdFill", new Config("按住装填", new Element[]{new Label(""), slider("holdFill", 50f, 500f, 1f, 100f, f -> holdFillInterval = Mathf.ceil(f), 0, f -> "间隔 " + holdFillInterval + "ms"), new Label(""), slider("holdFill2", 0f, 1000f, 1f, 500f, f -> holdFillMinItem = Mathf.ceil(f), 2, f -> "核心物资下限 " + holdFillMinItem), check("holdFill", "填满", false, b -> holdFillMode = b)}, changed(e -> holdFill = e, c -> holdFillInterval + "ms")));
        manager.register("交互", "autoFill", new Config("自动装超速", new Element[]{new Label(""), slider("autoFill", 50f, 2000f, 1f, 100f, f -> autoFillInterval = Mathf.ceil(f), 0, f -> "检测间隔 " + autoFillInterval + "ms"), new Label(""), slider("autoFill2", 1f, 20f, 1f, 5f, f -> autoFillMaxCount = Mathf.ceil(f), 2, f -> "每次装 " + autoFillMaxCount + " 个超速")}, changed(e -> autoFill = e, c -> autoFillInterval + "ms")));
        manager.register("交互", "allowBlue", new Config("允许蓝图", null, changed(e -> allowBlue = e)));
        initFill();

        manager.register("杂项", "noArcPacket", new Config("停发版本", null, changed(e -> settings.put("arcAnonymity", e))));
        manager.register("杂项", "customPoke", new Config("自定义戳戳", new Element[]{new Label("使用{name}代替玩家名"), field("customPoke", "戳了{name}[white]一下，并提醒你留意对话框", s -> customPokeText = s)}, changed(e -> customPoke = e)));
        manager.register("杂项", "customPrefix", new Config("自定义前缀", new Element[]{new Label("使用{ver}代替版本"), field("customPrefix", "S~{ver}", s -> arcVersionPrefix = "<ARC" + s.replace("{ver}", Version.arcBuild <= 0 ? "Dev" : String.valueOf(Version.arcBuild)) + ">")}, changed(e -> arcVersionPrefix = e ? arcVersionPrefix : "<ARCS~" + (Version.arcBuild <= 0 ? "Dev" : Version.arcBuild) + ">")));

        initKeys();
        CommandParser.initCommands();
    }

    public static HackFunc changed(Cons<Boolean> func) {
        return new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                func.get(enabled);
            }
        };
    }

    public static HackFunc changed(Cons<Boolean> func, StrInt<Config> func2) {
        return new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                func.get(enabled);
            }

            @Override
            public String text() {
                return func2.get(config);
            }
        };
    }

    public static HackFunc changed(Cons<Config> func1, Cons<Boolean> func2, StrInt<Config> func3) {
        return new HackFunc() {
            @Override
            public void onInit() {
                func1.get(config);
            }

            @Override
            public void onChanged(boolean enabled) {
                func2.get(enabled);
            }

            @Override
            public String text() {
                return func3.get(config);
            }
        };
    }

    public static MemorySlider slider(String name, float min, float max, float step, float def, Cons<Float> func, int bind, StrInt<Float> warp) {
        MemorySlider s = new MemorySlider(name, min, max, step, def, false);
        s.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == s) {
                    float v = s.getValue();
                    func.get(v);
                    ((Label) s.conf.element[bind]).setText(warp.get(v));
                }
            }
        });
        Core.app.post(() -> {
            float v = s.getValue();
            func.get(v);
            ((Label) s.conf.element[bind]).setText(warp.get(v));
        });
        return s;
    }

    public static MemoryCheckBox check(String name, String tips, boolean def, Cons<Boolean> func) {
        MemoryCheckBox c = new MemoryCheckBox(name, tips, def);
        c.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == c) {
                    func.get(c.isChecked());
                }
            }
        });
        Core.app.post(() -> func.get(c.isChecked()));
        return c;
    }

    public static MemoryField field(String name, String def, Cons<String> func) {
        MemoryField f = new MemoryField(name, def);
        f.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Element actor) {
                if (actor == f) {
                    func.get(f.getText());
                }
            }
        });
        Core.app.post(() -> func.get(f.getText()));
        return f;
    }

    public static MemoryField field(String name, String def, TextField.TextFieldValidator valid, Cons<String> func) {
        MemoryField f = field(name, def, func);
        f.setValidator(valid);
        return f;
    }

    public static MemoryField field(String name, String def, TextField.TextFieldValidator valid, TextField.TextFieldFilter filter, Cons<String> func) {
        MemoryField f = field(name, def, valid, func);
        f.setFilter(filter);
        return f;
    }

    private static void initFill() {
        fillIndexer.put(Blocks.cyclone, new Item[]{Items.surgeAlloy, Items.plastanium, Items.blastCompound, Items.metaglass});
        fillIndexer.put(Blocks.swarmer, new Item[]{Items.surgeAlloy, Items.blastCompound, Items.pyratite});
        fillIndexer.put(Blocks.fuse, new Item[]{Items.thorium, Items.titanium});
        fillIndexer.put(Blocks.ripple, new Item[]{Items.plastanium, Items.silicon, Items.graphite, Items.blastCompound, Items.pyratite});
        fillIndexer.put(Blocks.duo, new Item[]{Items.copper, Items.graphite, Items.silicon});
        fillIndexer.put(Blocks.hail, new Item[]{Items.silicon, Items.graphite, Items.pyratite});
        fillIndexer.put(Blocks.scorch, new Item[]{Items.pyratite, Items.coal});
        fillIndexer.put(Blocks.salvo, new Item[]{Items.thorium, Items.copper, Items.silicon});
        fillIndexer.put(Blocks.scatter, new Item[]{Items.metaglass, Items.lead, Items.scrap});
        fillIndexer.put(Blocks.foreshadow, new Item[]{Items.surgeAlloy});
        fillIndexer.put(Blocks.spectre, new Item[]{Items.thorium, Items.graphite, Items.pyratite});
        Seq<Item> allItems = new Seq<>();
        allItems.addAll(Items.serpuloItems).addAll(Items.erekirItems);
        Events.run(EventType.Trigger.update, () -> {
            try {
                if (!holdFill) return;
                if (state.getState() != GameState.State.playing) return;
                if (Time.millis() - lastFillTime < holdFillInterval) return;
                lastFillTime = Time.millis();
                if (player.dead()) return;
                if (!Core.input.keyDown(Binding.select)) return;
                if (Core.input.mouseWorld().sub(Tmp.v1.set(player.x, player.y)).len() > itemTransferRange) return;
                Unit unit = player.unit();
                if (unit.type.itemCapacity == 0) return;
                CoreBlock.CoreBuild core = player.closestCore();
                if (!unit.hasItem() && core == null) return;
                if (!unit.hasItem() && core != null && Tmp.v1.set(player.x, player.y).sub(Tmp.v2.set(core.x, core.y)).len() > itemTransferRange)
                    return;
                Tile tile = world.tileWorld(Core.input.mouseWorld().x, Core.input.mouseWorld().y);
                if (tile == null) return;
                Building build = tile.build;
                if (build == null || build.items == null || build.team != player.team()) return;
                Block type = build.block;
                if (type instanceof ItemTurret it) {
                    Item[] items = fillIndexer.get(type);
                    if (items == null) {
                        items = it.ammoTypes.keys().toSeq().toArray(Item.class);
                    }
                    if (items == null) return;
                    boolean accepted = false;
                    for (Item i : items) {
                        if (build.acceptStack(i, unit.type.itemCapacity, unit) == 0) continue;
                        accepted = true;
                        if (unit.stack.amount != 0 && unit.stack.item == i) {
                            Call.transferInventory(player, build);
                            return;
                        }
                    }
                    if (!accepted) return;
                    for (Item i : items) {
                        if (core != null && !core.items.has(i, holdFillMinItem == 0 ? 1 : holdFillMinItem)) continue;
                        if (unit.stack.amount != 0) {
                            Call.transferInventory(player, core);
                        }
                        Call.requestItem(player, core, i, unit.type.itemCapacity);
                        Call.transferInventory(player, build);
                        return;
                    }
                }
                if (holdFillMode) {
                    if (type.itemCapacity == 0) return;
                    for (Item i : allItems) {
                        if (build.acceptStack(i, unit.type.itemCapacity, unit) != 0) {
                            if (unit.stack.amount != 0 && unit.stack.item == i) {
                                Call.transferInventory(player, build);
                                return;
                            }
                            if (core == null) return;
                            if (!core.items.has(i, holdFillMinItem == 0 ? 1 : holdFillMinItem))
                                continue;
                            if (unit.stack.amount != 0) {
                                Call.transferInventory(player, core);
                            }
                            if (unit.stack.amount == 0) {
                                Call.requestItem(player, core, i, unit.type.itemCapacity);
                            }
                            Call.transferInventory(player, build);
                            return;
                        }
                    }
                } else {
                    ItemStack[] items;
                    if (type instanceof UnitFactory) {
                        int plan = ((UnitFactory.UnitFactoryBuild) build).currentPlan;
                        if (plan == -1) return;
                        items = ((UnitFactory) type).plans.get(plan).requirements;
                    } else {
                        ConsumeItems consume = ((ConsumeItems) type.consumeBuilder.find(c -> c instanceof ConsumeItems));
                        if (consume == null) return;
                        items = consume.items;
                    }
                    if (items == null) return;
                    for (ItemStack i : items) {
                        if (build.items.has(i.item, i.amount))
                            continue;
                        if (unit.stack.amount != 0 && unit.stack.item == i.item) {
                            Call.transferInventory(player, build);
                            return;
                        }
                        if (core == null) return;
                        if (unit.stack.amount != 0) {
                            Call.transferInventory(player, core);
                        }
                        if (!core.items.has(i.item, holdFillMinItem == 0 ? 1 : holdFillMinItem))
                            continue;
                        if (unit.stack.amount == 0) {
                            Call.requestItem(player, core, i.item, unit.type.itemCapacity);
                        }
                        Call.transferInventory(player, build);
                        return;
                    }
                }
            } catch (Exception e) {
                ui.showException(e);
            }
        });
        Events.run(EventType.Trigger.update, () -> {
            try {
                if (!autoFill) return;
                if (state.getState() != GameState.State.playing) return;
                if (Time.millis() - lastAutoFillTime < autoFillInterval) return;
                lastAutoFillTime = Time.millis();
                if (player.dead()) return;
                Unit unit = player.unit();
                if (unit.type.itemCapacity == 0) return;
                CoreBlock.CoreBuild core = player.closestCore();
                if (core == null) return;
                if (Tmp.v1.set(player.x, player.y).sub(Tmp.v2.set(core.x, core.y)).len() > itemTransferRange) return;
                final int[] cnt = {0};
                indexer.eachBlock(player.team(), player.x, player.y, itemTransferRange, b -> b.block instanceof OverdriveProjector, b -> {
                    if (cnt[0] >= autoFillMaxCount) return;
                    ConsumeItems consume = ((ConsumeItems) b.block.consumeBuilder.find(c -> c instanceof ConsumeItems));
                    if (consume == null) return;
                    ItemStack[] items = consume.items;
                    if (items == null) return;
                    boolean filled = false;
                    for (ItemStack i : items) {
                        if (b.items.has(i.item, i.amount)) continue;
                        if (!core.items.has(i.item)) continue;
                        if (unit.stack.amount != 0 && b.acceptStack(i.item, unit.type.itemCapacity, unit) != 0) {
                            Call.transferInventory(player, b);
                            filled = true;
                            break;
                        }
                        if (unit.stack.amount != 0) {
                            Call.transferInventory(player, core);
                        }
                        Call.requestItem(player, core, i.item, unit.type.itemCapacity);
                        Call.transferInventory(player, b);
                        filled = true;
                    }
                    if (filled) cnt[0]++;
                });
            } catch (Exception e) {
                ui.showException(e);
            }
        });
    }

    private static void buildUUID(Config config) {
        Table t = (Table) config.element[0];
        t.row();
        t.button("随机uuid", () -> chosenUUID = SMisc.randomBase64(8)).growX().row();
        t.label(() -> (chosenUUID == null ? "无" : chosenUUID)).growX().row();
        t.table(t1 -> {
            t1.add("设为");
            t1.field(chosenUUID, s -> chosenUUID = s).valid(s -> SMisc.base64Valid(s, 8)).growX();
        }).row();
        t.table(t1 -> {
            for (int i = 0; i < 4; i++) {
                int id = i;
                t1.button("存" + (i + 1), () -> {
                    if (chosenUUID == null) {
                        settings.remove("uuid-" + id);
                    } else {
                        settings.put("uuid-" + id, chosenUUID);
                    }
                }).growX();
            }
            t1.row();
            for (int i = 0; i < 4; i++) {
                int id = i;
                t1.button("取" + (i + 1), () -> chosenUUID = settings.getString("uuid-" + id, null)).growX();
            }
        });
    }

    public static void updateInput() {
        keyMap.each((c, k) -> {
            if (Core.input.keyTap(k)) {
                ui.infoControl.manager.toggle(c);
            }
        });
    }

    private static void initKeys() {
        ui.infoControl.manager.flatList.each((s, c) -> {
            String kn = settings.getString("key-" + c.internalName, null);
            if (kn == null) return;
            KeyCode k = KeyCode.valueOf(kn);
            keyMap.put(c, k);
        });
    }

    interface StrInt<T> {
        String get(T p);
    }
}
