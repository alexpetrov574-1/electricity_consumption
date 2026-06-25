
# ============================================================
# FastAPI-сервер для предсказания энергопотребления
# Запуск: просто выполните эту ячейку
# ============================================================

import os
import logging
from datetime import datetime
import numpy as np
import pandas as pd
import joblib
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn

# Настройка логирования
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
logger = logging.getLogger(__name__)

# ============================================================
# Пути к артефактам
# ============================================================
BASE_DIR = "/api/project"  # текущая рабочая директория (где запущен notebook)
os.chdir(BASE_DIR)
MODEL_PATH = os.path.join(BASE_DIR, "models", "xgb_model.pkl")
FEATURES_PATH = os.path.join(BASE_DIR, "models", "feature_names.pkl")

print(f"📂 Рабочая директория: {BASE_DIR}")
print(f"📦 Путь к модели: {MODEL_PATH}")
print(f"📦 Путь к признакам: {FEATURES_PATH}")

# ============================================================
# Загрузка модели
# ============================================================
_model = None
_feature_names = None

if os.path.exists(MODEL_PATH):
    _model = joblib.load(MODEL_PATH)
    logger.info(f"✅ Модель загружена: {MODEL_PATH}")
    if os.path.exists(FEATURES_PATH):
        _feature_names = joblib.load(FEATURES_PATH)
        logger.info(f"✅ Признаки загружены: {len(_feature_names)} шт.")
else:
    logger.warning(f"⚠️  Модель не найдена: {MODEL_PATH}")
    logger.warning("   Сервер будет работать в FALLBACK-режиме (эвристика)")

# ============================================================
# Инициализация FastAPI
# ============================================================
app = FastAPI(
    title="Energy Consumption Predictor API",
    description="REST API для предсказания потребления энергии",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================
# Pydantic-схемы
# ============================================================
class PredictionRequest(BaseModel):
    lights: float
    T1: float; RH_1: float
    T2: float; RH_2: float
    T3: float; RH_3: float
    T4: float; RH_4: float
    T5: float; RH_5: float
    T6: float; RH_6: float
    T7: float; RH_7: float
    T8: float; RH_8: float
    T9: float; RH_9: float
    T_out: float
    Press_mm_hg: float
    RH_out: float
    Windspeed: float
    Visibility: float
    Tdewpoint: float
    rv1: float
    rv2: float
    hour: int = Field(..., ge=0, le=23)
    dayofweek: int = Field(..., ge=0, le=6)
    is_weekend: int = Field(..., ge=0, le=1)
    Appliances_lag_1: float = Field(..., ge=0)


class PredictionResponse(BaseModel):
    predicted_energy_wh: float
    model_version: str = "1.0.0"
    status: str = "success"
    timestamp: str = Field(default_factory=lambda: datetime.utcnow().isoformat())


# ============================================================
# Вспомогательные функции
# ============================================================
def apply_feature_engineering(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["temp_humidity_out"] = df["T_out"] * df["RH_out"]
    df["pressure_visibility"] = df["Press_mm_hg"] * df["Visibility"]
    df["Appliances_rolling_mean_6"] = df["Appliances_lag_1"]
    return df


def align_features(df: pd.DataFrame) -> pd.DataFrame:
    if _feature_names is None:
        return df
    for col in _feature_names:
        if col not in df.columns:
            df[col] = 0.0
    return df[_feature_names]


def fallback_predict(input_dict: dict) -> float:
    base = input_dict.get("Appliances_lag_1", 50.0)
    noise = np.random.normal(0, 3)
    return round(max(0.0, base * 0.98 + noise), 2)


# ============================================================
# Эндпоинты
# ============================================================
@app.get("/")
async def root():
    return {
        "service": "Energy Consumption Predictor API",
        "version": "1.0.0",
        "docs": "/docs",
        "model_loaded": _model is not None,
    }


@app.get("/health")
async def health_check():
    return {
        "status": "healthy" if _model is not None else "degraded (fallback)",
        "model_loaded": _model is not None,
    }


@app.post("/predict", response_model=PredictionResponse)
async def predict_energy(request: PredictionRequest):
    try:
        input_dict = request.model_dump()
        logger.info(f"Запрос | T_out={request.T_out}°C, hour={request.hour}")

        if _model is None:
            pred = fallback_predict(input_dict)
            return PredictionResponse(predicted_energy_wh=pred, status="fallback")

        df = pd.DataFrame([input_dict])
        df = apply_feature_engineering(df)
        df = align_features(df)

        pred = float(_model.predict(df)[0])
        pred = max(0.0, pred)

        logger.info(f"✅ Предсказание: {pred:.2f} Вт·ч")
        return PredictionResponse(predicted_energy_wh=round(pred, 2))

    except Exception as e:
        logger.error(f"❌ Ошибка: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# ============================================================
# 🚀 ЗАПУСК СЕРВЕРА (исправленная версия для notebook)
# ============================================================
print("\n" + "="*70)
print("🚀 ЗАПУСК FASTAPI СЕРВЕРА")
print("="*70)
print(f"📖 Откройте в браузере: http://localhost:8000/docs")
print(f"🔍 Health check:       http://localhost:8000/health")
print("="*70 + "\n")

# ✅ КЛЮЧЕВОЕ ИСПРАВЛЕНИЕ: передаём объект app, а не строку "main:app"
uvicorn.run(
    app,           # ← сам объект FastAPI-приложения
    host="0.0.0.0",
    port=8000,
    log_level="info"
)
