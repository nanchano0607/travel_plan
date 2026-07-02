package com.min.edu.plan.place;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private static final int SEARCH_PAGE_SIZE = 5;
    private static final double SEARCH_RADIUS_METERS = 50000.0;

    private final RestClient restClient;
    private final String apiKey;

    public GooglePlacesClient(@Value("${google.places.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://places.googleapis.com/v1")
                .build();
    }

    public List<GooglePlaceSearchResult> searchPlaces(
            String regionName,
            String placeName,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        Map<String, Object> requestBody = Map.of(
                "textQuery", regionName + " " + placeName,
                "languageCode", "ko",
                "pageSize", SEARCH_PAGE_SIZE,
                "locationBias", Map.of(
                        "circle", Map.of(
                                "center", Map.of(
                                        "latitude", latitude,
                                        "longitude", longitude
                                ),
                                "radius", SEARCH_RADIUS_METERS
                        )
                )
        );

        log.info("Google Places text search request. textQuery={}, latitude={}, longitude={}, radiusMeters={}, pageSize={}",
                regionName + " " + placeName, latitude, longitude, Math.round(SEARCH_RADIUS_METERS), SEARCH_PAGE_SIZE);

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
            return List.of();
        }

        List<GooglePlaceSearchResult> candidates = response.getPlaces().stream()
                .filter(place -> hasRequiredFields(place))
                .map(place -> new GooglePlaceSearchResult(
                        place.getId(),
                        place.getDisplayName().getText(),
                        place.getFormattedAddress(),
                        place.getLocation().getLatitude(),
                        place.getLocation().getLongitude(),
                        place.getTypes() == null ? List.of() : place.getTypes()
                ))
                .toList();

        log.info("Google Places text search returned candidates. requestedPlaceName={}, candidateCount={}",
                placeName, candidates.size());
        candidates.forEach(candidate -> log.info(
                "Google Places text search candidate. requestedPlaceName={}, googlePlaceName={}, placeId={}, latitude={}, longitude={}, types={}",
                placeName,
                candidate.getDisplayName(),
                candidate.getPlaceId(),
                candidate.getLatitude(),
                candidate.getLongitude(),
                candidate.getTypes()));

        return candidates;
    }

    private boolean hasRequiredFields(GooglePlacesTextSearchResponse.GooglePlace place) {
        boolean hasRequiredFields = place.getId() != null
                && place.getDisplayName() != null
                && place.getDisplayName().getText() != null
                && place.getLocation() != null
                && place.getLocation().getLatitude() != null
                && place.getLocation().getLongitude() != null;

        if (!hasRequiredFields) {
            log.warn("Google Places result is missing required fields. id={}, displayNameExists={}, locationExists={}",
                    place.getId(), place.getDisplayName() != null, place.getLocation() != null);
        }

        return hasRequiredFields;
    }
}
