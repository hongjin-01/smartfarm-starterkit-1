package com.smartfarm.control;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ControlCommand {
    private String deviceId;
    private String action;
    private String target;
    private Double value;
}
