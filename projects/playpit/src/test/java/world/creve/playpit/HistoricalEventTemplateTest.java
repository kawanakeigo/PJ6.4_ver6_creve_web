package world.creve.playpit;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.context.*;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import world.creve.platform.dto.EventDetailResponse;
class HistoricalEventTemplateTest {
 @Test void historyWithoutImageOrExactTimeRendersWithoutInventedDetails() {
  var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");
  var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
  engine.setLinkBuilder(new StandardLinkBuilder(){@Override protected String computeContextPath(IExpressionContext c,String b,Map<String,Object> p){return "";}});
  var c=new Context();
  c.setVariable("event",new EventDetailResponse(8L,"PLAYPIT","history-test","羽化展",null,null,null,LocalDateTime.of(2024,3,17,0,0),LocalDateTime.of(2024,3,24,0,0),"本郷",null,null,null,null,"終了"));
  var hero=engine.process("playpit/fragments/event-presentation",Set.of("hero"),c);
  var facts=engine.process("playpit/fragments/event-presentation",Set.of("facts"),c);
  assertTrue(hero.contains("exhibition-hero-text"));assertFalse(hero.contains("<img"));assertTrue(hero.contains("羽化展"));
  assertTrue(facts.contains("2024/03/17"));assertTrue(facts.contains("2024/03/24"));assertFalse(facts.contains("00:00"));assertFalse(facts.contains("<dt>住所"));assertFalse(facts.contains("<dt>アクセス"));
 }
}
