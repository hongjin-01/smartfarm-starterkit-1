package com.smartfarm.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfarm.sensor.SensorDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttSubscriberService implements MqttCallbackExtended {

    private final MqttClient mqttClient;
    private final SensorDataService sensorDataService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.topics.sensor-data}")
    private String sensorTopic;

    @PostConstruct
    public void init() {
        mqttClient.setCallback(this);
        subscribe();
    }

    private void subscribe() {
        try {
            mqttClient.subscribe(sensorTopic, 1, this::handleMessage);
            log.info("MQTT 구독 시작: {}", sensorTopic);
        } catch (MqttException e) {
            log.error("MQTT 구독 실패: {}", sensorTopic, e);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            log.debug("MQTT 수신: topic={}", topic);
            SensorPayload sensorPayload = objectMapper.readValue(payload, SensorPayload.class);
            sensorDataService.save(extractDeviceId(topic), sensorPayload);
        } catch (Exception e) {
            log.error("MQTT 메시지 처리 실패: topic={}", topic, e);
        }
    }

    // 재연결 완료 시 호출 - cleanSession=false 여도 클라이언트 콜백은 재등록 필요
    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            log.info("MQTT 재연결 완료 - 구독 재등록: {}", sensorTopic);
            subscribe();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT 연결 끊김 - 자동 재연결 대기 중", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // subscribe()의 IMqttMessageListener가 처리하므로 빈 구현
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {}

    // topic 형식: smartfarm/{deviceId}/sensor/{type}
    private String extractDeviceId(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 1 ? parts[1] : "unknown";
    }
}
