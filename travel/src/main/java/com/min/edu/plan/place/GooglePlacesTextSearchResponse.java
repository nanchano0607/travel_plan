package com.min.edu.plan.place;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GooglePlacesTextSearchResponse {

    private List<GooglePlace> places;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class GooglePlace {
        private String id;
        private DisplayName displayName;
        private String formattedAddress;
        private Location location;
        private List<String> types;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DisplayName {
        private String text;
        private String languageCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Location {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }
}
