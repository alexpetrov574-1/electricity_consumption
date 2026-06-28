package com.example.mob_app;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mob_app.databinding.ActivityMainBinding;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;


    // Текущие значения ползунков
    private float tOut = 7.5f;
    private float tIn = 21.0f;
    private float humidity = 45.0f;
    private float lights = 0.0f;
    private float lag = 50.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupSeekBars();
        observeViewModel();
        updateDisplayValues();
    }

    private void setupSeekBars() {
        // Температура на улице: -10 до 35°C (диапазон 45°C, шаг 0.1°C)
        binding.seekTOut.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tOut = -10.0f + progress * 0.1f;
                updateDisplayValues();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Температура в доме: 15 до 30°C (диапазон 15°C, шаг 0.1°C)
        binding.seekTIn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tIn = 15.0f + progress * 0.1f;
                updateDisplayValues();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Влажность: 20 до 100% (диапазон 80%, шаг 1%)
        binding.seekHumidity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                humidity = 20.0f + progress;
                updateDisplayValues();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Освещение: 0 до 100 Вт·ч (шаг 1)
        binding.seekLights.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lights = progress;
                updateDisplayValues();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Потребление 10 мин назад: 0 до 200 Вт·ч (шаг 1)
        binding.seekLag.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lag = progress;
                updateDisplayValues();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Кнопка предсказания
        binding.btnPredict.setOnClickListener(v -> onPredictClicked());
    }

    private void updateDisplayValues() {
        binding.tvTOutValue.setText(String.format(Locale.getDefault(), "%.1f°C", tOut));
        binding.tvTInValue.setText(String.format(Locale.getDefault(), "%.1f°C", tIn));
        binding.tvHumidityValue.setText(String.format(Locale.getDefault(), "%.0f%%", humidity));
        binding.tvLightsValue.setText(String.format(Locale.getDefault(), "%.0f Wh", lights));
        binding.tvLagValue.setText(String.format(Locale.getDefault(), "%.0f Wh", lag));
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            binding.progressBar.setVisibility(state.isLoading ? View.VISIBLE : View.GONE);
            binding.btnPredict.setEnabled(!state.isLoading);

            if (state.error != null) {
                Toast.makeText(this, state.error, Toast.LENGTH_LONG).show();
            }

            if (state.data != null) {
                binding.tvResult.setText(
                        String.format(Locale.getDefault(), "%.1f", state.data.predictedEnergyWh)
                );
            }
        });
    }

    private void onPredictClicked() {
        viewModel.predict(tOut, tIn, humidity, lights, lag);
    }
}