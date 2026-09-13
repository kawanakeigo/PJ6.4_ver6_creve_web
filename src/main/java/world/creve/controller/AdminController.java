package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import world.creve.entity.MessageStatus;
import world.creve.service.AdminService;

@Controller
public class AdminController {
  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/admin")
  public String admin() {
    return "redirect:/admin/messages";
  }

  @GetMapping("/admin/login")
  public String login(Model model) {
    model.addAttribute("active", "admin");
    return "admin/login";
  }

  @GetMapping("/admin/messages")
  public String messages(
      @RequestParam(required = false) String eventId,
      @RequestParam(required = false) String creatorId,
      @RequestParam(required = false) String artworkId,
      @RequestParam(required = false) MessageStatus status,
      @RequestParam(required = false) String keyword,
      Model model
  ) {
    model.addAttribute("messages", adminService.searchMessages(
        eventId,
        creatorId,
        artworkId,
        status,
        keyword
    ));
    model.addAttribute("statuses", MessageStatus.values());
    model.addAttribute("eventId", eventId);
    model.addAttribute("creatorId", creatorId);
    model.addAttribute("artworkId", artworkId);
    model.addAttribute("selectedStatus", status);
    model.addAttribute("keyword", keyword);
    model.addAttribute("active", "admin");
    return "admin/messages";
  }

  @GetMapping("/admin/messages/{messageId}")
  public String messageDetail(@PathVariable Long messageId, Model model) {
    model.addAttribute("message", adminService.findMessage(messageId));
    model.addAttribute("statuses", MessageStatus.values());
    model.addAttribute("active", "admin");
    return "admin/message-detail";
  }

  @PostMapping("/admin/messages/{messageId}/publish")
  public String publish(@PathVariable Long messageId, RedirectAttributes redirectAttributes) {
    adminService.changeStatus(messageId, MessageStatus.PUBLISHED);
    redirectAttributes.addFlashAttribute("notice", "投稿を公開しました。");
    return "redirect:/admin/messages";
  }

  @PostMapping("/admin/messages/{messageId}/hide")
  public String hide(@PathVariable Long messageId, RedirectAttributes redirectAttributes) {
    adminService.changeStatus(messageId, MessageStatus.HIDDEN);
    redirectAttributes.addFlashAttribute("notice", "投稿を非公開にしました。");
    return "redirect:/admin/messages";
  }

  @PostMapping("/admin/messages/{messageId}/delete")
  public String delete(@PathVariable Long messageId, RedirectAttributes redirectAttributes) {
    adminService.deleteMessage(messageId);
    redirectAttributes.addFlashAttribute("notice", "投稿を削除扱いにしました。");
    return "redirect:/admin/messages";
  }

  @GetMapping("/admin/events")
  public String events(Model model) {
    model.addAttribute("events", adminService.findEvents());
    model.addAttribute("active", "admin");
    return "admin/events";
  }

  @GetMapping("/admin/creators")
  public String creators(Model model) {
    model.addAttribute("creators", adminService.findCreators());
    model.addAttribute("active", "admin");
    return "admin/creators";
  }

  @GetMapping("/admin/artworks")
  public String artworks(Model model) {
    model.addAttribute("artworks", adminService.findArtworks());
    model.addAttribute("active", "admin");
    return "admin/artworks";
  }
}
