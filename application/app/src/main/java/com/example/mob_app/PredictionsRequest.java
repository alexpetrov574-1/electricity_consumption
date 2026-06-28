package com.example.mob_app;

import java.util.Calendar;

public class PredictionsRequest {
    public float lights;
    public float T1, RH_1, T2, RH_2, T3, RH_3, T4, RH_4, T5, RH_5;
    public float T6, RH_6, T7, RH_7, T8, RH_8, T9, RH_9;
    public float T_out, Press_mm_hg, RH_out, Windspeed, Visibility, Tdewpoint;
    public float rv1, rv2;
    public int hour, dayofweek, is_weekend;
    public float Appliances_lag_1;

    public PredictionsRequest() {
        Calendar cal = Calendar.getInstance();
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.dayofweek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        this.is_weekend = (dayofweek >= 5) ? 1 : 0;
    }

    /**
     * Заполняет все 9 комнат с учётом их назначения.
     */
    public void fillWithRoomSpecifics(float tInBase, float rhBase, float tOut,
                                      float lights, float lag) {
        // Улица и погода
        this.T_out = tOut;
        this.RH_out = rhBase;
        this.Press_mm_hg = 750.0f;
        this.Windspeed = 2.0f;
        this.Visibility = 30.0f;
        this.Tdewpoint = tOut - 5.0f;
        this.lights = lights;
        this.Appliances_lag_1 = lag;

        // Комнаты с реалистичным разбросом
        this.T1 = tInBase + 1.5f;  this.RH_1 = rhBase + 5.0f;   // Кухня
        this.T2 = tInBase;         this.RH_2 = rhBase;          // Гостиная
        this.T3 = tInBase - 1.0f;  this.RH_3 = rhBase + 3.0f;   // Прачечная
        this.T4 = tInBase + 0.8f;  this.RH_4 = rhBase - 2.0f;   // Офис
        this.T5 = tInBase + 2.0f;  this.RH_5 = rhBase + 15.0f;  // Ванная
        this.T6 = (tInBase + tOut) / 2.0f;
        this.RH_6 = (rhBase + this.RH_out) / 2.0f;              // Снаружи (север)
        this.T7 = tInBase + 1.2f;  this.RH_7 = rhBase + 2.0f;   // Гладильная
        this.T8 = tInBase - 0.5f;  this.RH_8 = rhBase;          // Комната подростка
        this.T9 = tInBase - 0.8f;  this.RH_9 = rhBase - 3.0f;   // Спальня родителей

        this.rv1 = 0.0f;
        this.rv2 = 0.0f;
    }
}