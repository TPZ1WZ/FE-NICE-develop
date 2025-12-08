# 🏃 Nike FE - Android Login App

## ✅ Đã Hoàn Thành

### 📦 Files Đã Tạo:

```
fe-nike/
├── app/src/main/
│   ├── java/com/example/nike_fe/
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── AuthApi.java          ✅ API interface
│   │   │   │   └── RetrofitClient.java   ✅ Singleton client
│   │   │   └── model/
│   │   │       ├── LoginRequest.java     ✅ Request model
│   │   │       └── LoginResponse.java    ✅ Response model
│   │   └── ui/auth/
│   │       └── LoginActivity.java        ✅ Login screen
│   └── res/layout/
│       └── activity_login.xml            ✅ Login UI
└── build.gradle.kts                      ✅ Dependencies added
```

---

## 🎯 API Backend

**Endpoint**: `POST http://10.0.2.2:8080/api/v1/auth/login`

**Request**:
```json
{
  "username": "user@example.com",
  "password": "123456789"
}
```

**Response**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

---

## 🚀 Cách Chạy

### 1. Khởi động Backend

```bash
cd NICESTORE-develop
mvn spring-boot:run
```

Backend chạy tại: `http://localhost:8080`

### 2. Build Android App

1. Mở Android Studio
2. File → Open → Chọn thư mục `fe-nike`
3. Đợi Gradle Sync hoàn tất
4. Click "Run" (Ctrl+R hoặc Shift+F10)

### 3. Test Login

- Email: `user@example.com`
- Password: `123456789`

---

## 📱 Features

### ✅ UI Components (giống React)

- ✅ NIKE Logo
- ✅ Card Container với shadow
- ✅ Email input với validation
- ✅ Password input với validation
- ✅ Remember Me checkbox
- ✅ Forgot Password link
- ✅ Login button với loading state
- ✅ Google Sign-In button (demo)
- ✅ Register link

### ✅ Validation

- Email không được để trống
- Email phải hợp lệ (format check)
- Password không được để trống
- Password tối thiểu 6 ký tự

### ✅ Error Handling

- ❌ 401: "Email hoặc mật khẩu không chính xác"
- ❌ 500: "Lỗi server. Vui lòng thử lại sau"
- ❌ Network: "Không thể kết nối đến server"

### ✅ Success Flow

1. Nhập email + password
2. Click "ĐĂNG NHẬP"
3. Loading: "Đang đăng nhập..."
4. Success → Lưu token
5. Toast: "Chào mừng trở lại!"
6. Navigate → MainActivity

---

## 🔧 Dependencies Đã Thêm

```gradle
// Retrofit & OkHttp
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

// Gson
implementation("com.google.code.gson:gson:2.10.1")

// CardView
implementation("androidx.cardview:cardview:1.0.0")
```

---

## 🐛 Troubleshooting

### ❌ Connection Refused

**Kiểm tra**:
1. Backend đang chạy? → `curl http://localhost:8080`
2. URL đúng? → Emulator dùng `10.0.2.2`, thiết bị thật dùng IP máy tính

### ❌ Cleartext Traffic Not Permitted

**Đã fix** trong AndroidManifest.xml:
```xml
android:usesCleartextTraffic="true"
```

### ❌ 401 Unauthorized

**Nguyên nhân**: Email hoặc password sai  
**Kiểm tra**: Test trong Postman trước

---

## 📝 Code Highlights

### LoginActivity.java

```java
// Gửi request login
LoginRequest request = new LoginRequest(email, password);
authApi.login(request).enqueue(new Callback<LoginResponse>() {
    @Override
    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            // Lưu token
            retrofitClient.saveToken(response.body().getAccessToken());
            
            // Chuyển sang MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
    
    @Override
    public void onFailure(Call<LoginResponse> call, Throwable t) {
        Toast.makeText(this, "Lỗi kết nối", Toast.LENGTH_LONG).show();
    }
});
```

### RetrofitClient.java

```java
private static final String BASE_URL = "http://10.0.2.2:8080/";

public AuthApi getAuthApi() {
    return retrofit.create(AuthApi.class);
}

public void saveToken(String token) {
    SharedPreferences prefs = context.getSharedPreferences("nike_prefs", MODE_PRIVATE);
    prefs.edit().putString("access_token", token).apply();
}
```

---

## ✅ So Sánh React vs Android

| Feature | React (LoginForm.tsx) | Android (LoginActivity) |
|---------|----------------------|------------------------|
| Validation | Zod schema | Patterns.EMAIL_ADDRESS |
| Loading State | isSubmitting | setLoading(true/false) |
| Error Display | <p className="error"> | TextView.setVisibility() |
| Navigation | navigate('/web') | startActivity(Intent) |
| Toast | toast.success() | Toast.makeText() |
| API Call | authApi.login() | authApi.login().enqueue() |

---

## 🎉 Hoàn Thành

- [x] Phân tích React LoginForm.tsx
- [x] Tạo activity_login.xml (UI giống React)
- [x] Tạo LoginActivity.java (logic giống React)
- [x] Tạo AuthApi.java (kết nối backend)
- [x] Tạo RetrofitClient.java (Retrofit setup)
- [x] Tạo Model classes (LoginRequest, LoginResponse)
- [x] Thêm dependencies (Retrofit, Gson)
- [x] Cập nhật AndroidManifest.xml
- [x] Không dùng mockdata
- [x] Validation đầy đủ
- [x] Error handling

**Chúc bạn test thành công! 🚀**
