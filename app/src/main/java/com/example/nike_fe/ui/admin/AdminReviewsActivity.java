package com.example.nike_fe.ui.admin;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nike_fe.R;
import com.example.nike_fe.data.api.AdminApi;
import com.example.nike_fe.data.api.RetrofitClient;
import com.example.nike_fe.data.model.Review;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminReviewsActivity extends AppCompatActivity {

    private RecyclerView rvReviews;
    private ReviewsAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvStats;
    private Spinner spinnerStatus, spinnerRating;
    private Button btnRefresh;

    private AdminApi adminApi;
    private String token;
    private int currentPage = 0;
    private final int pageSize = 20;
    private String selectedStatus = "all";
    private Integer selectedRating = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reviews);

        initViews();
        setupToolbar();
        setupApi();
        loadStatistics();
        setupFilters();
        loadReviews();
    }

    private void initViews() {
        rvReviews = findViewById(R.id.rvReviews);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvStats = findViewById(R.id.tvStats);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        spinnerRating = findViewById(R.id.spinnerRating);
        btnRefresh = findViewById(R.id.btnRefresh);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReviewsAdapter(new ArrayList<>(), this::onReviewAction);
        rvReviews.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> {
            currentPage = 0;
            loadReviews();
            loadStatistics();
        });
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý Đánh giá");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupApi() {
        adminApi = RetrofitClient.getInstance(this).getAdminApi();
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        token = "Bearer " + prefs.getString("token", "");
    }

    private void setupFilters() {
        // Status filter
        String[] statuses = { "Tất cả", "Chờ duyệt", "Đã duyệt" };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedStatus = "all";
                        break;
                    case 1:
                        selectedStatus = "pending";
                        break;
                    case 2:
                        selectedStatus = "approved";
                        break;
                }
                currentPage = 0;
                loadReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Rating filter
        String[] ratings = { "Tất cả", "⭐⭐⭐⭐⭐ 5 sao", "⭐⭐⭐⭐ 4 sao", "⭐⭐⭐ 3 sao",
                "⭐⭐ 2 sao", "⭐ 1 sao" };
        ArrayAdapter<String> ratingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, ratings);
        ratingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(ratingAdapter);

        spinnerRating.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRating = position == 0 ? null : (6 - position);
                currentPage = 0;
                loadReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadStatistics() {
        adminApi.getReviewStatistics(token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> stats = response.body();
                    int total = ((Double) stats.get("totalReviews")).intValue();
                    int pending = ((Double) stats.get("pendingReviews")).intValue();
                    int approved = ((Double) stats.get("approvedReviews")).intValue();

                    tvStats.setText(String.format("Tổng: %d | Chờ duyệt: %d | Đã duyệt: %d",
                            total, pending, approved));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                tvStats.setText("Lỗi tải thống kê");
            }
        });
    }

    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        String status = selectedStatus.equals("all") ? null : selectedStatus;

        adminApi.getAllReviews(token, currentPage, pageSize, status, selectedRating, null)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Map<String, Object>> reviewsData = (List<Map<String, Object>>) response.body()
                                    .get("reviews");

                            List<Review> reviews = parseReviews(reviewsData);

                            if (reviews.isEmpty()) {
                                tvEmpty.setVisibility(View.VISIBLE);
                            } else {
                                adapter.updateReviews(reviews);
                            }
                        } else {
                            Toast.makeText(AdminReviewsActivity.this,
                                    "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                        Toast.makeText(AdminReviewsActivity.this,
                                "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Review> parseReviews(List<Map<String, Object>> data) {
        List<Review> reviews = new ArrayList<>();
        if (data != null) {
            for (Map<String, Object> item : data) {
                Review review = new Review();
                review.setId(((Double) item.get("id")).longValue());
                review.setProductName((String) item.get("productName"));
                review.setUserName((String) item.get("userName"));
                review.setRating(((Double) item.get("rating")).intValue());
                review.setComment((String) item.get("comment"));
                review.setTitle((String) item.get("title"));
                review.setCreatedAt((String) item.get("createdAt"));
                review.setApproved((Boolean) item.get("approved"));
                reviews.add(review);
            }
        }
        return reviews;
    }

    private void onReviewAction(Review review, String action) {
        switch (action) {
            case "approve":
                approveReview(review);
                break;
            case "reject":
                showRejectDialog(review);
                break;
            case "delete":
                showDeleteDialog(review);
                break;
            case "reply":
                showReplyDialog(review);
                break;
        }
    }

    private void approveReview(Review review) {
        adminApi.approveReview(token, review.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminReviewsActivity.this,
                            "Đã duyệt đánh giá", Toast.LENGTH_SHORT).show();
                    loadReviews();
                    loadStatistics();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(AdminReviewsActivity.this,
                        "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRejectDialog(Review review) {
        EditText input = new EditText(this);
        input.setHint("Lý do từ chối (tùy chọn)");

        new AlertDialog.Builder(this)
                .setTitle("Từ chối đánh giá")
                .setMessage("Bạn có chắc muốn từ chối đánh giá này?")
                .setView(input)
                .setPositiveButton("Từ chối", (dialog, which) -> {
                    Map<String, String> body = new HashMap<>();
                    body.put("reason", input.getText().toString());

                    adminApi.rejectReview(token, review.getId(), body)
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call,
                                        Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminReviewsActivity.this,
                                                "Đã từ chối đánh giá", Toast.LENGTH_SHORT).show();
                                        loadReviews();
                                        loadStatistics();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(AdminReviewsActivity.this,
                                            "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteDialog(Review review) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa đánh giá")
                .setMessage("Bạn có chắc muốn xóa đánh giá này vĩnh viễn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    adminApi.deleteReview(token, review.getId())
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call,
                                        Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminReviewsActivity.this,
                                                "Đã xóa đánh giá", Toast.LENGTH_SHORT).show();
                                        loadReviews();
                                        loadStatistics();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(AdminReviewsActivity.this,
                                            "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showReplyDialog(Review review) {
        EditText input = new EditText(this);
        input.setHint("Nhập phản hồi của shop...");
        input.setMinLines(3);

        new AlertDialog.Builder(this)
                .setTitle("Phản hồi đánh giá")
                .setView(input)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    String content = input.getText().toString().trim();
                    if (content.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, String> body = new HashMap<>();
                    body.put("content", content);

                    adminApi.replyToReview(token, review.getId(), body)
                            .enqueue(new Callback<Map<String, Object>>() {
                                @Override
                                public void onResponse(Call<Map<String, Object>> call,
                                        Response<Map<String, Object>> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(AdminReviewsActivity.this,
                                                "Đã gửi phản hồi", Toast.LENGTH_SHORT).show();
                                        loadReviews();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                                    Toast.makeText(AdminReviewsActivity.this,
                                            "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ReviewsAdapter inner class
    private static class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
        private List<Review> reviews;
        private final OnReviewActionListener listener;

        interface OnReviewActionListener {
            void onAction(Review review, String action);
        }

        public ReviewsAdapter(List<Review> reviews, OnReviewActionListener listener) {
            this.reviews = reviews;
            this.listener = listener;
        }

        public void updateReviews(List<Review> newReviews) {
            this.reviews = newReviews;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_admin_review, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Review review = reviews.get(position);
            holder.bind(review, listener);
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvProductName, tvUserName, tvRating, tvComment, tvDate, tvStatus;
            Button btnApprove, btnReject, btnDelete, btnReply;

            public ViewHolder(View itemView) {
                super(itemView);
                tvProductName = itemView.findViewById(R.id.tvProductName);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvRating = itemView.findViewById(R.id.tvRating);
                tvComment = itemView.findViewById(R.id.tvComment);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
                btnDelete = itemView.findViewById(R.id.btnDelete);
                btnReply = itemView.findViewById(R.id.btnReply);
            }

            void bind(Review review, OnReviewActionListener listener) {
                tvProductName.setText(review.getProductName());
                tvUserName.setText(review.getUserName());
                tvRating.setText("⭐".repeat(review.getRating()));
                tvComment.setText(review.getComment());
                tvDate.setText(review.getCreatedAt());

                if (review.getApproved()) {
                    tvStatus.setText("✓ Đã duyệt");
                    tvStatus.setTextColor(0xFF4CAF50);
                    btnApprove.setVisibility(View.GONE);
                    btnReject.setVisibility(View.GONE);
                } else {
                    tvStatus.setText("⏳ Chờ duyệt");
                    tvStatus.setTextColor(0xFFFFC107);
                    btnApprove.setVisibility(View.VISIBLE);
                    btnReject.setVisibility(View.VISIBLE);
                }

                btnApprove.setOnClickListener(v -> listener.onAction(review, "approve"));
                btnReject.setOnClickListener(v -> listener.onAction(review, "reject"));
                btnDelete.setOnClickListener(v -> listener.onAction(review, "delete"));
                btnReply.setOnClickListener(v -> listener.onAction(review, "reply"));
            }
        }
    }
}
