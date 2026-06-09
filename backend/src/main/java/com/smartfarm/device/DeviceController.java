package com.smartfarm.device;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<Device>> getAll() {
        return ResponseEntity.ok(deviceService.findAll());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<Device> getOne(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceService.findByDeviceId(deviceId));
    }

    @PostMapping
    public ResponseEntity<Device> register(@RequestBody Device device) {
        return ResponseEntity.ok(deviceService.register(device));
    }

    @PatchMapping("/{deviceId}/status")
    public ResponseEntity<Device> updateStatus(
        @PathVariable String deviceId,
        @RequestParam Device.DeviceStatus status
    ) {
        return ResponseEntity.ok(deviceService.updateStatus(deviceId, status));
    }
}
