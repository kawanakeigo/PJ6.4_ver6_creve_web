package world.creve.playpit.service;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.net.URI;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import world.creve.platform.exception.InvalidMessageException;
@Service public class ParticipantImportService {
 public record Row(int number,Long artistId,String slug,String name,String sns,String profile,String concept,String space,String action,String status,String error) {}
 public record Plan(Long eventId,String eventTitle,String fingerprint,List<List<String>> cells,List<Row> rows){public boolean isValid(){return rows.stream().allMatch(r->r.error().isEmpty());}}
 public record Result(int created,int linked,int skipped) {}
 private final JdbcTemplate db;private final ParticipantTableParser parser;
 public ParticipantImportService(JdbcTemplate db,ParticipantTableParser parser){this.db=db;this.parser=parser;}
 public String eventTitle(Long id){var names=db.queryForList("select title from events where event_id=? and event_type='PLAYPIT'",String.class,id);if(names.size()!=1)throw bad("対象のPLAYPIT展示が見つかりません。");return names.getFirst();}
 @Transactional(readOnly=true) public Plan preview(Long eventId,byte[] bytes,String filename){
  try{return plan(eventId,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),parser.parse(bytes,filename));}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
 }
 private Plan plan(Long eventId,String hash,List<List<String>> cells){
  String title=eventTitle(eventId);var rows=new ArrayList<Row>();var seen=new HashSet<String>();int number=1;
  for(var cell:cells){number++;Long id=null;String slug=cell.get(1).strip(),name=cell.get(2).strip(),sns=cell.get(3).strip(),profile=cell.get(4),concept=cell.get(5),space=null,action="エラー",status="",error="";
   try{
    if(!cell.get(0).isBlank()){if(!cell.get(0).strip().matches("[1-9][0-9]{0,17}"))throw bad("artist_idは正の整数です。");id=Long.parseLong(cell.get(0).strip());}
    if(name.length()>100||slug.length()>100||sns.length()>500)throw bad("公開名・識別子は100文字、SNS URLは500文字以下です。");
    if(!slug.isEmpty()&&!slug.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))throw bad("slugは半角英小文字・数字・ハイフンです。");
    if(!sns.isEmpty()){URI u=URI.create(sns);if(!"https".equals(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null||sns.contains("\n")||sns.contains("\r"))throw bad("SNSはhttpsの公開プロフィールURLです。");sns=sns.replaceAll("/+$","");}
    space=switch(cell.get(6).strip()){case "","未確認"->null;case "大","大スペース","LARGE"->"LARGE";case "アトリ","アトリエ","アトリスペース","アトリエスペース","ATELIER"->"ATELIER";case "中","中スペース","MEDIUM"->"MEDIUM";case "小","小スペース","SMALL"->"SMALL";default->throw bad("スペースは大/アトリエ/中/小/未確認。番号だけの区分は使用できません。");};
    boolean explicitSlug=!slug.isEmpty();if(id==null&&slug.isEmpty())slug="import-"+eventId+"-"+hash.substring(0,20)+"-"+number;
    var matches=new HashSet<Long>();if(id!=null)matches.add(id);
    if(!slug.isEmpty())matches.addAll(db.queryForList("select creator_id from creators where slug=?",Long.class,slug));
    if(!sns.isEmpty())matches.addAll(db.queryForList("select distinct c.creator_id from creators c left join creator_links l on l.creator_id=c.creator_id where rtrim(c.website_url,'/')=? or rtrim(l.url,'/')=?",Long.class,sns,sns));
    if(matches.size()>1)throw bad("ID・slug・SNSが別の作家を指しています。");
    if(!matches.isEmpty()){
     id=matches.iterator().next();var existing=db.queryForList("select slug,name,status from creators where creator_id=?",id);if(existing.size()!=1)throw bad("作家IDが存在しません。");var e=existing.getFirst();
     if(!name.isEmpty()&&!name.equals(e.get("name")))throw bad("公開名と既存IDの名前が異なります。対象を確認してください。");
     if(explicitSlug&&!slug.equals(e.get("slug")))throw bad("slugと既存IDが一致しません。");
     slug=(String)e.get("slug");name=(String)e.get("name");status=(String)e.get("status");
     action=db.queryForObject("select count(*) from event_creators where event_id=? and creator_id=?",Integer.class,eventId,id)>0?"登録済み・変更なし":"既存作家を参加追加";
    }else{
     if(name.isEmpty())throw bad("新規作家には公開名が必要です。");
     if(db.queryForObject("select count(*) from creators where name=?",Integer.class,name)>0)throw bad("同名の既存作家があります。ID/SNSで確認してください（名前だけでは統合しません）。");
     if(slug.isEmpty())slug="import-"+eventId+"-"+hash.substring(0,20)+"-"+number;
     status="DRAFT";action="新規作家・下書き";
    }
    String identity=id!=null?"id:"+id:(!sns.isEmpty()?"sns:"+sns:"slug:"+slug);
    if(!seen.add(identity)||(!name.isEmpty()&&!seen.add("name:"+name)))throw bad("表の中に重複または同名疑いがあります。行を確認してください。");
   }catch(Exception e){error=e instanceof InvalidMessageException?e.getMessage():"値の形式を確認してください。";action="エラー";}
   rows.add(new Row(number,id,slug,name,sns,profile,concept,space,action,status,error));
  }return new Plan(eventId,title,hash,cells,List.copyOf(rows));
 }
 @Transactional public Result commit(Plan preview){
  db.queryForList("select pg_advisory_xact_lock(hashtext(?))",preview.eventId()+":"+preview.fingerprint());
  var done=db.queryForList("select created_count,linked_count,skipped_count from participant_import_batches where event_id=? and fingerprint=?",preview.eventId(),preview.fingerprint());
  if(!done.isEmpty()){var d=done.getFirst();return new Result((Integer)d.get("created_count"),(Integer)d.get("linked_count"),(Integer)d.get("skipped_count"));}
  // Lock catalog changes while rechecking; a concurrent administrator cannot create a duplicate mid-import.
  db.execute("LOCK TABLE creators,creator_links,event_creators IN SHARE ROW EXCLUSIVE MODE");
  var fresh=plan(preview.eventId(),preview.fingerprint(),preview.cells());
  if(!fresh.isValid()||!fresh.rows().equals(preview.rows()))throw bad("データが変更されたかエラーがあります。もう一度プレビューしてください。");
  int created=0,linked=0,skipped=0;
  for(var row:fresh.rows()){
   if(row.action().equals("登録済み・変更なし")){skipped++;continue;}
   Long id=row.artistId();if(id==null){id=db.queryForObject("insert into creators(slug,name,profile,concept,status) values(?,?,?,?,'DRAFT') returning creator_id",Long.class,row.slug(),row.name(),row.profile(),row.concept());created++;
    if(!row.sns().isEmpty())db.update("insert into creator_links(creator_id,label,url,display_order) values(?,'SNS',?,0)",id,row.sns());
   }
   db.update("insert into event_creators(event_id,creator_id,display_order,space_type) values(?,?,0,?)",fresh.eventId(),id,row.space());linked++;
  }
  db.update("insert into participant_import_batches(event_id,fingerprint,created_count,linked_count,skipped_count) values(?,?,?,?,?)",fresh.eventId(),fresh.fingerprint(),created,linked,skipped);
  return new Result(created,linked,skipped);
 }
 private static InvalidMessageException bad(String message){return new InvalidMessageException(message);}
}
