package mindustry.annotations.misc.JS;

public abstract class JSBuilder {
    public boolean isBlock;
    public boolean noSemicolon;
    public boolean noWarp;
    public boolean forceSemicolon;
    public boolean forceWarp;

    JSBuilder() {

    }

    public abstract String build();

    String calcSpace(int space) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < space; i++) sb.append(" ");
        return sb.toString();
    }
}
