package com.smartfarm.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
public class SensorDataController {

    private final SensorDataRepository sensorDataRepository;

    @GetMapping("/recent")
    public ResponseEntity<List<SensorData>> getRecent(
        @RequestParam(defaultValue = "24") int hours
    ) {
        return ResponseEntity.ok(
            sensorDataRepository.findAllRecent(OffsetDateTime.now().minusHours(hours))
        );
    }

    @GetMapping("/{deviceId}/{sensorType}")
    public ResponseEntity<List<SensorData>> getSensorData(
        @PathVariable String deviceId,
        @PathVariable String sensorType,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from
    ) {
        OffsetDateTime since = from != null ? from : OffsetDateTime.now().minusHours(24);
        return ResponseEntity.ok(
            sensorDataRepository.findRecentByDeviceAndType(deviceId, sensorType, since)
        );
    }
}
