package world.creve.playpit;
import java.nio.file.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.web.multipart.MultipartFile;
import world.creve.playpit.service.LocalImageStorage;
import world.creve.platform.exception.InvalidMessageException;
class LocalImageStorageTest {
 @TempDir Path directory;
 static byte[] png(int w,int h)throws Exception {var out=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB),"png",out);return out.toByteArray();}
 MultipartFile file(byte[] data)throws Exception {var file=mock(MultipartFile.class);when(file.getSize()).thenReturn((long)data.length);when(file.getBytes()).thenReturn(data);when(file.isEmpty()).thenReturn(data.length==0);when(file.getOriginalFilename()).thenReturn("../../unsafe.svg");return file;}
 @Test void validatesPixelsAndUsesSafeNameWithoutChangingBytes()throws Exception {var store=new LocalImageStorage(directory.toString());var bytes=png(4,6);String url=store.store(file(bytes));assertTrue(url.matches("/playpit/uploads/[a-f0-9]{32}\\.png"));var path=store.resolve(url.substring(url.lastIndexOf('/')+1));assertArrayEquals(bytes,Files.readAllBytes(path));assertEquals(path,new LocalImageStorage(directory.toString()).resolve(path.getFileName().toString()));}
 @Test void acceptsRgbJpegAndPreservesOriginalBytes()throws Exception {var output=new ByteArrayOutputStream();assertTrue(ImageIO.write(new BufferedImage(32,24,BufferedImage.TYPE_INT_RGB),"jpeg",output));var bytes=output.toByteArray();var store=new LocalImageStorage(directory.toString());String url=store.store(file(bytes));assertTrue(url.endsWith(".jpg"));assertArrayEquals(bytes,Files.readAllBytes(store.resolve(url.substring(url.lastIndexOf('/')+1))));}
 @Test void rejectsSvgAndTruncatedImages()throws Exception {var store=new LocalImageStorage(directory.toString());assertThrows(InvalidMessageException.class,()->store.store(file("<svg onload='alert(1)'/>".getBytes())));assertThrows(InvalidMessageException.class,()->store.store(file(new byte[]{(byte)137,80,78,71})));}
 @Test void limitsDimensionsAndSizeBeforeReadingBytes()throws Exception {var store=new LocalImageStorage(directory.toString());assertThrows(InvalidMessageException.class,()->store.store(file(png(4097,1))));var large=mock(MultipartFile.class);when(large.getSize()).thenReturn(LocalImageStorage.MAX_BYTES+1);assertThrows(InvalidMessageException.class,()->store.store(large));verify(large,never()).getBytes();}
 @Test void rejectsTraversalAndSymlinks()throws Exception {var store=new LocalImageStorage(directory.toString());assertNull(store.resolve("../secret"));assertNull(store.resolve("/etc/passwd"));Path outside=Files.createTempFile("upload-outside-",".png");try{String name="a".repeat(32)+".png";Files.createSymbolicLink(directory.resolve(name),outside);assertNull(store.resolve(name));}finally{Files.deleteIfExists(outside);}}
}
