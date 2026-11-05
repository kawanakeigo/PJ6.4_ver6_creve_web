package world.creve.playpit;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import world.creve.platform.dto.*;
class CreatorTemplateTest {
    @Test void bothEventCardsRenderSameDedicatedProfilePath() {
        var resolver=new ClassLoaderTemplateResolver();resolver.setPrefix("templates/");resolver.setSuffix(".html");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(resolver);
        engine.setLinkBuilder(new StandardLinkBuilder() {
            @Override protected String computeContextPath(IExpressionContext context,String base,Map<String,Object> parameters) {return "";}
        });
        for(String slug:List.of("first","second")) {
            var context=new Context();
            context.setVariable("event",new EventResponse(1L,"PLAYPIT",slug,slug,null,null,null,null,null,null));
            context.setVariable("c",new CreatorSummaryResponse(7L,"artist","作者",null,null,"共通プロフィール",1L));
            String html=engine.process("playpit/fragments/cards",Set.of("creatorCard"),context);
            assertTrue(html.contains("href=\"/creators/artist?event="+slug+"\""),html);
            assertTrue(html.contains("共通プロフィール"),html);
        }
    }
}
