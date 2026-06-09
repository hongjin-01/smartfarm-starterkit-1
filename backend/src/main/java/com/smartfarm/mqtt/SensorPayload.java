package com.smartfarm.mqtt;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SensorPayload {
    private String sensorType;
    private Double value;
    private String unit;
}
