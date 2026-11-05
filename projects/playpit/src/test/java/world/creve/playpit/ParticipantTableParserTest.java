package world.creve.playpit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import world.creve.playpit.service.ParticipantTableParser;
import world.creve.platform.exception.InvalidMessageException;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
class ParticipantTableParserTest {
 ParticipantTableParser p=new ParticipantTableParser();String header=String.join(",",ParticipantTableParser.HEADERS)+"\n";
 @Test void csvBomQuotesJapaneseAndPrivateHeaders(){
  var rows=p.parse(("\ufeff"+header+",,作家,https://example.com/a,\"紹介,と改行\n次の行\",テーマ,中\n").getBytes(StandardCharsets.UTF_8),"table.csv");assertEquals(1,rows.size());assertEquals("紹介,と改行\n次の行",rows.getFirst().get(4));
  assertThrows(InvalidMessageException.class,()->p.parse("name,email,phone\n作者,x,y".getBytes(),"a.csv"));
  assertThrows(InvalidMessageException.class,()->p.parse(new byte[]{(byte)0xff},"a.csv"));
  assertThrows(InvalidMessageException.class,()->p.parse((header+",,\"未完").getBytes(),"a.csv"));
 }
 @Test void limitsAndInvalidType(){
  assertThrows(InvalidMessageException.class,()->p.parse(new byte[2*1024*1024+1],"a.csv"));
  assertThrows(InvalidMessageException.class,()->p.parse((header+",,作者,,,,\n".repeat(201)).getBytes(),"a.csv"));
  assertThrows(InvalidMessageException.class,()->p.parse((header+",,作者,,"+"a".repeat(20001)+",,\n").getBytes(),"a.csv"));
  assertThrows(InvalidMessageException.class,()->p.parse(header.getBytes(),"a.xls"));
 }
 byte[] xlsx(String extra,String rel)throws Exception{
  var out=new ByteArrayOutputStream();try(var z=new ZipOutputStream(out)){
   var cells=new StringBuilder();int i=0;for(String h:ParticipantTableParser.HEADERS)cells.append("<c r=\"").append((char)('A'+i++)).append("1\" t=\"inlineStr\"><is><t>").append(h).append("</t></is></c>");
   Map<String,String> files=new HashMap<>();files.put("xl/workbook.xml","<workbook><sheets><sheet name=\"参加者\"/></sheets></workbook>");files.put("xl/worksheets/sheet1.xml","<worksheet><sheetData><row r=\"1\">"+cells+"</row><row r=\"2\"><c r=\"C2\" t=\"inlineStr\"><is><t>作者</t></is>"+extra+"</c></row></sheetData></worksheet>");if(rel!=null)files.put("xl/_rels/workbook.xml.rels",rel);
   for(var e:files.entrySet()){z.putNextEntry(new ZipEntry(e.getKey()));z.write(e.getValue().getBytes(StandardCharsets.UTF_8));z.closeEntry();}
  }return out.toByteArray();
 }
 @Test void xlsxValuesOnlyAndNoFormulasExternalEntities()throws Exception{
  assertEquals("作者",p.parse(xlsx("",null),"table.xlsx").getFirst().get(2));
  assertThrows(InvalidMessageException.class,()->p.parse(xlsx("<f>WEBSERVICE(1)</f>",null),"table.xlsx"));
  assertThrows(InvalidMessageException.class,()->p.parse(xlsx("","<Relationships><Relationship TargetMode=\"External\" Target=\"https://example.com\"/></Relationships>"),"table.xlsx"));
  assertThrows(InvalidMessageException.class,()->p.parse(xlsx("","<!DOCTYPE a [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><Relationships>&x;</Relationships>"),"table.xlsx"));
 }
}
