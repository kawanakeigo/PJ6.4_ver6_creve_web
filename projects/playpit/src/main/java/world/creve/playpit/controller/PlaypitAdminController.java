package world.creve.playpit.controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import world.creve.playpit.dto.request.*;
import world.creve.playpit.service.ContentAdminService;
@Controller @RequestMapping("/admin") public class PlaypitAdminController {
    private final ContentAdminService content;
    public PlaypitAdminController(ContentAdminService content) {
        this.content=content;
    }
    @GetMapping("/events")public String listEvent(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("items",content.events(page));
        return "playpit/admin/events";
    }
    @GetMapping("/events/new")public String newEvent(Model model) {
        var f=new EventForm();
        f.setStatus("DRAFT");
        f.setEventType("PLAYPIT");
        model.addAttribute("form",f);
        model.addAttribute("id",null);
        return "playpit/admin/event-form";
    }
    @GetMapping("/events/{id}/edit")public String editEvent(@PathVariable Long id,Model model) {
        var e=content.event(id);
        var f=new EventForm();
        f.setEventType(e.getEventType());
        f.setSlug(e.getSlug());
        f.setTitle(e.getTitle());
        f.setSummary(e.getSummary());
        f.setDescription(e.getDescription());
        f.setMainImageUrl(e.getMainImageUrl());
        f.setStartAt(e.getStartAt());
        f.setEndAt(e.getEndAt());
        f.setVenueName(e.getVenueName());
        f.setAddress(e.getAddress());
        f.setAccess(e.getAccess());
        f.setPrice(e.getPrice());
        f.setTicketUrl(e.getTicketUrl());
        f.setStatus(e.getStatus());
        model.addAttribute("form",f);
        model.addAttribute("id",id);
        model.addAttribute("participants",content.participants(id));
        model.addAttribute("exhibitions",content.exhibitions(id));
        return "playpit/admin/event-form";
    }
    @PostMapping("/events")public String createEvent(@Valid @ModelAttribute("form")EventForm f,BindingResult errors,Model model) {
        model.addAttribute("id",null);
        if(errors.hasErrors())return "playpit/admin/event-form";
        Long id=content.saveEvent(null,f);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
    @PostMapping("/events/{id}")public String updateEvent(@PathVariable Long id,@Valid @ModelAttribute("form")EventForm f,BindingResult errors,Model model) {
        model.addAttribute("id",id);
        model.addAttribute("participants",content.participants(id));
        model.addAttribute("exhibitions",content.exhibitions(id));
        if(errors.hasErrors())return "playpit/admin/event-form";
        content.saveEvent(id,f);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
    @GetMapping("/creators")public String listCreator(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("items",content.creators(page));
        return "playpit/admin/creators";
    }
    @GetMapping("/creators/new")public String newCreator(Model model) {
        var f=new CreatorForm();
        f.setStatus("DRAFT");
        model.addAttribute("form",f);
        model.addAttribute("id",null);
        return "playpit/admin/creator-form";
    }
    @GetMapping("/creators/{id}/edit")public String editCreator(@PathVariable Long id,Model model) {
        var e=content.creator(id);
        var f=new CreatorForm();
        f.setSlug(e.getSlug());
        f.setName(e.getName());
        f.setNameKana(e.getNameKana());
        f.setProfile(e.getProfile());
        f.setConcept(e.getConcept());
        f.setGenre(e.getGenre());
        f.setProfileImageUrl(e.getProfileImageUrl());
        f.setWebsiteUrl(e.getWebsiteUrl());
        f.setStatus(e.getStatus());
        f.setSnsLinks(content.snsLines(id));
        model.addAttribute("form",f);
        model.addAttribute("id",id);
        return "playpit/admin/creator-form";
    }
    @PostMapping("/creators")public String createCreator(@Valid @ModelAttribute("form")CreatorForm f,BindingResult errors,Model model) {
        model.addAttribute("id",null);
        if(errors.hasErrors())return "playpit/admin/creator-form";
        Long id=content.saveCreator(null,f);
        return "redirect:/admin/creators/"+id+"/edit?saved";
    }
    @PostMapping("/creators/{id}")public String updateCreator(@PathVariable Long id,@Valid @ModelAttribute("form")CreatorForm f,BindingResult errors,Model model) {
        model.addAttribute("id",id);
        if(errors.hasErrors())return "playpit/admin/creator-form";
        content.saveCreator(id,f);
        return "redirect:/admin/creators/"+id+"/edit?saved";
    }
    @GetMapping("/artworks")public String listArtwork(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("items",content.artworks(page));
        return "playpit/admin/artworks";
    }
    @GetMapping("/artworks/new")public String newArtwork(Model model) {
        var f=new ArtworkForm();
        f.setStatus("DRAFT");
        model.addAttribute("form",f);
        model.addAttribute("id",null);
        return "playpit/admin/artwork-form";
    }
    @GetMapping("/artworks/{id}/edit")public String editArtwork(@PathVariable Long id,Model model) {
        var e=content.artwork(id);
        var f=new ArtworkForm();
        f.setCreatorId(e.getCreatorId());
        f.setSlug(e.getSlug());
        f.setTitle(e.getTitle());
        f.setDescription(e.getDescription());
        f.setBackground(e.getBackground());
        f.setConcept(e.getConcept());
        f.setMaterials(e.getMaterials());
        f.setProductionYear(e.getProductionYear());
        f.setMainImageUrl(e.getMainImageUrl());
        f.setStatus(e.getStatus());
        f.setMediaLines(content.mediaLines(id));
        model.addAttribute("form",f);
        model.addAttribute("id",id);
        return "playpit/admin/artwork-form";
    }
    @PostMapping("/artworks")public String createArtwork(@Valid @ModelAttribute("form")ArtworkForm f,BindingResult errors,Model model) {
        model.addAttribute("id",null);
        if(errors.hasErrors())return "playpit/admin/artwork-form";
        Long id=content.saveArtwork(null,f);
        return "redirect:/admin/artworks/"+id+"/edit?saved";
    }
    @PostMapping("/artworks/{id}")public String updateArtwork(@PathVariable Long id,@Valid @ModelAttribute("form")ArtworkForm f,BindingResult errors,Model model) {
        model.addAttribute("id",id);
        if(errors.hasErrors())return "playpit/admin/artwork-form";
        content.saveArtwork(id,f);
        return "redirect:/admin/artworks/"+id+"/edit?saved";
    }
    @PostMapping("/events/{id}/participants")public String participant(@PathVariable Long id,@RequestParam Long creatorId,@RequestParam(defaultValue="0")int displayOrder,@RequestParam(required=false)String exhibitionTitle) {
        content.addParticipant(id,creatorId,displayOrder,exhibitionTitle);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
    @PostMapping("/events/{id}/exhibitions")public String exhibition(@PathVariable Long id,@RequestParam Long artworkId,@RequestParam(defaultValue="0")int displayOrder,@RequestParam(required=false)String exhibitionArea) {
        content.addExhibition(id,artworkId,displayOrder,exhibitionArea);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
}
