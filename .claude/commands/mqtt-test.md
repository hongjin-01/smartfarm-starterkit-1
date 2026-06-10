MQTT 테스트 데이터를 발행합니다.

## 인자 형식
`/mqtt-test [deviceId] [sensorType] [value] [unit]`

예시:
- `/mqtt-test` → 기본값으로 발행
- `/mqtt-test device-001 temperature 25.5 °C`
- `/mqtt-test device-002 humidity 72.0 %`
- `/mqtt-test device-001 co2 850 ppm`

## 실행 지침

$ARGUMENTS를 공백으로 분리해 아래 순서로 파싱하세요:
- 1번째 인자 → deviceId (없으면 기본값: `device-001`)
- 2번째 인자 → sensorType (없으면 기본값: `temperature`)
- 3번째 인자 → value (없으면 기본값: `25.5`)
- 4번째 인자 → unit (없으면 기본값: `°C`)

파싱한 값으로 아래 Bash 명령을 실행하세요.
**반드시 Bash를 사용하세요. PowerShell은 JSON 따옴표를 제거하는 문제가 있습니다.**

```bash
docker exec smartfarm-mqtt mosquitto_pub \
  -t "smartfarm/{deviceId}/sensor/{sensorType}" \
  -m '{"sensorType":"{sensorType}","value":{value},"unit":"{unit}"}'
```

실행 후 다음을 확인하고 결과를 보고하세요:
1. 명령 성공/실패 여부
2. 발행한 토픽과 페이로드 내용
3. 실패 시 컨테이너 이름(`smartfarm-mqtt`) 또는 Docker 상태 점검 안내
