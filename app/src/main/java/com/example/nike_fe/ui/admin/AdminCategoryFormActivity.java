package com.example.nike_fe.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Category;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryFormActivity extends AppCompatActivity {
    
    private EditText etName, etDescription;
    private Button btnSave;
    private ProgressBar progressBar;
    private ImageView ivBack;
    
    private AdminApi adminApi;
    private String token;
    
    private Long categoryId;
    private boolean isEditMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_category_form);
        
        initViews();
        loadCategoryData();
    }
    
    private void initViews() {
        etName = findViewById(R.id.etCategoryName);
        etDescription = findViewById(R.id.etCategoryDescription);
        btnSave = findViewById(R.id.btnSaveCategory);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        
        RetrofitClient retrofitClient = RetrofitClient.getInstance(this);
        adminApi = retrofitClient.getAdminApi();
        token = retrofitClient.getToken();
        
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        ivBack.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> saveCategory());
    }
    
    private void loadCategoryData() {
        categoryId = getIntent().getLongExtra("category_id", -1);
        
        if (categoryId != -1) {
            isEditMode = true;
            String name = getIntent().getStringExtra("category_name");
            String description = getIntent().getStringExtra("category_description");
            
            etName.setText(name);
            etDescription.setText(description);
            btnSave.setText("Cập nhật");
        } else {
            btnSave.setText("Thêm mới");
        }
    }
    
    private void saveCategory() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        
        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên danh mục");
            etName.requestFocus();
            return;
        }
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        if (isEditMode) {
            updateCategory(category);
        } else {
            createCategory(category);
        }
    }
    
    private void createCategory(Category category) {
        adminApi.createCategory("Bearer " + token, category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCategoryFormActivity.this, 
                            "Thêm danh mục thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminCategoryFormActivity.this, 
                            "Lỗi thêm danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AdminCategoryFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCategory(Category category) {
        adminApi.updateCategory("Bearer " + token, categoryId, category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                
                if (response.isSuccessful()) {
                    Toast.makeText(AdminCategoryFormActivity.this, 
                            "Cập nhật danh mục thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminCategoryFormActivity.this, 
                            "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Category> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AdminCategoryFormActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
