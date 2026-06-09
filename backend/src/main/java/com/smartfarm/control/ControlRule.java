package com.smartfarm.control;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "control_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ControlRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 50)
    private String sensorType;

    @Column(nullable = false)
    private Double minThreshold;

    @Column(nullable = false)
    private Double maxThreshold;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 100)
    private String target;

    @Column(nullable = false)
    private boolean enabled;
}
