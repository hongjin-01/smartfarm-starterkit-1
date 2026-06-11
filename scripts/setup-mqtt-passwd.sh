#!/usr/bin/env bash
# MQTT 브로커 인증 초기 설정 스크립트
# 최초 1회 또는 패스워드 변경 시 실행 (docker-compose up 전에 실행)
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
PASSWD_FILE="$ROOT_DIR/docker/mosquitto/passwd"
ENV_FILE="$ROOT_DIR/.env"

# .env 파일에서 MQTT 자격증명 로드
if [ -f "$ENV_FILE" ]; then
    export "$(grep -E '^MQTT_USERNAME=' "$ENV_FILE" | head -1)"
    export "$(grep -E '^MQTT_PASSWORD=' "$ENV_FILE" | head -1)"
fi

MQTT_USER="${MQTT_USERNAME:-smartfarm-backend}"
MQTT_PASS="${MQTT_PASSWORD:-}"

if [ -z "$MQTT_PASS" ]; then
    echo "오류: MQTT_PASSWORD 가 .env 파일에 설정되어 있지 않습니다."
    echo "  .env.example 을 참고하여 .env 파일을 만들고 MQTT_PASSWORD 를 지정하세요."
    exit 1
fi

echo "MQTT 패스워드 파일 생성 중..."
echo "  사용자: $MQTT_USER"
echo "  파일:   $PASSWD_FILE"

# eclipse-mosquitto 이미지의 mosquitto_passwd 도구로 해시 생성
docker run --rm \
    -v "$PASSWD_FILE:/mosquitto/config/passwd" \
    eclipse-mosquitto:2 \
    mosquitto_passwd -c -b /mosquitto/config/passwd "$MQTT_USER" "$MQTT_PASS"

echo ""
echo "완료. 이제 docker-compose up -d 를 실행하세요."
