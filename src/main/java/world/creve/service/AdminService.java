package world.creve.service;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.entity.Admin;
import world.creve.entity.Artwork;
import world.creve.entity.Creator;
import world.creve.entity.Event;
import world.creve.entity.Message;
import world.creve.entity.MessageStatus;
import world.creve.exception.NotFoundException;
import world.creve.repository.AdminRepository;
import world.creve.repository.ArtworkRepository;
import world.creve.repository.CreatorRepository;
import world.creve.repository.EventRepository;
import world.creve.repository.MessageRepository;

@Service
public class AdminService {
  private final AdminRepository adminRepository;
  private final MessageRepository messageRepository;
  private final EventRepository eventRepository;
  private final CreatorRepository creatorRepository;
  private final ArtworkRepository artworkRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminService(
      AdminRepository adminRepository,
      MessageRepository messageRepository,
      EventRepository eventRepository,
      CreatorRepository creatorRepository,
      ArtworkRepository artworkRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.adminRepository = adminRepository;
    this.messageRepository = messageRepository;
    this.eventRepository = eventRepository;
    this.creatorRepository = creatorRepository;
    this.artworkRepository = artworkRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public void ensureDefaultAdmin(String email, String rawPassword) {
    String normalizedEmail = normalize(email);
    if (normalizedEmail == null || normalize(rawPassword) == null) {
      return;
    }
    adminRepository.findByEmail(normalizedEmail)
        .orElseGet(() -> adminRepository.save(
            new Admin(normalizedEmail, passwordEncoder.encode(rawPassword))
        ));
  }

  public List<Message> searchMessages(
      String eventId,
      String creatorId,
      String artworkId,
      MessageStatus status,
      String keyword
  ) {
    return messageRepository.searchMessages(
        normalize(eventId),
        normalize(creatorId),
        normalize(artworkId),
        status,
        normalize(keyword)
    );
  }

  public Message findMessage(Long messageId) {
    return messageRepository.findById(messageId)
        .orElseThrow(() -> new NotFoundException("投稿が見つかりません。"));
  }

  @Transactional
  public void changeStatus(Long messageId, MessageStatus status) {
    Message message = findMessage(messageId);
    if (status == MessageStatus.PUBLISHED) {
      message.publish();
    } else if (status == MessageStatus.HIDDEN) {
      message.hide();
    } else if (status == MessageStatus.DELETED) {
      message.markDeleted();
    }
  }

  @Transactional
  public void deleteMessage(Long messageId) {
    changeStatus(messageId, MessageStatus.DELETED);
  }

  public List<Event> findEvents() {
    return eventRepository.findAll();
  }

  public List<Creator> findCreators() {
    return creatorRepository.findAll();
  }

  public List<Artwork> findArtworks() {
    return artworkRepository.findAll();
  }

  private String normalize(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return value.trim();
  }
}
