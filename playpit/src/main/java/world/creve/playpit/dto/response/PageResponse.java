package world.creve.playpit.dto.response;

import java.util.List;

public record PageResponse(
        List<MessageRow> items,
        int page,
        int size,
        long total,
        boolean hasNext) {
}
