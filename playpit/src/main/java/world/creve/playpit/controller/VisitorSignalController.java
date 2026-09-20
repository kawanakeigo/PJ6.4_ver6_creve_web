package world.creve.playpit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import world.creve.platform.exception.RateLimitExceededException;
import world.creve.playpit.service.VisitorSignalService;
import world.creve.common.validation.CodePointLength;

@Controller
public class VisitorSignalController {
    public record Signal(
            @NotNull UUID metricId,
            @NotBlank @Size(max = 30) String kind,
            @NotNull Long eventId,
            Long creatorId,
            Long artworkId) {
    }

    public record Report(
            @NotNull Long messageId,
            @NotBlank @Size(max = 20) String category,
            @NotBlank @CodePointLength(max = 1000, strip = true) String detail) {
    }

    public record ReportAck(long reportId, String state) {}

    private final VisitorSignalService service;

    public VisitorSignalController(VisitorSignalService service) {
        this.service = service;
    }

    private void limit(HttpServletRequest request, int maximum) {
        var session = request.getSession(true);
        synchronized (session) {
            long now = System.currentTimeMillis();
            long[] bucket = (long[]) session.getAttribute("signal-window");
            if (bucket == null || now - bucket[0] >= 60_000) bucket = new long[]{now, 0};
            if (bucket[1] >= maximum) throw new RateLimitExceededException();
            bucket[1]++;
            session.setAttribute("signal-window", bucket);
        }
    }

    @PostMapping("/api/metrics")
    @ResponseBody
    public ResponseEntity<Void> signal(@Valid @RequestBody Signal signal, HttpServletRequest request) {
        limit(request, 60);
        service.signal(signal.metricId(), signal.kind(), signal.eventId(), signal.creatorId(), signal.artworkId());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/api/reports")
    @ResponseBody
    public ResponseEntity<ReportAck> report(@Valid @RequestBody Report report, HttpServletRequest request) {
        limit(request, 20);
        var receipt = service.report(report.messageId(), report.category(), report.detail());
        var response = new ReportAck(receipt.reportId(), receipt.created() ? "ACCEPTED" : "ALREADY_ACCEPTED");
        return ResponseEntity.status(receipt.created() ? 201 : 200).body(response);
    }

    @GetMapping("/admin/reports")
    public String reports(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("reports", service.reports(page));
        model.addAttribute("page", Math.max(0, page));
        return "playpit/admin/reports";
    }

    @PostMapping("/admin/reports/{id}/review")
    public String review(@PathVariable Long id) {
        service.review(id);
        return "redirect:/admin/reports";
    }

    @GetMapping({"/admin/metrics", "/admin/analytics"})
    public String analytics(@RequestParam(required = false) Long eventId, Model model) {
        model.addAttribute("analytics", eventId == null ? java.util.List.of() : service.analytics(eventId));
        model.addAttribute("eventId", eventId);
        model.addAttribute("summary", eventId == null ? null : service.summary(eventId));
        model.addAttribute("creatorTotals", eventId == null ? java.util.List.of() : service.creatorTotals(eventId));
        return "playpit/admin/analytics";
    }
}
