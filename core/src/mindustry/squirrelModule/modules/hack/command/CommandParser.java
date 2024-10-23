package mindustry.squirrelModule.modules.hack.command;

import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.squirrelModule.modules.tools.SMisc;
import mindustry.ui.dialogs.BaseDialog;

import java.util.Arrays;
import java.util.Objects;

import static mindustry.arcModule.ARCVars.getThemeColor;
import static mindustry.Vars.ui;
import static mindustry.squirrelModule.modules.hack.Hack.sui;
import static mindustry.squirrelModule.modules.hack.Hack.keyMap;

public class CommandParser {
    private static final ObjectMap<String, Command> cmd = new ObjectMap<>();
    public static boolean resolveMessage(String msg) {
        if (!msg.startsWith(".")) return false;
        String[] args = msg.substring(1).split(" ");
        try {
            Command c = cmd.get(args[0]);
            if (c == null) {
                ui.chatfrag.addMessage("[scarlet]无效的指令 输入.help查看帮助 以..开头会变成普通消息");
                return true;
            }
            c.get(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            Log.err(e);
            ui.chatfrag.addMessage("[scarlet]参数错误");
        }
        return true;
    }

    public static void registerCmd(Command c) {
        cmd.put(c.name, c);
    }

    public abstract static class Command {
        public String name, desc, args;
        public Command(String name, String desc, String... args) {
            this.name = name;
            this.desc = desc;
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg).append(' ');
            }
            this.args = sb.toString();
        }
        public abstract void get(String[] args);
    }

    public static void initCommands() {
        registerCmd(new Command("help", "查看帮助", "[page]") {
            @Override
            public void get(String[] args) {
                int page = 1;
                if (args.length != 0) {
                    page = Integer.parseInt(args[0]);
                }
                int allPage = Mathf.ceil(cmd.size / 10f);
                if (page >= allPage) {
                    page = allPage;
                } else if (page < 1) {
                    page = 1;
                }
                StringBuilder sb = new StringBuilder("帮助 第");
                sb.append(page).append("/").append(allPage).append("页");
                String[] keys = cmd.keys().toSeq().toArray(String.class);
                for (int i = (page - 1) * 10; i < Math.min(cmd.size, page * 10); i++) {
                    Command c = cmd.get(keys[i]);
                    sb.append("\n").append(keys[i]).append(" ").append(c.args).append(" -").append(c.desc);
                }
                ui.chatfrag.addMessage(SMisc.color(sb.toString(), 3, sui.infoControl.getColor()));
            }
        });
        registerCmd(new Command("bind", "绑定功能到指定键位", "<func>", "<key>") {
            @Override
            public void get(String[] args) {
                boolean[] found = {false};
                sui.infoControl.manager.flatList.each((s, c) -> {
                    if (s.equalsIgnoreCase(args[0]) || args[0].equalsIgnoreCase(c.displayName)) {
                        found[0] = true;
                        if (Objects.equals(args[1], "off") || Objects.equals(args[1], "none") || Objects.equals(args[1], "null")) {
                            keyMap.remove(c);
                            Core.settings.remove("key-" + c.internalName);
                            ui.chatfrag.addMessage("[green]已解除 " + c.displayName + " 的键位绑定");
                            return;
                        }
                        KeyCode key = null;
                        for (KeyCode k : KeyCode.all) {
                            if (k.name().equalsIgnoreCase(args[1])) key = k;
                        }
                        if (key == null) {
                            ui.chatfrag.addMessage("[scarlet]键位 " + args[1] + " 未找到!");
                            return;
                        }
                        Core.settings.put("key-" + c.internalName, key.name());
                        keyMap.put(c, key);
                        ui.chatfrag.addMessage("[green]已将 " + c.displayName + " 绑定到键位 " + key);
                    }
                });
                if (!found[0]) ui.chatfrag.addMessage("[scarlet]功能 " + args[0] + " 未找到!");
            }
        });
        registerCmd(new Command("binds", "打开键位绑定菜单") {
            @Override
            public void get(String[] args) {
                BaseDialog b = new BaseDialog("键位绑定");
                b.cont.table(t1 -> t1.pane(t2 -> sui.infoControl.manager.list.each((t, seq) -> {
                    t2.table(t3 -> {
                        t3.table(t4 -> t4.add(t).color(getThemeColor())).row();
                        t3.image().height(4).growX().pad(2).color(getThemeColor());
                    }).growX().row();
                    seq.each(c -> t2.table(t3 -> {
                        t3.table(t4 -> t4.label(() -> SMisc.packColor(sui.infoControl.getColor()) + c.displayName)).growX();
                        t3.table(t4 -> t4.label(() -> {
                            KeyCode name = keyMap.get(c);
                            return name == null ? "无" : name.value;
                        })).growX();
                        t3.table(t4 -> t4.button("重新绑定", () -> {
                            BaseDialog bind = new BaseDialog("绑定");
                            bind.addListener(new InputListener() {
                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                                    bind.hide();
                                    if(Core.app.isAndroid()) return false;
                                    keyMap.put(c, button);
                                    Core.settings.put("key-" + c.internalName, button.name());
                                    return false;
                                }

                                @Override
                                public boolean keyDown(InputEvent event, KeyCode keycode){
                                    bind.hide();
                                    if (keycode == KeyCode.escape) {
                                        keyMap.remove(c);
                                        Core.settings.remove("key-" + c.internalName);
                                        return false;
                                    }
                                    keyMap.put(c, keycode);
                                    Core.settings.put("key-" + c.internalName, keycode.name());
                                    return false;
                                }
                            });
                            bind.cont.add("请按一个键...");
                            bind.show();
                            Time.runTask(1f, () -> b.getScene().setScrollFocus(bind));
                        }).growX()).growX();
                    }).growX().row());
                })).grow()).grow();
                b.addCloseButton();
                b.show();
            }
        });
        registerCmd(new Command("colorful", "发送炫彩消息", "<msg>") {
            @Override
            public void get(String[] args) {
                if (args.length == 0) {
                    ui.chatfrag.addMessage("[red]不能发送空消息");
                    return;
                }
                String raw = Strings.join(" ", args);
                String msg = SMisc.color(args[0], 3, sui.infoControl.getColor(), false);
                if (msg.length() > Vars.maxTextLength) {
                    int max = Vars.maxTextLength / 10;
                    ui.chatfrag.addMessage("[red]文字过长! 限制为 " + max + " 个字符(" + Vars.maxTextLength +  "), 实际为 " + raw.length() + " 个字符(" + msg.length() + ")");
                    return;
                }
                Call.sendChatMessage(msg);
            }
        });
    }
}
