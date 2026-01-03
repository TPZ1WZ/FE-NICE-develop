# 🎰 HƯỚNG DẪN SỬ DỤNG ADMIN LUCKY WHEEL

## 📱 Vào trang Quản lý Vòng quay may mắn

### Bước 1: Đăng nhập với tài khoản Admin
- Email: `tanphat123@example.com` (hoặc tài khoản admin khác)
- Vào **Profile** → Nhấn **Admin Dashboard**

### Bước 2: Mở menu Admin
- Trong Admin Dashboard, nhấn vào icon **Menu** (☰) ở góc trên bên trái

### Bước 3: Chọn "Vòng quay may mắn"
- Trong menu hiện ra, tìm mục **"Vòng quay may mắn"** (có icon màu xanh lá)
- Nhấn vào để mở trang quản lý

---

## 🎯 4 Chức năng chính đã được thêm vào menu

Khi vào trang **Vòng quay may mắn**, bạn sẽ thấy 4 thẻ chức năng:

### 1️⃣ **Quản lý phần thưởng** (icon voucher màu xanh dương)
- **Chức năng**: Thêm / sửa / xóa phần thưởng
- **Ví dụ**: 
  - Giảm 10%
  - Giảm 20%
  - Freeship
  - Điểm thưởng
  - Chúc bạn may mắn lần sau

### 2️⃣ **Cấu hình xác suất** (icon settings màu cam)
- **Chức năng**: Set tỷ lệ trúng cho từng phần thưởng
- **Ví dụ**:
  - Giảm 20%: 5%
  - Giảm 10%: 20%
  - Lose: 40%
  - Hoặc bật chế độ random mặc định

### 3️⃣ **Quản lý sự kiện** (icon calendar màu xanh lá)
- **Chức năng**:
  - Bật / tắt tính năng vòng quay
  - Đặt thời gian sự kiện:
    - Chỉ mở trong ngày lễ / event
    - Từ ngày A đến ngày B

### 4️⃣ **Quản lý lượt quay** (icon profile màu tím)
- **Chức năng**:
  - Xem ai đã quay
  - Bao nhiêu lần / ngày
  - Reset lượt quay cho user
  - Tặng thêm lượt quay cho user

---

## 📊 Thống kê hiển thị

Trên đầu trang sẽ hiển thị 4 thẻ thống kê:
- **Tổng lượt quay**: Số lượt quay đã thực hiện
- **User đã quay**: Số user đã tham gia
- **Phần thưởng**: Số phần thưởng hiện có (mặc định: 6)
- **Trạng thái**: Vòng quay đang hoạt động hay không

---

## 🔧 Backend API đã sẵn sàng

Tất cả API đã được triển khai đầy đủ tại backend:

- `GET /api/v1/admin/lucky-wheel/prizes` - Lấy danh sách phần thưởng
- `POST /api/v1/admin/lucky-wheel/prizes` - Tạo phần thưởng mới
- `PUT /api/v1/admin/lucky-wheel/prizes/{id}` - Cập nhật phần thưởng
- `DELETE /api/v1/admin/lucky-wheel/prizes/{id}` - Xóa phần thưởng
- `POST /api/v1/admin/lucky-wheel/prizes/batch-probability` - Cập nhật xác suất
- `GET /api/v1/admin/lucky-wheel/config` - Lấy cấu hình
- `PUT /api/v1/admin/lucky-wheel/config` - Cập nhật cấu hình
- `POST /api/v1/admin/lucky-wheel/toggle?active=true` - Bật/tắt vòng quay
- `POST /api/v1/admin/lucky-wheel/event-schedule` - Đặt thời gian sự kiện
- `GET /api/v1/admin/lucky-wheel/user-spins` - Xem danh sách user
- `POST /api/v1/admin/lucky-wheel/user-spins/{userId}/reset` - Reset lượt quay
- `POST /api/v1/admin/lucky-wheel/user-spins/{userId}/grant?bonusSpins=5` - Tặng lượt quay

Xem chi tiết tại: `BE-Nice-develop/ADMIN_LUCKY_WHEEL_COMPLETE.md`

---

## ✅ Trạng thái triển khai

- ✅ **Backend**: Hoàn thành 100% (API đã test thành công)
- ✅ **Menu Admin**: Đã thêm nút "Vòng quay may mắn" vào menu
- ✅ **Trang chính**: Trang quản lý với 4 thẻ chức năng
- ⏳ **4 chức năng con**: Đang phát triển (hiện tại hiển thị placeholder)

---

## 🚀 Các bước tiếp theo

1. **Kết nối API**: Tích hợp API backend vào 4 màn hình con
2. **UI chi tiết**: Thiết kế giao diện chi tiết cho từng chức năng
3. **Test**: Kiểm tra toàn bộ flow quản lý Lucky Wheel

---

## 📸 Screenshots

Khi build và chạy app, bạn sẽ thấy:

1. **Menu Admin** → Có thêm mục "Vòng quay may mắn" (màu xanh lá)
2. **Trang Lucky Wheel** → 4 thẻ với icon màu sắc khác nhau
3. **Thống kê** → 4 thẻ hiển thị số liệu ở trên cùng

---

**Lưu ý**: Cần build lại app Android để thấy các thay đổi mới!
