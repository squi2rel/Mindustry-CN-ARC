package mindustry.squirrelModule.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.ChangeListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.squirrelModule.modules.hack.Config;
import mindustry.squirrelModule.modules.tools.SMisc;

import static mindustry.Vars.ui;
import static mindustry.squirrelModule.modules.tools.SMisc.PI20;

public class ControlTable extends Table {
    static float lineAdd = 0.06f, rotateDiv = 5f;
    ObjectMap<String, Seq<Config>> list;

    public ControlTable(ObjectMap<String, Seq<Config>> seq) {
        super();
        setFillParent(true);
        list = seq;
    }

    public void buildClickHUD(Group parent) {
        list.each((k, v) -> parent.addChild(new ClickHUD(k, v)));
    }

    private static class ClickHUD extends Table {
        boolean expand;

        public ClickHUD(String title, Seq<Config> seq) {
            super();
            Drawable gray = SStyles.tint(Color.valueOf("333333"));
            Drawable gray2 = SStyles.tint(Color.valueOf("555555"));
            top();
            x = Core.settings.getFloat(title + "x", 100f);
            y = Core.settings.getFloat(title + "y", 100f);
            expand = Core.settings.getBool(title + "e", false);
            table(t1 -> {
                t1.table(t2 -> {
                    t2.touchable = Touchable.enabled;
                    t2.add(title).center();
                    t2.setBackground(SStyles.tint(Color.valueOf("222222")));
                    t2.addListener(new InputListener() {
                        float lastX, lastY;
                        boolean dragged;

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            dragged = false;
                            Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
                            lastX = v.x;
                            lastY = v.y;
                            toFront();
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer) {
                            Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
                            if (!dragged && Tmp.v2.set(v).sub(Tmp.v3.set(lastX, lastY)).len() < 5) return;
                            dragged = true;
                            ClickHUD.this.x += v.x - lastX;
                            ClickHUD.this.y += v.y - lastY;
                            lastX = v.x;
                            lastY = v.y;
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            if (!dragged) expand = !expand;
                            dragged = false;
                            Core.settings.put(title + "x", ClickHUD.this.x);
                            Core.settings.put(title + "y", ClickHUD.this.y);
                            Core.settings.put(title + "e", expand);
                        }
                    });
                }).growX().height(50).row();
                final int[] num = {0};
                t1.table(t2 -> seq.each(conf -> {
                    int id = num[0]++;
                    boolean[] configShowed = {false};
                    Cell<?>[] settings = {null};
                    final Config value = conf;
                    final String name = conf.displayName;
                    t2.table(t3 -> {
                        t3.add(new Table(t4 -> {
                            t4.touchable = Touchable.enabled;
                            t4.add(name).update(l -> {
                                if (value.enabled) {
                                    l.setText("[#444444]" + name);
                                } else {
                                    l.setText(name);
                                    t4.setBackground(gray);
                                }
                            }).pad(5).left().growX();
                            t4.clicked(() -> {
                                if (value.enabled) {
                                    value.func.onDisable();
                                    value.enabled = false;
                                } else {
                                    value.func.onEnable();
                                    value.enabled = true;
                                }
                                value.func.onChanged(value.enabled);
                                Core.settings.put(value.internalName + "e", value.enabled);
                            });
                        }) {
                            @Override
                            protected void drawBackground(float x, float y) {
                                if (!value.enabled) {
                                    super.drawBackground(x, y);
                                    return;
                                }
                                float offset = 40 * Scl.scl();
                                for (float i = 0; i <= height; i++) {
                                    Draw.color(SMisc.color((((Time.globalTime / rotateDiv) % PI20) + id * lineAdd * offset + (offset - i) * lineAdd) % PI20));
                                    Lines.line(x, y + i, x + width, y + i);
                                }
                            }
                        }).grow();
                        if (value.element != null && value.element.length != 0) t3.table(t4 -> {
                            t4.touchable = Touchable.enabled;
                            t4.setBackground(gray2);
                            t4.center();
                            t4.add(":");
                            t4.clicked(() -> {
                                if (configShowed[0]) {
                                    settings[0].setElement(null).height(0);
                                    configShowed[0] = false;
                                } else {
                                    settings[0].setElement(new ScrollPane(new Table(t5 -> {
                                        t5.setBackground(gray2);
                                        for (Element e : value.element) {
                                            Element el = t5.add(e).growX().pad(5).left().get();
                                            el.addListener(new ChangeListener() {
                                                @Override
                                                public void changed(ChangeEvent event, Element actor) {
                                                    if (actor == el) {
                                                        value.func.onConfigure();
                                                    }
                                                }
                                            });
                                            t5.row();
                                        }
                                    }))).growX().update(p -> settings[0].height(Math.min(p.getPrefHeight(), 200)));
                                    configShowed[0] = true;
                                }
                            });
                        }).width(20).growY();
                    }).growX().height(40).row();
                    settings[0] = t2.add().growX();
                    t2.row();
                })).minWidth(250).visible(() -> expand);
            });
        }
    }
}
