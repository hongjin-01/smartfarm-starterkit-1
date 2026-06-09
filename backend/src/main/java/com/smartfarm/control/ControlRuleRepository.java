package com.smartfarm.control;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlRuleRepository extends JpaRepository<ControlRule, Long> {

    List<ControlRule> findByDeviceIdAndEnabled(String deviceId, boolean enabled);
}
