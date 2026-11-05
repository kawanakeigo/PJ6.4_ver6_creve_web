package world.creve.playpit;
import java.util.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.context.*;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import org.springframework.data.domain.PageImpl;
import world.creve.platform.dto.EventResponse;
class HomeExhibitionsTest {
 void verify(int count){
  var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
  engine.setLinkBuilder(new StandardLinkBuilder(){@Override protected String computeContextPath(IExpressionContext c,String b,Map<String,Object> p){return "";}});
  var items=new ArrayList<EventResponse>();for(int i=0;i<count;i++)items.add(new EventResponse((long)i,"PLAYPIT","e"+i,"展示"+i,"詳細"+i,i==0?"/icon.png":null,LocalDateTime.of(2026-i,1,1,0,0),LocalDateTime.of(2026-i,1,2,0,0),"会場"+i,"終了済み"));
  var c=new Context();c.setVariable("events",new PageImpl<>(items));String html=engine.process("playpit/fragments/home-exhibitions",c);
  assertEquals(Math.min(3,count),html.split("class=\"card event-card\"",-1).length-1);
  assertEquals(Math.max(0,count-3),html.split("class=\"home-exhibition-compact\"",-1).length-1);
  for(int i=0;i<count;i++){assertTrue(html.contains("href=\"/playpit/e"+i+"\""));if(i>=3){assertFalse(html.contains("詳細"+i));assertFalse(html.contains("会場"+i));}}
  assertFalse(html.contains("lin.ee"));assertFalse(html.contains("/lp/"));
  if(count==0)assertTrue(html.contains("公開中のイベントはありません"));
 }
 @Test void empty(){verify(0);}@Test void one(){verify(1);}@Test void three(){verify(3);}@Test void fourthAndLaterAreCompact(){verify(6);}
}
