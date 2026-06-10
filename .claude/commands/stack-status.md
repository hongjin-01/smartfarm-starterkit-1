스마트팜 전체 스택 상태를 점검합니다.

## 점검 대상
- Docker 컨테이너 3개: mosquitto(1884), postgres(5433), redis(6379)
- Spring Boot 백엔드: localhost:8080
- Next.js 프론트엔드: localhost:3000

## 실행 지침

아래 순서대로 점검한 뒤 결과를 표 형식으로 정리해주세요.

### 1. Docker 컨테이너 상태
다음 명령을 실행하세요:
```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```
`smartfarm-mqtt`, `smartfarm-postgres`, `smartfarm-redis` 세 컨테이너가 `Up` 상태인지 확인하세요.

### 2. Spring Boot 헬스체크
다음 명령을 실행하세요 (Bash):
```bash
curl -s http://localhost:8080/actuator/health
```
응답의 `"status": "UP"` 여부를 확인하세요. 연결 실패 시 ❌로 표시하세요.

### 3. Next.js 응답 확인
다음 명령을 실행하세요 (Bash):
```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000
```
HTTP 200 응답 여부를 확인하세요. 연결 실패 시 ❌로 표시하세요.

## 결과 출력 형식

점검이 완료되면 아래 형식으로 출력하세요:

| 서비스 | 상태 | 비고 |
|--------|------|------|
| mosquitto (1884) | ✅ Up / ❌ Down | |
| postgres (5433) | ✅ Up / ❌ Down | |
| redis (6379) | ✅ Up / ❌ Down | |
| Spring Boot (8080) | ✅ UP / ❌ Down | actuator 응답 |
| Next.js (3000) | ✅ 200 / ❌ Down | HTTP 상태코드 |

문제가 있는 서비스는 원인과 재기동 방법을 안내하세요:
- 컨테이너 다운: `docker-compose up -d`
- Spring Boot 미기동: `cd backend && ./gradlew bootRun`
- Next.js 미기동: `cd frontend && npm run dev`
