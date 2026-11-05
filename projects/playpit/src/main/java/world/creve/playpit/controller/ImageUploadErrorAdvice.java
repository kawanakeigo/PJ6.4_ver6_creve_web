package world.creve.playpit.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
@ControllerAdvice @Order(-10) public class ImageUploadErrorAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class) @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public String tooLarge(){return "playpit/admin/upload-error";}
}
