package mindustry.arcModule.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.*;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.HandCursorListener;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.*;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.util.*;
import mindustry.Vars;
import mindustry.arcModule.ARCClient;
import mindustry.arcModule.ARCEvents;
import mindustry.arcModule.ARCVars;
import mindustry.arcModule.RFuncs;
import mindustry.arcModule.ui.window.Window;
import mindustry.arcModule.ui.window.WindowEvents;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.net.ValidateException;
import mindustry.ui.Fonts;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static mindustry.arcModule.ARCClient.*;
import static mindustry.arcModule.ARCVars.arcui;
import static mindustry.arcModule.ui.RStyles.clearAccentNonei;

@SuppressWarnings("NewApi")
public class ARCChat {
    private static final IntMap<ChatTable> messages = new IntMap<>();
    private static final Color imgBackground = new Color(0, 0, 0, 0.2f);
    private static final GlyphLayout glyphLayout = new GlyphLayout(true);
    private static final IntMap<TextureRegion> avatarCache = new IntMap<>();
    private static final Table ph = new Table();
    private static final Interval timer = new Interval();
    public static int msgUnread;
    private static Table chatList;
    private static ChatTable cur;
    private static Window chat;
    private static Cell<Table> inner;
    private static TextButton.TextButtonStyle bs;
    private static boolean sizeChanged = false;
    private static TextField.TextFieldStyle myStyle, otherStyle, inputStyle;

    public static void init() throws Exception {
        ARCVars.arcClient.addHandlerStream("ARCChat", (p, s) -> {
            try {
                boolean isPrivate = s.readBoolean();
                if (isPrivate && Vars.player.id != s.readInt()) return;
                byte[] bytes = new byte[s.readShort()];
                int read = s.read(bytes);
                if (read != bytes.length) return;
                try {
                    addMessage(p, isPrivate ? new String(decrypt(bytes, myPrivate), StandardCharsets.UTF_8) : new String(bytes, StandardCharsets.UTF_8), isPrivate, null);
                } catch (Exception e) {
                    Log.err(e);
                }
            } catch (Exception ignored) {
            }
        });
        ARCVars.arcClient.addHandlerStream("ARCChatAvatar", (p, d) -> {
            try {
                setAvatar(p, d.readLong());
            } catch (Exception ignored) {
            }
        });
        Events.on(ARCEvents.PlayerKeySend.class, e -> ARCClient.send("ARCChatAvatar", s -> {
            try {
                s.writeLong(Core.settings.getLong("avatar", -1));
            } catch (Exception ignored) {
            }
        }));
        Events.on(ARCEvents.PlayerKeyReceived.class, e -> {
            if (!messages.containsKey(e.player.id)) buildButton(e.player);
        });
        Events.on(ARCEvents.PlayerLeave.class, e -> {
            ChatTable ct = messages.get(e.player.id);
            if (ct == null) return;
            if (ct == cur) {
                chatList.getCells().remove(chatList.getCell(cur.button));
                chatList.removeChild(cur.button);
                switchChat(0, null, null);
            }
            messages.remove(e.player.id);
        });
        Events.on(ARCEvents.Connect.class, e -> buildButton(null));
        Events.on(ARCEvents.Disconnected.class, e -> reset());
        chat = new Window("学术聊天", 800, 600, (Drawable) Icon.chat.getRegion(), arcui.WindowManager);
        chat.closeToRemove(false);
        bs = new TextButton.TextButtonStyle() {{
            over = RFuncs.tint(1204353279);
            disabled = over;
            font = Fonts.def;
            fontColor = Color.white;
            down = RFuncs.tint(262399231);
            up = RFuncs.tint(314045951);
        }};
        myStyle = new TextField.TextFieldStyle() {{
            font = Fonts.outline;
            fontColor = Color.white;
            selection = Tex.selection;
            background = Tex.chatCatMe;
        }};
        otherStyle = new TextField.TextFieldStyle(myStyle) {{
            background = Tex.chatCatOther;
        }};
        inputStyle = new TextField.TextFieldStyle() {{
            font = Fonts.outline;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            selection = Tex.selection;
            cursor = Tex.cursor;
        }};
        chat.setBody(new Table(t -> {
            t.background(Tex.whiteui);
            {
                Table t2 = new Table();
                chatList = t2;
                t2.align(Align.topLeft);
                t2.defaults().height(60).width(1000).padLeft(3);
                ScrollPane p = new ScrollPane(t2) {
                    @Override
                    public void draw() {
                        super.draw();
                        Draw.color(Color.gray);
                        Lines.stroke(1);
                        Lines.line(x + width, y, x + width, y + height);
                    }
                };
                p.setScrollingDisabledX(true);
                t.add(p).width(250).growY();
                t.addListener(new InputListener() {
                    @SuppressWarnings("unchecked")
                    final Cell<Table> cell = t.getCell(p);
                    float last, start;
                    boolean restored = true, dragging = false;

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                        Vec2 v = p.localToStageCoordinates(Tmp.v1.set(x, y));
                        dragging = p.getWidth() - 7 < x && x < p.getWidth() + 7 && 0 < y && y < p.getHeight();
                        last = v.x;
                        start = p.getWidth();
                        return true;
                    }

                    @Override
                    public void touchDragged(InputEvent event, float x, float y, int pointer) {
                        if (!dragging) return;
                        Vec2 v = p.localToStageCoordinates(Tmp.v1.set(x, y));
                        cell.width(Math.max(Math.min(start + v.x - last, 400), 63));
                        t.invalidate();
                        sizeChanged = true;
                        if (inner.get().getWidth() < 250) chat.setWidth(chat.getWidth() + 250 - inner.get().getWidth());
                    }

                    @Override
                    public boolean mouseMoved(InputEvent event, float x, float y) {
                        if (p.getWidth() - 7 < x && x < p.getWidth() + 7 && 0 < y && y < p.getHeight()) {
                            restored = false;
                            Core.graphics.cursor(arcui.resizeHorizontalCursor);
                        } else if (!restored) {
                            restored = true;
                            Core.graphics.restoreCursor();
                        }
                        return true;
                    }
                });
            }
            chat.addListener(WindowEvents.resizing, w -> {
                if (inner.get().getWidth() < 250) chat.setWidth(chat.getWidth() + 250 - inner.get().getWidth());
                sizeChanged = true;
            });
            inner = t.table().grow();
        }));
        reset();
    }

    public static void reset() {
        switchChat(0, null, null);
        chatList.clear();
        messages.clear();
        avatarCache.clear();
        msgUnread = 0;
    }

    @SuppressWarnings("rawtypes")
    private static void addMessage(Player p, String message, boolean isPrivate, @Nullable ChatTable target) {
        boolean isMe = Vars.player == p;
        if (isMe && isPrivate && target == null) return;
        ChatTable ct = target == null ? messages.get(isPrivate ? p.id : -1) : target;
        if (ct == null) throw new ValidateException(p, "玩家未发送key");
        ScrollPane pane = ct.pane;
        Table t = ct.messages.table().growX().get();
        ct.messages.row();
        Image i = new Image(p.icon());
        TextureRegion tr = avatarCache.get(p.id);
        if (tr != null) i.setDrawable(tr);
        i.setScaling(Scaling.fit);
        int align = isMe ? Align.right : Align.left;
        t.align(align);
        Table t2 = new Table();
        t2.align(align);
        if (!isMe) t2.add(p.name()).get().setAlignment(align);
        t2.row();
        Table t3 = t2.table().fillX().get();
        t3.align(align);
        String msg = message.substring(1);
        switch (message.charAt(0)) {
            case 'M' -> {
                t3.add(new ReadOnlyTextArea(msg, isMe ? myStyle : otherStyle, pane));
                String s = msg.substring(0, Math.min(20, msg.length()));
                ct.lastText = Strings.stripColors(isMe || isPrivate ? s : p.name() + s);
            }
            case 'I' -> {
                ct.lastText = Strings.stripColors(isMe || isPrivate ? "[图片]" : p.name() + "[图片]");
                Image img = new Image(Icon.refresh);
                img.setSize(64);
                img.setScaling(Scaling.fit);
                img.setAlign(align);
                img.update(() -> {
                    float height = img.getWidth() / img.getPrefWidth();
                    if (img.getHeight() != height) t3.getCell(img).minHeight(Math.min(500, height));
                });
                Http.get(msg, r -> {
                    long l = r.getContentLength();
                    if (l <= 0 || l > 32 * 1024 * 1024) {
                        img.setDrawable((Drawable) Core.atlas.getDrawable("error"));
                        return;
                    }
                    byte[] b = r.getResult();
                    new Thread(() -> {
                        try {
                            Pixmap pix = new Pixmap(b);
                            Core.app.post(() -> {
                                img.setDrawable(new TextureRegion(new Texture(pix)));
                                pix.dispose();
                            });
                        } catch (Exception e) {
                            Core.app.post(() -> img.setDrawable((Drawable) Core.atlas.getDrawable("error")));
                        }
                    }).start();
                }, e -> img.setDrawable((Drawable) Core.atlas.getDrawable("error")));
                t3.add(img).maxHeight(500).pad(10);
            }
            default -> t2.add(new ReadOnlyTextArea("[不支持的消息]", isMe ? myStyle : otherStyle, pane));
        }
        if (isMe) {
            t.add(t2).growX();
            t.table(t4 -> t4.add(i).size(30).pad(15)).growY().get().align(Align.top);
        } else {
            t.table(t4 -> t4.add(i).size(30).pad(15)).growY().get().align(Align.top);
            t.add(t2).growX();
        }
        if (cur == ct && isScrollPaneAtBottom(cur.pane)) setScrollYToButton(cur.pane);
        if (cur != ct) {
            msgUnread++;
            ct.curMsgUnread++;
        }
        Cell cell = chatList.getCell(ct.button);
        chatList.getCells().remove(cell);
        chatList.getCells().insert(0, cell);
    }

    private static void setAvatar(Player p, long avatar) {
        if (avatar == -1) return;
        TextureRegion cache = avatarCache.get(p.id);
        if (cache == null) Http.get("https://q1.qlogo.cn/g?b=qq&s=100&nk=" + avatar, r -> {
            Pixmap pix = new Pixmap(r.getResult());
            Core.app.post(() -> {
                TextureRegion tr = new TextureRegion(new Texture(pix));
                avatarCache.put(p.id, tr);
                pix.dispose();
            });
        }, e -> {
        });
    }

    private static boolean isScrollPaneAtBottom(ScrollPane pane) {
        return pane.getMaxY() - pane.getScrollY() <= pane.getHeight() * 0.05f;
    }

    private static void setScrollYToButton(ScrollPane pane) {
        Element content = pane.getWidget();
        pane.setScrollY(content.getHeight() - pane.getHeight());
    }

    private static void buildButton(@Nullable Player p) {
        ChatTable chatTable = new ChatTable(p);
        Table t = new Table() {
            @Override
            public void draw() {
                if (cur == chatTable) {
                    Draw.color(Color.white);
                    RStyles.black1.draw(x, y, width, height);
                    Draw.color(314045951);
                    Fill.crect(x - 3, y, 3, height);
                }
                if (chatTable.curMsgUnread != 0) {
                    Fonts.outline.setColor(Color.red);
                    Fonts.outline.draw(String.valueOf(chatTable.curMsgUnread), x, y + height);
                }
                super.draw();
            }
        };
        chatTable.button = t;
        t.align(Align.topLeft);
        chatList.add(t).row();
        boolean[] drawCircle = new boolean[]{true};
        Image i = new Image() {
            @Override
            public void draw() {
                if (drawCircle[0]) {
                    Draw.color(imgBackground);
                    Fill.circle(x + width / 2, y + height / 2, 25);
                }
                super.draw();
            }
        };
        t.add(i).size(36).pad(12).get();
        i.setScaling(Scaling.fit);
        Table t2 = t.table().grow().get();
        t2.align(Align.topLeft);
        int id = p == null ? -1 : p.id;
        Label l;
        if (p == null) {
            i.setDrawable(Icon.host);
            l = t2.add("服务器聊天").fillX().get();
            t2.update(() -> {
                if (timer.get(300)) {
                    Vars.net.pingHost(Reflect.get(Vars.ui.join, "lastIp"), Reflect.get(Vars.ui.join, "lastPort"), c -> l.setText(c.name), e -> {
                    });
                }
            });
        } else {
            i.update(() -> i.setDrawable(p.icon()));
            Timer.schedule(() -> {
                TextureRegion tr = avatarCache.get(p.id);
                if (tr == null) return;
                i.update(() -> {
                });
                i.setDrawable(tr);
                drawCircle[0] = false;
            }, 1);
            l = t2.label(() -> "[#" + p.color().toString() + "]" + p.name()).fillX().get();
        }
        l.setAlignment(Align.left);
        t2.row();
        t2.label(() -> chatTable.lastText).left().color(Color.gray);
        t.touchable = Touchable.enabled;
        t.addListener(new HandCursorListener());
        t.clicked(() -> switchChat(id, l, chatTable));
        messages.put(id, chatTable);
        if (cur == null) switchChat(id, l, chatTable);
    }

    private static void switchChat(int id, @Nullable Label l, @Nullable ChatTable chatTable) {
        cur = chatTable;
        if (l == null) {
            chat.update(() -> chat.setTitle("学术聊天"));
        } else {
            chat.update(() -> {
                String title = l.getText().toString();
                if (!Objects.equals(chat.getTitle(), title)) chat.setTitle(title);
            });
            msgUnread -= chatTable.curMsgUnread;
            chatTable.curMsgUnread = 0;
        }
        inner.setElement(chatTable == null ? ph : messages.get(id));
        if (inner.get() != null) inner.get().invalidateHierarchy();
        sizeChanged = true;
    }

    public static void show() {
        chat.add();
    }

    public static void send(String message, @Nullable Player player) throws Exception {
        if (player == null) {
            send0(false, message.getBytes(StandardCharsets.UTF_8), null);
        } else {
            send0(true, encrypt(message.getBytes(StandardCharsets.UTF_8), player), player);
            addMessage(Vars.player, message, true, cur);
        }
    }

    private static void send0(boolean isPrivate, byte[] data, @Nullable Player target) {
        if (data == null) return;
        ARCClient.send("ARCChat", s -> {
            try {
                s.writeBoolean(isPrivate);
                if (isPrivate) s.writeInt(target.id);
                s.writeShort(data.length);
                s.write(data);
            } catch (Exception ignored) {
            }
        });
    }

    static class ReadOnlyTextArea extends TextArea {
        float prefW = 0, prefH = 0;
        boolean prefSizeInvalid = true;
        ScrollPane pane;

        public ReadOnlyTextArea(String text, TextFieldStyle style, ScrollPane pane) {
            super(text, style);
            this.pane = pane;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (sizeChanged) invalidateHierarchy();
        }

        @Override
        public void invalidate() {
            super.invalidate();
            prefSizeInvalid = true;
        }

        @Override
        public float getPrefWidth() {
            if (prefSizeInvalid) computePrefSize();
            return prefW;
        }

        @Override
        public float getPrefHeight() {
            if (prefSizeInvalid) computePrefSize();
            return prefH;
        }

        private void computePrefSize() {
            if (pane == null) return;
            prefSizeInvalid = false;
            glyphLayout.setText(style.font, text, Color.white, Math.max(pane.getWidth() - style.background.getLeftWidth() - style.background.getRightWidth() - 60 - 10, 100), Align.left, true);
            prefW = glyphLayout.width + style.background.getLeftWidth() + style.background.getRightWidth();
            prefH = glyphLayout.height + style.background.getTopHeight() + style.background.getBottomHeight() + 6;
        }

        @Override
        protected InputListener createInputListener() {
            return new ReadOnlyTextAreaListener();
        }

        public class ReadOnlyTextAreaListener extends TextAreaListener {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                if (disabled) return false;
                if (imeData != null) return true;
                lastBlink = 0;
                cursorOn = false;
                Scene stage = getScene();
                if (stage == null || stage.getKeyboardFocus() != ReadOnlyTextArea.this) return false;
                boolean ctrl = Core.input.ctrl() && !Core.input.alt();
                if (ctrl) {
                    if (keycode == KeyCode.c || keycode == KeyCode.insert) {
                        copy();
                        return true;
                    }
                    if (keycode == KeyCode.a) {
                        selectAll();
                        return true;
                    }
                }
                return true;
            }

            @Override
            public boolean keyTyped(InputEvent event, char character) {
                return false;
            }
        }
    }

    static class ChatTable extends Table {
        public Table messages;
        public ScrollPane pane;
        public String lastText;
        public int curMsgUnread = 0;
        public Table button;

        public ChatTable(@Nullable Player p) {
            super();
            touchable = Touchable.enabled;
            pane = pane(t -> {
                t.align(Align.top);
                messages = t.table().growX().get();
                t.row();
                t.table(t2 -> t2.update(() -> {
                    if (sizeChanged) {
                        sizeChanged = false;
                        if (cur != null) cur.pane.layout();
                    }
                })).grow();
            }).grow().get();
            pane.setScrollingDisabledX(true);
            row();
            Table t3 = table().minHeight(200).maxHeight(300).growX().get();
            t3.add(new Table(tools -> {
                tools.defaults().size(32);
                ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle(clearAccentNonei);
                style.imageUpColor = Color.gray;
                tools.align(Align.left);
                tools.button(Icon.imageSmall, style, () -> Vars.platform.showFileChooser(true, "png", f -> {
                    if (f.length() > 32 * 1024 * 1024) {
                        Vars.ui.showInfo("[red]文件过大!");
                        return;
                    }
                    RFuncs.uploadToWeb(f, s -> Core.app.post(() -> {
                        try {
                            send("I" + s, p);
                        } catch (Exception e) {
                            Vars.ui.showException(e);
                        }
                    }));
                }));
            }) {
                @Override
                public void draw() {
                    super.draw();
                    Draw.color(Color.gray);
                    Lines.stroke(1);
                    Lines.line(x, y + height, x + width, y + height);
                }
            }).growX().row();
            TextArea area = t3.area("", inputStyle, s -> {
            }).grow().get();
            t3.row();
            t3.table(t4 -> {
                t4.defaults().size(80, 32).pad(6);
                t4.align(Align.right);
                t4.button("发送", bs, () -> {
                    if (Objects.equals(area.getText(), "")) return;
                    try {
                        send("M" + area.getText(), p);
                        area.setText("");
                    } catch (Exception e) {
                        Vars.ui.showException(e);
                    }
                });
            }).growX().get();
            addListener(new InputListener() {
                @SuppressWarnings("unchecked")
                final Cell<Table> cell = getCell(t3);
                float last, start;
                boolean restored = true, dragging = false;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    Vec2 v = t3.localToStageCoordinates(Tmp.v1.set(x, y));
                    dragging = 0 < x && x < t3.getWidth() && t3.getHeight() - 7 < y && y < t3.getHeight() + 7;
                    last = v.y;
                    start = t3.getHeight();
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    if (!dragging) return;
                    Vec2 v = t3.localToStageCoordinates(Tmp.v1.set(x, y));
                    cell.minHeight(Math.min(start + v.y - last, cell.maxHeight()));
                    invalidate();
                }

                @Override
                public boolean mouseMoved(InputEvent event, float x, float y) {
                    if (0 < x && x < t3.getWidth() && t3.getHeight() - 7 < y && y < t3.getHeight() + 7) {
                        restored = false;
                        Core.graphics.cursor(arcui.resizeVerticalCursor);
                    } else if (!restored) {
                        restored = true;
                        Core.graphics.restoreCursor();
                    }
                    return true;
                }
            });
        }
    }
}
