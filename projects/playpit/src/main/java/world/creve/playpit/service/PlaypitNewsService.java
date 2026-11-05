package world.creve.playpit.service;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
/** Dedicated, operator-maintained public announcements; never reads CreVe news. */
@Service public class PlaypitNewsService {
 public record Notice(String title,LocalDate date,String body) {}
 private final Path file;
 public PlaypitNewsService(@Value("${playpit.news-file:./config/public/playpit/news.properties}")String file){this.file=Path.of(file);}
 public List<Notice> notices(){
  try {
   if(!Files.isRegularFile(file)||Files.size(file)>1_000_000)return List.of();
   var p=new Properties();try(var reader=Files.newBufferedReader(file)){p.load(reader);}
   var result=new ArrayList<Notice>();
   for(int i=1;i<=100;i++){
    String prefix="news."+i+".";
    if(!"PUBLISHED".equals(p.getProperty(prefix+"status")))continue;
    String title=p.getProperty(prefix+"title","").trim(),body=p.getProperty(prefix+"body","");
    if(title.isBlank()||title.length()>150||body.length()>10000)continue;
    try {result.add(new Notice(title,LocalDate.parse(p.getProperty(prefix+"date","")),body));}catch(java.time.format.DateTimeParseException ignored){}
   }
   result.sort(Comparator.comparing(Notice::date).reversed());return List.copyOf(result);
  }catch(java.io.IOException|IllegalArgumentException e){return List.of();}
 }
}
