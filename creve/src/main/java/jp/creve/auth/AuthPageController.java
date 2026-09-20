package jp.creve.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {
    private final boolean studioEnabled;

    public AuthPageController(@Value("${features.studio:false}") boolean studioEnabled) {
        this.studioEnabled = studioEnabled;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/account/portal")
    public String portal(Model model) {
        model.addAttribute("studioEnabled", studioEnabled);
        return "account/portal";
    }

    @GetMapping("/studio")
    public String studio(Model model) {
        if (!studioEnabled) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND);
        model.addAttribute("studioState", "公開済み 1件 / 審査待ち 0件");
        return "studio/home";
    }
}
