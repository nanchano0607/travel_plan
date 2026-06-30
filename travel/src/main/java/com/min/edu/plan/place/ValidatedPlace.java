package com.min.edu.plan.place;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidatedPlace {

    private String placeName;
    private String placeId;
    private String formattedAddress;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private List<String> types;
}
