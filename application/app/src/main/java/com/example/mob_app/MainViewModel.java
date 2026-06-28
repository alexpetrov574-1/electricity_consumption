package com.example.mob_app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final ApiService api = ApiClient.getClient().create(ApiService.class);

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    public void predict(float tOut, float tIn, float humidity, float lights, float lag) {
        uiState.postValue(UiState.loading());

        PredictionsRequest request = new PredictionsRequest();
        request.fillWithRoomSpecifics(tIn, humidity, tOut, lights, lag);

        api.predict(request).enqueue(new Callback<PredictionsResponse>() {
            @Override
            public void onResponse(Call<PredictionsResponse> call, Response<PredictionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    uiState.postValue(UiState.success(response.body()));
                }
                else {
                    uiState.postValue(UiState.error("Ошибка сервера: " + response.code()));
                }
            }

            @Override
            public void onFailure(Call<PredictionsResponse> call, Throwable t) {
                uiState.postValue(UiState.error("Нет связи с сервером"));
            }
        });
    }

    public static class UiState {
        public final boolean isLoading;
        public final PredictionsResponse data;
        public final String error;

        private UiState(boolean loading, PredictionsResponse data, String error) {
            this.isLoading = loading;
            this.data = data;
            this.error = error;
        }

        public static UiState loading() { return new UiState(true, null, null); }
        public static UiState success(PredictionsResponse data) { return new UiState(false, data, null); }
        public static UiState error(String error) { return new UiState(false, null, error); }
    }
}