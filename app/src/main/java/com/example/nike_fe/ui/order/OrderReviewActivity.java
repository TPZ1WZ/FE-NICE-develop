package com.example.nike_fe.ui.order;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nike_fe.R;
import com.example.nike_fe.adapter.OrderReviewAdapter;
import com.example.nike_fe.data.api.OrderApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.api.UploadApi;
import com.example.nike_fe.data.api.UserReviewApi;
import com.example.nike_fe.data.model.CreateReviewRequest;
import com.example.nike_fe.data.model.Order;
import com.example.nike_fe.data.model.OrderItem;
import com.example.nike_fe.data.model.Review;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderReviewActivity extends AppCompatActivity implements OrderReviewAdapter.OnReviewSubmitListener {

    private static final String TAG = "OrderReviewActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivBack;
    private RecyclerView rvProducts;
    private OrderReviewAdapter adapter;
    private List<OrderItem> orderItems = new ArrayList<>();

    private OrderApi orderApi;
    private UserReviewApi userReviewApi;
    private UploadApi uploadApi;
    private String token;
    private Long orderId;

    private OrderItem currentItemForImage; // To track which item is adding image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_review);

        orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupApi();
        loadOrderDetails();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        rvProducts = findViewById(R.id.rvProducts);

        ivBack.setOnClickListener(v -> finish());

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderReviewAdapter(this, orderItems, this);
        rvProducts.setAdapter(adapter);
    }

    private void setupApi() {
        RetrofitClient client = RetrofitClient.getInstance(this);
        orderApi = client.getOrderApi();
        userReviewApi = client.getUserReviewApi();
        uploadApi = client.getUploadApi();
        token = client.getToken();

        if (token != null && !token.startsWith("Bearer ")) {
            token = "Bearer " + token;
        }
    }

    @Override
    public void onAddImageClick(OrderItem item) {
        currentItemForImage = item;
        Log.d(TAG, "onAddImageClick: User clicked add image button for product " + item.getProductName());
        checkPermissionAndOpenGallery();
    }

    private void checkPermissionAndOpenGallery() {
        Log.d(TAG, "checkPermissionAndOpenGallery: Checking permissions...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_MEDIA_IMAGES permission not granted, requesting...");
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_MEDIA_IMAGES }, 100);
            } else {
                Log.d(TAG, "READ_MEDIA_IMAGES permission already granted");
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ_EXTERNAL_STORAGE permission not granted, requesting...");
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                        100);
            } else {
                Log.d(TAG, "READ_EXTERNAL_STORAGE permission already granted");
                openGallery();
            }
        }
    }

    private void openGallery() {
        Log.d(TAG, "openGallery: Opening image picker...");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions,
            @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Log.d(TAG, "Image selected: " + imageUri.toString());
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        Log.d(TAG, "uploadImage: Starting upload for " + imageUri.toString());
        try {
            // Create temp file
            File file = new File(getCacheDir(), "upload_" + System.currentTimeMillis() + ".jpg");
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // Create multipart body
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

            Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

            uploadApi.uploadImage(body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body();
                        Log.d(TAG, "Upload successful! Image URL: " + imageUrl);
                        // Add to current item
                        if (currentItemForImage != null) {
                            if (currentItemForImage.getReviewImages() == null) {
                                currentItemForImage.setReviewImages(new ArrayList<>());
                                Log.d(TAG, "Created new ArrayList for review images");
                            }
                            currentItemForImage.getReviewImages().add(imageUrl);
                            Log.d(TAG, "Added image to list. Total images: " + currentItemForImage.getReviewImages().size());
                            
                            // Find position of current item and notify adapter
                            int position = orderItems.indexOf(currentItemForImage);
                            if (position != -1) {
                                Log.d(TAG, "Notifying adapter at position: " + position);
                                adapter.notifyItemChanged(position);
                            } else {
                                Log.w(TAG, "Could not find item position, calling notifyDataSetChanged");
                                adapter.notifyDataSetChanged();
                            }
                            
                            Toast.makeText(OrderReviewActivity.this, "Tải ảnh thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "currentItemForImage is null!");
                        }
                    } else {
                        Log.e(TAG, "Upload failed. Response code: " + response.code());
                        Toast.makeText(OrderReviewActivity.this, "Tải ảnh thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(OrderReviewActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderDetails() {
        orderApi.getOrderById(token, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    orderItems.clear();
                    if (order.getItems() != null) {
                        orderItems.addAll(order.getItems());
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(OrderReviewActivity.this, "Failed to load order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(OrderReviewActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewSubmit(OrderItem item, int rating, String comment) {
        Log.d(TAG, "onReviewSubmit: Product=" + item.getProductName() + ", Rating=" + rating + ", Comment length=" + comment.length());
        Log.d(TAG, "Review images count: " + (item.getReviewImages() != null ? item.getReviewImages().size() : 0));
        
        CreateReviewRequest request = new CreateReviewRequest(
                item.getProductId(),
                orderId,
                rating,
                comment,
                "Review for " + item.getProductName(),
                item.getReviewImages() // Pass the uploaded images
        );

        userReviewApi.createReview(token, request).enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                Log.d(TAG, "Review submit response code: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(OrderReviewActivity.this, "Đánh giá thành công! Đang chờ duyệt.", Toast.LENGTH_LONG)
                            .show();
                    item.setReviewed(true);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Review submit failed: " + response.message());
                    Toast.makeText(OrderReviewActivity.this, "Lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                Log.e(TAG, "Review submit failed", t);
                Toast.makeText(OrderReviewActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
