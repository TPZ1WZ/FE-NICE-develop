package com.example.nike_fe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike_fe.R;
import com.example.nike_fe.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private List<Product> originalProducts = new ArrayList<>();
    private Context context;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);

        void onAddClick(Product product);

        void onFavoriteClick(Product product);
    }

    private boolean isGridLayout = false;

    public HomeProductAdapter(Context context, OnProductClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Constructor matching MainActivity usage
    public HomeProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
        this.originalProducts = new ArrayList<>(products);
    }

    public void setProducts(List<Product> products) {
        this.products = products;
        this.originalProducts = new ArrayList<>(products);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            products = new ArrayList<>(originalProducts);
        } else {
            List<Product> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Product product : originalProducts) {
                if (product.getName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(product);
                }
            }
            products = filteredList;
        }
        notifyDataSetChanged();
    }

    public void useGridLayout(boolean useGrid) {
        this.isGridLayout = useGrid;
    }

    public void toggleFavorite(Long productId, boolean isFavorite) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(productId)) {
                products.get(i).setFavorite(isFavorite);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isGridLayout ? R.layout.item_product_grid : R.layout.item_home_product;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        if (products == null)
            return 0;
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvPrice;

        // Grid specific
        TextView tvRating;
        TextView tvSold;

        // List specific
        TextView tvBestSeller;
        ImageButton btnAdd;
        ImageView ivFavorite;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Common views
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);

            if (ivFavorite != null) {
                ivFavorite.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onFavoriteClick(products.get(position));
                    }
                });
            }

            // Layout specific views
            if (isGridLayout) {
                tvPrice = itemView.findViewById(R.id.tvProductPrice);
                // Grid specific (IDs from item_product_grid.xml)
            } else {
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvBestSeller = itemView.findViewById(R.id.tvBestSeller);
                btnAdd = itemView.findViewById(R.id.btnAdd);

                btnAdd.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onAddClick(products.get(position));
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
            });
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());

            // Handle Favorite Icon
            if (ivFavorite != null) {
                if (product.isFavorite()) {
                    ivFavorite.setImageResource(R.drawable.ic_heart); // Or filled version if available
                    ivFavorite.setColorFilter(android.graphics.Color.RED);
                } else {
                    ivFavorite.setImageResource(R.drawable.ic_heart_outline); // Or outline version
                    ivFavorite.setColorFilter(android.graphics.Color.WHITE); // Or default color
                }
            }

            // Format price
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = formatter.format(product.getPrice()) + " ₫";
            tvPrice.setText(formattedPrice);

            // Load image
            if (product.getThumbnail() != null && !product.getThumbnail().isEmpty()) {
                String imageUrl = product.getThumbnail();

                // Xử lý base64 image data
                if (imageUrl.startsWith("data:image")) {
                    // Base64 image - load trực tiếp
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder_shoe)
                            .error(R.drawable.img_placeholder_shoe)
                            .centerInside()
                            .into(ivProductImage);
                }
                // URL đầy đủ
                else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder_shoe)
                            .error(R.drawable.img_placeholder_shoe)
                            .centerInside()
                            .into(ivProductImage);
                }
                // Relative URL
                else if (imageUrl.startsWith("/")) {
                    imageUrl = "http://10.0.2.2:8080" + imageUrl;
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder_shoe)
                            .error(R.drawable.img_placeholder_shoe)
                            .centerInside()
                            .into(ivProductImage);
                } else {
                    // Fallback
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(R.drawable.img_placeholder_shoe)
                            .error(R.drawable.img_placeholder_shoe)
                            .centerInside()
                            .into(ivProductImage);
                }
            } else {
                ivProductImage.setImageResource(R.drawable.img_placeholder_shoe);
            }

            if (!isGridLayout && tvBestSeller != null) {
                tvBestSeller.setVisibility(View.VISIBLE);
            }
        }
    }
}
