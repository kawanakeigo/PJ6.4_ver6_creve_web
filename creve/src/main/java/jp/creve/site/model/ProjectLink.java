package jp.creve.site.model;

import java.util.UUID;

public record ProjectLink(UUID id, String key, String label, String url, int position) {
}
