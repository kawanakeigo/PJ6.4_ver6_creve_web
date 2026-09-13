package world.creve.playpit.dto.response;
import java.time.*;
import java.util.*;
public record PageResponse(List<PetalResponse> items, int page, int size, long totalElements, int totalPages) {
    public List<PetalResponse> getItems() {
        return items;
    }
    public int getPage() {
        return page;
    }
    public int getSize() {
        return size;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public int getTotalPages() {
        return totalPages;
    }
}
