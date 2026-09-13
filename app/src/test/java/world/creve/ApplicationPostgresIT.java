package world.creve;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
/** Run only with -Ppostgres-it and scripts/verify.sh. This refuses a non-test DB. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) class ApplicationPostgresIT {
    static ConfigurableApplicationContext context;
    static JdbcTemplate db;
    static String base;
    static long event,creator,artwork,nljEvent;
    static String playpitSlug,nljSlug;
    static Client client;
    @BeforeAll static void boot() throws Exception {
        String url=System.getenv("CREVE_DATASOURCE_URL");
        if(url==null||!url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/creve_modular_test"))throw new IllegalStateException("Integration tests require the dedicated local creve_modular_test DB");
        context=new SpringApplication(CreveApplication.class).run("--server.port=0","--creve.posting-enabled=true","--creve.bootstrap-admin=false","--spring.thymeleaf.cache=false");
        db=context.getBean(JdbcTemplate.class);
        base="http://localhost:"+context.getEnvironment().getProperty("local.server.port");
        playpitSlug="test-"+UUID.randomUUID().toString().substring(0,8);
        nljSlug=playpitSlug+"-nlj";
        event=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'テスト展示',localtimestamp,localtimestamp+interval '1 day','PUBLISHED') returning event_id",Long.class,playpitSlug);
        nljEvent=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('NLJ',?,'テストライブ',localtimestamp,localtimestamp+interval '1 day','PUBLISHED') returning event_id",Long.class,nljSlug);
        creator=db.queryForObject("insert into creators(slug,name,status) values(?,'作者','PUBLISHED') returning creator_id",Long.class,playpitSlug+"-creator");
        artwork=db.queryForObject("insert into artworks(creator_id,slug,title,status) values(?,?,'作品','PUBLISHED') returning artwork_id",Long.class,creator,playpitSlug+"-art");
        db.update("insert into event_creators(event_id,creator_id,display_order) values(?,?,1)",event,creator);
        db.update("insert into event_creators(event_id,creator_id,display_order) values(?,?,1)",nljEvent,creator);
        db.update("insert into event_artworks(event_id,artwork_id,display_order) values(?,?,1)",event,artwork);
        client=new Client();
        client.refresh("/playpit/"+playpitSlug+"/artworks/"+playpitSlug+"-art");
        assertTrue(client.page.contains("messageText"));
    }
    @AfterAll static void stop() {
        if(context!=null)context.close();
    }
    static class Client {
        HttpClient http=HttpClient.newBuilder().cookieHandler(new CookieManager(null,CookiePolicy.ACCEPT_ALL)).build();
        String csrf,header,page;
        void refresh(String path)throws Exception {
            var r=get(path);
            assertEquals(200,r.statusCode(),r.body());
            page=r.body();
            csrf=meta("_csrf");
            header=meta("_csrf_header");
        }
        String meta(String name) {
            Matcher m=Pattern.compile("<meta[^>]*name=\""+name+"\"[^>]*content=\"([^\"]+)\"").matcher(page);
            if(!m.find())throw new AssertionError("CSRF meta absent: "+name);
            return m.group(1);
        }
        HttpResponse<String> get(String path)throws Exception {
            return http.send(HttpRequest.newBuilder(URI.create(base+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> post(String json,String key,boolean token)throws Exception {
            var b=HttpRequest.newBuilder(URI.create(base+"/api/messages")).header("Content-Type","application/json");
            if(token)b.header(header,csrf);
            if(key!=null)b.header("Idempotency-Key",key);
            return http.send(b.POST(HttpRequest.BodyPublishers.ofString(json)).build(),HttpResponse.BodyHandlers.ofString());
        }
    }
    static String body(String message) {
        return "{\"eventId\":"+event+",\"creatorId\":"+creator+",\"artworkId\":"+artwork+",\"message\":\""+message+"\",\"displayName\":\"秘密名\",\"anonymous\":true,\"agreement\":true}";
    }
    @Test @Order(1)void publicAndMissingRoutes()throws Exception {
        assertEquals(200,client.get("/").statusCode());
        assertEquals(200,client.get("/creators").statusCode());
        var creatorPage=client.get("/creators/"+playpitSlug+"-creator");
        assertEquals(200,creatorPage.statusCode(),creatorPage.body());
        assertTrue(creatorPage.body().contains("messageText"));
        assertTrue(creatorPage.body().contains("テストライブ"));
        assertEquals(200,client.get("/playpit/events").statusCode());
        assertEquals(200,client.get("/NLJ").statusCode());
        assertEquals(200,client.get("/NLJ/"+nljSlug).statusCode());
        var legacyNlj=client.get("/live/"+nljSlug);
        assertEquals(308,legacyNlj.statusCode());
        assertEquals("/NLJ/"+nljSlug,legacyNlj.headers().firstValue("location").orElseThrow());
        var uppercasePlaypit=client.get("/PLAYPIT/events");
        assertEquals(308,uppercasePlaypit.statusCode());
        assertEquals("/playpit/events",uppercasePlaypit.headers().firstValue("location").orElseThrow());
        assertEquals(404,client.get("/playpit/does-not-exist").statusCode());
        assertEquals(302,client.get("/admin/messages").statusCode());
        assertEquals(401,client.get("/api/admin/no-route").statusCode());
    }
    @Test @Order(2)void csrfAndValidation()throws Exception {
        assertEquals(403,client.post(body("感想"),null,false).statusCode());
        assertEquals(400,client.post(body(""),"invalid",true).statusCode());
        assertEquals(400,client.post(body("あ".repeat(301)),"long",true).statusCode());
        assertEquals(400,client.post(body("未同意").replace("\"agreement\":true","\"agreement\":false"),"consent",true).statusCode());
        assertEquals(400,client.post(body("対象なし").replace("\"creatorId\":"+creator,"\"creatorId\":null").replace("\"artworkId\":"+artwork,"\"artworkId\":null"),"target",true).statusCode());
    }
    @Test @Order(3)void storeAndIdempotency()throws Exception {
        String json=body("あ".repeat(300));
        var first=client.post(json,"replay-key",true);
        assertEquals(201,first.statusCode(),first.body());
        assertTrue(first.body().contains("\"messageId\""));
        assertTrue(first.body().contains("completionMessage"));
        var replay=client.post(json,"replay-key",true);
        assertEquals(first.body(),replay.body());
        assertEquals(1,db.queryForObject("select count(*) from messages where event_id=?",Integer.class,event));
    }
    @Test @Order(4)void limits()throws Exception {
        assertEquals(201,client.post(body("2回目"),"second",true).statusCode());
        assertEquals(201,client.post(body("3回目"),"third",true).statusCode());
        assertEquals(429,client.post(body("4回目"),"fourth",true).statusCode());
    }
    @Test @Order(5)void tenMinuteDuplicateWindow()throws Exception {
        db.update("update message_submission_receipts set created_at=localtimestamp-interval '2 minutes' where message_id in(select message_id from messages where event_id=?)",event);
        assertEquals(429,client.post(body("2回目"),"new-key-same-body",true).statusCode());
    }
    @Test @Order(6)void pendingAnonymousAndPublicBoundary()throws Exception {
        Client other=new Client();
        other.refresh("/playpit/events");
        assertEquals(201,other.post(body("a@example.invalid"),"pending",true).statusCode());
        var response=other.get("/api/messages?eventId="+event);
        assertEquals(200,response.statusCode());
        assertFalse(response.body().contains("秘密名"));
        assertFalse(response.body().contains("a@example.invalid"));
        db.update("update events set status='HIDDEN' where event_id=?",event);
        assertEquals(404,other.get("/api/messages?eventId="+event).statusCode());
        db.update("update events set status='PUBLISHED' where event_id=?",event);
    }
    @Test @Order(7)void concurrentRetryIsOneInsert()throws Exception {
        Client same=new Client();
        same.refresh("/playpit/events");
        int before=db.queryForObject("select count(*) from messages where event_id=?",Integer.class,event);
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var tasks=java.util.stream.IntStream.range(0,5).mapToObj(i->(Callable<Integer>)()->same.post(body("並行再送"),"concurrent-key",true).statusCode()).toList();
            for(var f:pool.invokeAll(tasks))assertEquals(201,f.get());
        }
        assertEquals(before+1,db.queryForObject("select count(*) from messages where event_id=?",Integer.class,event));
    }
    @Test @Order(8)void realUkaPathLimits300()throws Exception {
        for(int i=100;i<450;i++)db.update("insert into messages(event_id,creator_id,artwork_id,message,is_anonymous,status,petal_type,petal_seed) values(?,?,?,'表示試験',true,'PUBLISHED',1,?)",event,creator,artwork,((long)i<<3)|4);
        var service=context.getBean(world.creve.playpit.service.UkaService.class);
        var data=service.getUkaData(event);
        assertEquals(300,data.petals().size());
        assertTrue(data.messageCount()>300);
        assertEquals(4,data.growthStage());
        assertEquals(200,client.get("/api/uka?eventId="+event).statusCode());
    }
    @Test @Order(9)void foreignKeysAndChecks() {
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("insert into messages(event_id,message,is_anonymous,status,petal_type,petal_seed) values(?,'無対象',true,'PUBLISHED',1,77777)",event));
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("insert into artworks(creator_id,slug,title,status) values(-1,'invalid-fk','x','PUBLISHED')"));
    }
}
