# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

스마트팜 복합환경관리 스타터킷. MQTT로 센서 데이터를 수집하고, TimescaleDB에 저장하며, WebSocket으로 실시간 대시보드에 브로드캐스트하는 파이프라인을 구축한다.

## 실행 명령어

### 인프라 (Docker)
```bash
# 루트 디렉토리에서 실행
docker-compose up -d        # PostgreSQL(5433), Mosquitto(1884), Redis(6379) 기동
docker-compose down         # 중지
docker-compose logs -f      # 로그 확인
```

### 백엔드 (Spring Boot)
```bash
cd backend
./gradlew bootRun           # 개발 서버 기동 (포트 8080)
./gradlew build             # 빌드 및 테스트
./gradlew test              # 테스트만 실행
./gradlew test --tests "com.smartfarm.sensor.SensorDataServiceTest"  # 단일 테스트 클래스
```

### 프론트엔드 (Next.js)
```bash
cd frontend
npm run dev     # 개발 서버 기동 (포트 3000)
npm run build   # 프로덕션 빌드
npm run lint    # ESLint 검사
```

### MQTT 테스트 데이터 발행
```bash
# Bash 사용 필수 (PowerShell은 JSON 따옴표 제거 문제 있음)
docker exec smartfarm-mqtt mosquitto_pub \
  -t "smartfarm/device-001/sensor/temperature" \
  -m '{"sensorType":"temperature","value":25.5,"unit":"°C"}'
```

## 포트 충돌 주의

로컬에 PostgreSQL 또는 Mosquitto가 설치된 경우 충돌 방지를 위해 Docker는 비표준 포트를 사용한다:
- PostgreSQL: 호스트 **5433** → 컨테이너 5432
- Mosquitto: 호스트 **1884** → 컨테이너 1883
- `application.yml`의 `datasource.url`과 `mqtt.broker-url`이 이 포트를 가리키고 있다

## 아키텍처

### 데이터 흐름
```
IoT 장치
  → MQTT publish (smartfarm/{deviceId}/sensor/{type})
  → Mosquitto 브로커 (Docker :1884)
  → MqttSubscriberService (Paho 클라이언트, @PostConstruct 구독)
  → SensorDataService.save()
      ├─ TimescaleDB sensor_data 하이퍼테이블 저장
      ├─ Redis 최신값 캐시 (sensor:{deviceId}:{type})
      ├─ STOMP /topic/sensors 브로드캐스트 → 대시보드 실시간 업데이트
      └─ ControlDecisionService.decide() → 임계값 초과 시 MQTT 제어 명령 발행
               (smartfarm/{deviceId}/control/command)
```

### 제어 전략 교체 (Strategy Pattern)
`ControlDecisionService` 인터페이스를 구현체 두 개가 담당한다:
- `RuleBasedControlService`: `control.mode=rule`일 때 활성화 (기본값). DB `control_rules` 테이블 기반.
- Phase 3 ML 서비스: `control.mode=ml`로 변경하면 `ml-service/` FastAPI 서버로 HTTP 위임. `@ConditionalOnProperty`로 빈 전환.

### WebSocket
- Spring `SimpleBroker` 사용 → MQTT 스타일 와일드카드(`#`) **미지원**
- 모든 센서 데이터를 단일 토픽 `/topic/sensors`로 브로드캐스트
- 프론트엔드 `useWebSocket()` 훅이 SockJS + STOMP로 이 토픽을 구독

### 프론트엔드 상태 관리
- `useSensorStore` (Zustand): `sensors`(최신값), `history`(시계열, 최대 60개)
- 페이지 마운트 시 `/api/v1/sensors/recent?hours=24`로 24시간 히스토리 초기 로딩
- 이후 WebSocket으로 실시간 업데이트
- Next.js `rewrites`로 `/api/*` → `localhost:8080` 프록시 (`next.config.js`)

### DB 스키마 핵심
- `sensor_data`는 `recorded_at` 기준 TimescaleDB 하이퍼테이블 — 시간 범위 쿼리 성능 최적화
- `ddl-auto: validate` — Hibernate가 스키마를 생성하지 않음. 스키마 변경은 `docker/postgres/init.sql` 수정 후 볼륨 재생성 필요

## ML 서비스 (Phase 3)

`ml-service/`는 FastAPI 플레이스홀더다. `POST /predict`는 현재 `null` 반환. 실제 모델 연동 시 `RuleBasedControlService`와 병렬로 `MlBasedControlService`를 구현하고 `control.mode=ml`로 전환한다.
