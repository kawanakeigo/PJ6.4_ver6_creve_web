package world.creve.platform.util;
import java.net.URI;
/** Reject executable URL schemes and protocol-relative URLs. No server-side remote fetch. */
public final class SafeUrls {
    private SafeUrls() {
    }
    public static String optional(String value) {
        if(value==null || value.isBlank())return null;
        String s=value.trim();
        if(s.chars().anyMatch(c->c<32 || c==127) || s.contains("\\"))throw new IllegalArgumentException("Invalid URL");
        if(s.startsWith("/")&&!s.startsWith("//"))return s;
        URI uri=URI.create(s);
        String scheme=uri.getScheme();
        if(!"https".equalsIgnoreCase(scheme)&&!"http".equalsIgnoreCase(scheme))throw new IllegalArgumentException("Invalid URL scheme");
        if(uri.getHost()==null || uri.getUserInfo()!=null)throw new IllegalArgumentException("Invalid URL host");
        return s;
    }
    public static String slug(String value) {
        if(value==null || !value.matches("[a-z0-9][a-z0-9-]{0,99}"))throw new IllegalArgumentException("Invalid slug");
        return value;
    }
}
