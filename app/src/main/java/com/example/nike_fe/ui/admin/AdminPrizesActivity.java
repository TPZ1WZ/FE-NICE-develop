package com.example.nike_fe.ui.admin;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.LuckyWheelApi;
import com.example.nike_fe.data.model.Prize;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPrizesActivity extends AppCompatActivity {

    private RecyclerView rvPrizes;
    private ExtendedFloatingActionButton fabAddPrize;
    private AdminPrizeAdapter adapter;
    private TextView tvTotalProbability;
    private TextView tvProbStatus;

    private LuckyWheelApi api;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_prizes);

        com.example.nike_fe.data.api.RetrofitClient retrofitClient = 
            com.example.nike_fe.data.api.RetrofitClient.getInstance(this);
        api = retrofitClient.getLuckyWheelApi();
        String savedToken = retrofitClient.getToken();
        if (savedToken != null) {
            token = "Bearer " + savedToken;
        }

        initViews();
        loadPrizes();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvPrizes = findViewById(R.id.rvPrizes);
        rvPrizes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminPrizeAdapter(new ArrayList<>(), this::showAddEditDialog, this::showDeleteConfirmDialog);
        rvPrizes.setAdapter(adapter);

        fabAddPrize = findViewById(R.id.fabAddPrize);
        fabAddPrize.setOnClickListener(v -> showAddEditDialog(null));

        tvTotalProbability = findViewById(R.id.tvTotalProbability);
        tvProbStatus = findViewById(R.id.tvProbStatus);
    }

    private void loadPrizes() {
        if (token == null)
            return;

        api.getAllPrizes(token).enqueue(new Callback<List<Prize>>() {
            @Override
            public void onResponse(Call<List<Prize>> call, Response<List<Prize>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Prize> prizes = response.body();
                    adapter.setPrizes(prizes);
                    updateProbabilitySummary(prizes);
                } else {
                    Toast.makeText(AdminPrizesActivity.this, "Lỗi tải danh sách phần thưởng", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<List<Prize>> call, Throwable t) {
                Toast.makeText(AdminPrizesActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProbabilitySummary(List<Prize> prizes) {
        double total = 0;
        for (Prize p : prizes) {
            if (p.getProbability() != null) {
                total += p.getProbability();
            }
        }

        // Format to 2 decimal places or integer if whole number
        String formattedTotal = (total % 1 == 0) ? String.format("%.0f", total * 100)
                : String.format("%.1f", total * 100);
        tvTotalProbability.setText(formattedTotal + "%");

        if (Math.abs(total - 1.0) < 0.001) {
            tvProbStatus.setText("Hợp lệ");
            tvProbStatus.setTextColor(Color.parseColor("#10B981")); // Green
            tvProbStatus.setBackgroundColor(Color.parseColor("#DCFCE7"));
        } else {
            tvProbStatus.setText("Chưa đạt 100%");
            tvProbStatus.setTextColor(Color.parseColor("#EF4444")); // Red
            tvProbStatus.setBackgroundColor(Color.parseColor("#FEE2E2"));
        }
    }

    private void showAddEditDialog(Prize prize) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_edit_prize);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
        EditText etName = dialog.findViewById(R.id.etName);
        AutoCompleteTextView spinnerType = dialog.findViewById(R.id.spinnerType);
        EditText etValue = dialog.findViewById(R.id.etValue);
        EditText etQuantity = dialog.findViewById(R.id.etQuantity);
        EditText etProbability = dialog.findViewById(R.id.etProbability);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Setup Spinner
        String[] types = new String[] { "VOUCHER", "FREESHIP", "POINTS", "NOTHING" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, types);
        spinnerType.setAdapter(adapter);

        if (prize != null) {
            tvTitle.setText("Chỉnh sửa phần thưởng");
            etName.setText(prize.getName());
            spinnerType.setText(prize.getType(), false);

            if (prize.getDiscountValue() != null)
                etValue.setText(String.valueOf(prize.getDiscountValue()));
            if (prize.getPointsValue() != null && etValue.getText().toString().isEmpty())
                etValue.setText(String.valueOf(prize.getPointsValue()));

            if (prize.getQuantity() != null)
                etQuantity.setText(String.valueOf(prize.getQuantity()));
            if (prize.getProbability() != null)
                etProbability.setText(String.valueOf(prize.getProbability()));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String type = spinnerType.getText().toString();
            String valStr = etValue.getText().toString();
            String qtyStr = etQuantity.getText().toString();
            String probStr = etProbability.getText().toString();

            if (name.isEmpty() || type.isEmpty() || qtyStr.isEmpty() || probStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Prize newPrize = new Prize();
            if (prize != null)
                newPrize.setId(prize.getId()); // Preserve ID if edit
            newPrize.setName(name);
            newPrize.setType(type);
            newPrize.setQuantity(Integer.parseInt(qtyStr));
            newPrize.setProbability(Double.parseDouble(probStr));

            if (!valStr.isEmpty()) {
                if ("POINTS".equals(type)) {
                    newPrize.setPointsValue(Integer.parseInt(valStr.split("\\.")[0]));
                } else {
                    newPrize.setDiscountValue(Double.parseDouble(valStr));
                }
            }

            // Set default icon based on type
            if (prize != null && prize.getIconUrl() != null) {
                newPrize.setIconUrl(prize.getIconUrl());
            }

            if (prize == null) {
                createPrize(newPrize, dialog);
            } else {
                updatePrize(newPrize, dialog);
            }
        });

        dialog.show();
    }

    private void createPrize(Prize prize, Dialog dialog) {
        api.createPrize(token, prize).enqueue(new Callback<Prize>() {
            @Override
            public void onResponse(Call<Prize> call, Response<Prize> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPrizesActivity.this, "Tạo thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadPrizes();
                } else {
                    Toast.makeText(AdminPrizesActivity.this, "Lỗi tạo: " + response.message(), Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<Prize> call, Throwable t) {
                Toast.makeText(AdminPrizesActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePrize(Prize prize, Dialog dialog) {
        api.updatePrize(token, prize.getId(), prize).enqueue(new Callback<Prize>() {
            @Override
            public void onResponse(Call<Prize> call, Response<Prize> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPrizesActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadPrizes();
                } else {
                    Toast.makeText(AdminPrizesActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Prize> call, Throwable t) {
                Toast.makeText(AdminPrizesActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmDialog(Prize prize) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa phần thưởng")
                .setMessage("Bạn có chắc muốn xóa '" + prize.getName() + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePrize(prize))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deletePrize(Prize prize) {
        api.deletePrize(token, prize.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminPrizesActivity.this, "Đã xóa phần thưởng", Toast.LENGTH_SHORT).show();
                    loadPrizes();
                } else {
                    Toast.makeText(AdminPrizesActivity.this, "Không thể xóa", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminPrizesActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
