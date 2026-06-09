package com.smartfarm.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Device findByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new IllegalArgumentException("장치를 찾을 수 없습니다: " + deviceId));
    }

    @Transactional
    public Device register(Device device) {
        device.setStatus(Device.DeviceStatus.ACTIVE);
        return deviceRepository.save(device);
    }

    @Transactional
    public Device updateStatus(String deviceId, Device.DeviceStatus status) {
        Device device = findByDeviceId(deviceId);
        device.setStatus(status);
        return deviceRepository.save(device);
    }
}
