package world.creve.portal.controller;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.spi.ProjectContribution;
import world.creve.platform.service.EventService;
import world.creve.portal.service.*;
@Controller public class CreveController {
    private final EventService events;
    private final NewsService news;
    private final List<ProjectContribution> projects;
    private final PublicContentService publicContent;
    public CreveController(EventService events,NewsService news,List<ProjectContribution> projects,PublicContentService publicContent) {
        this.events=events;
        this.news=news;
        this.projects=projects;
        this.publicContent=publicContent;
    }
    @GetMapping("/")public String showTopPage(Model model) {
        Map<String,Object> projectEvents=new LinkedHashMap<>();
        for(var p:projects)projectEvents.put(p.key(),p.upcomingEvents());
        model.addAttribute("projectEvents",projectEvents);
        model.addAttribute("playpitEvents",projectEvents.getOrDefault("PLAYPIT",List.of()));
        model.addAttribute("liveEvents",projectEvents.getOrDefault("LIVE",List.of()));
        model.addAttribute("newsList",events.getLatestNews());
        return "creve/index";
    }
    @GetMapping("/about")public String showAbout() {
        return "creve/about";
    }
    @GetMapping("/vr")public String showVr() {
        return "creve/vr";
    }
    @GetMapping("/news")public String showNews(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("newsPage",news.list(page,30));
        return "creve/news/list";
    }
    @GetMapping( {
        "/contact","/privacy","/terms"
    }
    )public String showPublicContent(jakarta.servlet.http.HttpServletRequest request,Model model) {
        String kind=request.getServletPath().substring(1);
        model.addAttribute("kind",kind);
        model.addAttribute("content",publicContent.content(kind));
        return "creve/public-content";
    }
}
