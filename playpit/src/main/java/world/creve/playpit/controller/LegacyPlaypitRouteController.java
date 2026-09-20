package world.creve.playpit.controller;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController public class LegacyPlaypitRouteController {
    @RequestMapping(path={"/PLAYPIT","/PLAYPIT/**"},method={RequestMethod.GET,RequestMethod.HEAD}) public ResponseEntity<Void> redirectUppercasePath(HttpServletRequest request) {
        String uri=request.getRequestURI();
        int prefix=uri.indexOf("/PLAYPIT");
        String location=uri.substring(0,prefix)+"/playpit"+uri.substring(prefix+"/PLAYPIT".length());
        if(request.getQueryString()!=null)location+="?"+request.getQueryString();
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).location(URI.create(location)).build();
    }
}
