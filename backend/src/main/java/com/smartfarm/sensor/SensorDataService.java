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
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public SensorData save(String deviceId, SensorPayload payload) {
        SensorData sensorData = SensorData.builder()
            .deviceId(deviceId)
            .sensorType(payload.getSensorType())
            .value(payload.getValue())
            .unit(payload.getUnit())
            .recordedAt(OffsetDateTime.now())
            .build();

        SensorData saved = sensorDataRepository.save(sensorData);

        // 최신 센서값 Redis 캐시
        String redisKey = String.format("sensor:%s:%s", deviceId, payload.getSensorType());
        redisTemplate.opsForValue().set(redisKey, payload.getValue());

        // 실시간 대시보드 브로드캐스트 (SimpleBroker는 와일드카드 미지원 → 단일 토픽 사용)
        messagingTemplate.convertAndSend("/topic/sensors", saved);

        // 자동제어 판단 및 명령 발행
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
