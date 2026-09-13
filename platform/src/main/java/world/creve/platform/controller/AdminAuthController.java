package world.creve.platform.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/** Shared login and dashboard; no product-specific imports. */
@Controller public class AdminAuthController {
    @GetMapping("/admin/login") public String showLogin() {
        return "admin/login";
    }
    @GetMapping( {
        "/admin","/admin/dashboard"
    }
    ) public String showDashboard() {
        return "admin/dashboard";
    }
}
