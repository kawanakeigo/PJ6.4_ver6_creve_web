package com.newlegacyjam.platform.api;

import com.newlegacyjam.platform.api.NljModels.ArtistCard;
import com.newlegacyjam.platform.api.NljModels.EventDetail;
import com.newlegacyjam.platform.api.NljModels.PublicEventCard;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemoCatalogService {
    private final boolean enabled;

    public DemoCatalogService(@Value("${nlj.demo-catalog:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public List<PublicEventCard> events() {
        if (!enabled) return List.of();
        Instant start = Instant.now().plus(14, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        return List.of(new PublicEventCard(202610L, "NEW LEGACY JAM 2026", "次の時代をつくるライブと投票の実験。",
                start, start.plus(6, ChronoUnit.HOURS), 1L, start.plus(5, ChronoUnit.HOURS), true));
    }

    public EventDetail event(long id) {
        PublicEventCard event = events().stream().filter(item -> item.eventId() == id).findFirst()
                .orElseThrow(NljException.NotFound::new);
        return new EventDetail(event, List.of(
                new ArtistCard(101, "LUCENT", "声と電子音を重ねる東京発のデュオ。", "A", 1),
                new ArtistCard(102, "Still Harbor", "静かなビートと映像で空間を組み替える。", "B", 2),
                new ArtistCard(103, "MAYU", "言葉の輪郭をリズムに変えるソロアーティスト。", "A", 3)));
    }
}
