# Architecture Design & Design Patterns

## 1. Phân Tầng Kiến Trúc Hệ Thống (MVC Layering)
[cite_start]Dự án áp dụng thiết kế kiến trúc Model-View-Controller (MVC) bắt buộc phối hợp chặt chẽ với mẫu kiến trúc Repository nhằm đạt được tiêu chí phân rã trách nhiệm rõ ràng (Single Responsibility Principle - SRP)[cite: 2, 9, 55, 92]:

+─────────────────────────────────────────────────────────────+
|                          VIEW LAYER                         |
| (MainView, OrderView, MapView, SimulatorView)               |
+──────────────────────────────┬──────────────────────────────+
│ (Gọi Controller)
▼
+─────────────────────────────────────────────────────────────+
|                       CONTROLLER LAYER                      |
| (OrderController, DriverController, SimulatorController...) |
+──────────────────────────────┬──────────────────────────────+
│ (Điều phối Flow)
▼
+─────────────────────────────────────────────────────────────+
|                       REPOSITORY LAYER                      |
| (CsvRepository, OrderRepository, DriverRepository...)    |
+──────────────────────────────┬──────────────────────────────+
│ (Thao tác dữ liệu Entities)
▼
+─────────────────────────────────────────────────────────────+
|                         MODEL LAYER                         |
| (8 Entities: Customer, Driver, Order, DeliveryRoute...)     |
+─────────────────────────────────────────────────────────────+

### Quy định trách nhiệm nghiêm ngặt giữa các tầng (Rubric Compliance):
- **Tầng View:** Chịu trách nhiệm in dữ liệu hiển thị (Menu giao diện, bảng biểu ASCII, bản đồ lưới mô phỏng) và tiếp nhận thông tin nhập vào từ bàn phím của người dùng[cite: 58]. View gọi xuống Controller để xử lý sự kiện[cite: 58]. *Tuyệt đối nghiêm cấm View đọc/ghi file CSV hoặc tham gia vào các tính toán khoảng cách[cite: 57, 58].*
- **Tầng Controller:** Chỉ đóng vai trò điều phối dòng chảy của ứng dụng (Flow Control)[cite: 56, 58]. Tiếp nhận các yêu cầu điều hướng từ View, kiểm tra tham số thô và chuyển giao tác vụ xuống tầng Repository tương ứng[cite: 58]. *Tuyệt đối nghiêm cấm Controller chứa logic điều phối tài xế hoặc trực tiếp thao tác ghi dữ liệu xuống file CSV[cite: 58, 101].*
- **Tầng Repository:** Nơi tập trung toàn bộ logic nghiệp vụ cốt lõi của hệ thống (Business Logic)[cite: 56]. Toàn bộ các thuật toán tìm kiếm tài xế rảnh gần nhất (`findNearestAvailable`), tính toán định tuyến khoảng cách, validate kiểm tra số lượng kho và các giải pháp giải quyết tranh chấp luồng đều bắt buộc phải nằm tại tầng này[cite: 56, 58, 92].
- **Tầng Model:** Định nghĩa cấu trúc dữ liệu của các đối tượng hệ thống, bao gồm cấu trúc phân cấp kế thừa từ `BaseEntity` và triển khai cơ chế Serialize/Deserialize dòng dữ liệu CSV (`toCsvLine` / `fromCsvLine`)[cite: 81, 92].

---

## 2. Thiết Kế Hướng Đối Tượng (OOP Optimization)
Hệ thống thiết kế cấu trúc lớp đảm bảo tính trừu tượng và tái sử dụng cao, đáp ứng các tiêu chuẩn đánh giá nâng cao[cite: 9, 92]:

### Mô hình hóa 8 Thực thể dữ liệu (Model Entities):
Thiết kế phân rã hệ thống thành đúng 8 thực thể dữ liệu độc lập, ánh xạ tương ứng ra 8 file cấu trúc CSV đảm bảo tính chuẩn hóa thông tin[cite: 49, 92]:
1.  `Customer`: Quản lý thông tin tài khoản người dùng[cite: 49].
2.  `Restaurant`: Quản lý thông tin điểm bán, nhà hàng đối tác[cite: 49].
3.  `MenuItem`: Quản lý danh mục món ăn và số lượng tồn kho khả dụng[cite: 49].
4.  `Driver`: Quản lý thông tin định danh và trạng thái hoạt động của tài xế[cite: 49].
5.  `Order`: Lưu trữ trạng thái tiến trình xử lý đơn hàng[cite: 49].
6.  `OrderItem`: Lưu trữ chi tiết số lượng món ăn riêng biệt trong từng đơn[cite: 49].
7.  `DeliveryRoute`: Quản lý chi tiết tính toán tọa độ, lưu trữ dữ liệu quãng đường di chuyển và thời gian vận chuyển ước tính của đơn hàng[cite: 49, 92]. Việc tách riêng thực thể này giúp thực hiện đúng mẫu thiết kế đơn nhiệm (SRP), tránh làm phình to dữ liệu của lớp `Order`[cite: 92].
8.  `SimulationRun`: Ghi nhận dữ liệu thực nghiệm sau mỗi lượt chạy của công cụ mô phỏng Simulator Tool (lưu trữ loại cơ chế khóa, tổng số lượng đơn chạy thử, tỷ lệ phát sinh lỗi của từng loại, và tổng Throughput đạt được)[cite: 49, 81, 92]. Dữ liệu từ thực thể này xuất ra file `simulation_runs.csv` là bằng chứng khoa học để chứng minh cho câu hỏi nghiên cứu của đề tài[cite: 32, 49].

### Kiến trúc mẫu thiết kế Generic Repository:
Lớp nền tảng `CsvRepository<T>` đóng vai trò là một lớp trừu tượng tổng quát hóa (Generic Class) cung cấp các phương thức thao tác tệp cơ bản mẫu như `readAll()`, `save()`, `update()`, và `delete()`[cite: 81, 92]. Toàn bộ 7 lớp Repository cụ thể của các thực thể (`CustomerRepository`, `DriverRepository`, `OrderRepository`...) đều kế thừa từ `CsvRepository<T>` và thực hiện ghi đè phương thức định dạng dòng dữ liệu tương ứng, tối ưu hóa tính đóng gói và kế thừa trong OOP[cite: 92].

---

## 3. Giải Pháp Kỹ Thuật Ngăn Chặn 3 Loại Race Condition
Hệ thống cung cấp các phương thức đặc hiệu tại tầng Repository để cài đặt và so sánh thực nghiệm các cơ chế đồng bộ hóa luồng[cite: 34, 86]:

- **Cơ chế Khóa Đồng Bộ Thuần Túy (Pessimistic Sync Locking):**
  Triển khai thông qua các phương thức `markBusyWithSync()` trong `DriverRepository` và `deductStockWithSync()` trong `MenuItemRepository`. Sử dụng từ khóa `synchronized` khoanh vùng phạm vi tài nguyên dùng chung (Per-resource keys) tại thời điểm đọc và cập nhật file CSV. Đảm bảo tại một thời điểm, chỉ có duy nhất một tiến trình được phép can thiệp vào dữ liệu tồn kho của món ăn hoặc trạng thái bận của tài xế, cô lập hoàn toàn lỗi `Oversell` và `Driver Overload`[cite: 81, 92].
  
- **Cơ chế Khóa Lạc Quan (Optimistic Locking):**
  Triển khai thông qua phương thức `assignDriverWithOptimistic()` trong `OrderRepository`. Giải pháp này dựa trên nền tảng của trường phiên bản (`version`) được tích hợp sẵn trong cấu trúc của ba thực thể điểm nóng là `MenuItem`, `Driver`, và `Order`[cite: 50, 52]. 
  Khi một luồng đọc dữ liệu ra, hệ thống sẽ ghi nhận số `version` hiện tại (ví dụ: `version = 1`)[cite: 51]. Khi luồng tiến hành ghi đè dữ liệu mới để gán tài xế, hệ thống sẽ đối chiếu số phiên bản giữ lại với số phiên bản thực tế đang nằm trong file CSV[cite: 51]. Nếu trùng khớp, lệnh ghi được chấp nhận và `version` tự động tăng lên 1[cite: 51]. Nếu số phiên bản trong file đã bị luồng khác thay đổi trước đó, hệ thống lập tức từ chối lệnh ghi, ném ra một ngoại lệ tùy biến (Custom Exception) và yêu cầu luồng thực hiện lại, giải quyết triệt để lỗi `Double Assignment` mà không cần khóa cứng file dữ liệu vật lý[cite: 51, 92].