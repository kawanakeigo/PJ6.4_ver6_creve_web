package com.newlegacyjam.platform.api;

import com.newlegacyjam.platform.api.NljModels.EventDetail;
import com.newlegacyjam.platform.api.NljModels.PageResult;
import com.newlegacyjam.platform.api.NljModels.PublicEventCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/events")
public class EventQueryController {
    private final DemoCatalogService catalog;

    public EventQueryController(DemoCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public PageResult<PublicEventCard> events(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        if (page != 0 || size < 1 || size > 100) throw new NljException.InvalidInput("INVALID_PAGE");
        var events = catalog.events();
        return new PageResult<>(events, 0, size, events.size(), false);
    }

    @GetMapping("/{id}")
    public EventDetail event(@PathVariable long id) {
        return catalog.event(id);
    }
}
