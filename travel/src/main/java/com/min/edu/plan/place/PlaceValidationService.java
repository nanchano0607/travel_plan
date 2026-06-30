package com.min.edu.plan.place;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.min.edu.exception.CustomException;
import com.min.edu.exception.ErrorCode;
import com.min.edu.plan.ai.dto.AiPlanItemResponseDto;
import com.min.edu.plan.ai.dto.PlanRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceValidationService {

    private final GooglePlacesClient googlePlacesClient;

    @Value("${google.places.coordinate-threshold-meters:5000}")
    private double coordinateThresholdMeters;

    public ValidatedPlace validateAndCorrect(PlanRequestDto requestDto, AiPlanItemResponseDto aiPlanItem) {
        try {
            log.info("Place validation started. regionName={}, placeName={}",
                    requestDto.getRegionName(), aiPlanItem.getPlaceName());

            GooglePlaceSearchResult googlePlace = googlePlacesClient.searchPlace(
                            requestDto.getRegionName(),
                            aiPlanItem.getPlaceName(),
                            requestDto.getLatitude(),
                            requestDto.getLongitude()
                    )
                    .orElseThrow(() -> new CustomException(ErrorCode.PLACE_VALIDATION_FAILED));

            validateCoordinateMatch(aiPlanItem, googlePlace);
            log.info("Place validation completed. aiPlaceName={}, googlePlaceName={}, placeId={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), googlePlace.getPlaceId());

            return new ValidatedPlace(
                    googlePlace.getDisplayName(),
                    googlePlace.getPlaceId(),
                    googlePlace.getFormattedAddress(),
                    googlePlace.getLatitude(),
                    googlePlace.getLongitude(),
                    googlePlace.getTypes()
            );
        } catch (CustomException e) {
            log.warn("Place validation failed. placeName={}, reason={}", aiPlanItem.getPlaceName(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.warn("Place validation failed by unexpected error. placeName={}", aiPlanItem.getPlaceName(), e);
            throw new CustomException(ErrorCode.PLACE_VALIDATION_FAILED);
        }
    }

    private void validateCoordinateMatch(AiPlanItemResponseDto aiPlanItem, GooglePlaceSearchResult googlePlace) {
        double distanceMeters = GeoDistanceCalculator.distanceMeters(
                aiPlanItem.getLatitude(),
                aiPlanItem.getLongitude(),
                googlePlace.getLatitude(),
                googlePlace.getLongitude()
        );

        log.info("Place coordinate distance calculated. aiPlaceName={}, googlePlaceName={}, distanceMeters={}, thresholdMeters={}",
                aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), Math.round(distanceMeters), coordinateThresholdMeters);

        if (distanceMeters > coordinateThresholdMeters) {
            log.warn("Place coordinate mismatch. aiPlaceName={}, googlePlaceName={}, distanceMeters={}, thresholdMeters={}",
                    aiPlanItem.getPlaceName(), googlePlace.getDisplayName(), Math.round(distanceMeters), coordinateThresholdMeters);
            throw new CustomException(ErrorCode.PLACE_VALIDATION_FAILED);
        }
    }
}
