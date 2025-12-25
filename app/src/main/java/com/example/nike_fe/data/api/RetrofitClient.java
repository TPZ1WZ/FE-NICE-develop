package com.example.nike_fe.data.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * RetrofitClient - Singleton để kết nối với backend NICESTORE-develop
 * Backend URL: http://10.0.2.2:8080/ (Emulator -> localhost)
 */
public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private Context context;

    private RetrofitClient(Context context) {
        this.context = context.getApplicationContext();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public AuthApi getAuthApi() {
        return retrofit.create(AuthApi.class);
    }

    public ProductApi getProductApi() {
        return retrofit.create(ProductApi.class);
    }

    public CartApi getCartApi() {
        return retrofit.create(CartApi.class);
    }

    public UserApi getUserApi() {
        return retrofit.create(UserApi.class);
    }

    public AdminCouponApi getAdminCouponApi() {
        return retrofit.create(AdminCouponApi.class);
    }

    public UserCouponApi getUserCouponApi() {
        return retrofit.create(UserCouponApi.class);
    }

    public OrderApi getOrderApi() {
        return retrofit.create(OrderApi.class);
    }

    public AdminApi getAdminApi() {
        return retrofit.create(AdminApi.class);
    }

    public AdminProductApi getAdminProductApi() {
        return retrofit.create(AdminProductApi.class);
    }

    public FavoriteApi getFavoriteApi() {
        return retrofit.create(FavoriteApi.class);
    }

    public CategoryApi getCategoryApi() {
        return retrofit.create(CategoryApi.class);
    }

    public NotificationApi getNotificationApi() {
        return retrofit.create(NotificationApi.class);
    }

    public void saveToken(String token) {
        SharedPreferences prefs = context.getSharedPreferences("nike_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("access_token", token).apply();
    }

    public String getToken() {
        SharedPreferences prefs = context.getSharedPreferences("nike_prefs", Context.MODE_PRIVATE);
        return prefs.getString("access_token", null);
    }

    public void clearToken() {
        SharedPreferences prefs = context.getSharedPreferences("nike_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("access_token").apply();
    }

    public UserReviewApi getUserReviewApi() {
        return retrofit.create(UserReviewApi.class);
    }

    public UploadApi getUploadApi() {
        return retrofit.create(UploadApi.class);
    }
}
