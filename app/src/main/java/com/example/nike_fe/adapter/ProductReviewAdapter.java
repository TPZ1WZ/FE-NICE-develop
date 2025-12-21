package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Review;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ViewHolder> {

    private Context context;
    private List<Review> reviews;

    public ProductReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);

        // Name
        String name = review.getUserName();
        if (name == null || name.isEmpty()) {
            name = "Người dùng ẩn danh"; // Anonymous
        } else {
            // Mask middle name ideally, but simple name for now
        }
        holder.tvUserName.setText(name);

        // Rating
        holder.tvRating.setText(String.valueOf(review.getRating()));

        // Date
        // Helper to format date if needed. Assuming ISO string from backend.
        // Simple display for now:
        if (review.getCreatedAt() != null) {
            try {
                // If it's full ISO 8601 like 2023-12-20T10:00:00, take first 10 chars
                String dateStr = review.getCreatedAt();
                if (dateStr.length() >= 10) {
                    holder.tvDate.setText(dateStr.substring(0, 10));
                } else {
                    holder.tvDate.setText(dateStr);
                }
            } catch (Exception e) {
                holder.tvDate.setText("");
            }
        }

        // Comment
        holder.tvComment.setText(review.getComment());

        // Title (Optional)
        if (review.getTitle() != null && !review.getTitle().isEmpty()) {
            holder.tvTitle.setText(review.getTitle());
            holder.tvTitle.setVisibility(View.VISIBLE);
        } else {
            holder.tvTitle.setVisibility(View.GONE);
        }

        // Images
        holder.llReviewImages.removeAllViews();
        List<String> images = review.getImages();
        if (images != null && !images.isEmpty()) {
            holder.llReviewImages.setVisibility(View.VISIBLE);
            for (String imageUrl : images) {
                addImageToLayout(imageUrl, holder.llReviewImages);
            }
        } else {
            holder.llReviewImages.setVisibility(View.GONE);
        }

        // Replies (Assuming simply checking first reply for now, or use Admin reply
        // logic if backend sends it)
        // Review model has `replies` list.
        if (review.getReplies() != null && !review.getReplies().isEmpty()) {
            Review.ReviewReply reply = review.getReplies().get(0); // Take first reply
            if (reply.getIsAdminReply()) {
                holder.llAdminReply.setVisibility(View.VISIBLE);
                holder.tvAdminReply.setText(reply.getComment());
            } else {
                holder.llAdminReply.setVisibility(View.GONE);
            }
        } else {
            holder.llAdminReply.setVisibility(View.GONE);
        }
    }

    private void addImageToLayout(String url, LinearLayout container) {
        ImageView iv = new ImageView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200); // px (approx 70dp) or use dp
                                                                                    // conversion
        params.setMargins(0, 0, 16, 0);
        iv.setLayoutParams(params);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundResource(R.drawable.bg_gray_rounded_small); // Assuming this drawable exists or similar

        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_image_placeholder) // Use placeholder
                .error(R.drawable.ic_error_placeholder)
                .into(iv);

        container.addView(iv);
    }

    @Override
    public int getItemCount() {
        return reviews == null ? 0 : reviews.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvRating, tvDate, tvTitle, tvComment, tvAdminReply;
        LinearLayout llReviewImages, llAdminReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvComment = itemView.findViewById(R.id.tvComment);
            llReviewImages = itemView.findViewById(R.id.llReviewImages);
            llAdminReply = itemView.findViewById(R.id.llAdminReply);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
        }
    }
}
