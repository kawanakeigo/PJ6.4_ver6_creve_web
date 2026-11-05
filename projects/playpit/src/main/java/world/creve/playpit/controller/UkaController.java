package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
/** Retired routes remain explicit so old bookmarks receive a clear outcome. */
@Controller public class UkaController {
 @GetMapping("/playpit/{eventSlug}/uka") @ResponseStatus(HttpStatus.GONE)
 public String retiredPage(){return "playpit/uka-retired";}
 @GetMapping("/api/uka") @ResponseBody
 public ResponseEntity<java.util.Map<String,String>> retiredApi(){return ResponseEntity.status(HttpStatus.GONE).body(java.util.Map.of("code","FEATURE_RETIRED","message","この機能は提供を終了しました。感想は作品・クリエイターページからご覧ください。"));}
}
