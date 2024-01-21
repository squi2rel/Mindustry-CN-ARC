package mindustry.annotations.misc.JS;

import arc.struct.ObjectMap;
import arc.struct.OrderedMap;

public class ObjectBuilder extends JSBuilder {
    OrderedMap<String, JSBuilder> map = new OrderedMap<>();
    int spaces;

    ObjectBuilder(int spaces) {
        this.spaces = spaces;
        noWarp = noSemicolon = true;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder("{");
        int codeSpaces = spaces + 4;
        ObjectMap.Entries<String, JSBuilder> list = map.entries();
        for (ObjectMap.Entry<String, JSBuilder> name : list) {
            JSBuilder code = name.value;
            if (code == null) {
                sb.append("\n").append(calcSpace(codeSpaces)).append(name.key).append(",");
            } else {
                if (code.isBlock) continue;
                sb.append("\n").append(calcSpace(codeSpaces)).append(name.key).append(" : ").append(code.build()).append(",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("\n").append(calcSpace(spaces)).append("}");
        return sb.toString();
    }

    public ObjectBuilder put(String name, JSBuilder code) {
        map.put(name, code);
        return this;
    }

    public ObjectBuilder put(String name) {
        map.put(name, null);
        return this;
    }

    public JSBuilder get(String name) {
        return map.get(name);
    }

    public ObjectBuilder remove(String name) {
        map.remove(name);
        return this;
    }
}
