package com.example.nike_fe.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Response khi xóa sản phẩm
 */
public class DeleteProductResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("details")
    private Details details;
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Details getDetails() {
        return details;
    }
    
    public static class Details {
        @SerializedName("cartItemsRemoved")
        private int cartItemsRemoved;
        
        @SerializedName("favoritesRemoved")
        private int favoritesRemoved;
        
        @SerializedName("softDelete")
        private boolean softDelete;
        
        public int getCartItemsRemoved() {
            return cartItemsRemoved;
        }
        
        public int getFavoritesRemoved() {
            return favoritesRemoved;
        }
        
        public boolean isSoftDelete() {
            return softDelete;
        }
    }
}
