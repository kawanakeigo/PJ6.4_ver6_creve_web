package jp.creve.site.web;

import jp.creve.site.service.HomeQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OfficialPageController {
    private final HomeQueryService home;

    public OfficialPageController(HomeQueryService home) {
        this.home = home;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("projectLinks", home.projectLinks());
        model.addAttribute("news", home.news().stream().limit(3).toList());
        model.addAttribute("rooms", home.rooms().stream().limit(3).toList());
        return "site/index";
    }

    @GetMapping("/news")
    public String news(Model model) {
        model.addAttribute("news", home.news());
        return "site/news";
    }

    @GetMapping({"/about", "/contact", "/privacy", "/terms", "/vr"})
    public String content(jakarta.servlet.http.HttpServletRequest request, Model model) {
        String type = request.getRequestURI().substring(1).toUpperCase(java.util.Locale.ROOT);
        model.addAttribute("content", home.content(type));
        return "site/content";
    }

    @GetMapping("/discover")
    public String discover(Model model) {
        model.addAttribute("rooms", home.rooms());
        return "site/discover";
    }
}
