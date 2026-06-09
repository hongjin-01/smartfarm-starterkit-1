package com.smartfarm.control;

public interface ControlDecisionService {

    /**
     * 센서 스냅샷을 기반으로 제어 명령을 결정합니다.
     * 룰 기반(기본) 또는 ML 기반 구현체로 교체 가능합니다.
     *
     * @param snapshot 현재 센서 데이터 스냅샷
     * @return 제어 명령 (제어가 불필요한 경우 null)
     */
    ControlCommand decide(SensorSnapshot snapshot);
}
