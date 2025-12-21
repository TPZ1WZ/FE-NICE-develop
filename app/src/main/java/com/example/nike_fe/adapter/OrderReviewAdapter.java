package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.OrderItem;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class OrderReviewAdapter extends RecyclerView.Adapter<OrderReviewAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> orderItems;
    private OnReviewSubmitListener listener;

    public interface OnReviewSubmitListener {
        void onReviewSubmit(OrderItem item, int rating, String comment);

        void onAddImageClick(OrderItem item);
    }

    public OrderReviewAdapter(Context context, List<OrderItem> orderItems, OnReviewSubmitListener listener) {
        this.context = context;
        this.orderItems = orderItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductSize.setText("Size: " + item.getSize());

        String imageUrl = item.getFirstImage();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_placeholder_shoe)
                    .error(R.drawable.img_placeholder_shoe)
                    .centerCrop()
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.img_placeholder_shoe);
        }

        // Setup Image Adapter for Review Images
        if (item.getReviewImages() != null && !item.getReviewImages().isEmpty()) {
            android.util.Log.d("OrderReviewAdapter",
                    "Setting up image adapter with " + item.getReviewImages().size() + " images");
            ReviewImageAdapter imageAdapter = new ReviewImageAdapter(context, item.getReviewImages());
            holder.rvReviewImages.setAdapter(imageAdapter);
            holder.rvReviewImages.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("OrderReviewAdapter", "No images - hiding RecyclerView");
            holder.rvReviewImages.setAdapter(null);
            holder.rvReviewImages.setVisibility(View.GONE);
        }

        // Debug log
        android.util.Log.d("OrderReviewAdapter",
                "Product: " + item.getProductName() + ", isReviewed: " + item.isReviewed());

        // Tạm thời bỏ qua reviewed status để cho phép điền review
        // TODO: Fix backend để chỉ set reviewed=true khi review được approve
        holder.btnSubmitReview.setText("Gửi đánh giá");
        holder.btnSubmitReview.setEnabled(true);
        holder.btnSubmitReview.setBackgroundTintList(context.getColorStateList(R.color.black));
        holder.etComment.setEnabled(true);
        holder.etComment.setFocusable(true);
        holder.etComment.setFocusableInTouchMode(true);
        holder.etComment.setClickable(true);
        holder.ratingBar.setIsIndicator(false);
        holder.btnAddImage.setVisibility(View.VISIBLE);
        holder.btnAddImage.setClickable(true);

        // Text Persistence logic
        // Remove previous watcher if tag exists to avoid duplicates
        if (holder.etComment.getTag() instanceof android.text.TextWatcher) {
            holder.etComment.removeTextChangedListener((android.text.TextWatcher) holder.etComment.getTag());
        }

        // Set current text from draft
        holder.etComment.setText(item.getCommentDraft());

        // Create new watcher
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                item.setCommentDraft(s.toString());
            }
        };

        // Add and save watcher to tag
        holder.etComment.addTextChangedListener(watcher);
        holder.etComment.setTag(watcher);

        // Đảm bảo keyboard hiện lên khi click vào comment box
        holder.etComment.setOnClickListener(v -> {
            holder.etComment.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) context
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(holder.etComment, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });

        holder.btnAddImage.setOnClickListener(v -> {
            if (listener != null)
                listener.onAddImageClick(item);
        });

        holder.btnSubmitReview.setOnClickListener(v -> {
            int rating = (int) holder.ratingBar.getRating();
            String comment = holder.etComment.getText().toString();

            if (rating == 0) {
                Toast.makeText(context, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onReviewSubmit(item, rating, comment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage, btnAddImage;
        TextView tvProductName, tvProductSize;
        RatingBar ratingBar;
        TextInputEditText etComment;
        Button btnSubmitReview;
        RecyclerView rvReviewImages;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductSize = itemView.findViewById(R.id.tvProductSize);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            etComment = itemView.findViewById(R.id.etComment);
            btnSubmitReview = itemView.findViewById(R.id.btnSubmitReview);
            btnAddImage = itemView.findViewById(R.id.btnAddImage);
            rvReviewImages = itemView.findViewById(R.id.rvReviewImages);
            rvReviewImages.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        }
    }

    // Inner Adapter for Review Images
    public static class ReviewImageAdapter extends RecyclerView.Adapter<ReviewImageAdapter.ImageViewHolder> {
        private Context context;
        private List<String> images;

        public ReviewImageAdapter(Context context, List<String> images) {
            this.context = context;
            this.images = images;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(context);
            // Convert 80dp to px
            int sizePx = (int) android.util.TypedValue.applyDimension(
                    android.util.TypedValue.COMPLEX_UNIT_DIP,
                    80,
                    context.getResources().getDisplayMetrics());

            imageView.setLayoutParams(new ViewGroup.LayoutParams(sizePx, sizePx));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 0, 8, 0);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = images.get(position);
            android.util.Log.d("ReviewImageAdapter", "Loading image at position " + position + ": " + imageUrl);
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.img_placeholder_shoe)
                    .error(R.drawable.img_placeholder_shoe)
                    .into((ImageView) holder.itemView);
        }

        @Override
        public int getItemCount() {
            android.util.Log.d("ReviewImageAdapter", "getItemCount: " + images.size());
            return images.size();
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            public ImageViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}
