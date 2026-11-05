package world.creve.playpit.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import world.creve.playpit.dto.request.*;
@Service public class UploadedContentService {
 private final ContentAdminService content;private final LocalImageStorage images;
 public UploadedContentService(ContentAdminService content,LocalImageStorage images){this.content=content;this.images=images;}
 @Transactional public Long saveEvent(Long id,EventForm form,MultipartFile image){
  Long saved=content.saveEvent(id,form);
  if(image!=null&&!image.isEmpty())content.event(saved).setMainImageUrl(images.store(image));return saved;
 }
 @Transactional public Long saveCreator(Long id,CreatorForm form,MultipartFile image){
  Long saved=content.saveCreator(id,form);
  if(image!=null&&!image.isEmpty())content.creator(saved).setProfileImageUrl(images.store(image));return saved;
 }
 @Transactional public Long saveArtwork(Long id,ArtworkForm form,MultipartFile image){
  Long saved=content.saveArtwork(id,form);
  if(image!=null&&!image.isEmpty())content.artwork(saved).setMainImageUrl(images.store(image));return saved;
 }
}
