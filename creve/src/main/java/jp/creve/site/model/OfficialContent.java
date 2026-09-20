package jp.creve.site.model;

import java.util.UUID;

public record OfficialContent(UUID id, String type, String slug, String title, String body) {
}
