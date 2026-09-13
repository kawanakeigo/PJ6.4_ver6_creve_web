package world.creve.platform.config;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import world.creve.platform.spi.ProjectContribution;
import world.creve.platform.util.SafeUrls;
@ControllerAdvice public class CommonViewModel {
    private final List<ProjectContribution> projects;
    private final boolean postingEnabled;
    private final String social;
    public CommonViewModel(List<ProjectContribution> projects,@Value("${creve.posting-enabled:false}")boolean enabled,@Value("${creve.social-url:}")String social) {
        this.projects=projects;
        this.postingEnabled=enabled;
        this.social=SafeUrls.optional(social);
    }
    @ModelAttribute public void common(Model model,HttpServletRequest request) {
        model.addAttribute("projectLinks",projects.stream().sorted(Comparator.comparingInt(ProjectContribution::displayOrder).thenComparing(ProjectContribution::key)).map(p->Map.of("key",p.key(),"title",p.title(),"path",p.path())).toList());
        model.addAttribute("requestPath",request.getServletPath());
        model.addAttribute("postingEnabled",postingEnabled);
        model.addAttribute("socialUrl",social);
    }
}
