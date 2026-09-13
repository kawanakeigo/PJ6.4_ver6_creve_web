package world.creve.playpit.controller;
import java.time.LocalDate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import world.creve.playpit.service.MessageService;
import world.creve.playpit.entity.MessageStatus;
@Controller public class AdminController {
    private final MessageService messages;
    public AdminController(MessageService messages) {
        this.messages=messages;
    }
    @GetMapping("/admin/messages")public String showMessageList(@RequestParam(required=false)Long eventId,@RequestParam(required=false)Long creatorId,@RequestParam(required=false)Long artworkId,@RequestParam(required=false)MessageStatus status,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate date,@RequestParam(required=false)String keyword,@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("messages",messages.searchMessages(eventId,creatorId,artworkId,status,date,keyword,page));
        return "admin/message-list";
    }
    @PostMapping("/admin/messages/{id}/status")public String changeStatus(@PathVariable Long id,@RequestParam MessageStatus status) {
        messages.changeStatus(id,status);
        return "redirect:/admin/messages";
    }
    @PostMapping("/admin/messages/{id}/delete")public String deleteMessage(@PathVariable Long id) {
        messages.deleteMessage(id);
        return "redirect:/admin/messages";
    }
}
