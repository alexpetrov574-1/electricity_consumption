package com.example.mob_app;

// ✅ ДОБАВЬТЕ ЭТИ ИМПОРТЫ:
import com.google.gson.annotations.SerializedName;

public class PredictionsResponse {

    @SerializedName("predicted_energy_wh")
    public double predictedEnergyWh;

    @SerializedName("model_version")
    public String modelVersion;

    @SerializedName("status")
    public String status;

    @SerializedName("timestamp")
    public String timestamp;
}