package io.kineticedge.ks201.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "$type")
public class BagClaim {

    private String trackingNumber;
    private String airlines;
    private String origin;
    private String destination;
    private Instant issued;
}
