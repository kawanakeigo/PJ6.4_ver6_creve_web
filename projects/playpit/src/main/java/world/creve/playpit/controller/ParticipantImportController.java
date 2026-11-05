package world.creve.playpit.controller;
import java.time.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import world.creve.playpit.service.*;
import world.creve.platform.exception.InvalidMessageException;
@Controller @RequestMapping("/admin/events/{eventId}/participants/import") public class ParticipantImportController {
 private static final String KEY="participant-import-preview";
 private final ParticipantImportService imports;private final Clock clock;
 record Pending(String token,Instant expires,ParticipantImportService.Plan plan){}
 public ParticipantImportController(ParticipantImportService imports,Clock clock){this.imports=imports;this.clock=clock;}
 private void modelPage(Long id,Model model){model.addAttribute("eventId",id);model.addAttribute("eventTitle",imports.eventTitle(id));}
 @GetMapping public String page(@PathVariable Long eventId,Model model){modelPage(eventId,model);return "playpit/admin/participant-import";}
 @GetMapping("/template.csv") @ResponseBody public ResponseEntity<byte[]> template(@PathVariable Long eventId){imports.eventTitle(eventId);String csv="\ufeff"+String.join(",",ParticipantTableParser.HEADERS)+"\r\n";return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8")).header("Content-Disposition","attachment; filename=playpit-participants.csv").cacheControl(CacheControl.noStore()).body(csv.getBytes(StandardCharsets.UTF_8));}
 @PostMapping("/preview") public String preview(@PathVariable Long eventId,@RequestParam MultipartFile table,HttpSession session,Model model){
  modelPage(eventId,model);session.removeAttribute(KEY);
  try{if(table.getSize()>ParticipantTableParser.MAX_BYTES)throw new InvalidMessageException("表は2MB以下にしてください。");var plan=imports.preview(eventId,table.getBytes(),table.getOriginalFilename());var pending=new Pending(UUID.randomUUID().toString(),clock.instant().plusSeconds(1800),plan);session.setAttribute(KEY,pending);model.addAttribute("plan",plan);model.addAttribute("token",pending.token());}
  catch(java.io.IOException e){model.addAttribute("error","ファイルを読み取れませんでした。");}catch(InvalidMessageException e){model.addAttribute("error",e.getMessage());}
  return "playpit/admin/participant-import";
 }
 @PostMapping("/commit") public String commit(@PathVariable Long eventId,@RequestParam String token,@RequestParam(defaultValue="false")boolean confirmed,HttpSession session,Model model){
  modelPage(eventId,model);
  try{var value=session.getAttribute(KEY);if(!(value instanceof Pending p)||!p.token().equals(token)||!p.plan().eventId().equals(eventId)||clock.instant().isAfter(p.expires())||!confirmed)throw new InvalidMessageException("プレビュー期限切れ、対象展示の相違、または確認未チェックです。再度アップロードしてください。");
   model.addAttribute("result",imports.commit(p.plan()));
  }catch(InvalidMessageException e){model.addAttribute("error",e.getMessage());}catch(org.springframework.dao.DataIntegrityViolationException e){model.addAttribute("error","競合のため全件を取り消しました。再度プレビューしてください。");}
  return "playpit/admin/participant-import";
 }
 @PostMapping("/cancel")public String cancel(@PathVariable Long eventId,HttpSession session){session.removeAttribute(KEY);return "redirect:/admin/events/"+eventId+"/participants/import";}
}
