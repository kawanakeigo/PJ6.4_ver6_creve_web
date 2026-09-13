package world.creve.playpit.controller;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.*;
import world.creve.platform.util.SafeUrls;
@RestController public class QrCodeController {
    private final EventService events;
    private final ArtworkService artworks;
    private final CreatorService creators;
    private final String base;
    public QrCodeController(EventService events,ArtworkService artworks,CreatorService creators,@Value("${creve.base-url}")String base) {
        this.events=events;
        this.artworks=artworks;
        this.creators=creators;
        this.base=SafeUrls.optional(base).replaceAll("/$","");
    }
    @GetMapping(value="/playpit/{eventSlug}/artworks/{artworkSlug}/qr.png",produces=MediaType.IMAGE_PNG_VALUE) public ResponseEntity<byte[]> artworkQr(@PathVariable String eventSlug,@PathVariable String artworkSlug)throws Exception {
        var e=events.getPublishedPlaypitEventBySlug(eventSlug);
        var a=artworks.getArtworkDetailBySlug(artworkSlug);
        artworks.validateExhibition(e.eventId(),a.artworkId());
        creators.validateCreatorParticipation(e.eventId(),a.creatorId());
        return png(base+"/playpit/"+e.slug()+"/artworks/"+a.slug()+"?source=qr");
    }
    @GetMapping(value="/playpit/{eventSlug}/creators/{creatorSlug}/qr.png",produces=MediaType.IMAGE_PNG_VALUE) public ResponseEntity<byte[]> creatorQr(@PathVariable String eventSlug,@PathVariable String creatorSlug)throws Exception {
        var e=events.getPublishedPlaypitEventBySlug(eventSlug);
        var c=creators.getCreatorDetailBySlug(creatorSlug);
        creators.validateCreatorParticipation(e.eventId(),c.creatorId());
        return png(base+"/playpit/"+e.slug()+"/creators/"+c.slug()+"?source=qr");
    }
    private ResponseEntity<byte[]> png(String url)throws Exception {
        var matrix=new QRCodeWriter().encode(url,BarcodeFormat.QR_CODE,512,512,Map.of(EncodeHintType.MARGIN,4));
        var out=new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix,"PNG",out);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(out.toByteArray());
    }
}
