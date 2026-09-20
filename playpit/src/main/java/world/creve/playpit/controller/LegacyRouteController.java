package world.creve.playpit.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class LegacyRouteController {
    @GetMapping("/playpit/{eventSlug}/uka")
    public RedirectView retiredUka(@PathVariable String eventSlug) {
        RedirectView redirect = new RedirectView("/playpit/" + eventSlug);
        redirect.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        return redirect;
    }
}
