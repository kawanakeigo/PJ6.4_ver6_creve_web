package world.creve.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import world.creve.entity.Artwork;
import world.creve.entity.Creator;
import world.creve.entity.Event;
import world.creve.entity.EventArtwork;
import world.creve.entity.EventCreator;
import world.creve.entity.EventType;
import world.creve.entity.LandingPage;
import world.creve.entity.News;
import world.creve.entity.PublishStatus;
import world.creve.repository.ArtworkRepository;
import world.creve.repository.CreatorRepository;
import world.creve.repository.EventArtworkRepository;
import world.creve.repository.EventCreatorRepository;
import world.creve.repository.EventRepository;
import world.creve.repository.LandingPageRepository;
import world.creve.repository.NewsRepository;

@Component
public class DataSeeder implements CommandLineRunner {
  public static final String UKA_EVENT_ID = "202606-uka";
  public static final String LIVE_EVENT_ID = "202606-opening-live";

  private final EventRepository eventRepository;
  private final CreatorRepository creatorRepository;
  private final ArtworkRepository artworkRepository;
  private final EventCreatorRepository eventCreatorRepository;
  private final EventArtworkRepository eventArtworkRepository;
  private final NewsRepository newsRepository;
  private final LandingPageRepository landingPageRepository;

  public DataSeeder(
      EventRepository eventRepository,
      CreatorRepository creatorRepository,
      ArtworkRepository artworkRepository,
      EventCreatorRepository eventCreatorRepository,
      EventArtworkRepository eventArtworkRepository,
      NewsRepository newsRepository,
      LandingPageRepository landingPageRepository
  ) {
    this.eventRepository = eventRepository;
    this.creatorRepository = creatorRepository;
    this.artworkRepository = artworkRepository;
    this.eventCreatorRepository = eventCreatorRepository;
    this.eventArtworkRepository = eventArtworkRepository;
    this.newsRepository = newsRepository;
    this.landingPageRepository = landingPageRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    seedEvents();
    seedNewsAndLandingPage();
    seedCreatorsAndArtworks();
  }

  private void seedEvents() {
    eventRepository.findById(UKA_EVENT_ID).orElseGet(() -> eventRepository.save(
        new Event(
            UKA_EVENT_ID,
            UKA_EVENT_ID,
            EventType.PLAYPIT,
            PublishStatus.PUBLISHED,
            "羽花展",
            "羽のように軽く、花のように咲く。まだ名づけられない表現たちへ。",
            "30名のクリエイターと来場者の言葉が、羽花として会場に咲いていくPLAYPIT企画。",
            "羽花展は、軽さ、揺らぎ、咲く前の沈黙をキーワードに、若いクリエイターたちの多様な表現を一つの空間に集める展示企画です。",
            OffsetDateTime.parse("2026-06-12T11:00:00+09:00"),
            OffsetDateTime.parse("2026-06-14T20:00:00+09:00"),
            "2026年6月12日(金)-14日(日) 11:00-20:00",
            "CreVe Studio",
            "東京都内予定 / 詳細は後日公開",
            "/images/playpit/uka-main.svg",
            true,
            1
        )
    ));
    eventRepository.findById(LIVE_EVENT_ID).orElseGet(() -> eventRepository.save(
        new Event(
            LIVE_EVENT_ID,
            LIVE_EVENT_ID,
            EventType.LIVE,
            PublishStatus.PUBLISHED,
            "PLAYPIT Opening Live",
            "展示の始まりを音と言葉で立ち上げるオープニングライブ。",
            "羽花展の初日に、参加クリエイターのトークとライブパフォーマンスを行います。",
            "PLAYPITのコンセプトを来場者と共有し、展示体験への入口をつくるライブイベントです。",
            OffsetDateTime.parse("2026-06-12T18:30:00+09:00"),
            OffsetDateTime.parse("2026-06-12T20:00:00+09:00"),
            "2026年6月12日(金) 18:30-20:00",
            "CreVe Studio",
            "東京都内予定 / 羽花展会場内",
            "/images/live/opening-live.svg",
            false,
            1
        )
    ));
  }

  private void seedNewsAndLandingPage() {
    if (newsRepository.count() == 0) {
      newsRepository.save(new News(
          "playpit-uka-announcement",
          PublishStatus.PUBLISHED,
          "PLAYPIT 羽花展のティザーサイトを公開しました",
          "クリエイターと来場者の言葉が羽花として育つ展示体験に向けて、PLAYPITページを公開しました。",
          OffsetDateTime.parse("2026-03-01T10:00:00+09:00")
      ));
      newsRepository.save(new News(
          "creator-lineup-first",
          PublishStatus.PUBLISHED,
          "参加クリエイター30名を順次紹介します",
          "Illustration、Graphic Design、Photographyなど、多様なジャンルのクリエイターが参加予定です。",
          OffsetDateTime.parse("2026-03-15T10:00:00+09:00")
      ));
    }
    landingPageRepository.findById("playpit-uka").orElseGet(() -> landingPageRepository.save(
        new LandingPage(
            "playpit-uka",
            "playpit-uka",
            PublishStatus.PUBLISHED,
            "PLAYPIT 羽花展 LP",
            "羽花展の告知・申込・来場導線をまとめるランディングページです。"
        )
    ));
  }

  private void seedCreatorsAndArtworks() throws IOException {
    ClassPathResource resource = new ClassPathResource("data/creators.csv");
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      String line = reader.readLine();
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        CreatorRow row = CreatorRow.fromCsv(line.split(",", -1));
        seedCreator(row);
        seedArtwork(row, 1, row.work1Title(), row.work1Description(), row.work1ImageUrl());
        seedArtwork(row, 2, row.work2Title(), row.work2Description(), row.work2ImageUrl());
      }
    }
  }

  private void seedCreator(CreatorRow row) {
    int order = displayOrder(row.id());
    if (!creatorRepository.existsById(row.id())) {
      creatorRepository.save(new Creator(
          row.id(),
          row.id(),
          PublishStatus.PUBLISHED,
          row.name(),
          row.artistName(),
          row.category(),
          row.profile(),
          row.concept(),
          row.iconImageUrl(),
          row.imageUrl(),
          row.instagram(),
          row.x(),
          row.website(),
          order
      ));
    }
    if (!eventCreatorRepository.existsByEventIdAndCreatorId(UKA_EVENT_ID, row.id())) {
      eventCreatorRepository.save(new EventCreator(UKA_EVENT_ID, row.id(), order));
    }
  }

  private void seedArtwork(
      CreatorRow row,
      int number,
      String title,
      String description,
      String imageUrl
  ) {
    String artworkId = row.id().replace("creator", "artwork") + "-" + number;
    int order = displayOrder(row.id()) * 10 + number;
    if (!artworkRepository.existsById(artworkId)) {
      artworkRepository.save(new Artwork(
          artworkId,
          artworkId,
          UKA_EVENT_ID,
          row.id(),
          PublishStatus.PUBLISHED,
          title,
          description,
          imageUrl,
          imageUrl,
          "会場での距離、光、鑑賞者の視線によって印象が変わるよう、羽花展に合わせて再構成した作品です。",
          "羽花展のために、来場者が言葉を残したくなる余白と手触りを作品内に残しています。",
          row.category(),
          "2026",
          order
      ));
    }
    if (!eventArtworkRepository.existsByEventIdAndArtworkId(UKA_EVENT_ID, artworkId)) {
      eventArtworkRepository.save(new EventArtwork(UKA_EVENT_ID, artworkId, order));
    }
  }

  private int displayOrder(String creatorId) {
    return Integer.parseInt(creatorId.replace("creator-", ""));
  }

  private record CreatorRow(
      String id,
      String name,
      String artistName,
      String category,
      String profile,
      String concept,
      String iconImageUrl,
      String imageUrl,
      String instagram,
      String x,
      String website,
      String work1Title,
      String work1Description,
      String work1ImageUrl,
      String work2Title,
      String work2Description,
      String work2ImageUrl
  ) {
    static CreatorRow fromCsv(String[] cells) {
      if (cells.length != 17) {
        throw new IllegalArgumentException("Expected 17 columns, got " + cells.length);
      }
      return new CreatorRow(
          cells[0], cells[1], cells[2], cells[3], cells[4], cells[5], cells[6], cells[7],
          cells[8], cells[9], cells[10], cells[11], cells[12], cells[13], cells[14],
          cells[15], cells[16]
      );
    }
  }
}
