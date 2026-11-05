package world.creve.playpit.controller;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import world.creve.playpit.service.LocalImageStorage;
import world.creve.platform.repository.*;
@RestController public class UploadedImageController {
 private final LocalImageStorage images;private final CreatorRepository creators;private final ArtworkRepository artworks;private final EventRepository events;
 public UploadedImageController(LocalImageStorage images,CreatorRepository creators,ArtworkRepository artworks,EventRepository events){this.images=images;this.creators=creators;this.artworks=artworks;this.events=events;}
 @GetMapping("/playpit/uploads/{name}") public ResponseEntity<Resource> image(@PathVariable String name,Authentication auth){
  var path=images.resolve(name);if(path==null)return ResponseEntity.notFound().build();
  String url="/playpit/uploads/"+name;
  boolean admin=auth!=null&&auth.isAuthenticated()&&auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
  if(!admin&&!creators.existsByProfileImageUrlAndStatus(url,"PUBLISHED")&&!artworks.isPublishedImage(url)&&!events.existsByMainImageUrlAndStatus(url,"PUBLISHED"))return ResponseEntity.notFound().build();
  return ResponseEntity.ok().contentType(name.endsWith(".png")?MediaType.IMAGE_PNG:MediaType.IMAGE_JPEG).header("X-Content-Type-Options","nosniff").header("Content-Disposition","inline").cacheControl(CacheControl.noStore()).body(new FileSystemResource(path));
 }
}
