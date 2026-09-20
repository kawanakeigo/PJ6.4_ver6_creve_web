package jp.creve.site.web;

import jp.creve.site.service.PublicRoomQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PublicRoomController {
    private final PublicRoomQueryService rooms;

    public PublicRoomController(PublicRoomQueryService rooms) {
        this.rooms = rooms;
    }

    @GetMapping("/r/{slug}")
    public String room(@PathVariable String slug, Model model) {
        model.addAttribute("room", rooms.getBySlug(slug));
        return "site/room";
    }
}
