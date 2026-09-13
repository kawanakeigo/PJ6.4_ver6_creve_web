package world.creve.nlj.controller;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import world.creve.nlj.service.NljService;
import world.creve.platform.service.CreatorService;
@Controller public class NljController {
    private final NljService nlj;
    private final CreatorService creators;
    public NljController(NljService nlj,CreatorService creators) {
        this.nlj=nlj;
        this.creators=creators;
    }
    @GetMapping("/NLJ")public String showNljList(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("events",nlj.list(page));
        return "nlj/list";
    }
    @GetMapping("/NLJ/{eventSlug}")public String showNljDetail(@PathVariable String eventSlug,Model model) {
        var event=nlj.detail(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("creators",creators.getCreatorsByEventId(event.eventId()));
        return "nlj/detail";
    }
    @GetMapping("/live")public ResponseEntity<Void> redirectLegacyList() {
        return permanent(URI.create("/NLJ"));
    }
    @GetMapping("/live/{eventSlug}")public ResponseEntity<Void> redirectLegacyDetail(@PathVariable String eventSlug) {
        URI location=UriComponentsBuilder.fromPath("/NLJ/{eventSlug}").buildAndExpand(eventSlug).encode().toUri();
        return permanent(location);
    }
    private ResponseEntity<Void> permanent(URI location) {
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(location).build();
    }
}
