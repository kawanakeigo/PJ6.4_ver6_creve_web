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
    static java.nio.file.Path uploadDirectory;
    @BeforeAll static void boot() throws Exception {
        String url=System.getenv("CREVE_DATASOURCE_URL");
        if(url==null||!url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/creve_modular_test"))throw new IllegalStateException("Integration tests require the dedicated local creve_modular_test DB");
        uploadDirectory=java.nio.file.Files.createTempDirectory("playpit-it-images-");
        context=new SpringApplication(CreveApplication.class).run("--creve.upload-dir="+uploadDirectory,"--server.port=0","--creve.posting-enabled=true","--creve.bootstrap-admin=false","--spring.thymeleaf.cache=false");
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
        if(uploadDirectory!=null)try(var files=java.nio.file.Files.walk(uploadDirectory)){for(var path:files.sorted(java.util.Comparator.reverseOrder()).toList())java.nio.file.Files.deleteIfExists(path);}catch(java.io.IOException e){throw new RuntimeException(e);}
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
        HttpResponse<String> form(String path,Map<String,String> fields)throws Exception {
            String body=fields.entrySet().stream().map(e->URLEncoder.encode(e.getKey(),java.nio.charset.StandardCharsets.UTF_8)+"="+URLEncoder.encode(e.getValue(),java.nio.charset.StandardCharsets.UTF_8)).collect(java.util.stream.Collectors.joining("&"));
            return http.send(HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/x-www-form-urlencoded").header(header,csrf).POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
        }
        HttpResponse<String> multipart(String path,Map<String,String> fields,String imageField,byte[] image,boolean token)throws Exception {
            String boundary="TestBoundary"+UUID.randomUUID();var data=new java.io.ByteArrayOutputStream();
            for(var field:fields.entrySet())data.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+field.getKey()+"\"\r\n\r\n"+field.getValue()+"\r\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
            if(image!=null){data.write(("--"+boundary+"\r\nContent-Disposition: form-data; name=\""+imageField+"\"; filename=\""+(imageField.equals("table")?"participants.csv":"test.png")+"\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes());data.write(image);data.write("\r\n".getBytes());}
            data.write(("--"+boundary+"--\r\n").getBytes());
            var request=HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","multipart/form-data; boundary="+boundary);
            if(token)request.header(header,csrf);
            return http.send(request.POST(HttpRequest.BodyPublishers.ofByteArray(data.toByteArray())).build(),HttpResponse.BodyHandlers.ofString());
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
    @Test @Order(0) void editorialHomeUsesOnlyFeaturedPublishedParticipation() throws Exception {
        db.update("update events set start_at=localtimestamp-interval '2 hours' where event_id=?",event);
        long hiddenCreator=db.queryForObject("insert into creators(slug,name,status) values(?,'HIDDEN-B-CREATOR','DRAFT') returning creator_id",Long.class,playpitSlug+"-hidden-b");
        long hiddenWork=db.queryForObject("insert into artworks(creator_id,slug,title,status) values(?,?,'HIDDEN-B-WORK','DRAFT') returning artwork_id",Long.class,creator,playpitSlug+"-hidden-work-b");
        db.update("insert into event_creators(event_id,creator_id,display_order) values(?,?,1)",event,hiddenCreator);
        db.update("insert into event_artworks(event_id,artwork_id,display_order) values(?,?,1)",event,hiddenWork);
        var home=client.get("/playpit");assertEquals(200,home.statusCode(),home.body());
        assertTrue(home.body().contains("poster-hero"));assertTrue(home.body().contains("Artists"));
        assertTrue(home.body().contains("/creators/"+playpitSlug+"-creator?event="+playpitSlug));
        assertTrue(home.body().contains("/playpit/"+playpitSlug+"/artworks/"+playpitSlug+"-art"));
        assertFalse(home.body().contains("HIDDEN-B-CREATOR"));assertFalse(home.body().contains("HIDDEN-B-WORK"));
        assertFalse(home.body().contains("テストライブ"));assertFalse(home.body().contains("レイアウト例"));
        db.update("update events set status='DRAFT' where event_id=?",event);
        try {
            var empty=client.get("/playpit");assertEquals(200,empty.statusCode(),empty.body());
            assertTrue(empty.body().contains("公開中のイベントはありません。"));
            assertTrue(empty.body().contains("展示作品は公開準備中です。"));
            assertFalse(empty.body().contains("/artworks/"+playpitSlug+"-art"));
        } finally {db.update("update events set status='PUBLISHED' where event_id=?",event);}
    }
    @Test @Order(0) void creatorOpeningAboutAndWorksRespectPublicationAndPaging() throws Exception {
        String slug=playpitSlug+"-layout";
        long person=db.queryForObject("insert into creators(slug,name,profile,status) values(?,'Layout artist',?,'PUBLISHED') returning creator_id",Long.class,slug,"最初の記憶。\n制作を続ける理由。<script>unsafe</script>");
        db.update("insert into artworks(creator_id,slug,title,status) values(?,?,'PRIVATE-LAYOUT-WORK','DRAFT')",person,slug+"-private");
        for(int count=0;count<=4;count++) {
            if(count>0)db.update("insert into artworks(creator_id,slug,title,main_image_url,status) values(?,?,?,?, 'PUBLISHED')",person,slug+"-"+count,"Layout work "+count,"/test-image-"+count+".png");
            var response=client.get("/creators/"+slug);assertEquals(200,response.statusCode());
            String page=response.body();
            assertFalse(page.contains("PRIVATE-LAYOUT-WORK"));assertFalse(page.contains("クリエイターの物語"));
            assertTrue(page.indexOf("id=\"about\"")<page.indexOf("id=\"works\""));
            assertTrue(page.contains("最初の記憶。\n制作を続ける理由。&lt;script&gt;unsafe&lt;/script&gt;"));
            String about=page.substring(page.indexOf("id=\"about\""),page.indexOf("id=\"works\""));
            assertFalse(about.contains("<img"));
            assertEquals(Math.min(3,count),page.split("class=\"opening-work\"",-1).length-1);
            if(count>0) {
                assertTrue(page.indexOf("id=\"selectedWorks\"")<page.indexOf("id=\"about\""));
                String opening=page.substring(page.indexOf("id=\"selectedWorks\""),page.indexOf("id=\"about\""));
                assertTrue(opening.contains("Layout work "+count));
                if(count==4)assertFalse(opening.contains("Layout work 1"));
                String next=client.get("/creators/"+slug+"?artworkPage=1").body();
                assertEquals(opening,next.substring(next.indexOf("id=\"selectedWorks\""),next.indexOf("id=\"about\"")));
            }
        }
        db.update("update creators set profile=null,profile_image_url='/chosen-icon.png' where creator_id=?",person);
        String page=client.get("/creators/"+slug).body();
        String about=page.substring(page.indexOf("id=\"about\""),page.indexOf("id=\"works\""));
        assertTrue(about.contains("/chosen-icon.png"));assertFalse(about.contains("/test-image-"));
        assertFalse(about.contains("最初の記憶"));
        String eventPage=client.get("/creators/"+playpitSlug+"-creator?event="+playpitSlug).body();
        assertTrue(eventPage.contains("id=\"selectedWorks\""));assertFalse(eventPage.contains("HIDDEN-B-WORK"));
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
        assertEquals(410,client.get("/api/uka?eventId="+event).statusCode());
        assertEquals(410,client.get("/playpit/"+playpitSlug+"/uka").statusCode());
        var page=client.get("/playpit/"+playpitSlug+"/artworks/"+playpitSlug+"-art").body();
        assertFalse(page.contains("data-uka-board"));assertFalse(page.contains("/js/uka.js"));
        assertTrue(page.contains("感想を送る"));assertTrue(page.contains("詳細・通報"));
        var comments=client.get("/api/messages?eventId="+event).body();
        assertTrue(comments.contains("表示試験"));assertFalse(comments.contains("positionX"));
    }
    @Test @Order(9)void foreignKeysAndChecks() {
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("insert into messages(event_id,message,is_anonymous,status,petal_type,petal_seed) values(?,'無対象',true,'PUBLISHED',1,77777)",event));
        assertThrows(org.springframework.dao.DataAccessException.class,()->db.update("insert into artworks(creator_id,slug,title,status) values(-1,'invalid-fk','x','PUBLISHED')"));
    }
    @Test @Order(10)void sharedCreatorAcrossTwoPlaypitEvents() throws Exception {
        String secondSlug=playpitSlug+"-second";
        long second=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'次回展示',localtimestamp,localtimestamp+interval '2 days','PUBLISHED') returning event_id",Long.class,secondSlug);
        long secondArtwork=db.queryForObject("insert into artworks(creator_id,slug,title,status) values(?,?,'次回限定作品','PUBLISHED') returning artwork_id",Long.class,creator,secondSlug+"-art");
        var admin=context.getBean(world.creve.playpit.service.ContentAdminService.class);
        int before=db.queryForObject("select count(*) from creators",Integer.class);
        admin.addParticipant(second,creator,1,"次回の展示名");
        admin.addParticipant(second,creator,2,"展示名更新");
        admin.addExhibition(second,secondArtwork,1,"次回の展示場所");
        assertEquals(before,db.queryForObject("select count(*) from creators",Integer.class));
        assertEquals(1,db.queryForObject("select count(*) from event_creators where event_id=? and creator_id=?",Integer.class,second,creator));
        String profile="/creators/"+playpitSlug+"-creator";
        for(String slug:List.of(playpitSlug,secondSlug)) {
            for(String path:List.of("/playpit/"+slug,"/playpit/"+slug+"/creators")) {
                var page=client.get(path); assertEquals(200,page.statusCode(),page.body());
                assertTrue(page.body().contains("href=\""+profile+"?event="+slug+"\""),page.body());
            }
            var legacy=client.get("/playpit/"+slug+"/creators/"+playpitSlug+"-creator?source=qr");
            assertEquals(308,legacy.statusCode());
            assertEquals(profile+"?event="+slug+"&page=0&artworkPage=0&source=qr",legacy.headers().firstValue("location").orElseThrow());
        }
        var first=client.get(profile+"?event="+playpitSlug);
        var next=client.get(profile+"?event="+secondSlug);
        assertEquals(200,first.statusCode(),first.body());assertEquals(200,next.statusCode(),next.body());
        assertTrue(first.body().contains("/playpit/"+playpitSlug+"/artworks/"+playpitSlug+"-art"));
        assertFalse(first.body().contains("次回限定作品"));
        assertTrue(next.body().contains("次回限定作品"));
        assertFalse(next.body().contains("/playpit/"+playpitSlug+"/artworks/"+playpitSlug+"-art"));
        assertFalse(next.body().contains("表示試験"));
        assertEquals(200,client.get("/playpit/"+secondSlug+"/artworks/"+secondSlug+"-art").statusCode());
        db.update("update events set status='HIDDEN' where event_id=?",second);
        assertEquals(404,client.get(profile+"?event="+secondSlug).statusCode());
        assertEquals(404,client.get("/playpit/"+secondSlug+"/creators/"+playpitSlug+"-creator").statusCode());
    }

    @Test @Order(11)void adminCanReuseAndEditSharedCreatorThroughHttp() throws Exception {
        Client admin=new Client();
        String email="test-"+UUID.randomUUID()+"@example.invalid", password=UUID.randomUUID().toString();
        var encoder=context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);
        db.update("insert into admins(email,password_hash,enabled) values(?,?,true)",email,encoder.encode(password));
        admin.refresh("/admin/login");
        var login=admin.form("/admin/login",Map.of("email",email,"password",password));
        assertEquals(302,login.statusCode(),login.body());
        assertTrue(login.headers().firstValue("location").orElseThrow().endsWith("/admin/dashboard"));
        admin.refresh("/admin/creators");
        assertTrue(admin.page.contains(playpitSlug+"-creator"));
        admin.refresh("/admin/events/"+event+"/edit");
        assertTrue(admin.page.contains("既存のクリエイターとIDを確認"));
        long third=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'管理経由展示',localtimestamp,localtimestamp+interval '3 days','PUBLISHED') returning event_id",Long.class,playpitSlug+"-third");
        int before=db.queryForObject("select count(*) from creators",Integer.class);
        for(int order:List.of(1,2)) {
            var response=admin.form("/admin/events/"+third+"/participants",Map.of("creatorId",creator+"","displayOrder",order+"","exhibitionTitle","今回の展示"));
            assertEquals(302,response.statusCode(),response.body());
        }
        assertEquals(before,db.queryForObject("select count(*) from creators",Integer.class));
        assertEquals(1,db.queryForObject("select count(*) from event_creators where event_id=? and creator_id=?",Integer.class,third,creator));
        admin.refresh("/admin/creators/"+creator+"/edit");
        assertTrue(admin.page.contains("readonly"));
        var form=new HashMap<String,String>();
        form.put("slug",playpitSlug+"-creator");form.put("name","作者の更新名");form.put("profile","全展示で共通の紹介");form.put("status","PUBLISHED");
        assertEquals(302,admin.form("/admin/creators/"+creator,form).statusCode());
        for(String slug:List.of(playpitSlug,playpitSlug+"-third")) {
            var page=client.get("/creators/"+playpitSlug+"-creator?event="+slug);
            assertEquals(200,page.statusCode(),page.body());assertTrue(page.body().contains("全展示で共通の紹介"));
        }
        form.put("slug","renamed");
        var invalidSlug=admin.form("/admin/creators/"+creator,form);
        assertEquals(200,invalidSlug.statusCode());
        assertTrue(invalidSlug.body().contains("登録後のURL識別子は変更できません"));
        assertEquals(playpitSlug+"-creator",db.queryForObject("select slug from creators where creator_id=?",String.class,creator));
        db.update("update creators set status='HIDDEN' where creator_id=?",creator);
        assertEquals(404,client.get("/creators/"+playpitSlug+"-creator?event="+playpitSlug).statusCode());
        assertEquals(404,client.get("/playpit/"+playpitSlug+"/creators/"+playpitSlug+"-creator").statusCode());
        db.update("update creators set status='PUBLISHED' where creator_id=?",creator);
    }

    @Test @Order(12)void playpitNavigationHidesOnlyRequestedProjects() throws Exception {
        for(String path:List.of("/playpit","/playpit/events","/playpit/archive","/playpit/"+playpitSlug,"/creators","/creators/"+playpitSlug+"-creator?event="+playpitSlug)) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            for(String section:List.of("header","footer")) {
                var matcher=Pattern.compile("<"+section+"\\b[^>]*>(.*?)</"+section+">",Pattern.DOTALL).matcher(response.body());
                assertTrue(matcher.find(),response.body());String nav=matcher.group(1);
                assertFalse(nav.contains("href=\"/about\""),nav);
                assertFalse(nav.contains("href=\"/NLJ\""),nav);
                assertFalse(nav.contains("href=\"/vr\""),nav);
                assertFalse(nav.contains("CreVeについて"),nav);assertFalse(nav.contains("VR構想"),nav);
                assertTrue(nav.contains("href=\"/creators\""),nav);assertTrue(nav.contains("href=\"/playpit\""),nav);
            }
        }
        for(String path:List.of("/","/NLJ","/about","/vr")) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            assertTrue(response.body().contains("href=\"/about\""),response.body());
            assertTrue(response.body().contains("href=\"/NLJ\""),response.body());
            assertTrue(response.body().contains("href=\"/vr\""),response.body());
        }
    }

    @Test @Order(12)void sixDestinationsAboutAndCircularDirectoryRemainPublic() throws Exception {
        for(String path:List.of("/playpit","/creators","/playpit/events","/playpit/archive","/playpit/about","/playpit/news","/playpit/contact")){
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            var header=Pattern.compile("<header[^>]*>(.*?)</header>",Pattern.DOTALL).matcher(response.body());assertTrue(header.find());
            for(String label:List.of("HOME","CREATER","EXHIBITION","ABOUT","NEWS","CONTACT"))assertTrue(header.group(1).contains(">"+label+"</a>"),header.group(1));
            assertFalse(response.body().contains("/lp/playpit/"));
        }
        var about=client.get("/playpit/about");assertFalse(about.body().contains("<iframe"));assertFalse(about.headers().firstValue("Content-Security-Policy").orElseThrow().contains("youtube-nocookie"));
        assertTrue(client.get("/creators").body().contains("creator-avatar"));
        assertTrue(client.get("/playpit/contact").body().contains("お問い合わせフォームは準備中"));
        assertFalse(client.get("/playpit/contact").body().contains("docs.google.com/forms"));
    }

    @Test @Order(13)void publicThemeReplacesOperationalLinksWithoutDeletingThem() throws Exception {
        String privateDescription="詳細\nhttps://canva.link/private-material\n参加フォーム\nhttps://docs.google.com/forms/private-form";
        String theme="変化の中でも、変わらず大切にしているもの。\n自分にとって、転機となった作品。\nこれからの自分につながる、新しい挑戦。<script>unsafe</script>";
        db.update("update events set start_at='2026-12-15 00:00:00',end_at='2026-12-20 16:30:00',summary=?,description=?,ticket_url='https://docs.google.com/forms/private-ticket',main_image_url='/playpit/images/events/beforeafter-2026.png' where event_id=?",theme,privateDescription,event);
        for(String path:List.of("/playpit/"+playpitSlug,"/playpit/events","/playpit")) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            assertFalse(response.body().contains("canva.link"));assertFalse(response.body().contains("docs.google.com/forms"));
            assertFalse(response.body().contains("チケット・来場予約"));assertFalse(response.body().contains("参加フォーム"));
            assertTrue(response.body().contains("beforeafter-2026.png"));
            assertFalse(response.body().contains("<script>unsafe</script>"));
        }
        String page=client.get("/playpit/"+playpitSlug).body();
        assertTrue(page.contains("2026/12/15"));assertFalse(page.contains("00:00"));assertTrue(page.contains("2026/12/20 16:30"));
        assertTrue(page.contains("&lt;script&gt;unsafe&lt;/script&gt;"));assertFalse(page.contains("https://lin.ee/t3x3VJr"));
        assertTrue(page.indexOf("id=\"theme\"")<page.indexOf("参加クリエイター"));
        assertEquals(privateDescription,db.queryForObject("select description from events where event_id=?",String.class,event));
        assertEquals("https://docs.google.com/forms/private-ticket",db.queryForObject("select ticket_url from events where event_id=?",String.class,event));
    }
    @Test @Order(13)void videoIsOptInAndCspExceptionIsLandingPageOnly() throws Exception {
        var top=client.get("/playpit");String policy=top.headers().firstValue("Content-Security-Policy").orElseThrow();
        assertTrue(policy.contains("frame-src 'self' https://www.youtube-nocookie.com"));
        assertTrue(policy.contains("script-src 'self'"));assertTrue(policy.contains("frame-ancestors 'none'"));
        assertFalse(top.body().contains("<iframe"));assertTrue(top.body().contains("data-load-exhibition-video"));
        assertTrue(top.body().indexOf("exhibition-film")<top.body().indexOf("id=\"artists\""));
        String js=client.get("/playpit/js/exhibition-video.js").body();
        assertTrue(js.contains("youtube-nocookie.com/embed/RuZj3S_MjQo?autoplay=0"));assertFalse(js.contains("autoplay=1"));
        for(String path:List.of("/","/NLJ","/creators","/playpit/events","/playpit/"+playpitSlug,"/admin/login")) {
            var response=client.get(path);
            assertEquals(policy.replace("; frame-src 'self' https://www.youtube-nocookie.com",""),response.headers().firstValue("Content-Security-Policy").orElseThrow(),path);
        }
    }

    @Test @Order(13)void recruitmentLpSharesEventButSeparatesAudienceAndKeepsGenericLp() throws Exception {
        db.update("update events set price='PRIVATE-PRICE-TEST' where event_id=?",event);
        var general=client.get("/playpit/"+playpitSlug);var recruitment=client.get("/lp/playpit/"+playpitSlug);
        assertEquals(200,general.statusCode());assertEquals(200,recruitment.statusCode());
        String publicBody=general.body(),lpBody=recruitment.body();
        assertFalse(publicBody.contains("lin.ee"));assertFalse(publicBody.contains("公式LINE"));assertFalse(publicBody.contains("/lp/playpit/"));
        assertTrue(publicBody.contains("Artists"));assertTrue(publicBody.contains("Works"));assertFalse(publicBody.contains("data-load-exhibition-video"));
        assertTrue(recruitment.headers().firstValue("Content-Security-Policy").orElseThrow().contains("frame-src 'self' https://www.youtube-nocookie.com"));
        assertTrue(lpBody.contains("https://lin.ee/t3x3VJr"));assertTrue(lpBody.contains("data-load-exhibition-video"));assertFalse(lpBody.contains("<iframe"));
        assertFalse(lpBody.contains("Artists"));assertFalse(lpBody.contains("Works"));
        for(String body:List.of(publicBody,lpBody)) {
            assertFalse(body.contains("PRIVATE-PRICE-TEST"));assertFalse(body.contains("<dt>料金</dt>"));
            assertFalse(body.contains("canva.link"));assertFalse(body.contains("docs.google.com/forms"));
        }
        Pattern theme=Pattern.compile("<section class=\"exhibition-theme\".*?</section>",Pattern.DOTALL);
        Matcher publicTheme=theme.matcher(publicBody),lpTheme=theme.matcher(lpBody);assertTrue(publicTheme.find());assertTrue(lpTheme.find());assertEquals(publicTheme.group(),lpTheme.group());
        assertTrue(client.get("/playpit").body().contains("href=\"/playpit/"+playpitSlug+"\""));
        for(String path:List.of("/playpit","/playpit/events","/playpit/"+playpitSlug,"/creators","/playpit/news","/playpit/contact"))assertFalse(client.get(path).body().contains("/lp/playpit/"),path);
        assertTrue(lpBody.contains("href=\"/playpit\""));
        assertEquals("PRIVATE-PRICE-TEST",db.queryForObject("select price from events where event_id=?",String.class,event));
        assertEquals(404,client.get("/lp/playpit/does-not-exist").statusCode());
        assertEquals(404,client.get("/lp/playpit/"+nljSlug).statusCode());
        db.update("update events set status='DRAFT' where event_id=?",event);
        try {assertEquals(404,client.get("/lp/playpit/"+playpitSlug).statusCode());}
        finally {db.update("update events set status='PUBLISHED' where event_id=?",event);}
        String generic=playpitSlug+"-generic-lp";
        db.update("insert into landing_pages(slug,title,body,status) values(?,'Existing LP','Original content','PUBLISHED')",generic);
        var existing=client.get("/lp/"+generic);assertEquals(200,existing.statusCode());assertTrue(existing.body().contains("Original content"));assertFalse(existing.headers().firstValue("Content-Security-Policy").orElseThrow().contains("youtube-nocookie"));
    }

    @Test @Order(14)void dedicatedNewsAndContactDoNotMixProjects() throws Exception {
        db.update("insert into news(slug,title,body,status,published_at) values(?,?,'CreVe only','PUBLISHED',localtimestamp)",playpitSlug+"-news","CREVE_ONLY_NEWS");
        db.update("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'HIDDEN_PLAYPIT_NOTICE',localtimestamp,localtimestamp+interval '1 day','HIDDEN')",playpitSlug+"-hidden");
        var news=client.get("/playpit/news");assertEquals(200,news.statusCode(),news.body());
        assertTrue(news.body().contains("PLAYPITのお知らせ"));assertTrue(news.body().contains("テスト展示"));
        assertFalse(news.body().contains("テストライブ"));assertFalse(news.body().contains("CREVE_ONLY_NEWS"));assertFalse(news.body().contains("HIDDEN_PLAYPIT_NOTICE"));
        assertTrue(client.get("/news").body().contains("CREVE_ONLY_NEWS"));
        var contact=client.get("/playpit/contact");assertEquals(200,contact.statusCode(),contact.body());
        assertTrue(contact.body().contains("お問い合わせフォームは準備中"));assertFalse(contact.body().contains("<form"));
        assertEquals(200,client.get("/contact").statusCode());
        for(String path:List.of("/playpit","/playpit/news","/playpit/contact","/admin/login","/news","/contact")) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            assertEquals(1,Pattern.compile("<header\\b").matcher(response.body()).results().count(),response.body());
            assertEquals(1,Pattern.compile("<footer\\b").matcher(response.body()).results().count(),response.body());
            var nav=Pattern.compile("<(?:header|footer)\\b.*?</(?:header|footer)>",Pattern.DOTALL).matcher(response.body());
            String links=nav.results().map(m->m.group()).collect(java.util.stream.Collectors.joining());
            boolean playpit=path.startsWith("/playpit");
            assertTrue(links.contains("href=\""+(playpit?"/playpit/news":"/news")+"\""));
            assertTrue(links.contains("href=\""+(playpit?"/playpit/contact":"/contact")+"\""));
            if(playpit){assertFalse(links.contains("href=\"/news\""));assertFalse(links.contains("href=\"/contact\""));}
        }
    }

    @Test @Order(15)void suppliedLogoOnlyBrandsPlaypitPages() throws Exception {
        String asset="/playpit/images/brand/playpit-logo.png";
        for(String path:List.of("/playpit","/playpit/news","/playpit/contact","/creators")) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());
            assertEquals(2,Pattern.compile("src=\""+Pattern.quote(asset)+"\"").matcher(response.body()).results().count());
            assertTrue(response.body().contains("alt=\"PLAYPIT!\""));assertTrue(response.body().contains(" | PLAYPIT</title>"));
            assertTrue(response.body().contains("aria-label=\"PLAYPIT トップ\""));
        }
        for(String path:List.of("/","/NLJ","/admin/login")) {
            var response=client.get(path);assertEquals(200,response.statusCode(),response.body());assertFalse(response.body().contains(asset));
            assertTrue(response.body().contains("CreVe"));
        }
        var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(base+asset)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,response.statusCode());assertTrue(response.headers().firstValue("content-type").orElseThrow().startsWith("image/png"));
        try(var source=ApplicationPostgresIT.class.getResourceAsStream("/static"+asset)) {assertNotNull(source);assertArrayEquals(source.readAllBytes(),response.body());}
    }

    static byte[] testPng(int width,int height)throws Exception {
        var output=new java.io.ByteArrayOutputStream();javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(width,height,java.awt.image.BufferedImage.TYPE_INT_RGB),"png",output);return output.toByteArray();
    }
    static Client authenticatedAdmin()throws Exception {
        Client admin=new Client();String email="upload-"+UUID.randomUUID()+"@example.invalid",password=UUID.randomUUID().toString();
        db.update("insert into admins(email,password_hash,enabled) values(?,?,true)",email,context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class).encode(password));
        admin.refresh("/admin/login");assertEquals(302,admin.form("/admin/login",Map.of("email",email,"password",password)).statusCode());admin.refresh("/admin/creators/new");return admin;
    }
    @Test @Order(16)void imageUploadAndCreatorOwnedWorksThroughHttp()throws Exception {
        Client admin=authenticatedAdmin();byte[] png=testPng(8,6);
        var person=new HashMap<String,String>();person.put("slug",playpitSlug+"-upload");person.put("name","画像試験の作者");person.put("status","DRAFT");
        assertEquals(403,admin.multipart("/admin/creators",person,"profileImage",png,false).statusCode());
        Client anonymous=new Client();anonymous.refresh("/playpit");assertEquals(302,anonymous.multipart("/admin/creators",person,"profileImage",png,true).statusCode());
        var created=admin.multipart("/admin/creators",person,"profileImage",png,true);assertEquals(302,created.statusCode(),created.body());
        long owner=db.queryForObject("select creator_id from creators where slug=?",Long.class,person.get("slug"));
        String image=db.queryForObject("select profile_image_url from creators where creator_id=?",String.class,owner);
        assertTrue(image.startsWith("/playpit/uploads/"));assertEquals(404,anonymous.get(image).statusCode());assertEquals(200,admin.get(image).statusCode());
        admin.refresh("/admin/creators/"+owner+"/edit");assertTrue(admin.page.contains("この人の作品を追加"));assertTrue(admin.page.contains("type=\"file\""));
        person.put("profileImageUrl",image);person.put("status","PUBLISHED");
        assertEquals(302,admin.multipart("/admin/creators/"+owner,person,"profileImage",null,true).statusCode());
        assertEquals(image,db.queryForObject("select profile_image_url from creators where creator_id=?",String.class,owner));assertEquals(200,anonymous.get(image).statusCode());
        var work=new HashMap<String,String>();work.put("creatorId",Long.toString(creator));work.put("slug",playpitSlug+"-uploaded-work");work.put("title","画像付き作品");work.put("description","作品説明を保持");work.put("status","PUBLISHED");
        admin.refresh("/admin/creators/"+owner+"/artworks/new");assertTrue(admin.page.contains("画像試験の作者"));
        var saved=admin.multipart("/admin/creators/"+owner+"/artworks",work,"artworkImage",png,true);assertEquals(302,saved.statusCode(),saved.body());
        long art=db.queryForObject("select artwork_id from artworks where creator_id=?",Long.class,owner);
        assertEquals(owner,db.queryForObject("select creator_id from artworks where artwork_id=?",Long.class,art));
        assertEquals(0,db.queryForObject("select count(*) from event_artworks where artwork_id=?",Integer.class,art));
        String artImage=db.queryForObject("select main_image_url from artworks where artwork_id=?",String.class,art);assertEquals(200,anonymous.get(artImage).statusCode());
        var edit=admin.get("/admin/artworks/"+art+"/edit");assertEquals(200,edit.statusCode(),edit.body());assertTrue(edit.body().contains("作品説明を保持"));
        work.put("creatorId",Long.toString(owner));work.put("mainImageUrl",artImage);work.put("title","作品名を編集");
        assertEquals(302,admin.multipart("/admin/artworks/"+art,work,"artworkImage",null,true).statusCode());
        assertEquals(artImage,db.queryForObject("select main_image_url from artworks where artwork_id=?",String.class,art));
        work.put("slug",playpitSlug+"-second-upload");work.put("title","2点目の作品");
        assertEquals(302,admin.multipart("/admin/creators/"+owner+"/artworks",work,"artworkImage",png,true).statusCode());
        var profile=admin.get("/admin/creators/"+owner+"/edit");assertTrue(profile.body().contains("作品名を編集"));assertTrue(profile.body().contains("2点目の作品"));
        assertEquals(2,db.queryForObject("select count(*) from artworks where creator_id=?",Integer.class,owner));
        db.update("update creators set status='HIDDEN' where creator_id=?",owner);assertEquals(404,anonymous.get(artImage).statusCode());assertEquals(200,admin.get(artImage).statusCode());
    }
    @Test @Order(19) void artworkIdentifiersAreAutomaticAndStable() throws Exception {
        Client admin=authenticatedAdmin();
        var form=new HashMap<String,String>();form.put("creatorId",Long.toString(creator));form.put("title","同じ日本語の作品名");form.put("status","DRAFT");
        var ids=new java.util.ArrayList<Long>();var slugs=new java.util.HashSet<String>();
        for(String input:new String[]{null,"","日本語の識別子"}) {
            if(input==null)form.remove("slug");else form.put("slug",input);
            admin.refresh("/admin/creators/"+creator+"/artworks/new");assertFalse(admin.page.contains("name=\"slug\""));
            var response=admin.multipart("/admin/creators/"+creator+"/artworks",form,"artworkImage",testPng(2,2),true);
            assertEquals(302,response.statusCode(),response.body());
            long id=Long.parseLong(java.net.URI.create(response.headers().firstValue("location").orElseThrow()).getPath().split("/")[3]);ids.add(id);
            String slug=db.queryForObject("select slug from artworks where artwork_id=?",String.class,id);
            assertTrue(slug.matches("work-[a-f0-9]{32}"));assertTrue(slugs.add(slug));
        }
        long id=ids.getFirst();String original=db.queryForObject("select slug from artworks where artwork_id=?",String.class,id);
        form.put("title","編集後の作品名");form.put("slug","URL改ざん");
        assertEquals(302,admin.multipart("/admin/artworks/"+id,form,"artworkImage",null,true).statusCode());
        assertEquals(original,db.queryForObject("select slug from artworks where artwork_id=?",String.class,id));
        String legacy=db.queryForObject("select slug from artworks where artwork_id=?",String.class,artwork);
        form.put("creatorId",Long.toString(creator));
        assertEquals(302,admin.multipart("/admin/artworks/"+artwork,form,"artworkImage",null,true).statusCode());
        assertEquals(legacy,db.queryForObject("select slug from artworks where artwork_id=?",String.class,artwork));
        form.put("title","");assertEquals(200,admin.multipart("/admin/creators/"+creator+"/artworks",form,"artworkImage",null,true).statusCode());
    }

    @Test @Order(17)void invalidImagesDoNotSaveAndFormsRemainEditable()throws Exception {
        Client admin=authenticatedAdmin();var fields=Map.of("slug",playpitSlug+"-invalid-upload","name","入力を保持","status","DRAFT");
        var invalid=admin.multipart("/admin/creators",fields,"profileImage","<svg onload='alert(1)'/>".getBytes(),true);
        assertEquals(200,invalid.statusCode(),invalid.body());assertTrue(invalid.body().contains("JPEGまたはPNG"));assertTrue(invalid.body().contains("入力を保持"));
        assertEquals(0,db.queryForObject("select count(*) from creators where slug=?",Integer.class,fields.get("slug")));
        var dimensions=admin.multipart("/admin/creators",fields,"profileImage",testPng(4097,1),true);assertEquals(200,dimensions.statusCode());assertTrue(dimensions.body().contains("4096px"));
        var oversize=admin.multipart("/admin/creators",fields,"profileImage",new byte[10*1024*1024+1],true);assertEquals(413,oversize.statusCode(),oversize.body());
        assertTrue(oversize.body().contains("画像の容量を確認"));
        assertEquals(0,db.queryForObject("select count(*) from creators where slug=?",Integer.class,fields.get("slug")));
    }
    @Test @Order(18)void databaseRollbackRemovesOnlyNewUpload()throws Exception {
        long before;try(var files=java.nio.file.Files.list(uploadDirectory)){before=files.count();}
        var service=context.getBean(world.creve.playpit.service.UploadedContentService.class);
        var transaction=new org.springframework.transaction.support.TransactionTemplate(context.getBean(org.springframework.transaction.PlatformTransactionManager.class));
        var form=new world.creve.playpit.dto.request.CreatorForm();form.setSlug(playpitSlug+"-rollback");form.setName("rollback");form.setStatus("DRAFT");
        var image=org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);byte[] png=testPng(2,2);
        org.mockito.Mockito.when(image.getSize()).thenReturn((long)png.length);org.mockito.Mockito.when(image.getBytes()).thenReturn(png);
        assertThrows(IllegalStateException.class,()->transaction.execute(status->{service.saveCreator(null,form,image);throw new IllegalStateException("rollback test");}));
        try(var files=java.nio.file.Files.list(uploadDirectory)){assertEquals(before,files.count());}
        assertEquals(0,db.queryForObject("select count(*) from creators where slug=?",Integer.class,form.getSlug()));
    }

    @Test @Order(99) void publicRankingFiltersHiddenAndCancelledParticipationAndValidatesSpace() throws Exception {
        long c=db.queryForObject("insert into creators(slug,name,status) values(?,'Ranking Artist','PUBLISHED') returning creator_id",Long.class,playpitSlug+"-ranking");
        long e=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'Ranking Event','2999-01-01','2999-01-02','PUBLISHED') returning event_id",Long.class,playpitSlug+"-ranking-event");
        var service=context.getBean(world.creve.playpit.service.ContentAdminService.class);
        var repo=context.getBean(world.creve.platform.repository.EventCreatorRepository.class);
        var creatorService=context.getBean(world.creve.platform.service.CreatorService.class);
        service.addParticipant(e,c,0,null,"LARGE");
        service.addParticipant(e,c,1,null,"__KEEP");
        assertEquals("LARGE",repo.findByEventIdAndCreatorId(e,c).orElseThrow().getSpaceType());
        assertEquals(1,db.queryForObject("select count(*) from event_creators where event_id=? and creator_id=?",Integer.class,e,c));
        assertEquals(c,creatorService.getPublishedCreators(0).getContent().getFirst().creatorId());
        assertThrows(world.creve.platform.exception.InvalidMessageException.class,()->service.addParticipant(e,c,0,null,"①"));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->db.update("update event_creators set space_type='BAD' where event_id=?",e));
        db.update("update events set status='HIDDEN' where event_id=?",e);
        assertTrue(repo.findPublicRankingParticipations().stream().noneMatch(p->p.eventId().equals(e)));
        db.update("update events set status='PUBLISHED' where event_id=?",e);
        db.update("delete from event_creators where event_id=? and creator_id=?",e,c);
        assertTrue(repo.findPublicRankingParticipations().stream().noneMatch(p->p.eventId().equals(e)));
        assertEquals(200,client.get("/creators").statusCode());
    }
    @Test @Order(100) void homeExhibitionsSameStartIsStableAndAdminInputsExist()throws Exception {
        long first=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'Tie First','2998-01-01','2998-01-02','PUBLISHED') returning event_id",Long.class,playpitSlug+"-tie-a");
        long second=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'Tie Second','2998-01-01','2998-01-02','PUBLISHED') returning event_id",Long.class,playpitSlug+"-tie-b");
        var list=context.getBean(world.creve.platform.service.EventService.class).homeExhibitions().getContent();
        var ids=list.stream().map(world.creve.platform.dto.EventResponse::eventId).toList();assertTrue(ids.indexOf(second)<ids.indexOf(first));
        var home=client.get("/playpit").body();assertTrue(home.contains("home-exhibition-compact"));assertFalse(home.contains("/lp/playpit/"));
        var admin=authenticatedAdmin();
        String creatorForm=admin.get("/admin/creators/new").body();for(String field:List.of("name","profileImage","profile","snsLinks","status"))assertTrue(creatorForm.contains("name=\""+field+"\""),field);
        String eventForm=admin.get("/admin/events/"+event+"/edit").body();for(String field:List.of("title","startAt","endAt","venueName","mainImageUrl","summary","creatorId","spaceType"))assertTrue(eventForm.contains("name=\""+field+"\""),field);
    }
    static String importToken(String html){var m=Pattern.compile("name=\"token\"[^>]*value=\"([^\"]+)\"").matcher(html);assertTrue(m.find(),html);return m.group(1);}
    @Test @Order(101) void participantImportPreviewCommitReplayAndCrossEvent()throws Exception {
        var admin=authenticatedAdmin();String route="/admin/events/"+event+"/participants/import";admin.refresh(route);
        String header="artist_id,slug,name,sns_url,profile,concept,space\n";
        String a=playpitSlug+"-bulk-a",b=playpitSlug+"-bulk-b";
        String csv=header+","+a+",表の作者A,https://example.com/"+a+",<script>alert(1)</script>,制作意図,大\n,"+b+",表の作者B,,,テーマ,小\n";
        int before=db.queryForObject("select count(*) from creators",Integer.class);
        assertEquals(403,admin.multipart(route+"/preview",Map.of(),"table",csv.getBytes(java.nio.charset.StandardCharsets.UTF_8),false).statusCode());
        var preview=admin.multipart(route+"/preview",Map.of(),"table",csv.getBytes(java.nio.charset.StandardCharsets.UTF_8),true);assertEquals(200,preview.statusCode(),preview.body());assertTrue(preview.body().contains("新規作家・下書き"));assertFalse(preview.body().contains("<script>alert"));assertTrue(preview.body().contains("&lt;script&gt;"));assertEquals(before,db.queryForObject("select count(*) from creators",Integer.class));
        String token=importToken(preview.body());
        var unconfirmed=admin.form(route+"/commit",Map.of("token",token));assertEquals(200,unconfirmed.statusCode());assertEquals(before,db.queryForObject("select count(*) from creators",Integer.class));
        var result=admin.form(route+"/commit",Map.of("token",token,"confirmed","true"));assertEquals(200,result.statusCode(),result.body());assertTrue(result.body().contains("実行結果"));assertEquals(before+2,db.queryForObject("select count(*) from creators",Integer.class));
        long artist=db.queryForObject("select creator_id from creators where slug=?",Long.class,a);assertEquals("DRAFT",db.queryForObject("select status from creators where creator_id=?",String.class,artist));assertFalse(client.get("/creators").body().contains("表の作者A"));
        admin.form(route+"/commit",Map.of("token",token,"confirmed","true"));assertEquals(before+2,db.queryForObject("select count(*) from creators",Integer.class));
        var again=admin.multipart(route+"/preview",Map.of(),"table",csv.getBytes(java.nio.charset.StandardCharsets.UTF_8),true);admin.form(route+"/commit",Map.of("token",importToken(again.body()),"confirmed","true"));assertEquals(before+2,db.queryForObject("select count(*) from creators",Integer.class));
        long other=db.queryForObject("insert into events(event_type,slug,title,start_at,end_at,status) values('PLAYPIT',?,'別開催','2027-01-01','2027-01-02','PUBLISHED') returning event_id",Long.class,playpitSlug+"-bulk-other");String otherRoute="/admin/events/"+other+"/participants/import";
        String existing=header+artist+",,表の作者A,,上書き禁止,,中\n";admin.refresh(otherRoute);var second=admin.multipart(otherRoute+"/preview",Map.of(),"table",existing.getBytes(java.nio.charset.StandardCharsets.UTF_8),true);assertTrue(second.body().contains("既存作家を参加追加"));
        assertTrue(admin.form(route+"/commit",Map.of("token",importToken(second.body()),"confirmed","true")).body().contains("対象展示の相違"));
        admin.form(otherRoute+"/commit",Map.of("token",importToken(second.body()),"confirmed","true"));
        assertEquals(2,db.queryForObject("select count(*) from event_creators where creator_id=?",Integer.class,artist));assertEquals("LARGE",db.queryForObject("select space_type from event_creators where creator_id=? and event_id=?",String.class,artist,event));assertEquals("MEDIUM",db.queryForObject("select space_type from event_creators where creator_id=? and event_id=?",String.class,artist,other));assertEquals("<script>alert(1)</script>",db.queryForObject("select profile from creators where creator_id=?",String.class,artist));
        String duplicate=header+artist+",,表の作者A,,,,小\n";admin.refresh(route);var third=admin.multipart(route+"/preview",Map.of(),"table",duplicate.getBytes(),true);assertTrue(third.body().contains("登録済み・変更なし"));admin.form(route+"/commit",Map.of("token",importToken(third.body()),"confirmed","true"));assertEquals("LARGE",db.queryForObject("select space_type from event_creators where creator_id=? and event_id=?",String.class,artist,event));
        db.update("update creators set status='PUBLISHED' where creator_id=?",artist);
        assertTrue(client.get("/playpit/"+playpitSlug).body().contains("/creators/"+a+"?event="+playpitSlug));assertTrue(client.get("/playpit/"+playpitSlug+"-bulk-other").body().contains("/creators/"+a+"?event="+playpitSlug+"-bulk-other"));
        assertEquals(2,context.getBean(world.creve.platform.repository.EventCreatorRepository.class).findPublicRankingParticipations().stream().filter(p->p.creatorId().equals(artist)).count());
        var invalid=admin.multipart(route+"/preview",Map.of(),"table",(header+",,表の作者A,,,,①\n").getBytes(),true);assertTrue(invalid.body().contains("エラー"));assertFalse(invalid.body().contains("name=\"token\""));
        var cancellation=admin.multipart(route+"/preview",Map.of(),"table",(header+",,取消する作者,,,,\n").getBytes(),true);String cancelToken=importToken(cancellation.body());admin.form(route+"/cancel",Map.of());assertTrue(admin.form(route+"/commit",Map.of("token",cancelToken,"confirmed","true")).body().contains("再度アップロード"));assertEquals(0,db.queryForObject("select count(*) from creators where name='取消する作者'",Integer.class));
    }
    @Test @Order(102) void eventImageUploadPreservesOldAndVisibility()throws Exception {
        var admin=authenticatedAdmin();String slug=playpitSlug+"-image-event";
        var fields=new HashMap<String,String>();fields.put("eventType","PLAYPIT");fields.put("slug",slug);fields.put("title","画像展示");fields.put("startAt","2027-01-01T00:00");fields.put("endAt","2027-01-02T00:00");fields.put("status","DRAFT");
        admin.refresh("/admin/events/new");var saved=admin.multipart("/admin/events",fields,"eventImage",testPng(10,10),true);assertEquals(302,saved.statusCode(),saved.body());
        long id=db.queryForObject("select event_id from events where slug=?",Long.class,slug);String url=db.queryForObject("select main_image_url from events where event_id=?",String.class,id);assertEquals(404,client.get(url).statusCode());assertEquals(200,admin.get(url).statusCode());
        fields.put("mainImageUrl",url);fields.put("status","PUBLISHED");admin.refresh("/admin/events/"+id+"/edit");assertEquals(302,admin.multipart("/admin/events/"+id,fields,"eventImage",null,true).statusCode());assertEquals(url,db.queryForObject("select main_image_url from events where event_id=?",String.class,id));assertEquals(200,client.get(url).statusCode());
        for(String path:List.of("/playpit","/playpit/events","/playpit/"+slug))assertTrue(client.get(path).body().contains(url));
        fields.put("title","保存されない変更");var invalid=admin.multipart("/admin/events/"+id,fields,"eventImage","<svg/>".getBytes(),true);assertEquals(200,invalid.statusCode());assertTrue(invalid.body().contains("JPEGまたはPNG"));assertEquals("画像展示",db.queryForObject("select title from events where event_id=?",String.class,id));assertEquals(url,db.queryForObject("select main_image_url from events where event_id=?",String.class,id));
    }
}
