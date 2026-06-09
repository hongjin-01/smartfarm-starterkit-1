from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional

app = FastAPI(title="SmartFarm ML Service", version="0.1.0")


class SensorSnapshot(BaseModel):
    device_id: str
    sensor_values: dict[str, float]


class ControlCommand(BaseModel):
    device_id: str
    action: str
    target: str
    value: Optional[float] = None


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict", response_model=Optional[ControlCommand])
def predict(snapshot: SensorSnapshot):
    """
    ML 기반 제어 명령 예측 엔드포인트.
    Phase 3에서 학습된 모델로 교체 예정.
    Spring Boot의 control.mode=ml 설정 시 호출됩니다.
    """
    # TODO: 학습된 모델 로드 및 예측 구현
    return None
