package world.creve.playpit.controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import world.creve.playpit.dto.request.*;
import world.creve.playpit.service.ContentAdminService;
import world.creve.playpit.service.UploadedContentService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.SmartValidator;
import org.springframework.dao.DataIntegrityViolationException;
import world.creve.platform.exception.InvalidMessageException;
@Controller @RequestMapping("/admin") public class PlaypitAdminController {
    private final ContentAdminService content;
    private final UploadedContentService uploads;
    private final SmartValidator validator;
    public PlaypitAdminController(ContentAdminService content,UploadedContentService uploads,SmartValidator validator) {
        this.content=content;this.uploads=uploads;this.validator=validator;
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
    @PostMapping("/events")public String createEvent(@Valid @ModelAttribute("form")EventForm f,BindingResult errors,@RequestParam(required=false)MultipartFile eventImage,Model model) {return saveEvent(null,f,errors,eventImage,model);}
    @PostMapping("/events/{id}")public String updateEvent(@PathVariable Long id,@Valid @ModelAttribute("form")EventForm f,BindingResult errors,@RequestParam(required=false)MultipartFile eventImage,Model model) {return saveEvent(id,f,errors,eventImage,model);}
    private String saveEvent(Long id,EventForm f,BindingResult errors,MultipartFile image,Model model){
        model.addAttribute("id",id);
        if(id!=null){model.addAttribute("participants",content.participants(id));model.addAttribute("exhibitions",content.exhibitions(id));}
        if(!errors.hasErrors())try{return "redirect:/admin/events/"+uploads.saveEvent(id,f,image)+"/edit?saved";}
        catch(InvalidMessageException e){errors.reject("save",e.getMessage());}
        catch(DataIntegrityViolationException e){errors.reject("save","保存できませんでした。識別子などの入力を確認してください。");}
        return "playpit/admin/event-form";
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
    @GetMapping("/creators/{id}/edit")public String editCreator(@PathVariable Long id,@RequestParam(defaultValue="0")int artworkPage,Model model) {
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
        creatorWorks(id,artworkPage,model);
        return "playpit/admin/creator-form";
    }
    private void creatorWorks(Long id,int page,Model model) {
        model.addAttribute("id",id);
        if(id!=null)model.addAttribute("creatorArtworks",content.artworksByCreator(id,page));
    }
    @PostMapping("/creators")public String createCreator(@Valid @ModelAttribute("form")CreatorForm f,BindingResult errors,@RequestParam(required=false)MultipartFile profileImage,Model model) {
        return saveCreator(null,f,errors,profileImage,model);
    }
    @PostMapping("/creators/{id}")public String updateCreator(@PathVariable Long id,@Valid @ModelAttribute("form")CreatorForm f,BindingResult errors,@RequestParam(required=false)MultipartFile profileImage,Model model) {
        content.creator(id);return saveCreator(id,f,errors,profileImage,model);
    }
    private String saveCreator(Long id,CreatorForm f,BindingResult errors,MultipartFile image,Model model) {
        creatorWorks(id,0,model);
        if(!errors.hasErrors())try {
            Long saved=uploads.saveCreator(id,f,image);
            return "redirect:/admin/creators/"+saved+"/edit?saved";
        } catch(InvalidMessageException e){errors.reject("save",e.getMessage());}
          catch(DataIntegrityViolationException e){errors.reject("save","保存できませんでした。URL識別子などの入力内容を確認してください。");}
        return "playpit/admin/creator-form";
    }
    @GetMapping("/artworks")public String listArtwork(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("items",content.artworks(page));
        return "playpit/admin/artworks";
    }
    @GetMapping("/artworks/new")public String newArtwork(Model model) {
        var f=new ArtworkForm();
        f.setStatus("DRAFT");
        model.addAttribute("form",f);
        artworkModel(f,null,null,model);
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
        artworkModel(f,id,e.getCreatorId(),model);
        return "playpit/admin/artwork-form";
    }
    @GetMapping("/creators/{creatorId}/artworks/new")public String newCreatorArtwork(@PathVariable Long creatorId,Model model) {
        var f=new ArtworkForm();f.setStatus("DRAFT");f.setCreatorId(creatorId);
        model.addAttribute("form",f);artworkModel(f,null,creatorId,model);
        return "playpit/admin/artwork-form";
    }
    private void artworkModel(ArtworkForm form,Long id,Long fixedCreatorId,Model model) {
        model.addAttribute("id",id);
        model.addAttribute("fixedCreator",fixedCreatorId==null?null:content.creator(fixedCreatorId));
        model.addAttribute("formAction",id!=null?"/admin/artworks/"+id:fixedCreatorId!=null?"/admin/creators/"+fixedCreatorId+"/artworks":"/admin/artworks");
    }
    @PostMapping("/creators/{creatorId}/artworks")public String createCreatorArtwork(@PathVariable Long creatorId,@ModelAttribute("form")ArtworkForm f,BindingResult errors,@RequestParam(required=false)MultipartFile artworkImage,Model model) {
        content.creator(creatorId);f.setCreatorId(creatorId);validator.validate(f,errors);
        return saveArtwork(null,creatorId,f,errors,artworkImage,model);
    }
    @PostMapping("/artworks")public String createArtwork(@Valid @ModelAttribute("form")ArtworkForm f,BindingResult errors,@RequestParam(required=false)MultipartFile artworkImage,Model model) {
        return saveArtwork(null,null,f,errors,artworkImage,model);
    }
    @PostMapping("/artworks/{id}")public String updateArtwork(@PathVariable Long id,@Valid @ModelAttribute("form")ArtworkForm f,BindingResult errors,@RequestParam(required=false)MultipartFile artworkImage,Model model) {
        Long owner=content.artwork(id).getCreatorId();
        if(!owner.equals(f.getCreatorId())){errors.reject("creator","制作者は変更できません。");f.setCreatorId(owner);}
        return saveArtwork(id,owner,f,errors,artworkImage,model);
    }
    private String saveArtwork(Long id,Long fixedCreatorId,ArtworkForm f,BindingResult errors,MultipartFile image,Model model) {
        artworkModel(f,id,fixedCreatorId,model);
        if(!errors.hasErrors())try {
            Long saved=uploads.saveArtwork(id,f,image);
            return "redirect:/admin/artworks/"+saved+"/edit?saved";
        }catch(InvalidMessageException e){errors.reject("save",e.getMessage());}
         catch(DataIntegrityViolationException e){errors.reject("save","保存できませんでした。URL識別子などの入力内容を確認してください。");}
        return "playpit/admin/artwork-form";
    }
    @PostMapping("/events/{id}/participants")public String participant(@PathVariable Long id,@RequestParam Long creatorId,@RequestParam(defaultValue="0")int displayOrder,@RequestParam(required=false)String exhibitionTitle,@RequestParam(required=false)String spaceType) {
        content.addParticipant(id,creatorId,displayOrder,exhibitionTitle,spaceType);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
    @PostMapping("/events/{id}/exhibitions")public String exhibition(@PathVariable Long id,@RequestParam Long artworkId,@RequestParam(defaultValue="0")int displayOrder,@RequestParam(required=false)String exhibitionArea) {
        content.addExhibition(id,artworkId,displayOrder,exhibitionArea);
        return "redirect:/admin/events/"+id+"/edit?saved";
    }
}
