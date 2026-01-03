package com.example.nike_fe.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.model.WheelConfig;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminWheelConfigActivity extends AppCompatActivity {

    private SwitchMaterial switchIsActive;
    private EditText etMaxSpins;
    private EditText etDescription;
    private SwitchMaterial switchTimeRestricted;
    private EditText etEventName;
    private EditText etStartDate;
    private EditText etEndDate;
    private Button btnSaveConfig;

    private LuckyWheelApi api;
    private String token;
    private WheelConfig currentConfig;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Using simple String for API compatibility in this demo, ideally use ISO 8601
    private String startDateStr;
    private String endDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_wheel_config);

        com.example.nike_fe.data.api.RetrofitClient retrofitClient = 
            com.example.nike_fe.data.api.RetrofitClient.getInstance(this);
        api = retrofitClient.getLuckyWheelApi();
        String savedToken = retrofitClient.getToken();
        if (savedToken != null) {
            token = "Bearer " + savedToken;
        }

        initViews();
        loadConfig();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        switchIsActive = findViewById(R.id.switchIsActive);
        etMaxSpins = findViewById(R.id.etMaxSpins);
        etDescription = findViewById(R.id.etDescription);
        switchTimeRestricted = findViewById(R.id.switchTimeRestricted);
        etEventName = findViewById(R.id.etEventName);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        btnSaveConfig = findViewById(R.id.btnSaveConfig);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate, true));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate, false));

        btnSaveConfig.setOnClickListener(v -> saveConfig());
    }

    private void showDatePicker(EditText editText, boolean isStart) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                android.R.style.Theme_DeviceDefault_Light_Dialog,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String date = dateFormat.format(calendar.getTime());
                    editText.setText(date);

                    // Append default time for backend parsing
                    String dateTime = date + "T00:00:00";
                    if (isStart) {
                        startDateStr = dateTime;
                    } else {
                        endDateStr = date + "T23:59:59";
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        datePickerDialog.setOnShowListener(dialog -> {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });
        datePickerDialog.show();
    }

    private void loadConfig() {
        if (token == null)
            return;

        api.getConfig(token).enqueue(new Callback<WheelConfig>() {
            @Override
            public void onResponse(Call<WheelConfig> call, Response<WheelConfig> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentConfig = response.body();
                    updateUI(currentConfig);
                } else {
                    Toast.makeText(AdminWheelConfigActivity.this, "Lỗi tải cấu hình", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WheelConfig> call, Throwable t) {
                Toast.makeText(AdminWheelConfigActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(WheelConfig config) {
        switchIsActive.setChecked(Boolean.TRUE.equals(config.getIsActive()));
        etMaxSpins.setText(String.valueOf(config.getMaxSpinsPerDay()));
        etDescription.setText(config.getDescription());
        switchTimeRestricted.setChecked(Boolean.TRUE.equals(config.getIsTimeRestricted()));
        etEventName.setText(config.getEventName());

        // Simple display logic, parsing would be better but keeping it simple
        if (config.getStartDate() != null)
            etStartDate.setText(config.getStartDate().toString().split("T")[0]);
        if (config.getEndDate() != null)
            etEndDate.setText(config.getEndDate().toString().split("T")[0]);

        // Store initial strings if not changed
        if (config.getStartDate() != null)
            startDateStr = config.getStartDate().toString();
        if (config.getEndDate() != null)
            endDateStr = config.getEndDate().toString();
    }

    private void saveConfig() {
        if (token == null)
            return;

        WheelConfig newConfig = new WheelConfig();
        newConfig.setIsActive(switchIsActive.isChecked());

        try {
            newConfig.setMaxSpinsPerDay(Integer.parseInt(etMaxSpins.getText().toString()));
        } catch (NumberFormatException e) {
            newConfig.setMaxSpinsPerDay(1);
        }

        newConfig.setDescription(etDescription.getText().toString());
        newConfig.setIsTimeRestricted(switchTimeRestricted.isChecked());
        newConfig.setEventName(etEventName.getText().toString());

        // Note: For full event functionality we need to use the `updateEventSchedule`
        // endpoint
        // OR the backend `updateConfig` needs to handle dates too.
        // Based on backend code, `updateConfig` DOES NOT update dates.
        // `updateEventSchedule` does.

        // 1. Update general config
        api.updateConfig(token, newConfig).enqueue(new Callback<WheelConfig>() {
            @Override
            public void onResponse(Call<WheelConfig> call, Response<WheelConfig> response) {
                if (response.isSuccessful()) {
                    // 2. If event details changed, update event schedule
                    if (switchTimeRestricted.isChecked() || !etEventName.getText().toString().isEmpty()) {
                        updateEventSchedule();
                    } else {
                        Toast.makeText(AdminWheelConfigActivity.this, "Lưu cấu hình thành công", Toast.LENGTH_SHORT)
                                .show();
                        finish();
                    }
                } else {
                    Toast.makeText(AdminWheelConfigActivity.this, "Lỗi lưu cấu hình", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WheelConfig> call, Throwable t) {
                Toast.makeText(AdminWheelConfigActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEventSchedule() {
        String eventName = etEventName.getText().toString();
        Boolean isTimeRestricted = switchTimeRestricted.isChecked();

        api.updateEventSchedule(token, eventName, startDateStr, endDateStr, isTimeRestricted)
                .enqueue(new Callback<WheelConfig>() {
                    @Override
                    public void onResponse(Call<WheelConfig> call, Response<WheelConfig> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminWheelConfigActivity.this, "Lưu cấu hình & sự kiện thành công",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AdminWheelConfigActivity.this, "Lưu sự kiện thất bại: " + response.message(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WheelConfig> call, Throwable t) {
                        Toast.makeText(AdminWheelConfigActivity.this, "Lỗi lưu sự kiện", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
