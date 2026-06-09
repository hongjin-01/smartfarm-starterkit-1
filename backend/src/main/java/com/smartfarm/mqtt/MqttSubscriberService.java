package com.smartfarm.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfarm.sensor.SensorDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSubscriberService {

    private final MqttClient mqttClient;
    private final SensorDataService sensorDataService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.topics.sensor-data}")
    private String sensorTopic;

    @PostConstruct
    public void subscribe() throws Exception {
        mqttClient.subscribe(sensorTopic, 1, (topic, message) -> {
            try {
                String payload = new String(message.getPayload());
                log.debug("MQTT 수신: topic={}", topic);
                SensorPayload sensorPayload = objectMapper.readValue(payload, SensorPayload.class);
                sensorDataService.save(extractDeviceId(topic), sensorPayload);
            } catch (Exception e) {
                log.error("MQTT 메시지 처리 실패: topic={}", topic, e);
            }
        });
        log.info("MQTT 구독 시작: {}", sensorTopic);
    }

    // topic 형식: smartfarm/{deviceId}/sensor/{type}
    private String extractDeviceId(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 1 ? parts[1] : "unknown";
    }
}
