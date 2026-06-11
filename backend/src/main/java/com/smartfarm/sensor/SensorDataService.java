package com.smartfarm.sensor;

import com.smartfarm.control.ControlCommand;
import com.smartfarm.control.ControlDecisionService;
import com.smartfarm.control.SensorSnapshot;
import com.smartfarm.mqtt.MqttPublisherService;
import com.smartfarm.mqtt.SensorPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository sensorDataRepository;
    private final ControlDecisionService controlDecisionService;
    private final MqttPublisherService mqttPublisherService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // @Transactional 없음: repository.save()의 기본 트랜잭션(REQUIRED)이 즉시 커밋되므로
    // DB 커밋 이후 Redis/WebSocket/Control 순서가 보장됨. 롤백 시 캐시 오염 없음.
    public SensorData save(String deviceId, SensorPayload payload) {
        SensorData sensorData = SensorData.builder()
            .deviceId(deviceId)
            .sensorType(payload.getSensorType())
            .value(payload.getValue())
            .unit(payload.getUnit())
            .recordedAt(OffsetDateTime.now())
            .build();

        SensorData saved = sensorDataRepository.save(sensorData);

        // DB 커밋 완료 후 실행
        String redisKey = String.format("sensor:%s:%s", deviceId, payload.getSensorType());
        redisTemplate.opsForValue().set(redisKey, payload.getValue(), Duration.ofHours(25));

        messagingTemplate.convertAndSend("/topic/sensors", saved);

        SensorSnapshot snapshot = SensorSnapshot.builder()
            .deviceId(deviceId)
            .sensorValues(Map.of(payload.getSensorType(), payload.getValue()))
            .build();

        ControlCommand command = controlDecisionService.decide(snapshot);
        if (command != null) {
            mqttPublisherService.publishControlCommand(command);
        }

        return saved;
    }
}
