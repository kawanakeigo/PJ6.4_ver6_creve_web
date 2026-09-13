package world.creve.playpit.controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import world.creve.playpit.service.VisitorSignalService;
import world.creve.platform.exception.RateLimitExceededException;
@Controller public class VisitorSignalController {
    public record Signal(@NotBlank @Size(max=40)String eventName,@NotNull Long eventId,Long creatorId,Long artworkId) {
    }
    public record Report(@NotBlank @Size(max=300)String reason) {
    }
    private final VisitorSignalService service;
    public VisitorSignalController(VisitorSignalService service) {
        this.service=service;
    }
    private void limit(HttpServletRequest request,int maximum) {
        var session=request.getSession(true);
        synchronized(session) {
            long now=System.currentTimeMillis();
            long[] bucket=(long[])session.getAttribute("signal-window");
            if(bucket==null||now-bucket[0]>=60000)bucket=new long[] {
                now,0
            }
            ;
            if(bucket[1]>=maximum)throw new RateLimitExceededException();
            bucket[1]++;
            session.setAttribute("signal-window",bucket);
        }
    }
    @PostMapping("/api/analytics") @ResponseBody public ResponseEntity<Void> signal(@Valid @RequestBody Signal s,HttpServletRequest request) {
        limit(request,60);
        service.signal(s.eventName(),s.eventId(),s.creatorId(),s.artworkId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/api/messages/{id}/reports") @ResponseBody public ResponseEntity<Void> report(@PathVariable Long id,@Valid @RequestBody Report r,HttpServletRequest request) {
        limit(request,60);
        service.report(id,r.reason());
        return ResponseEntity.status(201).build();
    }
    @GetMapping("/admin/reports") public String reports(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("reports",service.reports(page));
        model.addAttribute("page",Math.max(0,page));
        return "playpit/admin/reports";
    }
    @PostMapping("/admin/reports/{id}/review")public String review(@PathVariable Long id) {
        service.review(id);
        return "redirect:/admin/reports";
    }
    @GetMapping("/admin/analytics")public String analytics(@RequestParam(required=false)Long eventId,Model model) {
        model.addAttribute("analytics",eventId==null?java.util.List.of():service.analytics(eventId));
        model.addAttribute("eventId",eventId);
        model.addAttribute("summary",eventId==null?null:service.summary(eventId));
        model.addAttribute("creatorTotals",eventId==null?java.util.List.of():service.creatorTotals(eventId));
        return "playpit/admin/analytics";
    }
}
