package com.smartfarm.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartfarm.control.ControlCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MqttPublisherService {

    private final MqttClient mqttClient;
    private final ObjectMapper objectMapper;

    public void publishControlCommand(ControlCommand command) {
        try {
            String topic = String.format("smartfarm/%s/control/command", command.getDeviceId());
            String payload = objectMapper.writeValueAsString(command);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            mqttClient.publish(topic, message);
            log.info("제어 명령 발행: topic={}, action={}", topic, command.getAction());
        } catch (Exception e) {
            log.error("제어 명령 발행 실패: deviceId={}", command.getDeviceId(), e);
        }
    }
}
