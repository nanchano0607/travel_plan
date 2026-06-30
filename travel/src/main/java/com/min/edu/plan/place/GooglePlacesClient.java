package com.min.edu.plan.place;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GooglePlacesClient {

    private static final String FIELD_MASK = String.join(",",
            "places.id",
            "places.displayName",
            "places.formattedAddress",
            "places.location",
            "places.types"
    );

    private final RestClient restClient;
    private final String apiKey;

    public GooglePlacesClient(@Value("${google.places.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://places.googleapis.com/v1")
                .build();
    }

    public Optional<GooglePlaceSearchResult> searchPlace(
            String regionName,
            String placeName,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        Map<String, Object> requestBody = Map.of(
                "textQuery", regionName + " " + placeName,
                "languageCode", "ko",
                "pageSize", 1,
                "locationBias", Map.of(
                        "circle", Map.of(
                                "center", Map.of(
                                        "latitude", latitude,
                                        "longitude", longitude
                                ),
                                "radius", 50000.0
                        )
                )
        );

        log.info("Google Places text search request. textQuery={}, latitude={}, longitude={}, radiusMeters={}",
                regionName + " " + placeName, latitude, longitude, 50000);

        GooglePlacesTextSearchResponse response;
        try {
            response = restClient.post()
                    .uri("/places:searchText")
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", FIELD_MASK)
                    .body(requestBody)
                    .retrieve()
                    .body(GooglePlacesTextSearchResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("Google Places text search failed. statusCode={}, responseBody={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw e;
        } catch (RestClientException e) {
            log.warn("Google Places text search failed by client error.", e);
            throw e;
        }

        if (response == null || response.getPlaces() == null || response.getPlaces().isEmpty()) {
            log.warn("Google Places text search returned no places. regionName={}, placeName={}", regionName, placeName);
            return Optional.empty();
        }

        GooglePlacesTextSearchResponse.GooglePlace place = response.getPlaces().get(0);

        if (place.getId() == null || place.getDisplayName() == null || place.getLocation() == null) {
            log.warn("Google Places result is missing required fields. id={}, displayNameExists={}, locationExists={}",
                    place.getId(), place.getDisplayName() != null, place.getLocation() != null);
            return Optional.empty();
        }

        log.info("Google Places text search selected result. requestedPlaceName={}, googlePlaceName={}, placeId={}, latitude={}, longitude={}, types={}",
                placeName,
                place.getDisplayName().getText(),
                place.getId(),
                place.getLocation().getLatitude(),
                place.getLocation().getLongitude(),
                place.getTypes());

        return Optional.of(new GooglePlaceSearchResult(
                place.getId(),
                place.getDisplayName().getText(),
                place.getFormattedAddress(),
                place.getLocation().getLatitude(),
                place.getLocation().getLongitude(),
                place.getTypes() == null ? List.of() : place.getTypes()
        ));
    }
}
