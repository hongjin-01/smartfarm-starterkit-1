#!/bin/bash
# Claude Code 이벤트 → Slack 알림 스크립트
# 사용법: echo '{"message":"..."}' | bash slack_notify.sh notification
#        echo '{"session_id":"...","stop_hook_active":false}' | bash slack_notify.sh stop
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
NL=$'\n'

JQ="/c/Users/liant/AppData/Local/Microsoft/WinGet/Packages/jqlang.jq_Microsoft.Winget.Source_8wekyb3d8bbwe/jq.exe"
EVENT_TYPE="${1:-unknown}"
WEBHOOK_URL="${SLACK_WEBHOOK_URL:-}"

# 웹훅 URL 미설정 시 조용히 종료 (Claude Code 흐름 방해 금지)
PLACEHOLDER="https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
if [ -z "$WEBHOOK_URL" ] || [ "$WEBHOOK_URL" = "$PLACEHOLDER" ]; then
    exit 0
fi

INPUT=$(cat)

# ── Stop 이벤트: 무한 루프 방지 ──────────────────────────────────────
if [ "$EVENT_TYPE" = "stop" ]; then
    LOOP_GUARD=$(printf '%s' "$INPUT" | "$JQ" -r '.stop_hook_active // false' 2>/dev/null)
    if [ "$LOOP_GUARD" = "true" ]; then
        exit 0
    fi

    # 2분 이내 재알림 방지 (모든 응답마다 알림 과다 방지)
    DEBOUNCE_FILE="/tmp/claude_stop_last_notify"
    DEBOUNCE_SECS=120
    NOW=$(date +%s)
    LAST=0
    [ -f "$DEBOUNCE_FILE" ] && LAST=$(cat "$DEBOUNCE_FILE" 2>/dev/null || echo 0)
    DIFF=$((NOW - LAST))
    if [ "$DIFF" -lt "$DEBOUNCE_SECS" ]; then
        exit 0
    fi
    echo "$NOW" > "$DEBOUNCE_FILE"
fi

# ── 이벤트별 메시지 구성 ─────────────────────────────────────────────
if [ "$EVENT_TYPE" = "notification" ]; then
    RAW_MSG=$(printf '%s' "$INPUT" | "$JQ" -r '.message // "확인이 필요합니다"' 2>/dev/null)
    TEXT=":bell: *Claude Code 권한 요청*${NL}${RAW_MSG}${NL}${NL}:point_right: Claude Code 창을 확인해주세요."

elif [ "$EVENT_TYPE" = "stop" ]; then
    SESSION=$(printf '%s' "$INPUT" | "$JQ" -r '.session_id // "unknown"' 2>/dev/null | cut -c1-8)
    TEXT=":white_check_mark: *Claude Code 작업 완료*${NL}세션 \`${SESSION}...\` 응답이 완료되었습니다."

else
    TEXT=":information_source: Claude Code 이벤트: ${EVENT_TYPE}"
fi

# ── Slack 전송 (오류 무시, 5초 타임아웃) ────────────────────────────
# tr -d '\r': jq.exe(Windows)가 CRLF 출력하는 것 제거
# --data-binary @-: 인자 대신 stdin으로 전달 (Windows 인자 인코딩 깨짐 방지)
printf '%s' "$TEXT" \
    | "$JQ" -Rs '{text: ., username: "Claude Code Bot", icon_emoji: ":robot_face:"}' 2>/dev/null \
    | tr -d '\r' \
    | curl -s --max-time 5 \
        -X POST \
        -H 'Content-Type: application/json; charset=utf-8' \
        --data-binary @- \
        "$WEBHOOK_URL" > /dev/null 2>&1 || true

exit 0
