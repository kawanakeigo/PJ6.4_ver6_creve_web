package jp.creve.site.web;

import jp.creve.site.service.HomeQueryService;
import jp.creve.site.service.PublicRoomQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class SiteErrorHandler {
    @ExceptionHandler({PublicRoomQueryService.RoomNotFoundException.class, HomeQueryService.ContentNotFoundException.class})
    public ModelAndView notFound() {
        ModelAndView view = new ModelAndView("site/error");
        view.setStatus(HttpStatus.NOT_FOUND);
        view.addObject("status", 404);
        view.addObject("message", "お探しのページは見つかりませんでした。");
        return view;
    }
}
