# Business Analysis & System Flow

## 1. Mô tả hệ thống (System Overview)
Hệ thống xây dựng cấu trúc mô phỏng toàn bộ quy trình hoạt động của một mạng lưới giao đồ ăn trực tuyến (Food Delivery Network Simulation). Hệ thống vận hành trên môi trường đa luồng (Concurrency) với hàng trăm luồng Khách hàng (Customer Threads) và luồng Tài xế (Driver Threads) hoạt động đồng thời.

Các đặc trưng ràng buộc cốt lõi bao gồm:
- **Giới hạn lưu trữ:** Toàn bộ dữ liệu được lưu trữ thuần túy dưới dạng các file vật lý CSV với tổng quy mô toàn hệ thống đạt cấu hình ≥ 18.000 dòng. Không sử dụng bất kỳ hệ quản trị cơ sở dữ liệu hay bộ nhớ đệm in-memory nào để duy trì tính nhất quán.
- **Mục tiêu nghiên cứu (Big Question):** Khảo sát thực nghiệm hiệu năng của 4 cơ chế đồng bộ hóa dữ liệu (Locking Mechanisms) nhằm tìm ra giải pháp tối ưu giúp triệt tiêu hoàn toàn 100% lỗi xung đột dữ liệu (0% error rate) nhưng không làm sụt giảm tốc độ xử lý hệ thống (Throughput) quá 30% so với điều kiện không khóa (NO_LOCK baseline).

---

## 2. Luồng Nghiệp Vụ Chuẩn (Standard Business Flow)
Quy trình xử lý một đơn hàng trong hệ thống diễn ra tuần tự qua các giai đoạn sau:

1. **Khách hàng đặt món (Place Order):** Khách hàng duyệt thực đơn từ một nhà hàng cụ thể và tiến hành tạo đơn hàng gồm nhiều món ăn chi tiết.
2. **Kiểm tra tồn kho (Stock Validation):** Hệ thống kiểm tra số lượng tồn kho khả dụng (`stockQty`) của từng món ăn tại nhà hàng. Nếu đủ điều kiện, đơn hàng được khởi tạo thành công với trạng thái ban đầu là `PENDING`.
3. **Tính toán địa lý (Geo-spatial Calculation):** Hệ thống kích hoạt công cụ tiện ích `GeoUtils` sử dụng công thức Haversine để tính toán khoảng cách địa lý chính xác giữa vị trí nhà hàng, khách hàng và các tài xế.
4. **Khởi tạo Lộ trình (Delivery Route Generation):** Dựa trên khoảng cách đã tính toán, một thực thể `DeliveryRoute` được tạo ra để lưu vết thông tin cấu trúc quãng đường và thời gian giao hàng dự kiến của đơn hàng đó.
5. **Điều phối tài xế (Automatic Dispatch):** Bộ điều phối (`Dispatcher`) tự động tìm kiếm tài xế đang ở trạng thái sẵn sàng (`AVAILABLE`) nằm trong phạm vi khoảng cách gần nhất để gán đơn hàng.
6. **Nhận đơn & Giao hàng (Delivery Process):** Khi tài xế được gán đơn, trạng thái của tài xế lập tức chuyển sang `BUSY` để hệ thống không gán thêm đơn khác. Tài xế tiến hành di chuyển, cập nhật tiến độ giao hàng và hoàn tất đơn hàng (`DELIVERED`).

*Lưu ý về quyền hủy đơn:* Khách hàng có quyền hủy đơn khi đơn hàng vẫn ở trạng thái `PENDING`. Một khi đơn hàng đã gán cho tài xế thành công, đơn hàng không thể bị hủy để bảo toàn tính nhất quán nghiệp vụ.

---

## 3. Giải Thích Chi Tiết 3 Loại Race Condition Nguy Hiểm Nhất
Dưới điều kiện tài nguyên lưu trữ bị giới hạn ở file vật lý CSV, việc đọc/ghi bất đồng bộ từ hàng trăm tiến trình chạy đồng thời sẽ dẫn đến 3 rủi ro xung đột dữ liệu nghiêm trọng sau:

### Race Condition 1 — Đơn hàng bị nhận bởi 2 tài xế (Double Assignment)
- **Kịch bản xảy ra:** Hai tiến trình tài xế khác nhau (`Driver Thread-A` và `Driver Thread-B`) cùng thực hiện đọc file `orders.csv` tại một thời điểm. Cả hai đều đọc ra thông tin trạng thái đơn hàng cụ thể là `PENDING`. Do chưa có cơ chế khóa, cả hai tiến trình đều xác nhận đơn này trống, cùng ghi đè ID của mình vào trường `driverId` và lưu lại file.
- **Hậu quả hệ thống:** Một đơn hàng duy nhất bị gán đồng thời cho hai tài xế khác nhau. Gây ra lỗi trùng lặp vận chuyển, lãng phí tài nguyên của tài xế và phá vỡ ràng buộc logic cốt lõi của hệ thống (Một đơn hàng chỉ được gán cho đúng 1 tài xế).

### Race Condition 2 — Đặt hàng khi món ăn đã hết (Oversell MenuItem)
- **Kịch bản xảy ra:** Một món ăn phổ biến trong file `menu_items.csv` hiện tại chỉ còn số lượng tồn kho `stockQty = 2`. Hai luồng khách hàng khác nhau (`Customer Thread-1` và `Customer Thread-2`) cùng gửi yêu cầu đặt món ăn này tại cùng một mili-giây. Cả hai luồng cùng đọc ra số lượng tồn kho là 2, thấy hợp lệ nên cùng thực hiện trừ kho và xác nhận tạo đơn.
- **Hậu quả hệ thống:** Nhà hàng tiếp nhận cả 2 đơn hàng nhưng số lượng thực tế trong kho sau khi cập nhật bị đẩy về giá trị âm (`stockQty = -2`). Hệ thống buộc phải hủy đơn sau khi khách hàng đã thanh toán tiền, làm sụt giảm nghiêm trọng trải nghiệm người dùng.

### Race Condition 3 — Tài xế bị gán nhiều đơn cùng lúc (Driver Overload)
- **Kịch bản xảy ra:** Tài xế mang mã định danh `D005` đang ở trạng thái sẵn sàng (`status = AVAILABLE`). Hệ thống điều phối kích hoạt đa luồng xử lý đơn hàng: `Thread-X` xử lý đơn `0100` và `Thread-Y` xử lý đơn `0101`. Cả hai luồng xử lý độc lập này cùng thực hiện quét file `drivers.csv` và cùng đọc ra trạng thái của tài xế `D005` là `AVAILABLE` trước khi có bất kỳ luồng nào kịp ghi đè trạng thái bận.
- **Hậu quả hệ thống:** Tài xế `D005` bị gán đồng thời 2 đơn hàng tại cùng một thời điểm, vi phạm ràng buộc nghiệp vụ (Một tài xế chỉ xử lý tối đa 1 đơn hàng tại một thời điểm, khi bận không được gán thêm).