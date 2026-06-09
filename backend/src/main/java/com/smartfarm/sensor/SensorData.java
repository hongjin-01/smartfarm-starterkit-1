package com.smartfarm.sensor;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 50)
    private String sensorType;

    @Column(nullable = false)
    private Double value;

    @Column(length = 20)
    private String unit;

    @Column(nullable = false)
    private OffsetDateTime recordedAt;
}
