package mindustry.annotations.misc.JS;

import java.util.ArrayList;

public class ClassBuilder extends JSBuilder {
    String className;
    CodeBlock builder = new CodeBlock(0);
    ArrayList<CodeBlock> vars = new ArrayList<>();
    ArrayList<CodeBlock> func = new ArrayList<>();
    int spaces;

    public ClassBuilder(String name, int spaces) {
        className = name + " ";
        this.spaces = spaces;
        isBlock = noSemicolon = true;
    }

    public ClassBuilder(int spaces) {
        className = "";
        this.spaces = spaces;
        isBlock = noSemicolon = true;
    }

    public ClassBuilder(String name, String extend, int spaces) {
        className = name + " extends " + extend;
        this.spaces = spaces;
        isBlock = noSemicolon = forceWarp = true;
    }

    public ClassBuilder add(String code) {
        builder.add(code);
        return this;
    }

    public ClassBuilder add(CodeBlock code) {
        builder.add(code);
        return this;
    }

    public void addVariable(String name) {
        vars.add(new CodeBlock(name + ";"));
    }

    public void addVariable(String name, String defaultValue) {
        vars.add(new CodeBlock(name + " = " + defaultValue + ";"));
    }

    public CodeBlock addMethod(String name, String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String i : args) {
            sb.append(i).append(", ");
        }
        if (args.length != 0) sb.delete(sb.length() - 2, sb.length());
        CodeBlock cb = new CodeBlock(spaces + 4);
        func.add(cb);
        return cb.addSynx(name, sb.toString());
    }

    @Override
    public String build() {
        String space = calcSpace(spaces);
        String space2 = calcSpace(spaces + 4);
        StringBuilder sb = new StringBuilder(space + "class " + className + " {");
        for (CodeBlock i : vars) {
            sb.append("\n").append(space2).append(i.build());
        }
        for (CodeBlock i : func) {
            sb.append("\n").append(i.build());
        }
        sb.append("\n").append(space).append("}");
        return sb.toString();
    }
}
