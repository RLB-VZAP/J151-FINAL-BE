package com.vzap.trytons.service.catalog.feed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vzap.trytons.config.DotEnvConfig;
import com.vzap.trytons.dto.catalog.PlayerFeedDTO;
import com.vzap.trytons.exceptions.PlayerFeedException;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Server-side client for the external live-player feed.
 * <p>
 * The feed re-scrapes the whole source site on every request and takes roughly a
 * minute to respond, so this makes exactly one call per import - never in a loop.
 * The URL and timeout are configurable ({@code PLAYER_FEED_URL},
 * {@code PLAYER_FEED_TIMEOUT_SECONDS}) but default to the known endpoint so the
 * feature works with no extra environment setup.
 */
@ApplicationScoped
public class PlayerFeedClient {

    private static final Logger LOG = Logger.getLogger(PlayerFeedClient.class.getName());

    private static final String DEFAULT_URL =
            "https://ujxw0zb8qqn3sksfev56epq4.nexulartechnologies.co.za/players?refresh=true";
    private static final String DEFAULT_TIMEOUT_SECONDS = "150";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Fetches the full player list from the feed.
     *
     * @throws PlayerFeedException if the feed is unreachable, times out, returns a
     *                             non-200 status, or returns a body we cannot parse.
     */
    public List<PlayerFeedDTO> fetchPlayers() {
        String url = DotEnvConfig.get("PLAYER_FEED_URL", DEFAULT_URL);
        long timeoutSeconds = parseTimeoutSeconds();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "application/json")
                .GET()
                .build();

        LOG.log(Level.INFO, "Fetching player feed from {0}", url);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new PlayerFeedException(
                        "Player feed returned HTTP " + response.statusCode() + ".");
            }

            List<PlayerFeedDTO> players =
                    objectMapper.readValue(response.body(), new TypeReference<List<PlayerFeedDTO>>() {
                    });

            if (players == null || players.isEmpty()) {
                throw new PlayerFeedException("Player feed returned no players.");
            }

            LOG.log(Level.INFO, "Player feed returned {0} players.", players.size());
            return players;

        } catch (IOException e) {
            throw new PlayerFeedException("Unable to read the player feed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PlayerFeedException("Player feed request was interrupted.", e);
        }
    }

    private long parseTimeoutSeconds() {
        String raw = DotEnvConfig.get("PLAYER_FEED_TIMEOUT_SECONDS", DEFAULT_TIMEOUT_SECONDS);
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            LOG.log(Level.WARNING,
                    "Invalid PLAYER_FEED_TIMEOUT_SECONDS ''{0}'', falling back to {1}s.",
                    new Object[]{raw, DEFAULT_TIMEOUT_SECONDS});
            return Long.parseLong(DEFAULT_TIMEOUT_SECONDS);
        }
    }
}
