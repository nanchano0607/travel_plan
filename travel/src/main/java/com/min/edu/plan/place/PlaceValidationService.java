package com.min.edu.plan.place;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.min.edu.plan.ai.dto.AiPlanItemResponseDto;
import com.min.edu.plan.ai.dto.PlanRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceValidationService {

    private static final int GENERAL_MIN_SCORE = 20;
    private static final int STRICT_MIN_SCORE = 60;
    private static final double STRICT_PLACE_THRESHOLD_METERS = 1500.0;
    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("\\([^)]*\\)");
    private static final Pattern NON_NAME_CHARACTER_PATTERN = Pattern.compile("[\\s·・\\-_'\"()\\[\\],.]");
    private static final Pattern TOKEN_SPLIT_PATTERN = Pattern.compile("[\\s·・\\-_'\"()\\[\\],.]+");
    private static final Set<String> BROAD_PLACE_TYPES = Set.of(
            "locality",
            "administrative_area_level_1",
            "administrative_area_level_2",
            "administrative_area_level_3",
            "country",
            "political"
    );
    private static final Set<String> CAFE_TYPES = Set.of(
            "cafe",
            "coffee_shop",
            "bakery",
            "dessert_restaurant",
            "food",
            "point_of_interest",
            "establishment"
    );
    private static final Set<String> RESTAURANT_TYPES = Set.of(
            "restaurant",
            "food",
            "japanese_restaurant",
            "korean_restaurant",
            "chinese_restaurant",
            "ramen_restaurant",
            "sushi_restaurant",
            "meal_takeaway",
            "bar",
            "point_of_interest",
            "establishment"
    );
    private static final Set<String> TRANSIT_TYPES = Set.of(
            "train_station",
            "subway_station",
            "transit_station",
            "transportation_service",
            "point_of_interest",
            "establishment"
    );
    private static final Set<String> NAME_HINT_STOP_WORDS = Set.of(
            "카페",
            "커피",
            "식당",
            "맛집",
            "레스토랑",
            "restaurant",
            "coffee",
            "cafe",
            "점",
            "본점",
            "지점"
    );

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.coordinate-threshold-meters:5000}")
    private double coordinateThresholdMeters;

    public Optional<ValidatedPlace> validateAndCorrect(PlanRequestDto requestDto, AiPlanItemResponseDto aiPlanItem) {
        try {
            log.info("Place validation started. regionName={}, placeName={}",
                    requestDto.getRegionName(), aiPlanItem.getPlaceName());

            List<GooglePlaceSearchResult> googlePlaces = googlePlacesClient.searchPlaces(
                    requestDto.getRegionName(),
                    aiPlanItem.getPlaceName(),
                    requestDto.getLatitude(),
                    requestDto.getLongitude()
            );

            if (googlePlaces.isEmpty()) {
                log.warn("Place validation failed. Skipping this place. placeName={}, reason=no Google Places result",
                        aiPlanItem.getPlaceName());
                return Optional.empty();
            }

            Optional<ScoredPlaceCandidate> bestCandidate = selectBestCandidate(
                    requestDto.getRegionName(),
                    aiPlanItem,
                    googlePlaces
            );
            if (bestCandidate.isEmpty()) {
                log.warn("Place validation failed. Skipping this place. placeName={}, reason=no candidate passed scoring",
                        aiPlanItem.getPlaceName());
                return Optional.empty();
            }

            GooglePlaceSearchResult googlePlace = bestCandidate.get().googlePlace();
            log.info("Place validation completed. aiPlaceName={}, googlePlaceName={}, placeId={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), googlePlace.getPlaceId());

            return Optional.of(new ValidatedPlace(
                    googlePlace.getDisplayName(),
                    googlePlace.getPlaceId(),
                    googlePlace.getFormattedAddress(),
                    googlePlace.getLatitude(),
                    googlePlace.getLongitude(),
                    googlePlace.getTypes()
            ));
        } catch (RuntimeException e) {
            log.warn("Place validation failed by unexpected error. placeName={}", aiPlanItem.getPlaceName(), e);
            return Optional.empty();
        }
    }

    private Optional<ScoredPlaceCandidate> selectBestCandidate(
            String regionName,
            AiPlanItemResponseDto aiPlanItem,
            List<GooglePlaceSearchResult> googlePlaces
    ) {
        RequestedPlaceType requestedType = RequestedPlaceType.from(aiPlanItem.getPlaceName());
        int minScore = requestedType.isStrict() ? STRICT_MIN_SCORE : GENERAL_MIN_SCORE;

        return googlePlaces.stream()
                .map(googlePlace -> scoreCandidate(regionName, aiPlanItem, googlePlace, requestedType))
                .filter(candidate -> candidate.score() >= minScore)
                .max(Comparator.comparingInt(ScoredPlaceCandidate::score));
    }

    private ScoredPlaceCandidate scoreCandidate(
            String regionName,
            AiPlanItemResponseDto aiPlanItem,
            GooglePlaceSearchResult googlePlace,
            RequestedPlaceType requestedType
    ) {
        double distanceMeters = calculateDistanceMeters(aiPlanItem, googlePlace);
        double allowedDistanceMeters = allowedDistanceMeters(requestedType);

        if (isOnlyBroadPlace(googlePlace)) {
            log.info("Google Places candidate rejected by broad type. aiPlaceName={}, googlePlaceName={}, types={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), googlePlace.getTypes());
            return new ScoredPlaceCandidate(googlePlace, -1, distanceMeters);
        }

        if (distanceMeters > allowedDistanceMeters) {
            log.info("Google Places candidate rejected by distance. aiPlaceName={}, googlePlaceName={}, distanceMeters={}, thresholdMeters={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), Math.round(distanceMeters), allowedDistanceMeters);
            return new ScoredPlaceCandidate(googlePlace, -1, distanceMeters);
        }

        if (requestedType.isStrict() && !matchesRequestedType(requestedType, googlePlace.getTypes())) {
            log.info("Google Places candidate rejected by type. aiPlaceName={}, googlePlaceName={}, requestedType={}, types={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), requestedType, googlePlace.getTypes());
            return new ScoredPlaceCandidate(googlePlace, -1, distanceMeters);
        }

        if (requestedType.isStrict() && !matchesPrimaryKeyword(regionName, aiPlanItem.getPlaceName(), googlePlace.getDisplayName())) {
            log.info("Google Places candidate rejected by primary keyword. aiPlaceName={}, googlePlaceName={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName());
            return new ScoredPlaceCandidate(googlePlace, -1, distanceMeters);
        }

        int score = 0;
        if (isNameSimilar(aiPlanItem.getPlaceName(), googlePlace.getDisplayName())) {
            score += 50;
        }
        if (matchesPrimaryKeyword(regionName, aiPlanItem.getPlaceName(), googlePlace.getDisplayName())) {
            score += 30;
        }
        if (matchesRequestedType(requestedType, googlePlace.getTypes())) {
            score += requestedType.isStrict() ? 25 : 10;
        }
        score += distanceScore(distanceMeters, allowedDistanceMeters);

        log.info("Google Places candidate scored. aiPlaceName={}, googlePlaceName={}, score={}, distanceMeters={}, requestedType={}, types={}",
                aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), score, Math.round(distanceMeters),
                requestedType, googlePlace.getTypes());

        return new ScoredPlaceCandidate(googlePlace, score, distanceMeters);
    }

    private double calculateDistanceMeters(AiPlanItemResponseDto aiPlanItem, GooglePlaceSearchResult googlePlace) {
        double distanceMeters = GeoDistanceCalculator.distanceMeters(
                aiPlanItem.getLatitude(),
                aiPlanItem.getLongitude(),
                googlePlace.getLatitude(),
                googlePlace.getLongitude()
        );

        log.info("Place coordinate distance calculated. aiPlaceName={}, googlePlaceName={}, distanceMeters={}, thresholdMeters={}",
                aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), Math.round(distanceMeters), coordinateThresholdMeters);

        return distanceMeters;
    }

    private double allowedDistanceMeters(RequestedPlaceType requestedType) {
        if (requestedType.isStrict()) {
            return Math.min(coordinateThresholdMeters, STRICT_PLACE_THRESHOLD_METERS);
        }

        return coordinateThresholdMeters;
    }

    private boolean isOnlyBroadPlace(GooglePlaceSearchResult googlePlace) {
        List<String> types = googlePlace.getTypes();
        return types != null && !types.isEmpty() && BROAD_PLACE_TYPES.containsAll(types);
    }

    private boolean matchesRequestedType(RequestedPlaceType requestedType, List<String> googleTypes) {
        if (requestedType == RequestedPlaceType.GENERAL) {
            return true;
        }

        if (googleTypes == null || googleTypes.isEmpty()) {
            return false;
        }

        return switch (requestedType) {
            case CAFE -> containsAny(googleTypes, CAFE_TYPES);
            case RESTAURANT -> containsAny(googleTypes, RESTAURANT_TYPES);
            case TRANSIT -> containsAny(googleTypes, TRANSIT_TYPES);
            case GENERAL -> true;
        };
    }

    private boolean containsAny(List<String> values, Set<String> expectedValues) {
        return values.stream().anyMatch(expectedValues::contains);
    }

    private boolean isNameSimilar(String aiPlaceName, String googlePlaceName) {
        String normalizedAiName = normalizeName(aiPlaceName);
        String normalizedGoogleName = normalizeName(googlePlaceName);

        return !normalizedAiName.isBlank()
                && !normalizedGoogleName.isBlank()
                && (normalizedAiName.contains(normalizedGoogleName)
                || normalizedGoogleName.contains(normalizedAiName));
    }

    private boolean matchesPrimaryKeyword(String regionName, String aiPlaceName, String googlePlaceName) {
        String normalizedGoogleName = normalizeName(googlePlaceName);
        List<String> primaryKeywords = extractPrimaryKeywords(regionName, aiPlaceName);
        if (primaryKeywords.isEmpty()) {
            return false;
        }

        String firstKeyword = primaryKeywords.get(0);
        if (matchesKeyword(firstKeyword, normalizedGoogleName)) {
            return true;
        }

        long matchedKeywordCount = primaryKeywords.stream()
                .filter(keyword -> matchesKeyword(keyword, normalizedGoogleName))
                .count();

        return matchedKeywordCount >= 2;
    }

    private boolean matchesKeyword(String keyword, String normalizedGoogleName) {
        return normalizedGoogleName.contains(keyword)
                || matchesStationNameWithoutSuffix(keyword, normalizedGoogleName);
    }

    private boolean matchesStationNameWithoutSuffix(String primaryKeyword, String normalizedGoogleName) {
        return primaryKeyword.endsWith("역")
                && primaryKeyword.length() > 1
                && normalizedGoogleName.contains(primaryKeyword.substring(0, primaryKeyword.length() - 1));
    }

    private List<String> extractPrimaryKeywords(String regionName, String aiPlaceName) {
        String withoutParentheses = PARENTHESIS_PATTERN.matcher(aiPlaceName).replaceAll(" ");
        Set<String> dynamicStopWords = extractDynamicStopWords(regionName);

        return TOKEN_SPLIT_PATTERN.splitAsStream(withoutParentheses)
                .map(this::normalizeName)
                .map(this::removeCommonSuffix)
                .filter(token -> token.length() >= 3)
                .filter(token -> !NAME_HINT_STOP_WORDS.contains(token))
                .filter(token -> !dynamicStopWords.contains(token))
                .toList();
    }

    private Set<String> extractDynamicStopWords(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return Set.of();
        }

        return TOKEN_SPLIT_PATTERN.splitAsStream(regionName)
                .map(this::normalizeName)
                .filter(token -> !token.isBlank())
                .collect(java.util.stream.Collectors.toSet());
    }

    private String removeCommonSuffix(String token) {
        if (token.endsWith("점") && token.length() > 3) {
            return token.substring(0, token.length() - 1);
        }

        return token;
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }

        String withoutParentheses = PARENTHESIS_PATTERN.matcher(value).replaceAll("");
        return NON_NAME_CHARACTER_PATTERN.matcher(withoutParentheses.toLowerCase()).replaceAll("");
    }

    private int distanceScore(double distanceMeters, double allowedDistanceMeters) {
        double scoreRatio = Math.max(0.0, 1.0 - (distanceMeters / allowedDistanceMeters));
        return (int) Math.round(scoreRatio * 25);
    }

    private record ScoredPlaceCandidate(
            GooglePlaceSearchResult googlePlace,
            int score,
            double distanceMeters
    ) {
    }

    private enum RequestedPlaceType {
        GENERAL,
        CAFE,
        RESTAURANT,
        TRANSIT;

        static RequestedPlaceType from(String placeName) {
            String normalizedName = placeName == null ? "" : placeName.toLowerCase();

            if (normalizedName.contains("카페")
                    || normalizedName.contains("커피")
                    || normalizedName.contains("coffee")
                    || normalizedName.contains("스타벅스")
                    || normalizedName.contains("블루보틀")) {
                return CAFE;
            }

            if (normalizedName.contains("식당")
                    || normalizedName.contains("맛집")
                    || normalizedName.contains("레스토랑")
                    || normalizedName.contains("restaurant")
                    || normalizedName.contains("라멘")
                    || normalizedName.contains("우동")
                    || normalizedName.contains("스시")
                    || normalizedName.contains("초밥")
                    || normalizedName.contains("이자카야")
                    || normalizedName.contains("딘타이펑")) {
                return RESTAURANT;
            }

            if (normalizedName.contains("역") || normalizedName.contains("station")) {
                return TRANSIT;
            }

            return GENERAL;
        }

        boolean isStrict() {
            return this != GENERAL;
        }
    }
}
