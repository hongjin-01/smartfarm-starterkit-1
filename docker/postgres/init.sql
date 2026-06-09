-- TimescaleDB 확장 활성화
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- 장치 테이블
CREATE TABLE IF NOT EXISTS devices (
    id          BIGSERIAL PRIMARY KEY,
    device_id   VARCHAR(100) UNIQUE NOT NULL,
    name        VARCHAR(200) NOT NULL,
    location    VARCHAR(200),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 센서 데이터 테이블 (TimescaleDB 하이퍼테이블)
CREATE TABLE IF NOT EXISTS sensor_data (
    id          BIGSERIAL,
    device_id   VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(50)  NOT NULL,
    value       DOUBLE PRECISION NOT NULL,
    unit        VARCHAR(20),
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, recorded_at)
);

SELECT create_hypertable('sensor_data', 'recorded_at', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_sensor_device_time
    ON sensor_data (device_id, recorded_at DESC);

CREATE INDEX IF NOT EXISTS idx_sensor_type_time
    ON sensor_data (sensor_type, recorded_at DESC);

-- 자동제어 룰 테이블
CREATE TABLE IF NOT EXISTS control_rules (
    id            BIGSERIAL PRIMARY KEY,
    device_id     VARCHAR(100) NOT NULL,
    sensor_type   VARCHAR(50)  NOT NULL,
    min_threshold DOUBLE PRECISION NOT NULL,
    max_threshold DOUBLE PRECISION NOT NULL,
    action        VARCHAR(100) NOT NULL,
    target        VARCHAR(100) NOT NULL,
    enabled       BOOLEAN NOT NULL DEFAULT TRUE
);

-- 기본 샘플 데이터
INSERT INTO devices (device_id, name, location, status) VALUES
    ('device-001', '온실 1동 컨트롤러', '온실 1동', 'ACTIVE'),
    ('device-002', '온실 2동 컨트롤러', '온실 2동', 'ACTIVE')
ON CONFLICT DO NOTHING;

INSERT INTO control_rules (device_id, sensor_type, min_threshold, max_threshold, action, target) VALUES
    ('device-001', 'temperature', 18.0, 28.0, 'VENTILATE', 'fan'),
    ('device-001', 'humidity',    60.0, 85.0, 'IRRIGATE',  'sprinkler'),
    ('device-002', 'temperature', 18.0, 28.0, 'VENTILATE', 'fan')
ON CONFLICT DO NOTHING;
