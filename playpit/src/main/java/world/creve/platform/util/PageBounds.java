package world.creve.platform.util;
public final class PageBounds {
    private PageBounds() {
    }
    public static int page(int page) {
        return Math.max(0,page);
    }
    public static int size(int size) {
        return Math.min(100,Math.max(1,size));
    }
}
