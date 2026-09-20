package com.newlegacyjam.platform.api;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    private final String crevePublicBaseUrl;

    public PageController(@Value("${nlj.creve-public-base-url:http://localhost:8080}") String crevePublicBaseUrl) {
        URI uri = URI.create(crevePublicBaseUrl.strip());
        if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("nlj.creve-public-base-url must be an HTTP(S) URL");
        }
        String normalized = uri.toString();
        this.crevePublicBaseUrl = normalized.endsWith("/")
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    @GetMapping({"/", "/index.html"})
    public String index(Model model) {
        model.addAttribute("crevePublicBaseUrl", crevePublicBaseUrl);
        return "index";
    }
    @GetMapping("/event.html") public String event() { return "event"; }
    @GetMapping("/vote.html") public String vote() { return "vote"; }
    @GetMapping("/me.html") public String me() { return "me"; }
    @GetMapping("/ranking.html") public String ranking() { return "ranking"; }
}
