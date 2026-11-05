package world.creve.playpit.service;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.*;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.*;
import world.creve.platform.exception.InvalidMessageException;
@Service public class LocalImageStorage {
 public static final long MAX_BYTES=10L*1024*1024;
 private final Path directory;
 public LocalImageStorage(@Value("${creve.upload-dir:${user.home}/Library/Application Support/CreVe/uploads}")String directory){this.directory=Path.of(directory).toAbsolutePath().normalize();}
 public String store(MultipartFile file){
  if(file==null||file.isEmpty())throw new InvalidMessageException("画像を選択してください。");
  if(file.getSize()>MAX_BYTES)throw new InvalidMessageException("画像は1枚10MB以下にしてください。");
  Path target=null;
  try {
   byte[] bytes=file.getBytes();if(bytes.length>MAX_BYTES)throw new InvalidMessageException("画像は1枚10MB以下にしてください。");
   String format;
   try(var input=new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))){
    var readers=ImageIO.getImageReaders(input);
    if(!readers.hasNext())throw new InvalidMessageException("JPEGまたはPNGの画像ファイルを選択してください。");
    var reader=readers.next();
    try {
     format=reader.getFormatName().toLowerCase(Locale.ROOT);
     if(!Set.of("png","jpeg","jpg").contains(format))throw new InvalidMessageException("対応形式はJPEG・PNGです。SVG・GIF・WebP・HEICには対応していません。");
     reader.setInput(input,true,true);int w=reader.getWidth(0),h=reader.getHeight(0);
     if(w<1||h<1||w>4096||h>4096)throw new InvalidMessageException("画像の幅・高さはそれぞれ4096px以下にしてください。");
     if(reader.read(0)==null)throw new IOException();
    }finally{reader.dispose();}
   }
   Files.createDirectories(directory);
   String name=UUID.randomUUID().toString().replace("-","")+(format.equals("png")?".png":".jpg");
   target=directory.resolve(name);Files.write(target,bytes,StandardOpenOption.CREATE_NEW);
   Path stored=target;
   if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
    @Override public void afterCompletion(int status){if(status!=STATUS_COMMITTED)discard(stored);}
   });
   return "/playpit/uploads/"+name;
  }catch(IOException e){if(target!=null)discard(target);throw new InvalidMessageException("画像を保存できませんでした。画像形式と保存先の空き容量・権限を確認してください。");}
 }
 private static void discard(Path p){try{Files.deleteIfExists(p);}catch(IOException ignored){}}
 public Path resolve(String name){
  if(name==null||!name.matches("[a-f0-9]{32}\\.(png|jpg)"))return null;
  Path p=directory.resolve(name).normalize();return p.getParent().equals(directory)&&Files.isRegularFile(p,LinkOption.NOFOLLOW_LINKS)?p:null;
 }
}
