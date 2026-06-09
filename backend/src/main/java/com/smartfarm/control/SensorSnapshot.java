package com.smartfarm.control;

import lombok.*;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorSnapshot {
    private String deviceId;
    private Map<String, Double> sensorValues;
}
