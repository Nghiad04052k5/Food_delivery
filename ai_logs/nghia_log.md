# Nhật ký sử dụng AI (AI Logs) - Thành viên 2 (Đào Minh Nghĩa)

**Vai trò:** Data Architect & Project Setup

---

## Lần 1: Nhờ AI hỗ trợ cấu trúc dự án và xử lý lỗi Git
**Thời gian (Timestamp):** 2026-05-22T16:20:00+07:00
**Công cụ sử dụng:** Gemini (Antigravity Agent)

**Prompt (Câu hỏi của tôi):**
> Nhóm tôi có 3 thành viên, mỗi người làm việc trên một nhánh Git riêng. Tôi là Project Manager và đã thiết lập Branch Protection chặn push trực tiếp lên nhánh main. Làm thế nào để tôi có thể review code, gộp (merge) 2 nhánh của các thành viên khác vào nhánh main một cách an toàn mà vẫn bảo toàn được toàn bộ lịch sử commit của họ?

> Hãy kiểm tra cấu trúc và nội dung của thư mục dự án tại đường dẫn `C:\Users\daomi\.gemini\antigravity\scratch\Food_delivery-main`. Đánh giá xem tiến độ công việc hiện tại đã đáp ứng được những yêu cầu nào trong bảng phân công nhiệm vụ của nhóm.

> Theo phân công, tôi là Thành viên 2 (Data Architect) phụ trách thiết kế Schema, chưa tiến hành tạo file dữ liệu. Tại sao trong thư mục dự án hiện tại lại xuất hiện các file CSV? Hãy giải thích quy trình phối hợp giữa Thành viên 2 và Thành viên 3 (Data Engineer) trong trường hợp này.

**AI Trả lời (Tóm tắt):**
- AI hướng dẫn chi tiết cách dùng Pull Request trên GitHub để gộp code (Merge commit) nhằm bảo toàn 100% lịch sử commit của các thành viên khác thay vì dùng tính năng Squash.
- AI hướng dẫn cách "Bypass rules" do tôi đã bật Branch Protection (khóa nhánh main) trên GitHub.
- AI liệt kê toàn bộ tiến độ của dự án (kiểm tra thư mục docs/, data/, ai_logs/) và chỉ ra tôi chưa có thư mục `src/` cũng như chưa có AI Log. AI giải thích việc có file CSV là do thành viên 3 đã sinh dữ liệu và tôi vừa merge PR của bạn ấy.
- AI phát hiện ra tôi đã khởi tạo nhầm Git ở thư mục gốc `C:\Users\daomi` và cung cấp lệnh PowerShell/CMD (`rmdir /s /q C:\Users\daomi\.git`) để gỡ lỗi này, bảo vệ dữ liệu cá nhân.

**Kết quả áp dụng vào dự án:**
Tôi đã gộp thành công code của cả nhóm lên nhánh main mà vẫn giữ được lịch sử commit. Tạo đúng cấu trúc thư mục yêu cầu. Khắc phục được lỗi rò rỉ dữ liệu máy tính cá nhân.
