package world.creve.playpit.dto.response;
public record WordResponse(Long messageId,String message,String displayName,String artworkTitle,String creatorName,String createdAt) {
 public static WordResponse from(MessageRow m){return new WordResponse(m.messageId(),m.message(),m.publicName(),m.artworkTitle(),m.creatorName(),m.createdAt().toString());}
}
