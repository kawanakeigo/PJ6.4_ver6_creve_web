package world.creve.playpit.service;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.springframework.stereotype.Component;
import world.creve.platform.exception.InvalidMessageException;
@Component public class ParticipantTableParser {
 public static final List<String> HEADERS=List.of("artist_id","slug","name","sns_url","profile","concept","space");
 public static final int MAX_BYTES=2*1024*1024,MAX_ROWS=200;
 public List<List<String>> parse(byte[] bytes,String filename){
  if(bytes.length==0||bytes.length>MAX_BYTES)throw bad("表は2MB以下にしてください。");
  List<List<String>> rows;
  try{String name=filename==null?"":filename.toLowerCase(Locale.ROOT);
   if(name.endsWith(".csv")){String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();rows=csv(text.startsWith("\ufeff")?text.substring(1):text);}
   else if(name.endsWith(".xlsx"))rows=xlsx(bytes);else throw bad("UTF-8 CSVまたは.xlsxを選択してください。");
  }catch(InvalidMessageException e){throw e;}catch(Exception e){throw bad("表を読み取れません。UTF-8 CSVまたは単一シートの.xlsxを確認してください。");}
  if(rows.isEmpty()||!rows.getFirst().equals(HEADERS))throw bad("見出しをテンプレートの7列と同じ順序にしてください。私的な回答列は取り込みません。");
  var result=new ArrayList<List<String>>();for(var row:rows.subList(1,rows.size())){
   if(row.stream().allMatch(String::isBlank))continue;
   if(row.size()!=7)throw bad("各行はテンプレートと同じ7列にしてください。");
   for(String cell:row)if(cell.length()>20000||cell.indexOf('\0')>=0)throw bad("セルは20000文字以下にしてください。");
   result.add(List.copyOf(row));
  }
  if(result.isEmpty()||result.size()>MAX_ROWS)throw bad("データは1〜200行にしてください。");return List.copyOf(result);
 }
 private List<List<String>> csv(String text){
  var rows=new ArrayList<List<String>>();var row=new ArrayList<String>();var cell=new StringBuilder();boolean quoted=false,closed=false;
  for(int i=0;i<text.length();i++){char c=text.charAt(i);
   if(quoted){if(c=='"'){if(i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else{quoted=false;closed=true;}}else cell.append(c);}
   else if(c=='"'){if(cell.length()!=0||closed)throw bad("CSVの引用符を確認してください。");quoted=true;}
   else if(c==','||c=='\r'||c=='\n'){row.add(cell.toString());cell.setLength(0);closed=false;if(row.size()>7)throw bad("列数は7列です。");if(c!=','){rows.add(row);row=new ArrayList<>();if(c=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;}}
   else {if(closed)throw bad("CSVの引用符の後を確認してください。");cell.append(c);}
   if(cell.length()>20000||rows.size()>MAX_ROWS+1)throw bad("行数またはセル長が上限を超えています。");
  }
  if(quoted)throw bad("CSVの引用符が閉じていません。");if(!row.isEmpty()||cell.length()>0||closed){row.add(cell.toString());rows.add(row);}return rows;
 }
 private Document xml(byte[] bytes)throws Exception{
  var f=DocumentBuilderFactory.newInstance();f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);f.setFeature("http://xml.org/sax/features/external-general-entities",false);f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);f.setXIncludeAware(false);f.setExpandEntityReferences(false);f.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD","");f.setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema","");
  var b=f.newDocumentBuilder();b.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());return b.parse(new ByteArrayInputStream(bytes));
 }
 private List<List<String>> xlsx(byte[] bytes)throws Exception{
  Map<String,byte[]> files=new HashMap<>();int total=0,entries=0;
  try(var zip=new ZipInputStream(new ByteArrayInputStream(bytes))){ZipEntry e;while((e=zip.getNextEntry())!=null){
   if(++entries>64||e.getName().contains("..")||e.getName().startsWith("/")||e.getName().contains("\\"))throw bad("Excelの構造が不正です。");
   if(e.getName().contains("externalLinks")||e.getName().contains("vbaProject")||e.getName().contains("embeddings"))throw bad("外部リンクやマクロ・埋込オブジェクトは使用できません。");
   var out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
   while((n=zip.read(buf))!=-1){total+=n;if(total>10*1024*1024||out.size()+n>2*1024*1024)throw bad("Excelの展開サイズが上限を超えています。");out.write(buf,0,n);}
   if(e.getCompressedSize()>0&&out.size()>100000&&out.size()/e.getCompressedSize()>100)throw bad("圧縮率が高すぎるExcelは使用できません。");
   if(files.putIfAbsent(e.getName(),out.toByteArray())!=null)throw bad("Excel内に重複ファイルがあります。");
  }}
  if(!files.containsKey("xl/workbook.xml")||!files.containsKey("xl/worksheets/sheet1.xml"))throw bad("単一シートの.xlsxを使用してください。");
  for(var entry:files.entrySet())if(entry.getKey().endsWith(".rels")){var rel=xml(entry.getValue()).getElementsByTagName("Relationship");for(int i=0;i<rel.getLength();i++)if("External".equalsIgnoreCase(((Element)rel.item(i)).getAttribute("TargetMode")))throw bad("外部リンク付きExcelは使用できません。");}
  if(xml(files.get("xl/workbook.xml")).getElementsByTagName("sheet").getLength()!=1)throw bad("Excelは1シートだけにしてください。");
  var shared=new ArrayList<String>();if(files.containsKey("xl/sharedStrings.xml")){var nodes=xml(files.get("xl/sharedStrings.xml")).getElementsByTagName("si");if(nodes.getLength()>1407)throw bad("文字列の数が上限を超えています。");for(int i=0;i<nodes.getLength();i++){String v=nodes.item(i).getTextContent();if(v.length()>20000)throw bad("セルが長すぎます。");shared.add(v);}}
  var doc=xml(files.get("xl/worksheets/sheet1.xml"));if(doc.getElementsByTagName("f").getLength()>0)throw bad("数式は使用できません。値だけの表にしてください。");
  var nodes=doc.getElementsByTagName("row");if(nodes.getLength()>201)throw bad("データは200行以下にしてください。");var rows=new ArrayList<List<String>>();
  for(int i=0;i<nodes.getLength();i++){var row=(Element)nodes.item(i);if(!row.getAttribute("r").matches("[1-9][0-9]{0,2}")||Integer.parseInt(row.getAttribute("r"))>201)throw bad("行番号が範囲外です。");var cells=new ArrayList<>(Collections.nCopies(7,""));var seen=new HashSet<Integer>();var cs=row.getElementsByTagName("c");
   for(int j=0;j<cs.getLength();j++){var c=(Element)cs.item(j);String ref=c.getAttribute("r");if(!ref.matches("[A-G]"+row.getAttribute("r")))throw bad("セルはA〜G列を使用してください。");int col=ref.charAt(0)-'A';if(!seen.add(col))throw bad("重複セルがあります。");String type=c.getAttribute("t"),value="";
    var v=c.getElementsByTagName("v");if(v.getLength()>0)value=v.item(0).getTextContent();
    if(type.equals("s")){int index=Integer.parseInt(value);value=shared.get(index);}else if(type.equals("inlineStr")){var in=c.getElementsByTagName("is");value=in.getLength()>0?in.item(0).getTextContent():"";}else if(!type.isEmpty()&&!type.equals("n")&&!type.equals("str"))throw bad("セルは文字列または数値にしてください。");
    if(value.length()>20000)throw bad("セルが長すぎます。");cells.set(col,value);
   }rows.add(cells);
  }return rows;
 }
 private static InvalidMessageException bad(String message){return new InvalidMessageException(message);}
}
