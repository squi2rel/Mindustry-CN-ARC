package mindustry.annotations.misc.JS;

import arc.func.Cons;

import java.util.ArrayList;

public class CodeBlock extends JSBuilder {
    ArrayList<JSBuilder> list;
    String thisCode;
    Boolean isBlock = false;
    int spaces;
    JSBuilder lastBlock;

    public CodeBlock(String code) {
        thisCode = code;
    }

    public CodeBlock(int spaces) {
        list = new ArrayList<>();
        isBlock = true;
        this.spaces = spaces;
    }

    public static String clean(StringBuilder input) {
        if (input.length() == 0) return "";
        if (input.charAt(input.length() - 1) == ';') input.deleteCharAt(input.length() - 1);
        return input.toString();
    }

    public CodeBlock add(String str, Cons<CodeBlock> cbcb) {
        CodeBlock cb = new CodeBlock(str);
        list.add(cb);
        cbcb.get(cb);
        return this;
    }

    public CodeBlock add(String str) {
        list.add(new CodeBlock(str));
        return this;
    }

    public CodeBlock add(JSBuilder code) {
        list.add(code);
        return this;
    }

    public CodeBlock addSynx(String synx, String cond) {
        add(synx + "(" + cond + ") {", cb -> cb.noSemicolon = true);
        CodeBlock cb = newBlock();
        add("}", cbcb -> cbcb.noSemicolon = true);
        cb.noSemicolon = true;
        return cb;
    }

    public CodeBlock addMethod(String name, String args) {
        CodeBlock cb = new CodeBlock(spaces);
        cb.noSemicolon = true;
        add(cb);
        return cb.addSynx("function " + name, args);
    }

    @Override
    public String build() {
        if (!isBlock) return thisCode;
        StringBuilder sb = new StringBuilder();
        String space = calcSpace(spaces);
        Boolean[] next = {false};
        lastBlock = new CodeBlock("");
        for (JSBuilder code : list) {
            if (code.isBlock) {
                if (code.forceWarp || !lastBlock.noWarp && !code.noWarp) sb.append("\n");
                sb.append(code.build());
                if (code.forceSemicolon) sb.append(";");
            } else {
                if (next[0]) {
                    if (code.forceWarp || !lastBlock.noWarp && !code.noWarp) sb.append("\n");
                } else {
                    next[0] = true;
                }
                sb.append(space).append(code.build());
                if (code.forceSemicolon || !code.noSemicolon) sb.append(";");
            }
            lastBlock = code;
        }
        return clean(sb);
    }

    public ClassBuilder newClass(String name) {
        ClassBuilder cb = new ClassBuilder(name, spaces);
        add(cb);
        return cb;
    }

    public ClassBuilder newClass(String name, String extend) {
        ClassBuilder cb = new ClassBuilder(name, extend, spaces);
        add(cb);
        return cb;
    }

    public CodeBlock newBlock() {
        CodeBlock cb = new CodeBlock(spaces + 4);
        add(cb);
        return cb;
    }

    public CodeBlock newClosure() {
        CodeBlock cb = new CodeBlock(spaces + 4);
        add("(()=>{");
        add(cb);
        add("})()");
        return cb;
    }

    public CodeBlock newExp(JSBuilder code) {
        CodeBlock cb = new CodeBlock("(" + code.build() + ")");
        return this;
    }

    public ObjectBuilder newObject() {
        ObjectBuilder ob = new ObjectBuilder(spaces);
        add(ob);
        return ob;
    }

    public CodeBlock nextLine() {
        add("", cb -> cb.noSemicolon = cb.forceWarp = true);
        return this;
    }
}
