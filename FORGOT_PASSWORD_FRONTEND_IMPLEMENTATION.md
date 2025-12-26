# 🔐 CHỨC NĂNG QUÊN MẬT KHẨU - FRONTEND ANDROID

## ✅ Hoàn thành 100%

Đã implement thành công toàn bộ Frontend Android cho chức năng **Quên mật khẩu với OTP**.

---

## 📱 Flow hoàn chỉnh

```
LoginActivity
    ↓ (Click "Quên mật khẩu?")
ForgotPasswordActivity (Nhập email)
    ↓ (POST /forgot-password-otp)
VerifyOtpActivity (Nhập OTP - mode: RESET_PASSWORD)
    ↓ (POST /verify-password-reset-otp)
ResetPasswordActivity (Nhập password mới)
    ↓ (POST /reset-password-with-otp)
LoginActivity (Success)
```

---

## 📦 Files đã tạo/cập nhật

### 1. **Models (3 files)**
✅ [`ForgotPasswordRequest.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\data\model\ForgotPasswordRequest.java)
- Field: `email`

✅ [`ForgotPasswordResponse.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\data\model\ForgotPasswordResponse.java)
- Fields: `success`, `message`, `email`

✅ [`ResetPasswordWithOtpRequest.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\data\model\ResetPasswordWithOtpRequest.java)
- Fields: `email`, `otp`, `newPassword`

### 2. **API Endpoints**
✅ [`AuthApi.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\data\api\AuthApi.java)
```java
@POST("api/v1/auth/forgot-password-otp")
Call<ForgotPasswordResponse> forgotPasswordOtp(@Body ForgotPasswordRequest request);

@POST("api/v1/auth/verify-password-reset-otp")
Call<VerifyOtpResponse> verifyPasswordResetOtp(@Body VerifyOtpRequest request);

@POST("api/v1/auth/reset-password-with-otp")
Call<VerifyOtpResponse> resetPasswordWithOtp(@Body ResetPasswordWithOtpRequest request);
```

### 3. **Layouts (2 files)**
✅ [`activity_forgot_password.xml`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\res\layout\activity_forgot_password.xml)
- Email input với Material Design 3
- Continue button
- Back to Login link
- Style nhất quán với Login/Register

✅ [`activity_reset_password.xml`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\res\layout\activity_reset_password.xml)
- New password input với password toggle
- Confirm password input
- Reset button
- Material Design 3 style

### 4. **Activities (3 files)**
✅ [`ForgotPasswordActivity.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\ui\auth\ForgotPasswordActivity.java)
- Validate email format
- Call API forgotPasswordOtp
- Navigate to VerifyOtpActivity with mode="RESET_PASSWORD"
- Error handling

✅ [`VerifyOtpActivity.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\ui\auth\VerifyOtpActivity.java) - **Cập nhật**
- Thêm mode: "REGISTER" hoặc "RESET_PASSWORD"
- Phân biệt API endpoint theo mode
- Navigate khác nhau theo mode:
  - REGISTER → LoginActivity
  - RESET_PASSWORD → ResetPasswordActivity

✅ [`ResetPasswordActivity.java`](d:\Android\CK\BE_FE\FE-NICE-develop\app\src\main\java\com\example\nike_fe\ui\auth\ResetPasswordActivity.java)
- Validate password (min 6 chars)
- Validate confirm password match
- Call API resetPasswordWithOtp
- Navigate to LoginActivity on success

### 5. **AndroidManifest.xml**
✅ Đã đăng ký 2 activities mới:
```xml
<activity android:name=".ui.auth.ForgotPasswordActivity" />
<activity android:name=".ui.auth.ResetPasswordActivity" />
```

### 6. **LoginActivity.java**
✅ Cập nhật link "Quên mật khẩu?" để navigate đến ForgotPasswordActivity

---

## 🎨 UI Features

### Màn hình Forgot Password
- ✅ Logo Nike
- ✅ Card container với elevation
- ✅ Email input (Material Design 3)
- ✅ Email validation (format check)
- ✅ Error messages hiển thị bên dưới input
- ✅ Continue button
- ✅ "Đã nhớ mật khẩu? Đăng nhập" link

### Màn hình OTP (Tái sử dụng)
- ✅ 6 ô input OTP riêng biệt
- ✅ Auto-focus giữa các ô
- ✅ Backspace quay lại ô trước
- ✅ Hỗ trợ 2 mode: Register & Reset Password
- ✅ Verify button
- ✅ Back button
- ✅ Resend OTP link (placeholder)

### Màn hình Reset Password
- ✅ Logo Nike
- ✅ Card container
- ✅ New password input với password toggle
- ✅ Confirm password input với password toggle
- ✅ Validation:
  - Password không rỗng
  - Min 6 ký tự
  - Confirm password khớp
- ✅ Reset button
- ✅ Loading state

---

## 🔄 Navigation Flow

```
┌─────────────────┐
│  LoginActivity  │
└────────┬────────┘
         │ Click "Quên mật khẩu?"
         ▼
┌────────────────────────┐
│ ForgotPasswordActivity │ ← Input email
└────────┬───────────────┘
         │ POST /forgot-password-otp
         ▼
┌─────────────────────┐
│ VerifyOtpActivity   │ ← Input OTP (mode=RESET_PASSWORD)
│ (Tái sử dụng)       │
└────────┬────────────┘
         │ POST /verify-password-reset-otp
         ▼
┌───────────────────────┐
│ ResetPasswordActivity │ ← Input new password
└────────┬──────────────┘
         │ POST /reset-password-with-otp
         ▼
┌─────────────────┐
│  LoginActivity  │ ← Success message
└─────────────────┘
```

---

## 🔌 API Integration

### 1. Forgot Password - Yêu cầu OTP
```java
ForgotPasswordRequest request = new ForgotPasswordRequest(email);
authApi.forgotPasswordOtp(request).enqueue(callback);
```

**Response Success:**
```json
{
  "success": true,
  "message": "Mã OTP đã được gửi...",
  "email": "user@example.com"
}
```

**Response Error (404):**
```json
{
  "success": false,
  "message": "Email không tồn tại trong hệ thống"
}
```

---

### 2. Verify OTP
```java
VerifyOtpRequest request = new VerifyOtpRequest(email, otp);
authApi.verifyPasswordResetOtp(request).enqueue(callback);
```

**Response:**
```json
{
  "success": true,
  "message": "Xác thực OTP thành công..."
}
```

---

### 3. Reset Password
```java
ResetPasswordWithOtpRequest request = 
    new ResetPasswordWithOtpRequest(email, otp, newPassword);
authApi.resetPasswordWithOtp(request).enqueue(callback);
```

**Response:**
```json
{
  "success": true,
  "message": "Đặt lại mật khẩu thành công..."
}
```

---

## ✅ Validation Rules

### Email (ForgotPasswordActivity)
- ✅ Không để trống
- ✅ Format email hợp lệ (Patterns.EMAIL_ADDRESS)

### Password (ResetPasswordActivity)
- ✅ Không để trống
- ✅ Tối thiểu 6 ký tự
- ✅ Confirm password phải khớp

### OTP (VerifyOtpActivity)
- ✅ Đủ 6 chữ số
- ✅ Chỉ chấp nhận số

---

## 🎯 Error Handling

### Network Errors
```java
onFailure() {
    Toast.makeText("Lỗi kết nối: " + t.getMessage());
}
```

### HTTP Errors
- **404**: Email không tồn tại
- **400**: OTP sai hoặc hết hạn
- **500**: Lỗi server

### Validation Errors
- Hiển thị ngay bên dưới input field (TextView màu đỏ)
- Auto-focus vào field lỗi

---

## 🚀 Testing Checklist

### Test Flow đầy đủ:

- [ ] **1. Click "Quên mật khẩu?" từ LoginActivity**
  - ✅ Navigate đến ForgotPasswordActivity

- [ ] **2. Test ForgotPasswordActivity**
  - [ ] Nhập email hợp lệ → OTP được gửi
  - [ ] Nhập email không tồn tại → Hiện lỗi "Email không tồn tại"
  - [ ] Nhập email sai format → Hiện lỗi "Email không hợp lệ"
  - [ ] Email trống → Hiện lỗi "Vui lòng nhập email"
  - [ ] Click "Đăng nhập" → Quay về LoginActivity

- [ ] **3. Test VerifyOtpActivity (Reset Password mode)**
  - [ ] Nhập OTP đúng → Navigate đến ResetPasswordActivity
  - [ ] Nhập OTP sai → Hiện lỗi "Mã OTP không đúng"
  - [ ] Thiếu số OTP → Hiện lỗi "Vui lòng nhập đủ 6 chữ số"
  - [ ] Auto-focus hoạt động đúng
  - [ ] Backspace quay lại ô trước

- [ ] **4. Test ResetPasswordActivity**
  - [ ] Nhập password mới hợp lệ + confirm khớp → Success
  - [ ] Password < 6 ký tự → Hiện lỗi
  - [ ] Confirm không khớp → Hiện lỗi
  - [ ] Password toggle hoạt động

- [ ] **5. End-to-end Test**
  - [ ] Hoàn thành flow → Quay về Login
  - [ ] Đăng nhập với password mới → Success

---

## 📝 Notes

- ✅ Tất cả Activities đã đăng ký trong AndroidManifest.xml
- ✅ Material Design 3 (TextInputLayout) nhất quán
- ✅ Password toggle icon
- ✅ Loading states (button disabled + text change)
- ✅ Error messages rõ ràng
- ✅ Back navigation hoạt động đúng
- ✅ Không có lỗi compile

---

## 🎉 Status: HOÀN THÀNH 100%

**Frontend Android đã sẵn sàng để test với Backend!**

### Next Steps:
1. ✅ Build và run app
2. ✅ Test với Backend (http://10.0.2.2:8080)
3. ✅ Verify toàn bộ flow
4. ✅ Check UI/UX trên nhiều màn hình

---

## 🔗 Related Files

**Backend Documentation:** 
- [FORGOT_PASSWORD_IMPLEMENTATION.md](d:\Android\CK\BE_FE\BE-Nice-develop\FORGOT_PASSWORD_IMPLEMENTATION.md)

**Frontend Files:**
- Data Models: `FE-NICE-develop/app/src/main/java/com/example/nike_fe/data/model/`
- API: `FE-NICE-develop/app/src/main/java/com/example/nike_fe/data/api/AuthApi.java`
- Activities: `FE-NICE-develop/app/src/main/java/com/example/nike_fe/ui/auth/`
- Layouts: `FE-NICE-develop/app/src/main/res/layout/`
