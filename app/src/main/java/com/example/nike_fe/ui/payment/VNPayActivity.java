package com.example.nike_fe.ui.payment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.nike_fe.R;
import com.example.nike_fe.MainActivity;

public class VNPayActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private String paymentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay);

        // Get payment URL from intent
        paymentUrl = getIntent().getStringExtra("PAYMENT_URL");

        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy link thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        android.util.Log.d("VNPayActivity", "Payment URL: " + paymentUrl);

        // ✅ Mở WebView để thanh toán trong app
        initViews();
        setupWebView();
        loadPaymentUrl();
    }

    private void initViews() {
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> onBackPressed());
        
        // Set background để debug
        webView.setBackgroundColor(android.graphics.Color.WHITE);
        android.util.Log.d("VNPayActivity", "Views initialized");
    }

    private void setupWebView() {
        android.util.Log.d("VNPayActivity", "Setting up WebView...");
        
        // Enable JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(false);

        // Fix cho VNPay
        webView.getSettings().setMixedContentMode(0); // Allow mixed content
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);

        // Enable rendering
        webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null);
        webView.setScrollBarStyle(android.view.View.SCROLLBARS_INSIDE_OVERLAY);

        // User Agent
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent + " NikeApp/1.0");

        android.util.Log.d("VNPayActivity", "WebView settings configured");

        // WebViewClient to handle URL loading
        // WebViewClient to handle URL loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url, view);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(url, view);
            }

            private boolean handleUrl(String url, WebView view) {
                android.util.Log.d("VNPayActivity", "shouldOverrideUrlLoading: " + url);

                // ✅ Check if this is callback URL (bắt callback trước khi load)
                if (url.contains("/api/v1/orders/vnpay/callback") || url.contains("vnp_ResponseCode")) {
                    android.util.Log.d("VNPayActivity", "Callback detected, handling payment");
                    handlePaymentCallback(url);
                    return true; // Chặn WebView load URL localhost
                }

                // ✅ Replace localhost with 10.0.2.2 for emulator
                if (url.contains("localhost:8080") || url.contains("127.0.0.1:8080")) {
                    String newUrl = url.replace("localhost:8080", "10.0.2.2:8080")
                                      .replace("127.0.0.1:8080", "10.0.2.2:8080");
                    android.util.Log.d("VNPayActivity", "Replaced localhost with 10.0.2.2: " + newUrl);
                    view.loadUrl(newUrl);
                    return true;
                }

                // Let WebView handle normal navigation
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                android.util.Log.d("VNPayActivity", "Page started: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                android.util.Log.d("VNPayActivity", "Page finished: " + url);
                
                // Force render by evaluating JavaScript
                view.evaluateJavascript(
                    "(function() { " +
                    "  document.body.style.visibility='visible'; " +
                    "  document.body.style.display='block'; " +
                    "  document.documentElement.style.visibility='visible'; " +
                    "  return document.body.innerHTML.length; " +
                    "})()",
                    value -> {
                        android.util.Log.d("VNPayActivity", "Page HTML length: " + value);
                        if ("0".equals(value) || value == null) {
                            android.util.Log.w("VNPayActivity", "Page appears empty!");
                            Toast.makeText(VNPayActivity.this, "Trang thanh toán trống. Đang tải lại...", Toast.LENGTH_SHORT).show();
                        }
                    }
                );
            }

            @Override
            public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler,
                    android.net.http.SslError error) {
                // Ignore SSL certificate errors (for sandbox/testing)
                handler.proceed();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                android.util.Log.e("VNPayActivity", "Error: " + description + " URL: " + failingUrl);
                // Don't show toast for every error to avoid spamming user
            }
        });

        // WebChromeClient for progress
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        // WebChromeClient for better rendering
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                android.util.Log.d("VNPayActivity", "Loading progress: " + newProgress + "%");
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("VNPayWebConsole", consoleMessage.message() +
                        " -- From line " + consoleMessage.lineNumber() +
                        " of " + consoleMessage.sourceId());
                return true;
            }
        });
    }

    private void loadPaymentUrl() {
        android.util.Log.d("VNPayActivity", "Loading payment URL: " + paymentUrl);
        
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        
        // Load URL in WebView
        webView.loadUrl(paymentUrl);
        
        android.util.Log.d("VNPayActivity", "WebView.loadUrl() called successfully");
    }

    private void handlePaymentCallback(String url) {
        android.util.Log.d("VNPayActivity", "Payment callback - URL: " + url);
        
        // ✅ GỌI BACKEND CALLBACK ĐỂ CẬP NHẬT ORDER
        // Thay thế 10.0.2.2 bằng 10.0.2.2 để emulator có thể gọi localhost
        String callbackUrl = url.replace("localhost:8080", "10.0.2.2:8080")
                               .replace("127.0.0.1:8080", "10.0.2.2:8080");
        
        android.util.Log.d("VNPayActivity", "Calling backend callback: " + callbackUrl);
        
        // Load callback URL để backend xử lý (update order status)
        webView.loadUrl(callbackUrl);
        
        // Parse callback URL to get payment result
        Uri uri = Uri.parse(url);
        String responseCode = uri.getQueryParameter("vnp_ResponseCode");
        String txnRef = uri.getQueryParameter("vnp_TxnRef");

        android.util.Log.d("VNPayActivity", "Payment callback - ResponseCode: " + responseCode + ", TxnRef: " + txnRef);

        // Delay một chút để backend kịp xử lý
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if ("00".equals(responseCode)) {
                // Payment successful - Hiển thị dialog thông báo chi tiết
                showPaymentSuccessDialog(txnRef);
            } else {
                // Payment failed
                String errorMessage = getPaymentErrorMessage(responseCode);
                showPaymentFailedDialog(errorMessage);
            }
        }, 1000); // Delay 1 giây để backend xử lý xong
    }

    private void showPaymentSuccessDialog(String txnRef) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("✓ Thanh toán thành công!")
                .setMessage("Đơn hàng #" + txnRef + " đã được thanh toán.\n\n" +
                        "Trạng thái đơn hàng:\n" +
                        "• Đang chờ xác nhận từ Admin\n" +
                        "• Sau khi xác nhận, đơn hàng sẽ được chuẩn bị\n" +
                        "• Bạn có thể theo dõi trong phần Đơn hàng\n\n" +
                        "Chú ý: Nếu bạn có nhu cầu hủy đơn hàng, vui lòng liên hệ với bộ phận hỗ trợ hoặc dùng chatbox để được giúp đỡ.\n\n" +
                        "Cảm ơn bạn đã mua hàng!")
                .setPositiveButton("Xem Đơn Hàng", (dialog, which) -> {
                    // Navigate to Order History
                    navigateToOrderHistory();
                })
                .setNegativeButton("Về Trang Chủ", (dialog, which) -> {
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private void showPaymentFailedDialog(String errorMessage) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("✗ Thanh toán thất bại")
                .setMessage(errorMessage + "\n\nVui lòng thử lại hoặc chọn phương thức thanh toán khác.")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    navigateToHome();
                })
                .setCancelable(false)
                .show();
    }

    private String getPaymentErrorMessage(String responseCode) {
        if (responseCode == null)
            return "Lỗi không xác định";

        switch (responseCode) {
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP).";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá giới hạn giao dịch trong ngày.";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì.";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định.";
            default:
                return "Giao dịch thất bại. Mã lỗi: " + responseCode;
        }
    }

    private void navigateToOrderHistory() {
        // Navigate to Order History (giả sử có activity này)
        try {
            Intent intent = new Intent(this, Class.forName("com.example.nike_fe.ui.order.OrderHistoryActivity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            // Nếu chưa có OrderHistoryActivity, về Home
            android.util.Log.w("VNPayActivity", "OrderHistoryActivity not found, navigating to Home");
            Toast.makeText(this, "Vui lòng kiểm tra đơn hàng trong menu Profile", Toast.LENGTH_LONG).show();
            navigateToHome();
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
