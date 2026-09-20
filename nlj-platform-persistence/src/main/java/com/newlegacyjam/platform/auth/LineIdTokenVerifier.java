package com.newlegacyjam.platform.auth;

import com.newlegacyjam.platform.api.NljException;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class LineIdTokenVerifier {
    private final String channelId;
    private final RestClient client;
    private final Clock clock;

    public LineIdTokenVerifier(
            @Value("${nlj.line-channel-id:}") String channelId,
            RestClient.Builder builder,
            Clock clock) {
        this.channelId = channelId;
        this.clock = clock;
        var httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.client = builder.requestFactory(requestFactory).baseUrl("https://api.line.me").build();
    }

    @SuppressWarnings("unchecked")
    public VerifiedLineIdentity verify(String token) {
        if (token == null || token.isBlank()) throw new NljException.Unauthorized("LINE_TOKEN_REQUIRED");
        if (channelId.isBlank()) throw new NljException.Unavailable("LINE_NOT_CONFIGURED", "LINE本人確認はまだ設定されていません。");
        var form = new LinkedMultiValueMap<String, String>();
        form.add("id_token", token);
        form.add("client_id", channelId);
        Map<String, Object> response;
        try {
            response = client.post().uri("/oauth2/v2.1/verify")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(Map.class);
        } catch (Exception exception) {
            throw new NljException.Unavailable("LINE_VERIFY_UNAVAILABLE", "LINE本人確認に接続できませんでした。");
        }
        if (response == null || !channelId.equals(String.valueOf(response.get("aud")))) {
            throw new NljException.Unauthorized("LINE_TOKEN_INVALID");
        }
        long expires = Long.parseLong(String.valueOf(response.get("exp")));
        if (expires <= clock.instant().getEpochSecond()) throw new NljException.Unauthorized("LINE_TOKEN_EXPIRED");
        String subject = String.valueOf(response.get("sub"));
        if (subject.isBlank()) throw new NljException.Unauthorized("LINE_TOKEN_INVALID");
        return new VerifiedLineIdentity(subject, String.valueOf(response.getOrDefault("name", "LINE user")));
    }
}
