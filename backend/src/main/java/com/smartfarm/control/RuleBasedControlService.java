package com.smartfarm.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "control.mode", havingValue = "rule", matchIfMissing = true)
public class RuleBasedControlService implements ControlDecisionService {

    private final ControlRuleRepository ruleRepository;

    @Override
    public ControlCommand decide(SensorSnapshot snapshot) {
        List<ControlRule> rules = ruleRepository.findByDeviceIdAndEnabled(
            snapshot.getDeviceId(), true
        );

        for (ControlRule rule : rules) {
            Double sensorValue = snapshot.getSensorValues().get(rule.getSensorType());
            if (sensorValue == null) continue;

            if (sensorValue < rule.getMinThreshold() || sensorValue > rule.getMaxThreshold()) {
                log.info("제어 룰 발동: device={}, sensor={}, value={}, action={}",
                    snapshot.getDeviceId(), rule.getSensorType(), sensorValue, rule.getAction());

                return ControlCommand.builder()
                    .deviceId(snapshot.getDeviceId())
                    .action(rule.getAction())
                    .target(rule.getTarget())
                    .value(sensorValue)
                    .build();
            }
        }

        return null;
    }
}
